package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XMessagingPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("screenONSMS", false)) {
			try {
				supportBrightScreenOnNewSMS(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("isEnableAccentConvert", false)) {
			try {
				isEnableAccentConvert(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("getMaxSMSConcatenatedNumber", false)) {
			try {
				getMaxSMSConcatenatedNumber(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void supportBrightScreenOnNewSMS(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.HTC_MESSAGE + ".MmsConfig", classLoader,
					"supportBrightScreenOnNewSMS", XC_MethodReplacement.returnConstant(Boolean.TRUE));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void isEnableAccentConvert(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.HTC_MESSAGE + ".MmsConfig", classLoader, "isEnableAccentConvert",
					XC_MethodReplacement.returnConstant(Boolean.TRUE));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void getMaxSMSConcatenatedNumber(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.HTC_MESSAGE + ".MmsConfig", classLoader,
					"getMaxSMSConcatenatedNumber", XC_MethodReplacement.returnConstant(9999));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}
}
