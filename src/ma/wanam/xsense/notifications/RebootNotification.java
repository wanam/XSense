package ma.wanam.xsense.notifications;

import ma.wanam.xsense.R;
import ma.wanam.xsense.XSenseActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RebootNotification {

	private static final String NOTIFICATION_TAG = "RebootNotification";

	private static int number = 0;

	public static void notify(final Context context, final int n, boolean showSoftReboot) {
		number = n;

		final Resources res = context.getResources();

		final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);

		final String ticker = res.getString(R.string.reboot_required);
		final String title = res.getString(R.string.reboot_required_title);
		final String text = res.getString(R.string.reboot_required_message);

		final Notification.Builder builder = new Notification.Builder(context)
				.setDefaults(0)
				.setSmallIcon(android.R.drawable.ic_menu_rotate)
				.setContentTitle(title)
				.setContentText(text)
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setLargeIcon(picture)
				.setTicker(ticker)
				.setNumber(number)
				.setWhen(0)
				.setContentIntent(
						PendingIntent.getActivity(context, 0, new Intent(context, XSenseActivity.class),
								PendingIntent.FLAG_UPDATE_CURRENT))
				.setStyle(
						new Notification.BigTextStyle().bigText(text).setBigContentTitle(title)
								.setSummaryText(context.getString(R.string.pending_changes))).setAutoCancel(true);

		notify(context, builder.build());
	}

	private static void notify(final Context context, final Notification notification) {
		final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_TAG, 0, notification);
	}

	public static void cancel(final Context context) {
		final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_TAG, 0);
	}

	public static int getNumber() {
		return number;
	}
}