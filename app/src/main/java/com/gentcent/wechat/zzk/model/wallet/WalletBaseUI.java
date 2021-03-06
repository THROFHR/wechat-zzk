package com.gentcent.wechat.zzk.model.wallet;

import android.app.Activity;

import com.blankj.utilcode.util.PhoneUtils;
import com.gentcent.wechat.zzk.background.UploadUtil;
import com.gentcent.wechat.zzk.util.XLog;
import com.gentcent.wechat.zzk.model.wallet.bean.BackMoneyResult;
import com.gentcent.zzk.xped.XC_MethodHook;
import com.gentcent.zzk.xped.XposedHelpers;
import com.gentcent.zzk.xped.callbacks.XC_LoadPackage.LoadPackageParam;

public class WalletBaseUI {
	public static void b(int i, int i2, String str) {
		XLog.d("MyWalletBaseUI payCallback  errType:" + i + "，errCode:" + i2 + "，errMsg:" + str);
		if (str.contains("支付密码错误，请重试")) {
			UploadUtil.sendToBack(new BackMoneyResult(PhoneUtils.getIMEI(), Boolean.FALSE, str, 1));
		} else if (!str.contains("银行卡") || !str.contains("余额不足")) {
			UploadUtil.sendToBack(new BackMoneyResult(PhoneUtils.getIMEI(), Boolean.FALSE, str, 3));
		} else {
			UploadUtil.sendToBack(new BackMoneyResult(PhoneUtils.getIMEI(), Boolean.FALSE, str, 2));
		}
	}
	
	public static void hook(LoadPackageParam loadPackageParam) {
		try {
			Class loadClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.wallet_core.ui.WalletBaseUI");
			XposedHelpers.findAndHookMethod(loadClass, "onDestroy", new XC_MethodHook() {
				public void beforeHookedMethod(MethodHookParam methodHookParam) {
					if (WalletPayUI.a) {
						WalletPayUI.a = false;
					}
				}
			});
			XposedHelpers.findAndHookMethod(loadPackageParam.classLoader.loadClass("com.tencent.mm.wallet_core.ui.WalletBaseUI"), "onSceneEnd", Integer.TYPE, Integer.TYPE, String.class, loadPackageParam.classLoader.loadClass("com.tencent.mm.ak.m"), Boolean.TYPE, new XC_MethodHook() {
				public void beforeHookedMethod(MethodHookParam methodHookParam) {
					if (WalletPayUI.a) {
						Activity activity = (Activity) methodHookParam.thisObject;
						int intValue = (Integer) methodHookParam.args[0];
						int intValue2 = (Integer) methodHookParam.args[1];
						String str = (String) methodHookParam.args[2];
						if (intValue != 0 || intValue2 != 0) {
							WalletBaseUI.b(intValue, intValue2, str);
							activity.finish();
							LuckyMoney.a();
							Remittance.a();
							WalletPayUI.a();
						} else if (WalletPayUI.c) {
							UploadUtil.sendToBack(new BackMoneyResult(PhoneUtils.getIMEI(), Boolean.TRUE, "成功", 0));
						}
					}
				}
			});
		} catch (Throwable th) {
			XLog.d("MyWalletBaseUI hookTargetClass e:" + th.getMessage());
		}
	}
}
