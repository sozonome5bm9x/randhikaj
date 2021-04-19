package com.fun.fouresquarevenuefinder.foursquare;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fun.fouresquarevenuefinder.beans.VenueBean;

public class FourSquareAPIHandler {
	public final static String CLIENT_ID = "VO5WZOTFQXOISC1KHKUTBJ3M3BOQCY3TJP2VNXEQBUNPVIVC";
	public final static String CLIENT_SECRET = "24GFI2PPTXLJ1MZUTXLXYNECE3K2RYTQXJXRNZCIN5ODB31K";
	public final static int REQUEST_CODE_FSQ_CONNECT = 23522;
	public final static int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 23523;
	public final static String NEARBY_VENUE_API_CALL = "https://api.foursquare.com/v2/venues/search?";
	public final static String CHECK_IN_API_CALL = "https://api.foursquare.com/v2/checkins/add?";
	public final static String CLIENT_ID_ATTR = "client_id=";
	public final static String CLIENT_SECRET_ATTR = "client_secret=";
	public final static String VERSION_ATTR = "v=";
	public final static String CURRENT_FOURSQUARE_VERSION = "20141128";
	public final static String LIMIT_ATTR = "limit=";
	public final static String RADIUS_ATTR = "radius=";
	public final static String LONG_LAT_ATTR = "ll=";
	public final static String AND_SPLIT = "&";
	public final static String HANDLER_ATTR = "m=";
	public final static String FOURSQUARE_HANDLER = "foursquare";
	public final static String SWARM_HANDLER = "swarm";
	public final static String VENUE_KEY = "venues";
	public final static String RESPONSE_KEY = "response";
	public final static String ID_KEY = "id";
	public final static String NAME_KEY = "name";
	public final static String LONG_KEY = "lng";
	public final static String LAT_KEY = "lat";
	public final static String LOCATION = "location";
	public final static String CITY = "city";
	public final static String ICON = "icon";
	public final static String PREFIX = "prefix";
	public final static String SUFFIX = "suffix";
	public final static String IMAGE_SIZE = "bg_32";
	public final static String CATEGORIES = "categories";
	public final static String VENUE_ID = "venueId=";
	public final static String OAUTH_TOKEN = "oauth_token=";

	public static void checkIn(String venueId, double latitude,
			double longitude, String auth, String version,
			Response.Listener<JSONObject> successCallback,
			Response.ErrorListener errorCallback, RequestQueue queue) {
		String ll = getLatLongString(latitude, longitude);
		String requestURL = CHECK_IN_API_CALL + OAUTH_TOKEN + auth + AND_SPLIT
				+ VENUE_ID  + venueId + AND_SPLIT + LONG_LAT_ATTR
				+ ll + AND_SPLIT + HANDLER_ATTR + SWARM_HANDLER+ AND_SPLIT + VERSION_ATTR + CURRENT_FOURSQUARE_VERSION;
		JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
				requestURL, null, successCallback, errorCallback);
		queue.add(request);
	}

	public static void getNearByVenues(double latitude, double longitude,
			String version, Response.Listener<JSONObject> successCallback,
			Response.ErrorListener errorCallback, RequestQueue queue) {
		String ll = getLatLongString(latitude, longitude);
		String requestURl = NEARBY_VENUE_API_CALL + CLIENT_ID_ATTR + CLIENT_ID
				+ AND_SPLIT + CLIENT_SECRET_ATTR + CLIENT_SECRET + AND_SPLIT
				+ VERSION_ATTR + CURRENT_FOURSQUARE_VERSION + AND_SPLIT
				+ LONG_LAT_ATTR + ll + AND_SPLIT + LIMIT_ATTR + 10 + AND_SPLIT
				+ RADIUS_ATTR + 800;
		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
				requestURl, null, successCallback, errorCallback);
		queue.add(request);

	}

	private static String getLatLongString(double latitude, double longitude) {
		String latString = Double.toString(latitude);
		String longString = Double.toString(longitude);
		String ll = latString.substring(0, 4) + ","
				+ longString.substring(0, 4);
		return ll;
	}

	public static List<VenueBean> fetchingVenuesBeans(JSONObject venues) {
		List<VenueBean> venuesList = new ArrayList<VenueBean>();
		try {
			JSONArray venuesArray = venues.getJSONObject(RESPONSE_KEY)
					.getJSONArray(VENUE_KEY);
			for (int index = 0; index < venuesArray.length(); index++) {
				VenueBean venueBean = new VenueBean();
				JSONObject venueObject = venuesArray.getJSONObject(index);
				if (venueObject.has(ID_KEY)) {
					String venueID = venueObject.getString(ID_KEY);
					venueBean.setVenueID(venueID);
				}
				if (venueObject.has(NAME_KEY)) {
					String venueName = venueObject.getString(NAME_KEY);
					venueBean.setVenueName(venueName);
				}
				if (venueObject.getJSONObject(LOCATION).has(LONG_KEY)) {
					double venueLongitude = venueObject.getJSONObject(LOCATION)
							.getDouble(LONG_KEY);
					venueBean.setVenueLongitude(venueLongitude);
				}
				if (venueObject.getJSONObject(LOCATION).has(LAT_KEY)) {
					double venueLatitude = venueObject.getJSONObject(LOCATION)
							.getDouble(LAT_KEY);
					venueBean.setVenueLatitude(venueLatitude);
				}
				if (venueObject.getJSONObject(LOCATION).has(CITY)) {
					String venueCity = venueObject.getJSONObject(LOCATION)
							.getString(CITY);
					venueBean.setVenueCity(venueCity);
				}
				if (venueObject.getJSONArray(CATEGORIES).length() > 0) {
					JSONObject iconObject = venueObject
							.getJSONArray(CATEGORIES).getJSONObject(0)
							.getJSONObject(ICON);
					String icon = iconObject.getString(PREFIX) + IMAGE_SIZE
							+ iconObject.getString(SUFFIX);
					venueBean.setCatImage(icon);
				}
				venuesList.add(venueBean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return venuesList;

	}
}
