package ma.wanam.xsense.dialogs;

import ma.wanam.xsense.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class CreditsDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		PackageInfo pInfo;
		String pkgVersion = "";
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			pkgVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		TextView tv = new TextView(getActivity());
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(R.string.credit_details);
		tv.setPadding(16, 16, 16, 16);
		builder.setCancelable(true).setView(tv).setTitle(getString(R.string.app_name) + " " + pkgVersion)
				.setPositiveButton(R.string.rate_app, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						getActivity().startActivity(goToMarket);
					}
				}).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		return builder.create();
	}
}
