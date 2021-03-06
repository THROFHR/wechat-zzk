package com.gentcent.wechat.zzk.model.sns;

import android.text.TextUtils;
import android.util.Log;

import com.gentcent.wechat.zzk.model.sns.bean.SnsCommentBean;
import com.gentcent.wechat.zzk.model.sns.bean.SnsContentItemBean;
import com.gentcent.wechat.zzk.model.sns.bean.SnsLikeBean;
import com.gentcent.wechat.zzk.util.XLog;
import com.gentcent.wechat.zzk.wcdb.UserDao;
import com.gentcent.zzk.xped.XposedHelpers;
import com.gentcent.zzk.xped.callbacks.XC_LoadPackage.LoadPackageParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author zuozhi
 * @since 2019-07-25
 */
public class SnsHandler {
	private static final String TAG = "SnsManager:  ";
	private static final int IMAGE = 1;
	private static final int ARTICLE = 3;
	private static final int VIDEO = 15;
	private static final String CONTEXT_KEY = "1";
	private static final String WAKE_TYPE_KEY = "2";
	public static String SelfCommend = "";
	
	/**
	 * 获得所有朋友圈数据
	 */
	public static List<SnsContentItemBean> getAllDatas(LoadPackageParam lpparam) {
		return getSnsContentItemBeanList_By_Rowids(lpparam, SnsDao.getSnsRowids());
	}
	
	/**
	 * 获得自己的所有朋友圈数据
	 */
	public static List<SnsContentItemBean> getSelfAllDatas(LoadPackageParam lpparam) {
		return getSnsContentItemBeanList_By_Rowids(lpparam, SnsDao.getMyFriendGroupRowids());
	}
	
	/**
	 * 回复评论
	 *
	 * @param wSnsContentBean 微信朋友圈对象
	 * @param description     回复内容
	 * @param wComment        微信评论对象
	 */
	private static void makeComment_Reply(Object wSnsContentBean, String description, Object wComment, LoadPackageParam lpparam) throws ClassNotFoundException {
		Boolean bool = (Boolean) XposedHelpers.callMethod(wSnsContentBean, "FH", 32);
		Class loadClass = lpparam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.an$a");
		String methodName = "a";
		Object[] params = new Object[6];
		params[0] = wSnsContentBean;
		params[1] = bool ? 8 : 2;
		params[2] = description;
		params[3] = wComment;
		params[4] = 0;
		params[5] = 0;
		XposedHelpers.callStaticMethod(loadClass, methodName, params);
	}
	
