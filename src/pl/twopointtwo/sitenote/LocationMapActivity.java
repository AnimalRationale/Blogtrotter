package pl.twopointtwo.sitenote;

import pl.twopointtwo.sitenote.DbHelper.FeedSiteNote;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationMapActivity extends Activity implements OnMapClickListener {

	private GoogleMap mMap;
	private String location;
	private LatLng original;
	private LatLng finalLocation;
	private long lDbId;
	private int listIndex;
	private String dbId;
	private boolean resultSQL;
	private DataSet item;
	private Bitmap icon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_big_map);
		// Show the Up button in the action bar.
		setupActionBar();

		final LatLng KRAKOW = new LatLng(50.06601, 19.945064);

		Intent intent = getIntent();
		dbId = intent.getStringExtra(MainActivity.DB_ID);
		lDbId = Long.parseLong(dbId);

		listIndex = Integer.parseInt(intent
				.getStringExtra(MainActivity.EDIT_NOTE_LIST_INDEX));

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		/*
		 * CameraPosition cameraPosition = new CameraPosition.Builder()
		 * .target(KRAKOW) // Sets the center of the map to Krakow .zoom(12) //
		 * Sets the zoom .bearing(0) // Sets the orientation of the camera
		 * .tilt(0) // Sets the tilt of the camera .build(); // Creates a
		 * CameraPosition from the builder
		 * mMap.animateCamera(CameraUpdateFactory
		 * .newCameraPosition(cameraPosition));
		 * mMap.setOnMapClickListener(this);
		 */
		original = null;
		item = MainActivity.noteList.get(listIndex);
		if (item.getLocation().length() > 5) {
			String location = item.getLocation();
			Log.d("LogInfo001", "LMA - location.length: " + location.length());
			String[] latlngArray = location.split(",");
			Log.d("LogInfo001", "LMA - location: " + item.getLocation());
			Log.d("LogInfo001", "LMA - latlngArray[0]: " + latlngArray[0]);
			Log.d("LogInfo001", "LMA - latlngArray[1]: " + latlngArray[1]);
			Float nLa = Float.parseFloat(latlngArray[0]);
			Float nLo = Float.parseFloat(latlngArray[1]);
			original = new LatLng(nLa, nLo);
			makeMarker(original);
		} else if (item.getExifGpsLatitude() != null) {
			Float nLa = dmsToDeg(item.getExifGpsLatitude());
			Float nLo = dmsToDeg(item.getExifGpsLongitude());
			original = new LatLng(nLa, nLo);
			makeMarker(original);
		}
		if (original == null) {
			original = KRAKOW;
		}
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(original) // Sets the center of the map to Krakow
				.zoom(13) // Sets the zoom
				.bearing(0) // Sets the orientation of the camera
				.tilt(0) // Sets the tilt of the camera
				.build(); // Creates a CameraPosition from the builder
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		mMap.setOnMapClickListener(this);
	}

	@Override
	public void onMapClick(LatLng point) {
		mMap.clear();
		makeMarker(point);
		finalLocation = point;
	}    
	
	public void changeMapType() {
		if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		} else
			if (mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			}
	}
	
	public void saveEditedLocation() {
		if (finalLocation != null) {
			DbHelper mDbHelper = new DbHelper(LocationMapActivity.this);
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			location = "" + finalLocation.latitude + ","
					+ finalLocation.longitude;
			Log.d("LogInfo001",
					"LMA - finalLocation: " + finalLocation.toString());
			ContentValues args = new ContentValues();
			args.put(FeedSiteNote.COLUMN_NAME_LOCATION, location);
			Log.d("LogInfo001", "LMA - args: " + args.toString());

			resultSQL = db.update(DbHelper.FeedSiteNote.TABLE_NAME, args,
					FeedSiteNote._ID + "=" + lDbId, null) > 0;
			if (!resultSQL) {
				Toast.makeText(this, R.string.sql_update_alert,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, R.string.location_update_confirm,
						Toast.LENGTH_LONG).show();
			}

			Log.d("LogInfo001", "LMA - TableName: "
					+ DbHelper.FeedSiteNote.TABLE_NAME);
			Log.d("LogInfo001", "LMA - lDbId: " + lDbId);

			db.close();
			mDbHelper.close();
			Log.d("LogInfo001", "LMA - db.update");
		}
		finish();
	}

	@Override
	public void finish() {
		// Prepare data intent
		Intent data = new Intent();
		data.putExtra("editedNoteListIndex", String.valueOf(listIndex));
		data.putExtra("editedLocation", location);
		if (resultSQL) { // Activity finished ok, return the data
			setResult(RESULT_OK, data);
			super.finish();
		}
		setResult(RESULT_CANCELED, data); // Activity finished NOT ok
		super.finish();
	}

	private void makeMarker(LatLng point) {

		item = MainActivity.noteList.get(listIndex);
		icon = BitmapFactory.decodeFile(item.getThumb());

		Matrix matrix = new Matrix(); // Bitmap rotation
		float rotation = (float) item.getRotation();
		if (rotation != 0f) {
			matrix.preRotate(rotation);
			icon = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(),
					icon.getHeight(), matrix, true);
		}
		icon = (addColorBorder(adjustImage(icon), 6, "WHITE")); // Bitmap
																// borders
		BitmapDescriptor descriptor = BitmapDescriptorFactory
				.fromBitmap(addColorBorder(icon, 1, "GRAY"));

		mMap.addMarker(new MarkerOptions().position(point)
				.title(item.getDateTime()).snippet(item.getNote())
				.icon(descriptor));
	}

	private Bitmap addColorBorder(Bitmap bmp, int borderSize, String color) {
		Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize
				* 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
		Canvas canvas = new Canvas(bmpWithBorder);
		canvas.drawColor(Color.parseColor(color));
		canvas.drawBitmap(bmp, borderSize, borderSize, null);
		return bmpWithBorder;
	}

	protected Bitmap adjustImage(Bitmap image) {
		int mDpi = getResources().getDisplayMetrics().densityDpi;
		int dpi = image.getDensity();
		if (dpi == mDpi) {
			int width = (image.getWidth() / 3);
			int height = (image.getHeight() / 3);
			Bitmap adjustedImage = Bitmap.createScaledBitmap(image, width,
					height, true);
			return adjustedImage;
		} else {
			int width = (image.getWidth() * mDpi + dpi / 2) / dpi;
			int height = (image.getHeight() * mDpi + dpi / 2) / dpi;
			Bitmap adjustedImage = Bitmap.createScaledBitmap(image, width,
					height, true);
			adjustedImage.setDensity(mDpi);
			return adjustedImage;
		}
	}

	private Float dmsToDeg(String gpsExif) {

		Float result = null;
		String[] dms = gpsExif.split(",", 3);

		String[] stringD = dms[0].split("/", 2);
		Double d0 = new Double(stringD[0]);
		Double d1 = new Double(stringD[1]);
		Double floatD = d0 / d1;

		String[] stringM = dms[1].split("/", 2);
		Double m0 = new Double(stringM[0]);
		Double m1 = new Double(stringM[1]);
		Double floatM = m0 / m1;

		String[] stringS = dms[2].split("/", 2);
		Double s0 = new Double(stringS[0]);
		Double s1 = new Double(stringS[1]);
		Double floatS = s0 / s1;

		result = new Float(floatD + (floatM / 60) + (floatS / 3600));
		return result;
	} // dmsToDeg

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_location_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_save_edited_location:
			saveEditedLocation();
			return true;
		case R.id.menu_change_map_type:
			changeMapType();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
