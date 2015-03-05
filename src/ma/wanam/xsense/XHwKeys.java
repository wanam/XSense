/*
 * Copyright (C) 2013 rovo89@xda
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Copyright (C) 2013 Mohamed Karami wanam@xda
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.wanam.xsense;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ma.wanam.xsense.utils.Packages;
import ma.wanam.xsense.utils.Utils;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XHwKeys {

	private static boolean mIsLongPress = false;
	private static Boolean isMusicActive = false;
	private static Context mContext;

	private static Class<?> classActivityManagerNative;
	private static final String CLASS_ACTIVITY_MANAGER_NATIVE = "android.app.ActivityManagerNative";
	private static Handler mHandler;

	private static boolean longPressTrackSkip;
	private static boolean longBackToKillProcess, longBackToKillApp;
	private static final int longPressDelay = 500;

	private static Context wanamContext;

	private static List<String> mKillIgnoreList = new ArrayList<String>(Arrays.asList("com.android.systemui",
			"android.process.acore", "com.google.process.gapps", "com.android.smspush"));

	static void init(XSharedPreferences prefs) {
		try {

			longPressTrackSkip = prefs.getBoolean("longPressTrackSkip", false);
			longBackToKillProcess = prefs.getBoolean("longBackToKillProcess", false);
			longBackToKillApp = prefs.getBoolean("longBackToKillApp", false);

			if (longPressTrackSkip || longBackToKillProcess) {

				Class<?> classPhoneWindowManager = findClass("com.android.internal.policy.impl.PhoneWindowManager",
						null);
				XposedBridge.hookAllConstructors(classPhoneWindowManager, handleConstructPhoneWindowManager);

				findAndHookMethod(classPhoneWindowManager, "interceptKeyBeforeQueueing", KeyEvent.class, int.class,
						boolean.class, handleInterceptKeyBeforeQueueing);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		if (prefs.getBoolean("disableAllCaps", false)) {
			try {
				disableAllCaps();
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}
	}

	private static void disableAllCaps() {
		XposedHelpers.findAndHookMethod("com.htc.util.res.HtcResUtil", null, "isInAllCapsLocale", Context.class,
				XC_MethodReplacement.returnConstant(Boolean.FALSE));
	}

	private static XC_MethodHook handleInterceptKeyBeforeQueueing = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			final boolean isScreenOn = (Boolean) param.args[2];
			final KeyEvent event = (KeyEvent) param.args[0];
			final int keyCode = event.getKeyCode();
			if (mContext == null) {
				mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
			}

			if (wanamContext == null) {
				wanamContext = mContext.createPackageContext(Packages.XSense, Context.CONTEXT_IGNORE_SECURITY);
			}

			if (!isScreenOn) {

				isMusicActive = (Boolean) callMethod(param.thisObject, "isMusicActive");
				if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
						&& longPressTrackSkip && isMusicActive == true) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						mIsLongPress = false;
						handleVolumeLongPress(param.thisObject, keyCode);
						param.setResult(0);
						return;
					} else {
						handleVolumeLongPressAbort(param.thisObject);
						if (mIsLongPress) {
							param.setResult(0);
							return;
						}

						// send an additional "key down" because the first one
						// was eaten
						// the "key up" is what we are just processing
						Object[] newArgs = new Object[3];
						newArgs[0] = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
						newArgs[1] = param.args[1];
						newArgs[2] = param.args[2];
						XposedBridge.invokeOriginalMethod(param.method, param.thisObject, newArgs);
					}
				}
			} else if (longBackToKillProcess) {

				boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
				mHandler = (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");
				if (down) {
					boolean isFromSystem = (event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0;
					if (keyCode == KeyEvent.KEYCODE_BACK && isFromSystem && event.getRepeatCount() == 0) {
						mHandler.postDelayed(mBackLongPress, ViewConfiguration.getLongPressTimeout());
					}
				} else {
					mHandler.removeCallbacks(mBackLongPress);
				}
			}
		}
	};

	private static Runnable mBackLongPress = new Runnable() {

		@Override
		public void run() {
			if (longBackToKillApp) {
				killForegroundApp();
			} else {
				killForegroundProcess();
			}
		}
	};

	private static XC_MethodHook handleConstructPhoneWindowManager = new XC_MethodHook() {
		@Override
		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

			/**
			 * When a volumeup-key longpress expires, skip songs based on key
			 * press
			 */
			Runnable mVolumeUpLongPress = new Runnable() {
				@Override
				public void run() {
					// set the long press flag to true
					mIsLongPress = true;

					// Shamelessly copied from Kmobs LockScreen controls, works
					// for Pandora, etc...
					sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_NEXT);
				};
			};

			/**
			 * When a volumedown-key longpress expires, skip songs based on key
			 * press
			 */
			Runnable mVolumeDownLongPress = new Runnable() {
				@Override
				public void run() {
					// set the long press flag to true
					mIsLongPress = true;

					// Shamelessly copied from Kmobs LockScreen controls, works
					// for Pandora, etc...
					sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
				};
			};

			setAdditionalInstanceField(param.thisObject, "mVolumeUpLongPress", mVolumeUpLongPress);
			setAdditionalInstanceField(param.thisObject, "mVolumeDownLongPress", mVolumeDownLongPress);
		}
	};

	@SuppressLint("Wakelock")
	private static void sendMediaButtonEvent(Object phoneWindowManager, int code) {

		if (longPressTrackSkip && isMusicActive) {
			long eventtime = SystemClock.uptimeMillis();
			Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
			keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			dispatchMediaButtonEvent(keyEvent);

			keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
			keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			dispatchMediaButtonEvent(keyEvent);
		}
	}

	/*
	 * Attempt to execute the following with reflection.
	 * 
	 * [Code] IAudioService audioService = IAudioService.Stub.asInterface(b);
	 * audioService.dispatchMediaKeyEvent(keyEvent); This seems to work
	 * correctly with Google Play Music on 4.3 And from looking at the AOSP
	 * source they started doing it this way in 4.1.1
	 * 
	 * See:
	 * https://android.googlesource.com/platform/frameworks/base.git/+/android
	 * -4.1
	 * .1_r1.1/policy/src/com/android/internal/policy/impl/KeyguardViewBase.java
	 */
	private static void dispatchMediaButtonEvent(KeyEvent keyEvent) {
		try {
			IBinder iBinder = (IBinder) Class.forName("android.os.ServiceManager")
					.getDeclaredMethod("checkService", String.class).invoke(null, Context.AUDIO_SERVICE);

			// get audioService from IAudioService.Stub.asInterface(IBinder)
			Object audioService = Class.forName("android.media.IAudioService$Stub")
					.getDeclaredMethod("asInterface", IBinder.class).invoke(null, iBinder);

			// Dispatch keyEvent using
			// IAudioService.dispatchMediaKeyEvent(KeyEvent)
			Class.forName("android.media.IAudioService").getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
					.invoke(audioService, keyEvent);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static void handleVolumeLongPress(Object phoneWindowManager, int keycode) {
		Handler mHandler = (Handler) getObjectField(phoneWindowManager, "mHandler");
		Runnable mVolumeUpLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress");
		Runnable mVolumeDownLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager,
				"mVolumeDownLongPress");

		mHandler.postDelayed(keycode == KeyEvent.KEYCODE_VOLUME_UP ? mVolumeUpLongPress : mVolumeDownLongPress,
				longPressDelay);
	}

	private static void handleVolumeLongPressAbort(Object phoneWindowManager) {
		Handler mHandler = (Handler) getObjectField(phoneWindowManager, "mHandler");
		Runnable mVolumeUpLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress");
		Runnable mVolumeDownLongPress = (Runnable) getAdditionalInstanceField(phoneWindowManager,
				"mVolumeDownLongPress");

		mHandler.removeCallbacks(mVolumeUpLongPress);
		mHandler.removeCallbacks(mVolumeDownLongPress);
	}

	private static void killForegroundApp() {
		if (mHandler == null)
			return;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				try {

					ActivityManager activityManager = (ActivityManager) mContext
							.getSystemService(Context.ACTIVITY_SERVICE);
					List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
					RunningTaskInfo info = tasks.get(0);

					for (int i = 0; i < mKillIgnoreList.size(); i++) {
						String packageName = mKillIgnoreList.get(i);
						if (info.topActivity.getPackageName().equalsIgnoreCase(packageName)) {
							return;
						}
					}

					String targetKilled = info.topActivity.getPackageName();

					Utils.runSuCommand("am force-stop " + targetKilled);

					try {
						final PackageManager pm = mContext.getPackageManager();
						targetKilled = (String) pm.getApplicationLabel(pm.getApplicationInfo(targetKilled, 0));
					} catch (PackageManager.NameNotFoundException nfe) {

					}

					Resources resXsense = wanamContext.getResources();

					if (targetKilled != null) {
						Toast.makeText(mContext,
								String.format(resXsense.getString(R.string.app_killed), (String) targetKilled),
								Toast.LENGTH_SHORT).show();
					}

				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static void killForegroundProcess() {
		if (mHandler == null)
			return;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					final Intent intent = new Intent(Intent.ACTION_MAIN);
					final PackageManager pm = mContext.getPackageManager();
					String defaultHomePackage = "com.android.launcher";
					intent.addCategory(Intent.CATEGORY_HOME);

					final ResolveInfo res = pm.resolveActivity(intent, 0);
					if (res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
						defaultHomePackage = res.activityInfo.packageName;
					}

					classActivityManagerNative = XposedHelpers.findClass(CLASS_ACTIVITY_MANAGER_NATIVE, null);
					Object mgr = XposedHelpers.callStaticMethod(classActivityManagerNative, "getDefault");

					@SuppressWarnings("unchecked")
					List<RunningAppProcessInfo> apps = (List<RunningAppProcessInfo>) XposedHelpers.callMethod(mgr,
							"getRunningAppProcesses");

					String targetKilled = null;
					for (RunningAppProcessInfo appInfo : apps) {
						int uid = appInfo.uid;
						// Make sure it's a foreground user application (not
						// system,
						// root, phone, etc.)
						if (uid >= Process.FIRST_APPLICATION_UID && uid <= Process.LAST_APPLICATION_UID
								&& appInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
								&& !mKillIgnoreList.contains(appInfo.processName)
								&& !appInfo.processName.equals(defaultHomePackage)) {
							Process.killProcess(appInfo.pid);
							targetKilled = appInfo.processName;
							try {
								targetKilled = (String) pm.getApplicationLabel(pm.getApplicationInfo(targetKilled, 0));
							} catch (PackageManager.NameNotFoundException nfe) {

							}
							break;
						}
					}

					Resources resXsense = wanamContext.getResources();

					if (targetKilled != null) {
						Toast.makeText(mContext,
								String.format(resXsense.getString(R.string.app_killed), (String) targetKilled),
								Toast.LENGTH_SHORT).show();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}

}
