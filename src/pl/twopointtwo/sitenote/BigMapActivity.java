package pl.twopointtwo.sitenote;

import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class BigMapActivity extends Activity {

	private LatLng notePoint;
	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_big_map);
		// Show the Up button in the action bar.
		setupActionBar();

		final LatLng KRAKOW = new LatLng(50.06601, 19.945064);
		
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(KRAKOW) // Sets the center of the map to Krakow
				.zoom(12) // Sets the zoom
				.bearing(0) // Sets the orientation of the camera
				.tilt(0) // Sets the tilt of the camera
				.build(); // Creates a CameraPosition from the builder
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		if (MainActivity.noteList.isEmpty()) {
			Log.d("LogInfoBigMap01", "No items on list");
		} else {
			for (int i = 0, n = MainActivity.noteList.size(); i < n; i++) {
				DataSet item = MainActivity.noteList.get(i);
				notePoint = null;
				if (item.getLocation().length() > 5) {
					String location = item.getLocation();
					Log.d("LogInfo001",
							"BMA - location.length: " + location.length());
					String[] latlngArray = location.split(",");
					Log.d("LogInfo001", "BMA - location: " + item.getLocation());
					Log.d("LogInfo001", "BMA - latlngArray[0]: "
							+ latlngArray[0]);
					Log.d("LogInfo001", "BMA - latlngArray[1]: "
							+ latlngArray[1]);
					Float nLa = Float.parseFloat(latlngArray[0]);
					Float nLo = Float.parseFloat(latlngArray[1]);
					notePoint = new LatLng(nLa, nLo);

				} else if (item.getExifGpsLatitude() != null) {
					Random rand = new Random();
					;
					Float nLa = dmsToDeg(item.getExifGpsLatitude())
							+ (0.0002f * (rand.nextInt(3) + 2));
					Float nLo = dmsToDeg(item.getExifGpsLongitude())
							+ (0.0002f * (rand.nextInt(3) + 2));
					notePoint = new LatLng(nLa, nLo);
				}

				if (notePoint != null) {
					Bitmap icon = BitmapFactory.decodeFile(item.getThumb());

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

					mMap.addMarker(new MarkerOptions().position(notePoint)
							.title(item.getDateTime()).snippet(item.getNote())
							.icon(descriptor));

				}
			}
		}
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
		getMenuInflater().inflate(R.menu.activity_big_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_change_map_type:
			changeMapType();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void changeMapType() {
		if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		} else
			if (mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
}