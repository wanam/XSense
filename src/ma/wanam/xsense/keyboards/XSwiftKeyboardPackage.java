package ma.wanam.xsense.keyboards;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSwiftKeyboardPackage {

	public static void doHook(ClassLoader classLoader) {

		try {
			disableKBFullScreen(classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void disableKBFullScreen(ClassLoader classLoader) {
		try {
			XposedHelpers.findAndHookMethod("com.touchtype.keyboard.service.TouchTypeSoftKeyboard", classLoader,
					"onEvaluateFullscreenMode", XC_MethodReplacement.returnConstant(Boolean.FALSE));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

}
