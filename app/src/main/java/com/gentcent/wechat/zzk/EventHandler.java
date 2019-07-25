package com.gentcent.wechat.zzk;

import android.content.Context;
import android.content.Intent;

import com.gentcent.wechat.zzk.bean.MessageBean;
import com.gentcent.wechat.zzk.manager.SendMessageManager;
import com.gentcent.wechat.zzk.util.HookParams;
import com.gentcent.wechat.zzk.util.XLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author zuozhi
 * @since 2019-07-08
 */
public class EventHandler {
	
	/**
	 * 发送消息
	 */
	public static void sendMessage(MessageBean messageBean) {
		Context context = MyApplication.getAppContext();
		XLog.d("添加至消息队列 类型:" + messageBean.getType());
		SendMessageManager.addToQueque(messageBean);
		//添加至消息队列
		sendBroad(context);
	}
	
	/**
	 * 发送广播，传递消息队列
	 */
	private static void sendBroad(final Context context) {
		int quequeSize = SendMessageManager.getQuequeSize();
		if(quequeSize>0 && !SendMessageManager.isLock()){
			SendMessageManager.lock();
			List<MessageBean> list = new ArrayList<>(SendMessageManager.getQueque());
			SendMessageManager.clearQueque();
			
			Intent intent = new Intent("WxAction");
			intent.putExtra("act", "send_message");
			intent.putExtra("msgQueue", (Serializable)list);
			context.sendBroadcast(intent);
			XLog.d("发送消息 | 个数："+quequeSize);
			
			//发送完成后再次执行，直到消息队列为空为止
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					SendMessageManager.unLock();
					sendBroad(context);
				}
			};
			new Timer().schedule(task, HookParams.SEND_TIME_INTERVAL * (quequeSize + 1));
		}
	}
	
	/**
	 * 添加好友
	 */
	public static void addFriend(String id) {
		try {
			Context context = MyApplication.getAppContext();
			Intent intent = new Intent("WxAction");
			intent.putExtra("act", "add_friend");
			intent.putExtra("addFriendName", id);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送朋友圈
	 */
	public static void sendSns(String snsJson) {
		try {
			XLog.d("发送朋友圈");
			Context context = MyApplication.getAppContext();
			Intent intent = new Intent("WxAction");
			intent.putExtra("act", "send_sns");
			intent.putExtra("snsJson", snsJson);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取微信数据库
	 */
	public static void getWcdb() {
		try {
			XLog.d("读取微信数据库");
			Context context = MyApplication.getAppContext();
			Intent intent = new Intent("WxAction");
			intent.putExtra("act", "sync_info");
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}