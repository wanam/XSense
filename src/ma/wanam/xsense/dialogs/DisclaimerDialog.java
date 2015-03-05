package ma.wanam.xsense.dialogs;

import ma.wanam.xsense.R;
import ma.wanam.xsense.utils.Constants;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class DisclaimerDialog extends DialogFragment {

	private Dialog dialog;

	public DisclaimerDialog() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new Builder(getActivity());

		dialog = builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.disclaimer_title)
				.setMessage(R.string.disclaimer_message).setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton(R.string.agree, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(Constants.XPOSED_DOWNLOAD_PAGE);
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						getActivity().startActivity(goToMarket);
						dialog.dismiss();
					}
				}).setCancelable(true).create();
		return dialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		getActivity().finish();
	}

}
