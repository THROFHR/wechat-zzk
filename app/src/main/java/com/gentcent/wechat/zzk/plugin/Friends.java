package com.gentcent.wechat.zzk.plugin;

import com.gentcent.wechat.zzk.model.friend.AddFriendHook;
import com.gentcent.zzk.xped.callbacks.XC_LoadPackage;


public class Friends implements IPlugin {
	@Override
	public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
		AddFriendHook.hook(lpparam);
	}
}
