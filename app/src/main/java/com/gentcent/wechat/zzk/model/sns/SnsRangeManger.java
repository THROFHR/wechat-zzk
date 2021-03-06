package com.gentcent.wechat.zzk.model.sns;

import android.app.Activity;
import android.content.Intent;

import com.gentcent.zzk.xped.XposedHelpers;

/**
 * 设置朋友圈查看权限
 */
public class SnsRangeManger {
	private static final int requestCode = 5;
	private static final int resultCode = -1;
	String Klabel_name_list;
	String Kother_user_name_list;
	int Ktag_range_index;
	
	public SnsRangeManger(int Ktag_range_index, String Klabel_name_list, String Kother_user_name_list) {
		this.Ktag_range_index = Ktag_range_index;
		this.Klabel_name_list = Klabel_name_list;
		this.Kother_user_name_list = Kother_user_name_list;
	}
	
	public void addRange(Activity activity) {
		if (this.Klabel_name_list == null) {
			this.Klabel_name_list = "";
		}
		if (this.Kother_user_name_list == null) {
			this.Kother_user_name_list = "";
		}
		if (this.Ktag_range_index < 2) {
			return;
		}
		if (!this.Kother_user_name_list.equals("") || !this.Klabel_name_list.equals("")) {
			Intent intent = new Intent();
			intent.putExtra("Ktag_range_index", this.Ktag_range_index);
			intent.putExtra("Klabel_name_list", this.Klabel_name_list);
			intent.putExtra("Kother_user_name_list", this.Kother_user_name_list);
			XposedHelpers.callMethod(activity, "onActivityResult", 5, -1, intent);
		}
	}
	
	public String toString() {
		return "PengyouquanRangeManger{Ktag_range_index='" +
				this.Ktag_range_index +
				'\'' +
				", Klabel_name_list=" +
				this.Klabel_name_list +
				", Kother_user_name_list='" +
				this.Kother_user_name_list +
				'\'' +
				'}';
	}
}
