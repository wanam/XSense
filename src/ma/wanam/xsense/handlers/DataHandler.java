package ma.wanam.xsense.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import ma.wanam.xsense.R;
import ma.wanam.xsense.utils.Packages;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemTileImage;

public class DataHandler implements InvocationHandler {

	private Context mContext;
	private String mDataONLabel;
	private String mDataOffLabel;
	private String mDataAction;
	private Drawable mDataIcon;
	private static HtcListItem2LineText messageView = null;

	public DataHandler(Context context, String mDataAction, String mDataONLabel, String mDataOffLabel,
			Drawable mScreenRecordIcon) {
		this.mContext = context;
		this.mDataIcon = mScreenRecordIcon;

		this.mDataONLabel = mDataONLabel;
		this.mDataOffLabel = mDataOffLabel;
		this.mDataAction = mDataAction;
	}

	private Boolean dataEnabled() {
		boolean mobileDataEnabled = false; // Assume disabled
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Class<?> cmClass = Class.forName(cm.getClass().getName());
			Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
			method.setAccessible(true); // Make the method callable
			// get the setting for "mobile data"
			mobileDataEnabled = (Boolean) method.invoke(cm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mobileDataEnabled;
	}

	private void takeScreenrecord() {
		try {
			ConnectivityManager dataManager;
			dataManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
			dataMtd.setAccessible(true);
			if (dataEnabled()) {
				dataMtd.invoke(dataManager, false);
			} else {
				dataMtd.invoke(dataManager, true);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();

		if (methodName.equals("create")) {
			mContext = (Context) args[0];
			Context xContext = mContext.createPackageContext(Packages.XSense, Context.CONTEXT_IGNORE_SECURITY);
			LayoutInflater li = LayoutInflater.from(xContext);
			View v = li.inflate(R.layout.globalactions_list_item_imageicon_2text_2stamp, (ViewGroup) args[2], false);

			HtcListItemTileImage img = (HtcListItemTileImage) v.findViewById(R.id.icon);
			img.setTileImageDrawable(mDataIcon);
			img.setTranslationX(10.0F * mContext.getResources().getDisplayMetrics().density);

			messageView = (HtcListItem2LineText) v.findViewById(R.id.text1);
			messageView.setPrimaryText(mDataAction);
			if (dataEnabled()) {
				messageView.setSecondaryText(mDataONLabel);
			} else {
				messageView.setSecondaryText(mDataOffLabel);
			}

			messageView.setTranslationX(5.0F * mContext.getResources().getDisplayMetrics().density);

			return v;
		} else if (methodName.equals("onPress")) {
			takeScreenrecord();
			return null;
		} else if (methodName.equals("onLongPress")) {
			return true;
		} else if (methodName.equals("showDuringKeyguard")) {
			return true;
		} else if (methodName.equals("showBeforeProvisioning")) {
			return true;
		} else if (methodName.equals("isEnabled")) {
			return true;
		} else if (methodName.equals("showConditional")) {
			return true;
		} else {
			return null;
		}
	}
}
