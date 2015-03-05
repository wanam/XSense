package ma.wanam.xsense.keyboards;

import ma.wanam.xsense.utils.Packages;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XHtcKeyboardPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("disableKBFullScreen", false)) {
			try {
				disableKBFullScreen(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("disableKBAutoCorrection", false)) {
			try {
				disableKBAutoCorrection(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void disableKBFullScreen(ClassLoader classLoader) {
		try {
			XposedHelpers.findAndHookMethod(Packages.HTC_KEYBOARD + ".HTCIMEService", classLoader,
					"onEvaluateFullscreenMode", XC_MethodReplacement.returnConstant(Boolean.FALSE));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void disableKBAutoCorrection(ClassLoader classLoader) {

		try {
			XposedHelpers.findAndHookMethod(Packages.HTC_KEYBOARD + ".XT9IME.XT9Engine", classLoader,
					"getActiveWordIndex", XC_MethodReplacement.returnConstant(Integer.valueOf(0)));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

}
