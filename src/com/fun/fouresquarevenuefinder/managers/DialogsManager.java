package com.fun.fouresquarevenuefinder.managers;

import android.app.Dialog;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class DialogsManager {
	public static void showGooglePlayServiceErrorDialog(
			FragmentActivity context, int status) {
		int requestCode = 10;

		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, context,
				requestCode);
		dialog.show();
	}
}
