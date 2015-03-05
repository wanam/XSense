package ma.wanam.xsense.receivers;

import java.net.URISyntaxException;

import ma.wanam.xsense.XSenseActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class WanamLaunchReceiver extends BroadcastReceiver {

	public static final String START_HOME_LONG_ACTIVITY = "ma.wanam.xposed.action.START_HOME_LONG_ACTIVITY";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equalsIgnoreCase(START_HOME_LONG_ACTIVITY)) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String intentUri = prefs.getString("homeLongApplicationUri", "");
			Intent i = null;
			if (intentUri == null || intentUri.equalsIgnoreCase("")) {
				i = new Intent(context, XSenseActivity.class);
			} else {
				try {
					i = Intent.parseUri(intentUri, 0);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}
