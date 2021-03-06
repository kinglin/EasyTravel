package cn.sharesdk.onekeyshare;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.FakeActivity;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;

public class PlatformListFakeActivity extends FakeActivity {
	protected HashMap<String, Object> shareParamsMap;
	protected boolean silent;
	protected ArrayList<CustomerLogo> customerLogos;
	protected HashMap<String, String> hiddenPlatforms;
	private boolean canceled = false;
	protected View backgroundView;

	protected OnShareButtonClickListener onShareButtonClickListener;
	protected boolean dialogMode = false;
	protected ThemeShareCallback themeShareCallback;

	public static interface OnShareButtonClickListener {
		void onClick(View v, List<Object> checkPlatforms);
	}

	public void onCreate() {
		super.onCreate();

		canceled = false;

		if(themeShareCallback == null) {
			finish();
		}
	}

	public boolean onKeyEvent(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			canceled = true;
		}
		return super.onKeyEvent(keyCode, event);
	}

	protected void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean onFinish() {

		// 閸欐牗绉烽崚鍡曢煩閼挎粌宕熼惃鍕埠鐠侊拷		
		if (canceled) {
			ShareSDK.logDemoEvent(2, null);
		}

		return super.onFinish();
	}

	@Override
	public void show(Context context, Intent i) {
		super.show(context, i);
	}

	public HashMap<String, Object> getShareParamsMap() {
		return shareParamsMap;
	}

	public void setShareParamsMap(HashMap<String, Object> shareParamsMap) {
		this.shareParamsMap = shareParamsMap;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public ArrayList<CustomerLogo> getCustomerLogos() {
		return customerLogos;
	}

	public void setCustomerLogos(ArrayList<CustomerLogo> customerLogos) {
		this.customerLogos = customerLogos;
	}

	public HashMap<String, String> getHiddenPlatforms() {
		return hiddenPlatforms;
	}

	public void setHiddenPlatforms(HashMap<String, String> hiddenPlatforms) {
		this.hiddenPlatforms = hiddenPlatforms;
	}

	public View getBackgroundView() {
		return backgroundView;
	}

	public void setBackgroundView(View backgroundView) {
		this.backgroundView = backgroundView;
	}

	public OnShareButtonClickListener getOnShareButtonClickListener() {
		return onShareButtonClickListener;
	}

	public void setOnShareButtonClickListener(OnShareButtonClickListener onShareButtonClickListener) {
		this.onShareButtonClickListener = onShareButtonClickListener;
	}

	public boolean isDialogMode() {
		return dialogMode;
	}

	public void setDialogMode(boolean dialogMode) {
		this.dialogMode = dialogMode;
	}

	public ThemeShareCallback getThemeShareCallback() {
		return themeShareCallback;
	}

	public void setThemeShareCallback(ThemeShareCallback themeShareCallback) {
		this.themeShareCallback = themeShareCallback;
	}

	protected void onShareButtonClick(View v, List<Object> checkedPlatforms) {

		if(onShareButtonClickListener != null) {
			onShareButtonClickListener.onClick(v, checkedPlatforms);
		}

		HashMap<Platform, HashMap<String, Object>> silentShareData = new HashMap<Platform, HashMap<String,Object>>();
		final List<Platform> supportEditPagePlatforms = new ArrayList<Platform>();

		Platform plat;
		HashMap<String, Object> shareParam;
		for(Object item : checkedPlatforms) {
			if(item instanceof CustomerLogo){
				CustomerLogo customerLogo = (CustomerLogo)item;
				customerLogo.listener.onClick(v);
				continue;
			}

			plat = (Platform)item;
			String name = plat.getName();

			// EditPage娑撳秵鏁幐浣镐簳娣団�閽╅崣鑸拷Google+閵嗕傅Q閸掑棔闊╅妴涓砳nterest閵嗕椒淇婇幁顖氭嫲闁喕娆㈤敍灞撅拷閺勵垱澧界悰宀�纯閹恒儱鍨庢禍锟�		
			if(silent || ShareCore.isDirectShare(plat)) {
				shareParam = new HashMap<String, Object>(shareParamsMap);
				shareParam.put("platform", name);
				silentShareData.put(plat, shareParam);
			} else {
				supportEditPagePlatforms.add(plat);
			}
		}
		if (silentShareData.size() > 0) {
			themeShareCallback.doShare(silentShareData);
		}

		// 鐠哄疇娴咵ditPage閸掑棔闊�		
		if(supportEditPagePlatforms.size() > 0) {
			showEditPage(supportEditPagePlatforms);
		}

		finish();
	}

	protected void showEditPage(List<Platform> platforms) {
		showEditPage(getContext(), platforms);
	}

	public void showEditPage(Context context, Platform platform) {
		ArrayList<Platform> platforms = new ArrayList<Platform>(1);
		platforms.add(platform);
		showEditPage(context, platforms);
	}

	protected void showEditPage(Context context, List<Platform> platforms) {
		EditPageFakeActivity editPageFakeActivity;
		String editPageClass = ((Object)this).getClass().getPackage().getName()+".EditPage";
		try {
			editPageFakeActivity = (EditPageFakeActivity) Class.forName(editPageClass).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		editPageFakeActivity.setBackgroundView(backgroundView);
		editPageFakeActivity.setShareData(shareParamsMap);
		editPageFakeActivity.setPlatforms(platforms);
		if (dialogMode) {
			editPageFakeActivity.setDialogMode();
		}
		editPageFakeActivity.showForResult(context, null, new FakeActivity() {
			public void onResult(HashMap<String, Object> data) {
				if(data == null)
					return;
				if (data.containsKey("editRes")) {
					@SuppressWarnings("unchecked")
					HashMap<Platform, HashMap<String, Object>> editRes
							= (HashMap<Platform, HashMap<String, Object>>) data.get("editRes");
					themeShareCallback.doShare(editRes);
				}
			}
		});
	}
}
