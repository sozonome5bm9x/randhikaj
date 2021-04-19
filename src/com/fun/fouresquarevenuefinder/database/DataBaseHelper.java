package com.fun.fouresquarevenuefinder.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.fun.fouresquarevenuefinder.beans.VenueBean;

public class DataBaseHelper {

	/******************** if debug is set true then it will show all Logcat message ************/
	public static final boolean DEBUG = true;

	/******************** Logcat TAG ************/
	public static final String LOG_TAG = "DataBaseHelper";
	/******************** Venues Table Fields ******/
	public static final String KEY_VENUE_LONG = "long";
	public static final String KEY_VENUE_LAT = "lat";
	public static final String KEY_VENUE_NAME = "name";
	public static final String KEY_VENUE_ID = "id";
	public static final String KEY_VENUE_CITY = "city";
	public static final String KEY_VENUE_ADDRESS = "address";
	public static final String KEY_ICON = "icon";
	/******************** Database Name ************/
	public static final String DATABASE_NAME = "FOURESQUAREVENUES";
	/******************** Database Version (Increase one if want to also upgrade your database) ************/
	public static final int DATABASE_VERSION = 1;
	/** Table names */
	public static final String VENUE_TABLE = "venues";

	/**************** All tables *******/
	public static final String[] ALL_TABLES = { VENUE_TABLE };

	/***************** Create tables **************/
	private static final String VENUES_CREATE = "create table if not exists venues("
			+ KEY_VENUE_ID
			+ " TEXT PRIMARY KEY ,"
			+ KEY_VENUE_NAME
			+ " TEXT NOT NULL,"
			+ KEY_VENUE_LONG
			+ " DOUBLE NOT NULL,"
			+ KEY_VENUE_LAT
			+ " DOUBLE NOT NULL,"
			+ KEY_VENUE_CITY
			+ " TEXT NOT NULL," + KEY_ICON + " TEXT NOT NULL );";
	/******************** Used to open database in syncronized way ************/
	private static DataBaseHandler DBHelper = null;
	static Context contextt;

	protected DataBaseHelper() {
	}

	/******************* Initialize database *************/
	public static void init(Context context) {
		contextt = context;
		if (DBHelper == null) {
			if (DEBUG)
				// Log.v("DBAdapter", context.toString());
				DBHelper = new DataBaseHandler(context);
		}
	}

	/********************** Main Database creation INNER class ********************/
	private static class DataBaseHandler extends SQLiteOpenHelper {
		public DataBaseHandler(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// if (DEBUG)
			// Log.v(LOG_TAG, "new create");
			try {
				db.execSQL(VENUES_CREATE);
			} catch (Exception exception) {
				// if (DEBUG)
				// Log.v(LOG_TAG, "Exception onCreate() exception");
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// if (DEBUG)
			// Log.w(LOG_TAG, "Upgrading database from version" + oldVersion
			// + "to" + newVersion + "...");

			for (String table : ALL_TABLES) {

				db.execSQL("DROP TABLE IF EXISTS " + table);
			}
			onCreate(db);
		}

	} // Inner class closed

	/********************** Open database for insert,update,delete in syncronized manner ********************/
	private static synchronized SQLiteDatabase open() throws SQLException {

		return DBHelper.getWritableDatabase();

	}

	/************************ General functions **************************/

	/*********************** Escape string for single quotes (Insert,Update) ************/
	private static String sqlEscapeString(String aString) {
		String aReturn = "";

		if (null != aString) {
			// aReturn = aString.replace("'", "''");
			aReturn = DatabaseUtils.sqlEscapeString(aString);
			// Remove the enclosing single quotes ...
			aReturn = aReturn.substring(1, aReturn.length() - 1);
		}

		return aReturn;
	}

	public static Boolean addNewVenueBean(VenueBean venueBean) {
		final SQLiteDatabase db = open();
		Boolean flag = true;
		String venueName = sqlEscapeString(venueBean.getVenueName());
		String id = sqlEscapeString(venueBean.getVenueID());
		String city = sqlEscapeString(venueBean.getVenueCity());
		double longitude = venueBean.getVenueLongitude();
		double latitude = venueBean.getVenueLatitude();
		String icon = sqlEscapeString(venueBean.getCatImage());
		ContentValues cVal = new ContentValues();
		cVal.put(KEY_ICON, icon);
		cVal.put(KEY_VENUE_ID, id);
		cVal.put(KEY_VENUE_LAT, latitude);
		cVal.put(KEY_VENUE_LONG, longitude);
		cVal.put(KEY_VENUE_NAME, venueName);
		cVal.put(KEY_VENUE_CITY, city);
		try {
			db.insertOrThrow(VENUE_TABLE, null, cVal);
		} catch (SQLiteConstraintException e) {
			Toast.makeText(contextt, e.toString(), Toast.LENGTH_LONG).show();

			flag = false;
		}
		db.close();
		return flag;
	}

	public static ArrayList<VenueBean> getAllVenues() {
		final SQLiteDatabase db = open();
		ArrayList<VenueBean> veneusList = new ArrayList<VenueBean>();
		String venuesQuery = "SELECT * FROM " + VENUE_TABLE + " ;";
		Cursor cursor = db.rawQuery(venuesQuery, null);
		if (cursor.moveToFirst()) {
			do {
				String id = cursor.getString(0);
				String venueName = cursor.getString(1);
				double longitude = cursor.getDouble(2);
				double latitude = cursor.getDouble(3);
				String city = cursor.getString(4);
				String icon = cursor.getString(5);
				VenueBean venueBean = new VenueBean();
				venueBean.setVenueID(id);
				venueBean.setCatImage(icon);
				venueBean.setVenueName(venueName);
				venueBean.setVenueCity(city);
				venueBean.setVenueLatitude(latitude);
				venueBean.setVenueLongitude(longitude);
				veneusList.add(venueBean);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return veneusList;

	}
/**
 * Delete all venues
 */
	public static void deleteAllVenues() {
		final SQLiteDatabase db = open();
		String allVenues = "DELETE  FROM " + VENUE_TABLE + " WHERE "
				+ KEY_VENUE_ID + " NOT NULL ;";
		Cursor deletedItemsCurstor = db.rawQuery(allVenues, null);
		if (deletedItemsCurstor.moveToFirst()) {
			do {
				String id = deletedItemsCurstor.getString(0);
				Log.v("DataBaseHelper", "Deleted Item id " + id);
			} while (deletedItemsCurstor.moveToNext());
		}
		deletedItemsCurstor.close();
		db.close();
	}
}
