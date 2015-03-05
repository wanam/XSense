package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.res.XModuleResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class XLauncherResources {

	private static InitPackageResourcesParam resparam;
	private static XModuleResources moduleResources;

	public static void doHook(XSharedPreferences prefs, InitPackageResourcesParam resparam,
			XModuleResources moduleResources) {

		XLauncherResources.resparam = resparam;
		XLauncherResources.moduleResources = moduleResources;

		if (prefs.getBoolean("enable4x5grid", false)) {
			try {
				setCellCountY();
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void setCellCountY() {
		try {
			resparam.res.setReplacement(Packages.HTC_LAUNCHER, "integer", "cell_count_y", 5);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			resparam.res.setReplacement(Packages.HTC_LAUNCHER, "dimen", "workspace_cell_height",
					moduleResources.fwd(R.dimen.workspace_cell_height_port));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			resparam.res.setReplacement(Packages.HTC_LAUNCHER, "dimen", "workspace_cell_height_port",
					moduleResources.fwd(R.dimen.workspace_cell_height_port));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			resparam.res.setReplacement(Packages.HTC_LAUNCHER, "dimen", "app_icon_padding_top",
					moduleResources.fwd(R.dimen.app_icon_padding_top));
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

}
