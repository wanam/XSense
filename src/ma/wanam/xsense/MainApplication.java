package ma.wanam.xsense;

import ma.wanam.xsense.utils.Utils;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class MainApplication extends Application {

	private static Context mContext;
	private static Point windowsSize;

	public static Point getWindowsSize() {
		return windowsSize;
	}

	public static void setWindowsSize(Point windowsSize) {
		MainApplication.windowsSize = windowsSize;
	}

	public MainApplication() {
		super();
	}

	public static Context getAppContext() {
		return mContext;
	}

	public void onCreate() {

		super.onCreate();
		mContext = getApplicationContext();

		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
				"forceEnglish", false)) {
			Utils.setEnglishLocale(mContext);
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
				"forceEnglish", false)) {
			Utils.setEnglishLocale(mContext);
		}
	}

}