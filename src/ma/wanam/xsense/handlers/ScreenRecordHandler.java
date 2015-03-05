package ma.wanam.xsense.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import ma.wanam.xsense.R;
import ma.wanam.xsense.services.ScreenRecordingService;
import ma.wanam.xsense.utils.Packages;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemTileImage;

public class ScreenRecordHandler implements InvocationHandler {

	private Context mContext;
	private String mScreenRecordLabel;
	private String mcScreenRecordLabel;
	private Drawable mScreenRecordIcon;

	public ScreenRecordHandler(Context context, String mScreenRecordLabel, String mcScreenRecordLabel,
			Drawable mScreenRecordIcon) {
		this.mContext = context;
		this.mScreenRecordLabel = mScreenRecordLabel;
		this.mScreenRecordIcon = mScreenRecordIcon;
		this.mcScreenRecordLabel = mcScreenRecordLabel;
	}

	private void takeScreenrecord() {
		try {
			Context gbContext = mContext.createPackageContext(Packages.XSense, Context.CONTEXT_IGNORE_SECURITY);
			Intent intent = new Intent(gbContext, ScreenRecordingService.class);
			intent.setAction(ScreenRecordingService.ACTION_SCREEN_RECORDING_START);
			gbContext.startService(intent);
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
			img.setTileImageDrawable(mScreenRecordIcon);
			img.setTranslationX(10.0F * mContext.getResources().getDisplayMetrics().density);

			HtcListItem2LineText messageView = (HtcListItem2LineText) v.findViewById(R.id.text1);
			messageView.setPrimaryText(mScreenRecordLabel);
			messageView.setSecondaryText(mcScreenRecordLabel);
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
