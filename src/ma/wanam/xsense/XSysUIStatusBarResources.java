package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.res.XModuleResources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class XSysUIStatusBarResources {

	private static XSharedPreferences prefs;
	private static InitPackageResourcesParam resparam;

	public static void doHook(XSharedPreferences prefs, InitPackageResourcesParam resparam,
			XModuleResources moduleResources) {

		XSysUIStatusBarResources.prefs = prefs;
		XSysUIStatusBarResources.resparam = resparam;

		if (!prefs.getString("clockPosition", "Right").equalsIgnoreCase("Right")) {
			try {
				hookClockLayout();
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void hookClockLayout() {

		try {
			resparam.res.hookLayout(Packages.SYSTEM_UI, "layout", "super_status_bar", new XC_LayoutInflated() {

				@Override
				public void handleLayoutInflated(LayoutInflatedParam layoutInflatedParam) throws Throwable {

					LinearLayout linearLayoutStatusBarContents = (LinearLayout) layoutInflatedParam.view
							.findViewById(layoutInflatedParam.res.getIdentifier("status_bar_contents", "id",
									Packages.SYSTEM_UI));

					if (linearLayoutStatusBarContents != null) {
						LinearLayout linearLayoutSystemIconArea = (LinearLayout) linearLayoutStatusBarContents
								.findViewById(layoutInflatedParam.res.getIdentifier("system_icon_area", "id",
										Packages.SYSTEM_UI));

						LinearLayout linearLayoutnotificationIconArea = (LinearLayout) linearLayoutStatusBarContents
								.findViewById(layoutInflatedParam.res.getIdentifier("notification_icon_area", "id",
										Packages.SYSTEM_UI));

						TextView textView = (TextView) linearLayoutSystemIconArea.findViewById(layoutInflatedParam.res
								.getIdentifier("clock", "id", Packages.SYSTEM_UI));
						if (textView != null && linearLayoutSystemIconArea != null
								&& linearLayoutnotificationIconArea != null) {

							LinearLayout linearLayout = new LinearLayout(linearLayoutStatusBarContents.getContext());
							linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

							linearLayout.setId(1);

							linearLayoutSystemIconArea.removeView(textView);

							linearLayoutSystemIconArea.setGravity(Gravity.RIGHT);

							if (prefs.getString("clockPosition", "Right").equalsIgnoreCase("Center")) {
								try {

									setCenterClock(linearLayoutStatusBarContents, linearLayoutSystemIconArea,
											linearLayoutnotificationIconArea, textView, linearLayout);

								} catch (Throwable e) {
									XposedBridge.log(e);
								}
							} else if (prefs.getString("clockPosition", "Right").equalsIgnoreCase("Left")) {
								try {

									linearLayoutnotificationIconArea.addView(textView, 0);
								} catch (Throwable e) {
									XposedBridge.log(e);
								}
							} else if (prefs.getString("clockPosition", "Right").equalsIgnoreCase("Hide")) {
								try {
									linearLayout.addView(textView);
									((ViewGroup) linearLayoutStatusBarContents.getParent()).addView(linearLayout);
									linearLayout.setVisibility(View.GONE);
								} catch (Throwable e) {
									XposedBridge.log(e);
								}
							}

						}
					}
				}

				private void setCenterClock(LinearLayout linearLayoutStatusBarContents,
						LinearLayout linearLayoutSystemIconArea, LinearLayout linearLayoutNotificationIconArea,
						TextView textViewClock, LinearLayout linearLayoutClock) {

					int screenSize = prefs.getInt("notificationSize", -1);

					RelativeLayout relativeLayoutStatusBarContents = new RelativeLayout(linearLayoutStatusBarContents
							.getContext());

					relativeLayoutStatusBarContents.setLayoutParams(new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

					RelativeLayout relativeLayoutSystemIconArea = new RelativeLayout(linearLayoutStatusBarContents
							.getContext());

					RelativeLayout.LayoutParams layoutParamsSystemIconArea = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
					layoutParamsSystemIconArea.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					relativeLayoutSystemIconArea.setLayoutParams(layoutParamsSystemIconArea);
					relativeLayoutSystemIconArea.setGravity(Gravity.RIGHT);

					linearLayoutClock.addView(textViewClock);
					linearLayoutClock.setGravity(Gravity.CENTER);

					ViewGroup.LayoutParams layoutParamsNotificationIconArea = linearLayoutNotificationIconArea
							.getLayoutParams();

					layoutParamsNotificationIconArea.width = (screenSize == RelativeLayout.LayoutParams.MATCH_PARENT ? RelativeLayout.LayoutParams.MATCH_PARENT
							: screenSize / 3);

					linearLayoutNotificationIconArea.setLayoutParams(layoutParamsNotificationIconArea);

					linearLayoutStatusBarContents.removeView(linearLayoutNotificationIconArea);
					linearLayoutStatusBarContents.removeView(linearLayoutSystemIconArea);

					relativeLayoutSystemIconArea.addView(linearLayoutSystemIconArea);

					relativeLayoutStatusBarContents.addView(linearLayoutNotificationIconArea);
					relativeLayoutStatusBarContents.addView(linearLayoutClock);
					relativeLayoutStatusBarContents.addView(relativeLayoutSystemIconArea);

					linearLayoutStatusBarContents.addView(relativeLayoutStatusBarContents);
				}
			});
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

}
