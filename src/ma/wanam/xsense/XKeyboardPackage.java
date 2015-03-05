package ma.wanam.xsense;

import ma.wanam.xsense.keyboards.XAOSPKeyboardPackage;
import ma.wanam.xsense.keyboards.XGoogleKeyboardPackage;
import ma.wanam.xsense.keyboards.XHtcKeyboardPackage;
import ma.wanam.xsense.keyboards.XSwiftKeyboardPackage;
import ma.wanam.xsense.utils.Packages;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class XKeyboardPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader, String packageName) {

		if (prefs.getBoolean("disableKBFullScreen", false)) {
			try {

				if (packageName.equalsIgnoreCase(Packages.SWIFT_KEYBOARD)
						|| packageName.equalsIgnoreCase(Packages.SWIFT_KEYBOARD_TRIAL)) {
					XSwiftKeyboardPackage.doHook(classLoader);
				} else if (packageName.equalsIgnoreCase(Packages.GOOGLE_KEYBOARD)) {
					XGoogleKeyboardPackage.doHook(classLoader);
				} else if (packageName.equalsIgnoreCase(Packages.AOSP_KEYBOARD)) {
					XAOSPKeyboardPackage.doHook(classLoader);
				}

			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (packageName.equalsIgnoreCase(Packages.HTC_KEYBOARD)) {
			XHtcKeyboardPackage.doHook(prefs,classLoader);
		}

	}

}
