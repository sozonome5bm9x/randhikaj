package com.fun.fouresquarevenuefinder.listeners;

import android.graphics.Bitmap;

import com.android.volley.Response;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

@SuppressWarnings("rawtypes")
public class MarkerImageListener implements Response.Listener {
	MarkerOptions markerOptions;
	GoogleMap googleMap;
	public MarkerImageListener(MarkerOptions markerOptions, GoogleMap googleMap) {
		this.markerOptions = markerOptions;
		this.googleMap = googleMap;
	}

	@Override
	public void onResponse(Object response) {
		Bitmap bitmap = (Bitmap) response;
		if (bitmap != null) {
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
			googleMap.addMarker(markerOptions);
		}
	}
}
