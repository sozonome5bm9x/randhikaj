package com.fun.fouresquarevenuefinder.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.fun.fouresquarevenuefinder.R;
import com.fun.fouresquarevenuefinder.beans.VenueBean;
import com.fun.fouresquarevenuefinder.database.DataBaseHelper;
import com.fun.fouresquarevenuefinder.foursquare.FourSquareAPIHandler;
import com.fun.fouresquarevenuefinder.listeners.MarkerImageListener;
import com.fun.fouresquarevenuefinder.managers.DialogsManager;
import com.fun.fouresquarevenuefinder.managers.LocationManagerUtility;
import com.fun.fouresquarevenuefinder.utilities.LocationUtils;
import com.fun.fouresquarevenuefinder.utilities.LruBitmapCache;
import com.fun.fouresquarevenuefinder.utilities.NetworkUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener,
		OnMarkerClickListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private GoogleMap googleMap;
	private double currentLongitude;
	private double currentLatitude;
	private Marker marker;
	MarkerOptions markerOptions = new MarkerOptions();
	boolean onSaveStateFlag = true;
	LocationManagerUtility locationManagerUtil = new LocationManagerUtility();
	public static final String SESSION_ACCESS_TOKEN = "session_token";
	private String sessionToken = null;
	private LocationManager locationManager;
	private Timer timer;
	private Location location;
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	boolean mUpdatesRequested = false;
	private RequestQueue queue;
	private static final String TAG = "MainActivity";
	private boolean nearbyVenuesFound = false;
	ImageLoader imageLoader;
	private HashMap<String, VenueBean> venuesMap = new HashMap<String, VenueBean>();
	ProgressDialog progress;
	LatLngBounds.Builder builder = new LatLngBounds.Builder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		queue = Volley.newRequestQueue(this);

		setContentView(R.layout.activity_main);
		imageLoader = new ImageLoader(queue, new LruBitmapCache());
		DataBaseHelper.init(this.getApplicationContext());
		initLocationManager();
		initMap();
		initAccessToken();
		List<VenueBean> venueListCheckDB = DataBaseHelper.getAllVenues();
		if (venueListCheckDB.size() > 0) {
			updateMapWithVeneues(venueListCheckDB);
		}
	}

	public int getDefaultLruCacheSize() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;

		return cacheSize;
	}

	private void initLocationManager() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		location = getLastKnownLocation();
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		/*
		 * Set the update interval
		 */
		mLocationRequest
				.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Note that location updates are off until the user turns them on
		mUpdatesRequested = false;
		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */

		mLocationClient = new LocationClient(this, this, this);
		timer = new Timer();
		timer.schedule(new GetLastLocation(), 20000);
	}

	class GetLastLocation extends TimerTask {

		@Override
		public void run() {

			// locationManager.removeUpdates(locationListener);
			Location tempLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (tempLocation != null) {
				location = tempLocation;
				timer.cancel();
			}
			System.out.println("loc.." + location);

			// showLocationResult(location);
			return;
		}
	}

	private void initAccessToken() {
		sessionToken = getIntent().getStringExtra(SESSION_ACCESS_TOKEN);
	}

	private Location getLastKnownLocation() {
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER,
				LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS, 100, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS, 100, this);
		List<String> providers = locationManager.getProviders(true);
		Location bestLocation = null;
		for (String provider : providers) {
			location = locationManager.getLastKnownLocation(provider);

			Log.v(TAG, "last known location, provider: %s, location: %s"
					+ provider);

			if (location == null) {
				continue;
			}
			if (bestLocation == null
					|| location.getAccuracy() < bestLocation.getAccuracy()) {
				Log.d(TAG, "Found Last Known Location");
				bestLocation = location;

			}
		}
		if (bestLocation == null) {
			return null;
		}
		return bestLocation;
	}

	private void getNearByVenues() {

		FourSquareAPIHandler.getNearByVenues(currentLatitude, currentLongitude,
				FourSquareAPIHandler.CURRENT_FOURSQUARE_VERSION,
				nearByRequestListener, errorResponseHander, queue);
		nearbyVenuesFound = true;
	}

	private Response.Listener<JSONObject> checkIntListener = new Response.Listener<JSONObject>() {

		@Override
		public void onResponse(JSONObject response) {
			if (response != null) {
				if (progress != null) {
					if (progress.isShowing()) {
						progress.dismiss();
					}
				}
				Toast.makeText(MainActivity.this, "Check in successful",
						Toast.LENGTH_LONG).show();
				Log.v(TAG, response.toString());
			}
		}

	};
	private Response.Listener<JSONObject> nearByRequestListener = new Response.Listener<JSONObject>() {

		@Override
		public void onResponse(JSONObject response) {
			if (response != null) {

				Log.v(TAG, response.toString());
				List<VenueBean> venuesList = FourSquareAPIHandler
						.fetchingVenuesBeans(response);
				if (venuesList.size() > 0) {
					DataBaseHelper.deleteAllVenues();
					for (int index = 0; index < venuesList.size(); index++) {
						VenueBean venueBean = venuesList.get(index);
						DataBaseHelper.addNewVenueBean(venueBean);
					}
					List<VenueBean> venueListCheckDB = DataBaseHelper
							.getAllVenues();
					updateMapWithVeneues(venueListCheckDB);
				}

			}
		}
	};
	private Response.ErrorListener errorResponseHander = new Response.ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError error) {
			if (progress != null) {
				if (progress.isShowing()) {
					progress.dismiss();
				}
			}

			nearbyVenuesFound = false;
		}

	};

	@SuppressLint("InflateParams")
	private class CustomInfoWindowAdapter implements InfoWindowAdapter {
		private View windowView;

		public CustomInfoWindowAdapter() {

		}

		@Override
		public View getInfoContents(Marker marker) {
			windowView = getLayoutInflater().inflate(
					R.layout.check_in_info_window, null);
			if (MainActivity.this.marker != null
					&& MainActivity.this.marker.isInfoWindowShown()) {
				MainActivity.this.marker.hideInfoWindow();
				MainActivity.this.marker.showInfoWindow();
			}

			return windowView;
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			MainActivity.this.marker = marker;

			return windowView;
		}
	}

	public void updateMapWithVeneues(List<VenueBean> venuesList) {
		for (int index = 0; index < venuesList.size(); index++) {
			VenueBean venueBean = venuesList.get(index);
			String venueID = venueBean.getVenueID();
			venuesMap.put(venueID, venueBean);
			String icon = venueBean.getCatImage();
			double longitude = venueBean.getVenueLongitude();
			double latitude = venueBean.getVenueLatitude();
			MarkerOptions markerOption = new MarkerOptions();
			markerOption.draggable(false);
			markerOption.position(new LatLng(latitude, longitude));
			// markerOption.title("\u200e" + name);
			markerOption.snippet(venueBean.getVenueID());
			@SuppressWarnings("unchecked")
			ImageRequest request = new ImageRequest(icon,
					new MarkerImageListener(markerOption, googleMap), 0, 0,
					null, errorResponseHander);

			builder.include(new LatLng(latitude, longitude));
			googleMap.setOnMarkerClickListener(this);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 200, 200, 0));
			queue.add(request);
		}

	}

	private void initMap() {
		int status = LocationManagerUtility
				.checkGooglePlayServiceStatus(getApplicationContext());
		if (!LocationManagerUtility.checkGooglePlayServices(
				getApplicationContext(), status)) {
			DialogsManager.showGooglePlayServiceErrorDialog(this, status);
		} else {
			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			googleMap = fm.getMap();
			googleMap.setMyLocationEnabled(true);
			googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
			googleMap
					.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

						@Override
						public void onInfoWindowClick(Marker marker) {
							String markerId = marker.getSnippet();
							if (markerId != null) {
								FourSquareAPIHandler.checkIn(
										markerId,
										marker.getPosition().latitude,
										marker.getPosition().longitude,
										sessionToken,
										FourSquareAPIHandler.CURRENT_FOURSQUARE_VERSION,
										checkIntListener, errorResponseHander,
										queue);
								progress = ProgressDialog.show(
										MainActivity.this, "CheckIn",
										"Loading..", true);
								Log.v(TAG, "Marker id" + markerId);
							}

						}
					});
			navigateToUserLocation(locationManager);
		}
	}

	private void navigateToUserLocation(LocationManager locationManager) {
		if (location != null) {
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);
			location = locationManager.getLastKnownLocation(provider);
			initUserLocation();
			LocationManagerUtility.userLocationFound = true;
			locationManager.requestLocationUpdates(provider, 20000, 0, this);
			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			builder.include(new LatLng(currentLatitude, currentLongitude));
			googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 25, 25, 0));

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		onSaveStateFlag = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!isFinishing()) {
			FragmentManager fm = getSupportFragmentManager();
			Fragment fragment = fm.findFragmentById(R.id.map);
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(fragment);
			ft.commit();
		}
	}

	private void initUserLocation() {
		if (location != null) {
			currentLatitude = location.getLatitude();
			currentLongitude = location.getLongitude();
			LocationManagerUtility.userLatitude = currentLatitude;
			LocationManagerUtility.userLongitude = currentLongitude;

			if (NetworkUtil.haveInternet(getApplicationContext())) {
				if (!nearbyVenuesFound) {
					getNearByVenues();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Please check internet connection", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		String markerId = marker.getSnippet();
		VenueBean venueBean = venuesMap.get(markerId);
		if (venueBean != null) {
			Log.v(TAG, "VenueName: " + venueBean.getVenueName());
		}
		return false;
	}

	@Override
	public void onLocationChanged(Location locationChanged) {
		if (locationChanged != null) {
			location = locationChanged;
			if (!nearbyVenuesFound) {
				if (location != null) {
					initUserLocation();
					navigateToUserLocation(locationManager);
					timer.cancel();
				}
			}
		}

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {

	}

	@Override
	public void onDisconnected() {

	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mLocationClient.isConnected()) {
			mLocationClient.connect();
		}
	}

}