	/**
	 * 对指定朋友圈id回复评论
	 *
	 * @param snsId       朋友圈ID
	 * @param description 回复内容
	 * @param username    微信id
	 */
	public static void makeComment_Reply_By_ID(String snsId, String description, String username, LoadPackageParam lpparam) {
		XLog.d(TAG + "makeComment_Reply_By_ID start");
		Object GetWSnsContentBean_By_snsID = GetWSnsContentBean_By_snsID(lpparam, snsId);
		if (GetWSnsContentBean_By_snsID != null) {
			try {
				Object comment = null;
				LinkedList wCommentList = (LinkedList) XposedHelpers.getObjectField(XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.ak"), "p", GetWSnsContentBean_By_snsID), "CommentUserList");
				for (Object wCommen : wCommentList) {
					if (username.equals((String) XposedHelpers.getObjectField(wCommen, "Username"))) {
						comment = wCommen;
						break;
					}
				}
				if (comment != null) {
					makeComment_Reply(GetWSnsContentBean_By_snsID, description, comment, lpparam);
				} else {
					XLog.d("makeComment_Reply_By_ID Ocomment==null");
				}
			} catch (Throwable th) {
				XLog.d(TAG + "makeComment_Reply_By_ID e ：" + Log.getStackTraceString(th));
				th.printStackTrace();
			}
		} else {
			XLog.d(TAG + "makeComment_Reply_By_ID Osns==null");
		}
	}
	
	/**
	 * 追加朋友圈评论
	 *
	 * @param wSnsContentBean 微信朋友圈对象
	 * @param description     回复内容
	 */
	private static void makeComment_Append(Object wSnsContentBean, String description, LoadPackageParam lpparam) throws ClassNotFoundException {
		Class loadClass = lpparam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.an$a");
		Boolean bool = (Boolean) XposedHelpers.callMethod(wSnsContentBean, "FH", 32);
		String methodName = "a";
		Object[] params = new Object[6];
		params[0] = wSnsContentBean;
		params[1] = bool ? 8 : 2;
		params[2] = description;
		params[3] = XposedHelpers.newInstance(lpparam.classLoader.loadClass("com.tencent.mm.protocal.protobuf.cds"));
		params[4] = 0;
		params[5] = 0;
		XposedHelpers.callStaticMethod(loadClass, methodName, params);
		XLog.d("makeComment_Append info is " + description);
	}
	
	/**
	 * 通过snsId追加评论
	 *
	 * @param snsId       朋友圈Id
	 * @param description 评论内容
	 */
	public static void makeComment_Append_By_SnsID(LoadPackageParam lpparam, String snsId, String description) {
		XLog.d("makeComment_Append_By_SnsID start");
		Object GetWSnsContentBean_By_snsID = GetWSnsContentBean_By_snsID(lpparam, snsId);
		if (GetWSnsContentBean_By_snsID != null) {
			try {
				makeComment_Append(GetWSnsContentBean_By_snsID, description, lpparam);
			} catch (ClassNotFoundException e) {
				XLog.d("makeComment_Append_By_SnsID e ：" + Log.getStackTraceString(e));
				e.printStackTrace();
			}
		} else {
			XLog.d("makeComment_Append_By_SnsID Osns==null");
		}
	}
	
	/**
	 * 点赞
	 *
	 * @param obj 微信朋友圈bean
	 */
	private static void clickLike(Object obj, LoadPackageParam loadPackageParam) throws Throwable {
		int intField = XposedHelpers.getIntField(obj, "field_likeFlag");
		XposedHelpers.getLongField(obj, "field_snsId");
		if (intField == 0) {
			XposedHelpers.setIntField(obj, "field_likeFlag", 1);
			XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.storage.h"), "a", XposedHelpers.callMethod(obj, "getSnsId"), obj);
			XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.an$a"), "a", obj, (Boolean) XposedHelpers.callMethod(obj, "FH", 32) ? 7 : 1, "", 0);
			return;
		}
		XposedHelpers.setIntField(obj, "field_likeFlag", 0);
		XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.storage.h"), "a", XposedHelpers.callMethod(obj, "getSnsId"), obj);
		XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.an$a"), "abP", XposedHelpers.callMethod(obj, "getSnsId"));
	}
	
	/**
	 * 批量点赞
	 */
	public static void clickLike_By_SnsID_Numbers(LoadPackageParam lpparam, int i) {
		XLog.d(TAG + "clickLike_By_SnsID_Numbers " + i);
		List<String> friendGroupRowids = SnsDao.getSnsRowids();
		int size = friendGroupRowids.size();
		XLog.d(TAG + "clickLike_By_SnsID_Numbers size:" + size);
		if (size < i) {
			i = size;
		}
		int i2 = 0;
		while (i > 0 && i2 <= size - 1) {
			String sb3 = "sns_table_" + friendGroupRowids.get(i2);
			Object GetWSnsContentBean_By_RowID = GetWSnsContentBean_By_RowID(lpparam, sb3);
			if (GetWSnsContentBean_By_RowID == null) {
				XLog.d(TAG + "clickLike_By_SnsID_Numbers  snscontentbean is null");
				i2++;
			} else {
				String rowId = "sns_table_" + size;
				SnsContentItemBean snsContentItemBean = getSnsContentItemBean(lpparam, GetWSnsContentBean_By_RowID, rowId);
				XLog.d(TAG + "snsFriendIsLike snsContentItemBean :" + snsContentItemBean.toString());
				if (!snsContentItemBean.isSelfLike()) {
					try {
						XLog.d("批量  clickLike ");
						clickLike(GetWSnsContentBean_By_RowID, lpparam);
						i--;
					} catch (Throwable th) {
						th.printStackTrace();
					}
				}
				i2++;
			}
		}
	}
	
	/**
	 * 根据朋友圈ID删除朋友圈
	 *
	 * @param snsId 朋友圈ID
	 */
	public static void delete_SnsContentBean_By_SnsID(LoadPackageParam lpparam, String snsId) {
		XLog.d("delete_SnsContentBean_By_SnsID start");
		Object GetWSnsContentBean_By_snsID = GetWSnsContentBean_By_snsID(lpparam, snsId);
		if (GetWSnsContentBean_By_snsID != null) {
			delete_SnsContentBean(GetWSnsContentBean_By_snsID, lpparam);
		} else {
			XLog.d("delete_SnsContentBean_By_SnsID Osns==null");
		}
	}
	
	/**
	 * 删除朋友圈
	 *
	 * @param wSnsContentBean 微信朋友圈对象
	 */
	private static void delete_SnsContentBean(Object wSnsContentBean, LoadPackageParam loadPackageParam) {
		try {
			long longField = XposedHelpers.getLongField(wSnsContentBean, "field_snsId");
			int intField = XposedHelpers.getIntField(wSnsContentBean, "field_type");
			long longValue = (Long) XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.storage.v"), "adb", (String) XposedHelpers.callMethod(wSnsContentBean, "getSnsId"));
			XposedHelpers.callMethod(XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.ag"), "cwq"), "lW", longValue);
			XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.kernel.g"), "Wc");
			XposedHelpers.callMethod(XposedHelpers.getObjectField(XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.kernel.g"), "Wa"), "fbW"), "a", XposedHelpers.newInstance(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.r"), longValue, 1), 0);
			XposedHelpers.callMethod(XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.ag"), "cwr"), "delete", longValue);
			XposedHelpers.callMethod(XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.ag"), "cww"), "mi", longValue);
			XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.storage.i"), "mh", longValue);
			Object callMethod = XposedHelpers.callMethod(wSnsContentBean, "czu");
			if (callMethod != null) {
				String str = null;
				Object objectField = XposedHelpers.getObjectField(callMethod, "yDr");
				if (objectField != null) {
					str = (String) XposedHelpers.getObjectField(objectField, "Id");
				}
				if (!(Boolean) XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.sdk.platformtools.bo"), "isNullOrNil", str)) {
					Object staticObjectField = XposedHelpers.getStaticObjectField(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.c.a"), "gHA");
					if ((Boolean) XposedHelpers.callMethod(staticObjectField, "ej", str)) {
						String str2 = (String) XposedHelpers.callMethod(staticObjectField, "eh", str);
						Object newInstance = XposedHelpers.newInstance(loadPackageParam.classLoader.loadClass("com.tencent.mm.g.a.ot"));
						Object objectField2 = XposedHelpers.getObjectField(newInstance, "cYK");
						XposedHelpers.setObjectField(objectField2, "appId", str);
						XposedHelpers.setObjectField(objectField2, "cYL", XposedHelpers.getObjectField(callMethod, "kfE"));
						XposedHelpers.setObjectField(objectField2, "crY", str2);
						XposedHelpers.setObjectField(objectField2, "mediaTagName", XposedHelpers.getObjectField(callMethod, "yDw"));
						XposedHelpers.callMethod(XposedHelpers.getStaticObjectField(loadPackageParam.classLoader.loadClass("com.tencent.mm.sdk.b.a"), "yVI"), "l", newInstance);
					}
				}
			}
			Object callStaticMethod = XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.modelsns.b"), "mB", 739);
			if (callStaticMethod == null) {
				callStaticMethod = XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.modelsns.b"), "mC", 739);
			}
			if (callStaticMethod != null) {
				Object callMethod2 = XposedHelpers.callMethod(XposedHelpers.callMethod(callStaticMethod, "vm", XposedHelpers.callStaticMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.sns.data.i"), "j", wSnsContentBean)), "mE", intField);
				String str3 = longField == 0 ? CONTEXT_KEY : "0";
				if ((Boolean) XposedHelpers.callMethod(wSnsContentBean, "czW")) {
					str3 = WAKE_TYPE_KEY;
				}
				XposedHelpers.callMethod(callMethod2, "vm", str3);
				XposedHelpers.callMethod(callStaticMethod, "aoy");
			}
		} catch (Throwable th) {
			th.printStackTrace();
			XLog.d(" delete_SnsContentBean e:" + Log.getStackTraceString(th));
		}
	}
	
	/**
	 * 通过RowID得到微信调用的朋友圈对象
	 *
	 * @param rowId 微信数据库的RowID
	 * @return wSnsContentBean    微信朋友圈对象
	 */
	public static Object GetWSnsContentBean_By_RowID(LoadPackageParam lpparam, String rowId) {
		try {
			return GetWSnsContentBean(lpparam, "acI", rowId);
		} catch (Throwable unused) {
			return null;
		}
	}
	
	/**
	 * 通过SnsID得到微信调用的朋友圈对象
	 *
	 * @param snsId 微信数据库的SnsId
	 * @return wSnsContentBean    微信朋友圈对象
	 */
	public static Object GetWSnsContentBean_By_snsID(LoadPackageParam lpparam, String snsId) {
		return GetWSnsContentBean(lpparam, "acH", snsId);
	}
	
	/**
	 * @param methodName "XA":bySnsId    "XB":byRowId
	 */
	private static Object GetWSnsContentBean(LoadPackageParam lpparam, String methodName, String id) {
		if (methodName.equals("acH") || methodName.equals("acI")) {
			try {
				return XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass("com.tencent.mm.plugin.sns.storage.h"), methodName, id);
			} catch (ClassNotFoundException e) {
				XLog.d("GetWSnsContentBean e:" + Log.getStackTraceString(e));
				return null;
			}
		} else {
			XLog.d("GetWSnsContentBean  参数错误");
			return null;
		}
	}
	
	/**
	 * 批量通过RowID得到微信调用的朋友圈对象
	 *
	 * @param list rowid集合
	 */
	public static List<SnsContentItemBean> getSnsContentItemBeanList_By_Rowids(LoadPackageParam lpparam, List<String> list) {
		
		try {
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			for (String str : list) {
				String rowid = "sns_table_" + str;
				Object GetWSnsContentBean_By_RowID = GetWSnsContentBean_By_RowID(lpparam, rowid);
				if (GetWSnsContentBean_By_RowID != null) {
					hashMap.put(rowid, GetWSnsContentBean_By_RowID);
				}
			}
			ArrayList<SnsContentItemBean> arrayList = new ArrayList<>();
			for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
				arrayList.add(getSnsContentItemBean(lpparam, entry.getValue(), entry.getKey()));
			}
			return arrayList;
		} catch (Exception e) {
			XLog.e("error:" + Log.getStackTraceString(e));
			return null;
		}
	}
	
	/**
	 * 封装成自己的SnsContentItemBean
	 *
	 * @param wSnsContentBean 微信朋友圈对象
	 * @param rowId           微信数据库的RowID
	 */
	public static SnsContentItemBean getSnsContentItemBean(LoadPackageParam lpparam, Object wSnsContentBean, String rowId) {
		SnsContentItemBean snsContentItemBean = new SnsContentItemBean();
		snsContentItemBean.setSnsWxid((String) XposedHelpers.getObjectField(wSnsContentBean, "field_userName"));
		snsContentItemBean.setNickName(UserDao.getNickByWxid(snsContentItemBean.getSnsWxid()));
		snsContentItemBean.setSnsID((String) XposedHelpers.callMethod(wSnsContentBean, "getSnsId"));
		Object cmi = XposedHelpers.callMethod(wSnsContentBean, "czu");
		snsContentItemBean.setTimestamp((long) (Integer) XposedHelpers.getObjectField(cmi, "CreateTime"));
		snsContentItemBean.setTimestamp(snsContentItemBean.getTimestamp() * 1000);
		snsContentItemBean.setContent((String) XposedHelpers.getObjectField(cmi, "yDp"));
		snsContentItemBean.setSelfLike((Integer) XposedHelpers.getObjectField(wSnsContentBean, "field_likeFlag") == 1);
		snsContentItemBean.setImages(new ArrayList<String>());
		Object wsx = XposedHelpers.getObjectField(cmi, "yDs");
		int type = (Integer) XposedHelpers.getObjectField(wsx, "xxw");
		if (type == IMAGE) {
			snsContentItemBean.setType(2);
			for (Object o : ((LinkedList) XposedHelpers.getObjectField(wsx, "xxx"))) {
				snsContentItemBean.getImages().add((String) XposedHelpers.getObjectField(o, "Url"));
			}
		} else if (type == ARTICLE) {
			snsContentItemBean.setType(4);
			String title = (String) XposedHelpers.getObjectField(wsx, "Title");
			String url = (String) XposedHelpers.getObjectField(wsx, "Url");
			LinkedList linkedList = (LinkedList) XposedHelpers.getObjectField(wsx, "xxx");
			if (linkedList != null && linkedList.size() > 0) {
				snsContentItemBean.setArticleImage((String) XposedHelpers.getObjectField(linkedList.get(0), "Url"));
			}
			snsContentItemBean.setArticleTitle(title);
			snsContentItemBean.setArticleUrl(url);
		} else if (type != VIDEO) {
			snsContentItemBean.setType(1);
		} else {
			snsContentItemBean.setType(3);
			LinkedList linkedList2 = (LinkedList) XposedHelpers.getObjectField(wsx, "xxx");
			if (linkedList2 != null && linkedList2.size() > 0) {
				Object obj2 = linkedList2.get(0);
				if (obj2 != null) {
					snsContentItemBean.setVideo((String) XposedHelpers.getObjectField(obj2, "Url"));
				}
			}
		}
		try {
			Object callStaticMethod = XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass("com.tencent.mm.plugin.sns.model.ak"), "p", wSnsContentBean);
			LinkedList linkedList3 = (LinkedList) XposedHelpers.getObjectField(callStaticMethod, "CommentUserList");
			for (Object o : ((LinkedList) XposedHelpers.getObjectField(callStaticMethod, "LikeUserList"))) {
				snsContentItemBean.getLikelist().add(getSnsLikeBean(o));
			}
			for (Object o : linkedList3) {
				snsContentItemBean.getCommentlist().add(getSnsCommentBean(o));
			}
			int intField = XposedHelpers.getIntField(callStaticMethod, "ExtFlag");
			if (intField != 3) {
				if (intField != 5) {
					snsContentItemBean.setPublic(true);
					if (snsContentItemBean.isPublic()) {
						snsContentItemBean.setLookUpType(0);
					} else if (snsContentItemBean.isSee()) {
						snsContentItemBean.setLookUpType(2);
					} else {
						snsContentItemBean.setLookUpType(3);
					}
					return snsContentItemBean;
				}
			}
			snsContentItemBean.setPublic(false);
			LinkedList linkedList4 = null;
			if (intField == 3) {
				snsContentItemBean.setSee(false);
				linkedList4 = (LinkedList) XposedHelpers.getObjectField(callStaticMethod, "BlackList");
			} else if (intField == 5) {
				snsContentItemBean.setSee(true);
				linkedList4 = (LinkedList) XposedHelpers.getObjectField(callStaticMethod, "GroupUser");
			}
			if (linkedList4 == null || linkedList4.size() <= 0) {
				XLog.d(TAG + "getSnsContentItemBean localLinkedList3  ==null");
//				if (snsContentItemBean.isPublic()) {
//				}
				return snsContentItemBean;
			}
			for (Object next : linkedList4) {
				if (next != null) {
					snsContentItemBean.getChooseFriends().add((String) XposedHelpers.getObjectField(next, "yte"));
				}
			}
//			if (snsContentItemBean.IsPublic) {
//			}
			return snsContentItemBean;
		} catch (Throwable ignored) {
		}
		return snsContentItemBean;
	}
	
	/**
	 * 微信对象封装成自己的对象
	 *
	 * @param obj 微信点赞Bean对象
	 */
	private static SnsLikeBean getSnsLikeBean(Object obj) {
		SnsLikeBean snsLikeBean = new SnsLikeBean();
		snsLikeBean.setTimestamp((long) XposedHelpers.getIntField(obj, "CreateTime"));
		snsLikeBean.setWxid((String) XposedHelpers.getObjectField(obj, "Username"));
		snsLikeBean.setNickName(UserDao.getNickByWxid(snsLikeBean.getWxid()));
		return snsLikeBean;
	}
	
	/**
	 * 微信对象封装成自己的对象
	 *
	 * @param obj 微信评论Bean对象
	 */
	private static SnsCommentBean getSnsCommentBean(Object obj) {
		SnsCommentBean snsCommentBean = new SnsCommentBean();
		try {
			snsCommentBean.setTimestamp((long) XposedHelpers.getIntField(obj, "CreateTime") * 1000);
			snsCommentBean.setWxid((String) XposedHelpers.getObjectField(obj, "Username"));
			snsCommentBean.setNickName(UserDao.getNickByWxid(snsCommentBean.getWxid()));
			snsCommentBean.setCommentId(snsCommentBean.getWxid());
			snsCommentBean.setCommentText((String) XposedHelpers.getObjectField(obj, "nWR"));
			snsCommentBean.setBeReviewedWxid((String) XposedHelpers.getObjectField(obj, "yxR"));
			if (!TextUtils.isEmpty(snsCommentBean.getBeReviewedWxid())) {
				snsCommentBean.setBeReviewedNickName(UserDao.getNickByWxid(snsCommentBean.getBeReviewedWxid()));
			}
		} catch (Throwable th) {
			th.printStackTrace();
			XLog.d(TAG + "getSnsCommentBean e:" + Log.getStackTraceString(th));
		}
		return snsCommentBean;
	}
	
}
