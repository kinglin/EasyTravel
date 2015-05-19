/*
 * 鐎规缍夐崷鎵彲:http://www.mob.com
 * 閹讹拷閺堫垱鏁幐涓礠: 4006852216
 * 鐎规ɑ鏌熷顔讳繆:ShareSDK   閿涘牆顩ч弸婊冨絺鐢啯鏌婇悧鍫熸拱閻ㄥ嫯鐦介敍灞惧灉娴狀剙鐨㈡导姘鳖儑娑擄拷閺冨爼妫块柅姘崇箖瀵邦喕淇婄亸鍡欏閺堫剚娲块弬鏉垮敶鐎硅甯归柅浣虹舶閹劊锟藉倸顩ч弸婊�▏閻劏绻冪粙瀣╄厬閺堝鎹㈡担鏇㈡６妫版﹫绱濇稊鐔峰讲娴犮儵锟芥俺绻冨顔讳繆娑撳孩鍨滄禒顒�絿瀵版浠堢化浼欑礉閹存垳婊戠亸鍡曠窗閸︼拷24鐏忓繑妞傞崘鍛舶娴滃牆娲栨径宥忕礆
 *
 * Copyright (c) 2013楠烇拷 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare;

import static cn.sharesdk.framework.utils.BitmapHelper.captureView;
import static cn.sharesdk.framework.utils.R.getStringRes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import cn.sharesdk.framework.CustomPlatform;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;

/**
 * 韫囶偅宓庨崚鍡曢煩閻ㄥ嫬鍙嗛崣锟� * <p>
 * 闁俺绻冩稉宥呮倱閻ㄥ墕etter鐠佸墽鐤嗛崣鍌涙殶閿涘瞼鍔ч崥搴ょ殶閻⑩杽@link #show(Context)}閺傝纭堕崥顖氬З韫囶偅宓庨崚鍡曢煩
 */
public class OnekeyShare implements PlatformActionListener, Callback {
	private static final int MSG_TOAST = 1;
	private static final int MSG_ACTION_CCALLBACK = 2;
	private static final int MSG_CANCEL_NOTIFY = 3;

	private HashMap<String, Object> shareParamsMap;
	private ArrayList<CustomerLogo> customers;
	private boolean silent;
	private PlatformActionListener callback;
	private ShareContentCustomizeCallback customizeCallback;
	private boolean dialogMode = false;
	private boolean disableSSO;
	private HashMap<String, String> hiddenPlatforms;
	private View bgView;
	private OnekeyShareTheme theme;

	private Context context;
	private PlatformListFakeActivity.OnShareButtonClickListener onShareButtonClickListener;

	public OnekeyShare() {
		shareParamsMap = new HashMap<String, Object>();
		customers = new ArrayList<CustomerLogo>();
		callback = this;
		hiddenPlatforms = new HashMap<String, String>();
	}

