package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.Context;
import android.media.AudioManager;
import android.view.ContextThemeWrapper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XPhonePackage {

	private static XSharedPreferences xPrefs;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {
		xPrefs = prefs;
		try {
			disableNoiseSuppression(classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			enableCallRecording(classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void enableCallRecording(final ClassLoader classLoader) {

		try {

			XposedHelpers.findAndHookMethod(Packages.PHONE + ".PhoneGlobals", classLoader, "onCreate",
					new XC_MethodHook() {

						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

							try {

								final Class<?> voiceRecorderHelper = XposedHelpers.findClass(Packages.PHONE
										+ ".util.VoiceRecorderHelper", classLoader);

								XposedHelpers.setStaticBooleanField(voiceRecorderHelper, "IS_INCALL_RECORDING_ENABLE",
										xPrefs.getBoolean("enableCallRecording", false));

								ContextThemeWrapper mContext = (ContextThemeWrapper) param.thisObject;
								AudioManager audioManage = (AudioManager) mContext
										.getSystemService(Context.AUDIO_SERVICE);

								if (xPrefs.getBoolean("disableNoiseSuppression", false)) {
									audioManage.setParameters("noise_suppression=off");
								} else {
									audioManage.setParameters("noise_suppression=auto");
								}

							} catch (Throwable e) {
								e.printStackTrace();
							}
						}

					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void disableNoiseSuppression(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.PHONE + ".PhoneUtils", classLoader, "turnOnNoiseSuppression",
					Context.class, boolean.class, boolean.class, new XC_MethodHook() {

						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.args[1] = !xPrefs.getBoolean("disableNoiseSuppression", false);
							param.args[2] = Boolean.TRUE;
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {

			XposedHelpers.findAndHookMethod(Packages.PHONE + ".PhoneUtils", classLoader, "isNoiseSuppressionOn",
					Context.class, new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							boolean noiseState = xPrefs.getBoolean("disableNoiseSuppression", false);
							Context mContext = (Context) param.args[0];
							AudioManager audioManage = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

							if (noiseState) {
								audioManage.setParameters("noise_suppression=off");
							} else {
								audioManage.setParameters("noise_suppression=auto");
							}

							return !noiseState;
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}
}
