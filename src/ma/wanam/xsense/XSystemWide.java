package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.res.XResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class XSystemWide {

	private static XSharedPreferences prefs;

	public static void doHook(String modulePath, XSharedPreferences prefs) {

		XSystemWide.prefs = prefs;

		try {
			setSystemWideTweaks();
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			XHwKeys.init(prefs);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void setSystemWideTweaks() {

		try {
			XResources.setSystemWideReplacement(Packages.ANDROID, "bool", "config_unplugTurnsOnScreen",
					prefs.getBoolean("unplugScreenOn", false));
			XResources.setSystemWideReplacement(Packages.ANDROID_HTC_FRAMEWORK, "bool", "config_unplugTurnsOnScreen",
					prefs.getBoolean("unplugScreenOn", false));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			XResources.setSystemWideReplacement(Packages.ANDROID, "bool", "show_ongoing_ime_switcher",
					!prefs.getBoolean("hideImeSwitcher", false));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			XResources.setSystemWideReplacement(Packages.ANDROID, "bool", "config_useMasterVolume",
					prefs.getBoolean("useMasterVolume", false));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			XResources.setSystemWideReplacement(Packages.ANDROID, "bool", "preferences_prefer_dual_pane",
					prefs.getBoolean("enableDualPane", false));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		if (prefs.getBoolean("disbaleLowBatteryCloseWarningLevel", false)) {
			try {
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryWarningLevel", 1);
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryCloseWarningLevel",
						1);
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_criticalBatteryWarningLevel",
						1);
			} catch (Throwable e) {

				XposedBridge.log(e);
			}
		} else if (prefs.getInt("configCriticalBatteryWarningLevel", 5) != 5) {
			try {
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_criticalBatteryWarningLevel",
						prefs.getInt("configCriticalBatteryWarningLevel", 5));
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryWarningLevel",
						prefs.getInt("configCriticalBatteryWarningLevel", 5));
				XResources.setSystemWideReplacement(Packages.ANDROID, "integer", "config_lowBatteryCloseWarningLevel",
						prefs.getInt("configCriticalBatteryWarningLevel", 5));
			} catch (Throwable e) {

				XposedBridge.log(e);
			}
		}

		try {
			XResources.setSystemWideReplacement("android", "bool", "config_allowAllRotations",
					prefs.getBoolean("enableAllRotation", false));
			XResources.setSystemWideReplacement(Packages.ANDROID_HTC_FRAMEWORK, "bool", "config_allowAllRotations",
					prefs.getBoolean("enableAllRotation", false));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
