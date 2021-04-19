package com.fun.fouresquarevenuefinder.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

	public static boolean haveInternet(Context ctx) {

		NetworkInfo info = ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnectedOrConnecting()) {
			return false;
		}
		return true;
	}
}