	public void show(Context context) {
		ShareSDK.initSDK(context);
		this.context = context;

		// 閹垫挸绱戦崚鍡曢煩閼挎粌宕熼惃鍕埠鐠侊拷
		ShareSDK.logDemoEvent(1, null);

		// 閺勫墽銇氶弬鐟扮础閺勵垳鏁眕latform閸滃ilent娑撱倓閲滅�妤侇唽閹貉冨煑閻拷
		// 婵″倹鐏塸latform鐠佸墽鐤嗘禍鍡礉閸掓瑦妫ゆい缁樻▔缁�桨绡��顐ｇ壐閿涘苯鎯侀崚娆撳厴娴兼碍妯夌粈鐚寸幢
		// 婵″倹鐏塻ilent娑撶皪rue閿涘矁銆冪粈杞扮瑝鏉╂稑鍙嗙紓鏍帆妞ょ敻娼伴敍灞芥儊閸掓瑤绱版潻娑樺弳閵嗭拷
		// 閺堫剛琚崣顏勫灲閺傜捀latform閿涘苯娲滄稉杞扮瘈鐎诡偅鐗搁弰鍓с仛娴犮儱鎮楅敍灞肩皑娴犳湹姘︾紒姗甽atformGridView閹貉冨煑
		// 瑜版悕latform閸滃ilent闁垝璐焧rue閿涘苯鍨惄瀛樺复鏉╂稑鍙嗛崚鍡曢煩閿涳拷
		// 瑜版悕latform鐠佸墽鐤嗘禍鍡礉娴ｅ棙妲竤ilent娑撶alse閿涘苯鍨崚銈嗘焽閺勵垰鎯侀弰顖楋拷婊�▏閻劌顓归幋椋庮伂閸掑棔闊╅垾婵堟畱楠炲啿褰撮敍锟�		// 閼汇儰璐熼垾婊�▏閻劌顓归幋椋庮伂閸掑棔闊╅垾婵堟畱楠炲啿褰撮敍灞藉灟閻╁瓨甯撮崚鍡曢煩閿涘苯鎯侀崚娆掔箻閸忋儳绱潏鎴︺�闂堬拷
		if (shareParamsMap.containsKey("platform")) {
			String name = String.valueOf(shareParamsMap.get("platform"));
			Platform platform = ShareSDK.getPlatform(name);

			if (silent
					|| ShareCore.isUseClientToShare(name)
					|| platform instanceof CustomPlatform
					) {
				HashMap<Platform, HashMap<String, Object>> shareData
						= new HashMap<Platform, HashMap<String,Object>>();
				shareData.put(ShareSDK.getPlatform(name), shareParamsMap);
				share(shareData);
				return;
			}
		}

		PlatformListFakeActivity platformListFakeActivity;
		try {
			if(OnekeyShareTheme.SKYBLUE == theme){
				platformListFakeActivity = (PlatformListFakeActivity) Class.forName("cn.sharesdk.onekeyshare.theme.skyblue.PlatformListPage").newInstance();
			}else{
				platformListFakeActivity = (PlatformListFakeActivity) Class.forName("cn.sharesdk.onekeyshare.theme.classic.PlatformListPage").newInstance();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		platformListFakeActivity.setDialogMode(dialogMode);
		platformListFakeActivity.setShareParamsMap(shareParamsMap);
		platformListFakeActivity.setSilent(silent);
		platformListFakeActivity.setCustomerLogos(customers);
		platformListFakeActivity.setBackgroundView(bgView);
		platformListFakeActivity.setHiddenPlatforms(hiddenPlatforms);
		platformListFakeActivity.setOnShareButtonClickListener(onShareButtonClickListener);
		platformListFakeActivity.setThemeShareCallback(new ThemeShareCallback() {

			@Override
			public void doShare(HashMap<Platform, HashMap<String, Object>> shareData) {
				share(shareData);
			}
		});
		if (shareParamsMap.containsKey("platform")) {
			String name = String.valueOf(shareParamsMap.get("platform"));
			Platform platform = ShareSDK.getPlatform(name);
			platformListFakeActivity.showEditPage(context, platform);
			return;
		}
		platformListFakeActivity.show(context, null);
	}

	public void setTheme(OnekeyShareTheme theme) {
		this.theme = theme;
	}

	/** address閺勵垱甯撮弨鏈垫眽閸︽澘娼冮敍灞肩矌閸︺劋淇婇幁顖氭嫲闁喕娆㈡担璺ㄦ暏閿涘苯鎯侀崚娆忓讲娴犮儰绗夐幓鎰返 */
	public void setAddress(String address) {
		shareParamsMap.put("address", address);
	}

	/**
	 * title閺嶅洭顣介敍灞芥躬閸楁媽钖勭粭鏃囶唶閵嗕線鍋栫粻渚匡拷浣蜂繆閹垬锟戒礁浜曟穱鈽呯礄閸栧懏瀚總钘夊几閵嗕焦婀呴崣瀣箑閸滃本鏁归挊蹇ョ礆閵嗭拷
	 * 閺勬挷淇婇敍鍫濆瘶閹奉剙銈介崣瀣拷浣规箙閸欏婀�敍澶堬拷浣锋眽娴滆櫣缍夐崪瀛甉缁屾椽妫挎担璺ㄦ暏閿涘苯鎯侀崚娆忓讲娴犮儰绗夐幓鎰返
	 */
	public void setTitle(String title) {
		shareParamsMap.put("title", title);
	}

	/** titleUrl閺勵垱鐖ｆ０妯兼畱缂冩垹绮堕柧鐐复閿涘奔绮庨崷銊ゆ眽娴滆櫣缍夐崪瀛甉缁屾椽妫挎担璺ㄦ暏閿涘苯鎯侀崚娆忓讲娴犮儰绗夐幓鎰返 */
	public void setTitleUrl(String titleUrl) {
		shareParamsMap.put("titleUrl", titleUrl);
	}

	/** text閺勵垰鍨庢禍顐ｆ瀮閺堫剨绱濋幍锟介張澶婇挬閸欎即鍏橀棁锟界憰浣界箹娑擃亜鐡у▓锟�*/
	public void setText(String text) {
		shareParamsMap.put("text", text);
	}

	/** 閼惧嘲褰噒ext鐎涙顔岄惃鍕拷锟�*/
	public String getText() {
		return shareParamsMap.containsKey("text") ? String.valueOf(shareParamsMap.get("text")) : null;
	}

	/** imagePath閺勵垱婀伴崷鎵畱閸ュ墽澧栫捄顖氱窞閿涘矂娅嶭inked-In婢舵牜娈戦幍锟介張澶婇挬閸欎即鍏橀弨顖涘瘮鏉╂瑤閲滅�妤侇唽 */
	public void setImagePath(String imagePath) {
		if(!TextUtils.isEmpty(imagePath))
			shareParamsMap.put("imagePath", imagePath);
	}

	/** imageUrl閺勵垰娴橀悧鍥╂畱缂冩垹绮剁捄顖氱窞閿涘本鏌婂ù顏勪簳閸楁哎锟戒椒姹夋禍铏圭秹閵嗕傅Q缁屾椽妫块崪瀛nked-In閺�垱瀵斿銈呯摟濞堬拷 */
	public void setImageUrl(String imageUrl) {
		if (!TextUtils.isEmpty(imageUrl))
			shareParamsMap.put("imageUrl", imageUrl);
	}

	/** url閸︺劌浜曟穱鈽呯礄閸栧懏瀚總钘夊几閵嗕焦婀呴崣瀣箑閺�儼妫岄敍澶婃嫲閺勬挷淇婇敍鍫濆瘶閹奉剙銈介崣瀣嫲閺堝寮搁崷鍫礆娑擃厺濞囬悽顭掔礉閸氾箑鍨崣顖欎簰娑撳秵褰佹笟锟�*/
 	public void setUrl(String url) {
		shareParamsMap.put("url", url);
	}

	/** filePath閺勵垰绶熼崚鍡曢煩鎼存梻鏁ょ粙瀣碍閻ㄥ嫭婀伴崷鎷岀熅閸旇绱濇禒鍛躬瀵邦喕淇婇敍鍫熸娣団槄绱氭總钘夊几閸滃瓕ropbox娑擃厺濞囬悽顭掔礉閸氾箑鍨崣顖欎簰娑撳秵褰佹笟锟�*/
	public void setFilePath(String filePath) {
		shareParamsMap.put("filePath", filePath);
	}

	/** comment閺勵垱鍨滅�纭呯箹閺夆�鍨庢禍顐ゆ畱鐠囧嫯顔戦敍灞肩矌閸︺劋姹夋禍铏圭秹閸滃Q缁屾椽妫挎担璺ㄦ暏閿涘苯鎯侀崚娆忓讲娴犮儰绗夐幓鎰返 */
	public void setComment(String comment) {
		shareParamsMap.put("comment", comment);
	}

	/** site閺勵垰鍨庢禍顐ｎ劃閸愬懎顔愰惃鍕秹缁旀瑥鎮曠粔甯礉娴犲懎婀猀Q缁屾椽妫挎担璺ㄦ暏閿涘苯鎯侀崚娆忓讲娴犮儰绗夐幓鎰返 */
	public void setSite(String site) {
		shareParamsMap.put("site", site);
	}

	/** siteUrl閺勵垰鍨庢禍顐ｎ劃閸愬懎顔愰惃鍕秹缁旀瑥婀撮崸锟介敍灞肩矌閸︹墻Q缁屾椽妫挎担璺ㄦ暏閿涘苯鎯侀崚娆忓讲娴犮儰绗夐幓鎰返 */
	public void setSiteUrl(String siteUrl) {
		shareParamsMap.put("siteUrl", siteUrl);
	}

	/** foursquare閸掑棔闊╅弮鍓佹畱閸︾増鏌熼崥锟�*/
	public void setVenueName(String venueName) {
		shareParamsMap.put("venueName", venueName);
	}

	/** foursquare閸掑棔闊╅弮鍓佹畱閸︾増鏌熼幓蹇氬牚 */
	public void setVenueDescription(String venueDescription) {
		shareParamsMap.put("venueDescription", venueDescription);
	}

	/** 閸掑棔闊╅崷鎵惈鎼达讣绱濋弬鐗堟爱瀵邦喖宕ラ妴浣藉悩鐠侇垰浜曢崡姘嫲foursquare閺�垱瀵斿銈呯摟濞堬拷 */
	public void setLatitude(float latitude) {
		shareParamsMap.put("latitude", latitude);
	}

	/** 閸掑棔闊╅崷鎵病鎼达讣绱濋弬鐗堟爱瀵邦喖宕ラ妴浣藉悩鐠侇垰浜曢崡姘嫲foursquare閺�垱瀵斿銈呯摟濞堬拷 */
	public void setLongitude(float longitude) {
		shareParamsMap.put("longitude", longitude);
	}

	/** 閺勵垰鎯侀惄瀛樺复閸掑棔闊�*/
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	/** 鐠佸墽鐤嗙紓鏍帆妞ょ數娈戦崚婵嗩潗閸栨牠锟藉鑵戦獮鍐插酱 */
	public void setPlatform(String platform) {
		shareParamsMap.put("platform", platform);
	}

	/** 鐠佸墽鐤咾akaoTalk閻ㄥ嫬绨查悽銊ょ瑓鏉炶棄婀撮崸锟�*/
	public void setInstallUrl(String installurl) {
		shareParamsMap.put("installurl", installurl);
	}

	/** 鐠佸墽鐤咾akaoTalk閻ㄥ嫬绨查悽銊﹀ⅵ瀵拷閸︽澘娼�*/
	public void setExecuteUrl(String executeurl) {
		shareParamsMap.put("executeurl", executeurl);
	}

	/** 鐠佸墽鐤嗗顔讳繆閸掑棔闊╅惃鍕叾娑旀劗娈戦崷鏉挎絻 */
	public void setMusicUrl(String musicUrl) {
		shareParamsMap.put("musicUrl", musicUrl);
	}

	/** 鐠佸墽鐤嗛懛顏勭暰娑斿娈戞径鏍劥閸ョ偠鐨�*/
	public void setCallback(PlatformActionListener callback) {
		this.callback = callback;
	}

	/** 鏉╂柨娲栭幙宥勭稊閸ョ偠鐨�*/
	public PlatformActionListener getCallback() {
		return callback;
	}

	/** 鐠佸墽鐤嗛悽銊ょ艾閸掑棔闊╂潻鍥┾柤娑擃叏绱濋弽瑙勫祦娑撳秴鎮撻獮鍐插酱閼奉亜鐣炬稊澶婂瀻娴滎偄鍞寸�鍦畱閸ョ偠鐨�*/
	public void setShareContentCustomizeCallback(ShareContentCustomizeCallback callback) {
		customizeCallback = callback;
	}

	/** 鏉╂柨娲栭懛顏勭暰娑斿鍨庢禍顐㈠敶鐎瑰湱娈戦崶鐐剁殶 */
	public ShareContentCustomizeCallback getShareContentCustomizeCallback() {
		return customizeCallback;
	}

	/** 鐠佸墽鐤嗛懛顏勭箒閸ョ偓鐖ｉ崪宀�仯閸戣绨ㄦ禒璁圭礉閸欘垯浜掗柌宥咁槻鐠嬪啰鏁ゅǎ璇插婢舵碍顐�*/
	public void setCustomerLogo(Bitmap enableLogo,Bitmap disableLogo, String label, OnClickListener ocListener) {
		CustomerLogo cl = new CustomerLogo();
		cl.label = label;
		cl.enableLogo = enableLogo;
		cl.disableLogo = disableLogo;
		cl.listener = ocListener;
		customers.add(cl);
	}

	/** 鐠佸墽鐤嗘稉锟芥稉顏咃拷璇茬磻閸忕绱濋悽銊ょ艾閸︺劌鍨庢禍顐㈠閼汇儵娓剁憰浣瑰房閺夊喛绱濋崚娆戭洣閻⑩暞so閸旂喕鍏�*/
 	public void disableSSOWhenAuthorize() {
		disableSSO = true;
	}

	/** 鐠佸墽鐤嗙紓鏍帆妞ょ敻娼伴惃鍕▔缁�儤膩瀵繋璐烡ialog濡�绱�*/
	public void setDialogMode() {
		dialogMode = true;
		shareParamsMap.put("dialogMode", dialogMode);
	}

	/** 濞ｈ濮炴稉锟芥稉顏堟閽樺繒娈憄latform */
	public void addHiddenPlatform(String platform) {
		hiddenPlatforms.put(platform, platform);
	}

	/** 鐠佸墽鐤嗘稉锟芥稉顏勭殺鐞氼偅鍩呴崶鎯у瀻娴滎偆娈慥iew , surfaceView閺勵垱鍩呮稉宥勭啊閸ュ墽澧栭惃锟�/
	public void setViewToShare(View viewToShare) {
		try {
			Bitmap bm = captureView(viewToShare, viewToShare.getWidth(), viewToShare.getHeight());
			shareParamsMap.put("viewToShare", bm);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/** 閼垫崘顔嗗顔煎触閸掑棔闊╂径姘炊閸ュ墽澧�*/
	public void setImageArray(String[] imageArray) {
		shareParamsMap.put("imageArray", imageArray);
	}

	public void setEditPageBackground(View bgView) {
		this.bgView = bgView;
	}

	public void setOnShareButtonClickListener(PlatformListFakeActivity.OnShareButtonClickListener onShareButtonClickListener) {
		this.onShareButtonClickListener = onShareButtonClickListener;
	}

	/** 瀵邦亞骞嗛幍褑顢戦崚鍡曢煩 */
	public void share(HashMap<Platform, HashMap<String, Object>> shareData) {
		boolean started = false;
		for (Entry<Platform, HashMap<String, Object>> ent : shareData.entrySet()) {
			Platform plat = ent.getKey();
			plat.SSOSetting(disableSSO);
			String name = plat.getName();

//			boolean isGooglePlus = "GooglePlus".equals(name);
//			if (isGooglePlus && !plat.isValid()) {
//				Message msg = new Message();
//				msg.what = MSG_TOAST;
//				int resId = getStringRes(context, "google_plus_client_inavailable");
//				msg.obj = context.getString(resId);
//				UIHandler.sendMessage(msg, this);
//				continue;
//			}

			boolean isKakaoTalk = "KakaoTalk".equals(name);
			if (isKakaoTalk && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "kakaotalk_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isKakaoStory = "KakaoStory".equals(name);
			if (isKakaoStory && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "kakaostory_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isLine = "Line".equals(name);
			if (isLine && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "line_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isWhatsApp = "WhatsApp".equals(name);
			if (isWhatsApp && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "whatsapp_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isPinterest = "Pinterest".equals(name);
			if (isPinterest && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "pinterest_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			if ("Instagram".equals(name) && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "instagram_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isLaiwang = "Laiwang".equals(name);
			boolean isLaiwangMoments = "LaiwangMoments".equals(name);
			if(isLaiwang || isLaiwangMoments){
				if (!plat.isClientValid()) {
					Message msg = new Message();
					msg.what = MSG_TOAST;
					int resId = getStringRes(context, "laiwang_client_inavailable");
					msg.obj = context.getString(resId);
					UIHandler.sendMessage(msg, this);
					continue;
				}
			}

			boolean isYixin = "YixinMoments".equals(name) || "Yixin".equals(name);
			if (isYixin && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "yixin_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			HashMap<String, Object> data = ent.getValue();
			int shareType = Platform.SHARE_TEXT;
			String imagePath = String.valueOf(data.get("imagePath"));
			if (imagePath != null && (new File(imagePath)).exists()) {
				shareType = Platform.SHARE_IMAGE;
				if (imagePath.endsWith(".gif")) {
					shareType = Platform.SHARE_EMOJI;
				} else if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
					shareType = Platform.SHARE_WEBPAGE;
					if (data.containsKey("musicUrl") && !TextUtils.isEmpty(data.get("musicUrl").toString())) {
						shareType = Platform.SHARE_MUSIC;
					}
				}
			} else {
				Bitmap viewToShare = (Bitmap) data.get("viewToShare");
				if (viewToShare != null && !viewToShare.isRecycled()) {
					shareType = Platform.SHARE_IMAGE;
					if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
						shareType = Platform.SHARE_WEBPAGE;
						if (data.containsKey("musicUrl") && !TextUtils.isEmpty(data.get("musicUrl").toString())) {
							shareType = Platform.SHARE_MUSIC;
						}
					}
				} else {
					Object imageUrl = data.get("imageUrl");
					if (imageUrl != null && !TextUtils.isEmpty(String.valueOf(imageUrl))) {
						shareType = Platform.SHARE_IMAGE;
						if (String.valueOf(imageUrl).endsWith(".gif")) {
							shareType = Platform.SHARE_EMOJI;
						} else if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
							shareType = Platform.SHARE_WEBPAGE;
							if (data.containsKey("musicUrl") && !TextUtils.isEmpty(data.get("musicUrl").toString())) {
								shareType = Platform.SHARE_MUSIC;
							}
						}
					}
				}
			}
			data.put("shareType", shareType);

			if (!started) {
				started = true;
				if (this == callback) {
					int resId = getStringRes(context, "sharing");
					if (resId > 0) {
						showNotification(context.getString(resId));
					}
				}
			}
			plat.setPlatformActionListener(callback);
			ShareCore shareCore = new ShareCore();
			shareCore.setShareContentCustomizeCallback(customizeCallback);
			shareCore.share(plat, data);
		}
	}

	public void onComplete(Platform platform, int action,
			HashMap<String, Object> res) {
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	public void onError(Platform platform, int action, Throwable t) {
		t.printStackTrace();

		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = t;
		UIHandler.sendMessage(msg, this);

		// 閸掑棔闊╂径杈Е閻ㄥ嫮绮虹拋锟�		
		ShareSDK.logDemoEvent(4, platform);
	}

	public void onCancel(Platform platform, int action) {
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case MSG_TOAST: {
				String text = String.valueOf(msg.obj);
				Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
			}
			break;
			case MSG_ACTION_CCALLBACK: {
				switch (msg.arg1) {
					case 1: {
						// 閹存劕濮�						
						int resId = getStringRes(context, "share_completed");
						if (resId > 0) {
							showNotification(context.getString(resId));
						}
					}
					break;
					case 2: {
						// 婢惰精瑙�						
						String expName = msg.obj.getClass().getSimpleName();
						if ("WechatClientNotExistException".equals(expName)
								|| "WechatTimelineNotSupportedException".equals(expName)
								|| "WechatFavoriteNotSupportedException".equals(expName)) {
							int resId = getStringRes(context, "wechat_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("GooglePlusClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "google_plus_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("QQClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "qq_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("YixinClientNotExistException".equals(expName)
								|| "YixinTimelineNotSupportedException".equals(expName)) {
							int resId = getStringRes(context, "yixin_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("KakaoTalkClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "kakaotalk_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}else if ("KakaoStoryClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "kakaostory_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}else if("WhatsAppClientNotExistException".equals(expName)){
							int resId = getStringRes(context, "whatsapp_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}else {
							int resId = getStringRes(context, "share_failed");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}
					}
					break;
					case 3: {
						// 閸欐牗绉�						
						int resId = getStringRes(context, "share_canceled");
						if (resId > 0) {
							showNotification(context.getString(resId));
						}
					}
					break;
				}
			}
			break;
			case MSG_CANCEL_NOTIFY: {
				NotificationManager nm = (NotificationManager) msg.obj;
				if (nm != null) {
					nm.cancel(msg.arg1);
				}
			}
			break;
		}
		return false;
	}

	// 閸︺劎濮搁幀浣圭埉閹绘劗銇氶崚鍡曢煩閹垮秳缍�	
	public void showNotification(String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();		
	}

	/** 閺勵垰鎯侀弨顖涘瘮QQ,QZone閹哄牊娼堥惂璇茬秿閸氬骸褰傚顔煎触 */
	public void setShareFromQQAuthSupport(boolean shareFromQQLogin)
	{
		shareParamsMap.put("isShareTencentWeibo", shareFromQQLogin);
	}
}
