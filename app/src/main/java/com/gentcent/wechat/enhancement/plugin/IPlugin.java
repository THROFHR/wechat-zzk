package com.gentcent.wechat.enhancement.plugin;

import com.gentcent.zzk.xped.callbacks.XC_LoadPackage;

public interface IPlugin {
    public void hook(XC_LoadPackage.LoadPackageParam lpparam);
}
