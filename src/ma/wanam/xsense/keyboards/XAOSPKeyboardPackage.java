package ma.wanam.xsense.keyboards;

import ma.wanam.xsense.utils.Packages;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XAOSPKeyboardPackage {

	public static void doHook(ClassLoader classLoader) {

		try {
			disableKBFullScreen(classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void disableKBFullScreen(ClassLoader classLoader) {
		try {
			XposedHelpers.findAndHookMethod(Packages.AOSP_KEYBOARD + ".LatinIME", classLoader,
					"onEvaluateFullscreenMode", XC_MethodReplacement.returnConstant(Boolean.FALSE));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

}
