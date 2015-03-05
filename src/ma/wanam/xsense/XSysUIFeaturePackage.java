package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUIFeaturePackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("expandNotifications", false)) {
			try {
				expandAllNotifications(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		try {
			XBatteryStyle.init(prefs, classLoader);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (prefs.getBoolean("headsUPNotifications", false)) {
			try {
				headsUPNotifications(prefs, classLoader);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		try {
			setOperatorName(prefs, classLoader);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private static void expandAllNotifications(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.ExpandableNotificationRow", classLoader,
					"isUserExpanded", new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							return Boolean.TRUE;
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void headsUPNotifications(final XSharedPreferences prefs, ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.phone.PhoneStatusBar", classLoader,
					"addNotification", IBinder.class, StatusBarNotification.class, boolean.class, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							XposedHelpers.setBooleanField(param.thisObject, "mUseHeadsUp", Boolean.TRUE);
						}

					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {

			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.phone.PhoneStatusBar", classLoader,
					"loadDimens", new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							XposedHelpers.setIntField(param.thisObject, "mHeadsUpNotificationDecay",
									prefs.getInt("headsUpNotificationDecay", 3700));
						}

					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {

			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.BaseStatusBar", classLoader,
					"shouldInterrupt", StatusBarNotification.class, new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							StatusBarNotification n = (StatusBarNotification) param.args[0];

							PowerManager powerManager = (PowerManager) XposedHelpers.getObjectField(param.thisObject,
									"mPowerManager");
							return (!n.isOngoing() || (n.isOngoing() && prefs.getBoolean("headsUPOngoingNotifications",
									false))) && powerManager.isScreenOn();
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void setOperatorName(final XSharedPreferences prefs, ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.phone.CarrierLabel", classLoader,
					"updateNetworkName", boolean.class, String.class, boolean.class, String.class, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							String opName = prefs.getString("customCarrierNotification", "");
							if (prefs.getBoolean("hideCarrierNotification", false)) {
								param.args[1] = "";
								param.args[3] = "";
							} else if (!opName.isEmpty()) {
								param.args[1] = opName;
								param.args[3] = opName;
							}

						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}
}
