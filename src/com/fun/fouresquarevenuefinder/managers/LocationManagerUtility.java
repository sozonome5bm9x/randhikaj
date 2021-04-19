package com.fun.fouresquarevenuefinder.managers;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class LocationManagerUtility {
	public static double userLongitude = 0;
	public static double userLatitude = 0;
	public static boolean userLocationFound = false;

	/**
	 * Check google play services.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkGooglePlayServices(Context context, int status) {
		boolean isFound = false;

		// Showing status
		if (status == ConnectionResult.SUCCESS) {
			isFound = true;
		}
		return isFound;
	}

	public static int checkGooglePlayServiceStatus(Context context) {
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);

		return status;

	}
}
