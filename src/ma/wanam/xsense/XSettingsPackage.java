package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.Context;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSettingsPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("disableUSBNotification", false)) {
			try {
				disableUSBNotification(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("isDisclosed", false)) {
			try {
				disableDevDetailsProtection(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void disableUSBNotification(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.SETTINGS + ".PSService", classLoader, "SetUSBNotification",
					Context.class, boolean.class, XC_MethodReplacement.DO_NOTHING);

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void disableDevDetailsProtection(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.SETTINGS + ".framework.flag.HtcSkuFlags", classLoader,
					"isDisclosed", XC_MethodReplacement.returnConstant(Boolean.TRUE));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

}
