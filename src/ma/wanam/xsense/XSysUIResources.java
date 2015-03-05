package ma.wanam.xsense;

import android.content.res.XModuleResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class XSysUIResources {

	public static void doHook(XSharedPreferences prefs, InitPackageResourcesParam resparam,
			XModuleResources moduleResources) {

		try {
			XSysUIStatusBarResources.doHook(prefs, resparam, moduleResources);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		if (prefs.getBoolean("selectedBatteryIcon", false) || prefs.getBoolean("hideBatteryIcon", false)) {
			try {
				XBatteryStyle.initResources(prefs, resparam);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}
}
