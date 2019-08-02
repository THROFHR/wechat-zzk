package com.gentcent.wechat.zzk.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;
import com.gentcent.wechat.zzk.WxBroadcast;
import com.gentcent.wechat.zzk.R;
import com.gentcent.wechat.zzk.model.message.bean.SendMessageBean;
import com.gentcent.wechat.zzk.service.MyService;
import com.gentcent.wechat.zzk.util.GsonUtils;
import com.gentcent.wechat.zzk.util.HookParams;
import com.gentcent.wechat.zzk.util.MyHelper;
import com.gentcent.wechat.zzk.util.SearchClasses;
import com.gentcent.wechat.zzk.util.XLog;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.PathClassLoader;

public class MainActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// 这是前提——你的app至少运行了一个service。这里表示当进程不在前台时，马上开启一个service
		Intent intent = new Intent(this, MyService.class);
		startService(intent);
	}
	
	/**
	 * 发消息
	 *
	 * @param view 代表被点击的视图
	 */
	public void sendMessage(View view) {
		EditText editText = findViewById(R.id.send_message_content);
		String content = editText.getText().toString();
		EditText editText2 = findViewById(R.id.send_message_target_id);
		String sendId = editText2.getText().toString();
		
		SendMessageBean messageBean = new SendMessageBean();
		messageBean.setFriendWxId(sendId);
		messageBean.setContent(content);
		messageBean.setType(1);
		
		WxBroadcast.sendMessage(GsonUtils.GsonString(messageBean));
	}
	
	
	/**
	 * 添加好友
	 *
	 * @param view 代表被点击的视图
	 */
	public void addFriend(View view) {
		EditText editText = findViewById(R.id.add_friend);
		String id = editText.getText().toString();
		EditText editText2 = findViewById(R.id.hello_text);
		String helloText = editText2.getText().toString();
		
		Map<String, String> map = new HashMap<>();
		map.put("addFriendName", id);
		map.put("helloText", helloText);
		
		WxBroadcast.addFriend(GsonUtils.GsonString(map));
	}
	
	/**
	 * 发送朋友圈
	 *
	 * @param view 代表被点击的视图
	 */
	public void sendSns(View view) {
		EditText editText = findViewById(R.id.sns_text);
		String snsJson = editText.getText().toString();
		WxBroadcast.sendSns(snsJson);
	}
	
	/**
	 * 读取微信数据库
	 *
	 * @param view 代表被点击的视图
	 */
	public void getWcdb(View view) {
		WxBroadcast.syncInfo();
	}
	
	/**
	 * 初始化
	 *
	 * @param view 代表被点击的视图
	 */
	public void init(View view) {
		final Context context = getApplication();
		if (context == null) {
			return;
		}
		final PackageManager packageManager = context.getPackageManager();
		if (packageManager == null) {
			return;
		}
		
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.generating));
		dialog.show();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean success = false;
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(HookParams.WECHAT_PACKAGE_NAME, 0);
					String wechatApk = packageInfo.applicationInfo.sourceDir;
					PathClassLoader wxClassLoader = new PathClassLoader(wechatApk, ClassLoader.getSystemClassLoader());
					SearchClasses.generateConfig(wechatApk, wxClassLoader, packageInfo.versionName);
					
					String config = new Gson().toJson(HookParams.getInstance());
					MyHelper.writeLine("params", config);
					success = true;
					
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				final String msg = getResources().getString(success ? R.string.generate_success : R.string.generate_failed);
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
					}
				});
			}
		}, "generate-config").start();
	}
	
	/**
	 * 获取所有已安装App信息
	 *
	 * @param view 代表被点击的视图
	 */
	public void getAppList(View view) {
		List<AppUtils.AppInfo> appsInfo = AppUtils.getAppsInfo();
		Toast.makeText(getApplication(), "已输出日志", Toast.LENGTH_SHORT).show();
		for (AppUtils.AppInfo appInfo : appsInfo) {
			String name = appInfo.getName();
			String packageName = appInfo.getPackageName();
			String versionName = appInfo.getVersionName();
			XLog.d("name: " + name + "  packageName: " + packageName + "  versionName: " + versionName);
		}
	}
	
	/**
	 * 安装app
	 *
	 * @param view 代表被点击的视图
	 */
	@SuppressLint("SdCardPath")
	public void installXposedCkeck(View view) {
		AppUtils.installApp(new File("/sdcard/XposedCheck.apk"));
	}
	
	/**
	 * 卸载app
	 *
	 * @param view 代表被点击的视图
	 */
	public void uninstallXposedCkeck(View view) {
		AppUtils.uninstallApp("com.ssrj.xposedcheck");
	}
	
	
}