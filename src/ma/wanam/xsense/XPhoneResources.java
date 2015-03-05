package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.res.XModuleResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class XPhoneResources {

	private static InitPackageResourcesParam resparam;
	private static XModuleResources moduleResources;

	public static void doHook(XSharedPreferences prefs, InitPackageResourcesParam resparam,
			XModuleResources moduleResources) {

		XPhoneResources.resparam = resparam;
		XPhoneResources.moduleResources = moduleResources;

		if (prefs.getBoolean("enableBigCallerId", false)) {
			try {
				enableBigCallerId();
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		try {
			enableNoiseSuppressionSupport();
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	private static void enableBigCallerId() {
		try {
			resparam.res.setReplacement(Packages.PHONE, "dimen", "photo_frame_height",
					moduleResources.fwd(R.dimen.am_photo_frame_height));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void enableNoiseSuppressionSupport() {
		try {
			resparam.res.setReplacement(Packages.PHONE, "bool", "has_in_call_noise_suppression", Boolean.TRUE);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

}
