package com.gentcent.wechat.zzk.wcdb;

import android.database.Cursor;
import android.text.TextUtils;

import com.blankj.utilcode.util.ObjectUtils;
import com.gentcent.wechat.zzk.util.MyHelper;
import com.gentcent.wechat.zzk.util.XLog;
import com.gentcent.zzk.xped.XposedHelpers;
import com.gentcent.zzk.xped.callbacks.XC_LoadPackage;

/**
 * @author zuozhi
 * @since 2019-07-25
 */
public class UserDao {
	private final static String TAG = "UserDao:  ";
	
	/**
	 * 获取自己的微信昵称
	 */
	public static String getMyName() {
		Cursor c1 = WcdbHolder.excute("select value from userinfo where id='4'");
		String myName = "";
		while (c1.moveToNext()) {
			myName = c1.getString(c1.getColumnIndex("value"));
			XLog.d(TAG + "getMyName: " + myName);
		}
		c1.close();
		return myName;
	}
	
	/**
	 * 获取自己的微信ID
	 */
	public static String getMyWxid() {
		String myWxid = "";
		String result = "";
		Cursor a2 = WcdbHolder.excute("select value from userinfo where id='2'");
		if (ObjectUtils.isNotEmpty((Object) a2)) {
			while (a2.moveToNext()) {
				myWxid = a2.getString(a2.getColumnIndex("value"));
			}
		} else {
			XLog.d("getSelfWxid user_ID is empty");
		}
		if (a2 != null) {
			a2.close();
		}
		if (ObjectUtils.isNotEmpty((CharSequence) myWxid)) {
			MyHelper.writeLine("myWechatID", myWxid);
			result = myWxid;
		}
		if (TextUtils.isEmpty(result)) {
			result = MyHelper.readLine("myWechatID", "");
		}
		XLog.d("我的微信ID: " + result);
		return result;
	}
	
	/**
	 * 获取？？的微信ID
	 */
	public static String b(XC_LoadPackage.LoadPackageParam lpparam) {
		String wxid = "";
		try {
			wxid = (String) XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass("com.tencent.mm.model.q"), "Wt");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		XLog.d("HToolsUSERNAME :" + wxid);
		return wxid;
	}
	
	/**
	 * 根据wxid获取昵称(有备注则获取备注)
	 *
	 * @param wxid 微信ID
	 * @return 昵称/备注
	 */
	public static String getNickByWxid(String wxid) {
		if (TextUtils.equals(wxid, getMyWxid())) {
			return getMyName();
		}
		String nickName = "";
		String conRemark = "";
		String sql = "select nickname,conRemark from rcontact where username='" + wxid + "'";
		Cursor c1 = WcdbHolder.excute(sql);
		while (c1.moveToNext()) {
			nickName = c1.getString(c1.getColumnIndex("nickname"));
			conRemark = c1.getString(c1.getColumnIndex("conRemark"));
		}
		if (!TextUtils.isEmpty(conRemark)) {
			nickName = conRemark;
		}
		return nickName;
	}
	
}