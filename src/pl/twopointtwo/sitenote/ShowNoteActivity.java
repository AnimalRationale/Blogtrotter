package pl.twopointtwo.sitenote;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TabHost;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class ShowNoteActivity extends TabActivity {
	// TabActivity is deprecated, we'll replace it with fragments

	private String nLa;
	private String nLo;
	TextView noteTextView = null;
	String noteListIndex;
	private static final int SN_EDIT_NOTE_REQUEST = 1699;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("LogInfo001", "SNA - onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_note);
		// Show the Up button in the action bar.
		setupActionBar();

		String style = "";		
		WebView noteWebView = null;
		WebView noteMapView = null;		
		TextView noteDateView = null;

		Intent intent = getIntent();
		noteListIndex = intent.getStringExtra(MainActivity.EDIT_NOTE_LIST_INDEX);
		Log.d("LogInfo001", "SNA - noteListIndex == " + noteListIndex);
		String noteImageFile = intent.getStringExtra(MainActivity.SHOW_NOTE_FILE);
		String noteDate = intent.getStringExtra(MainActivity.SHOW_NOTE_DATE);
		String noteText = intent.getStringExtra(MainActivity.SHOW_NOTE_NOTE);
		String noteLocation = intent.getStringExtra(MainActivity.SHOW_NOTE_LOCATION);
		String noteLatitude = intent.getStringExtra(MainActivity.SHOW_NOTE_LATITUDE);
		String noteLatitudeRef = intent.getStringExtra(MainActivity.SHOW_NOTE_LATITUDE_REF);
		String noteLongitude = intent.getStringExtra(MainActivity.SHOW_NOTE_LONGITUDE);
		String noteLongitudeRef = intent.getStringExtra(MainActivity.SHOW_NOTE_LONGITUDE_REF);
		String noteExifOrientation = intent.getStringExtra(MainActivity.SHOW_NOTE_EXIF_ORIENT);
		
		// Tabs
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		spec = tabHost.newTabSpec("noteImage").setIndicator("Photo")
				.setContent(R.id.tab1);
		tabHost.addTab(spec);
		spec = tabHost.newTabSpec("noteLoc").setIndicator("Map")
				.setContent(R.id.tab2);
		tabHost.addTab(spec);
		spec = tabHost.newTabSpec("noteNote").setIndicator("Note")
				.setContent(R.id.tab3);
		tabHost.addTab(spec);
		tabHost.setCurrentTab(0);

		// image WebView
		noteWebView = (WebView) findViewById(R.id.noteImage);
		noteWebView.getSettings().setBuiltInZoomControls(true);		
		noteWebView.setBackgroundColor(0);
		noteWebView.getSettings().setUseWideViewPort(true);
		noteWebView.getSettings().setLoadWithOverviewMode(true);						
		File imageFile = new File(noteImageFile);		
		if (imageFile.exists()) {		
			if (noteExifOrientation.equals("6")) {
						style = "\" style=\"-webkit-transform: rotate(90deg); position: absolute; left:-50%;\" >";
						Log.d("LogInfo001", "SNA - INNER " + noteExifOrientation);
			} else {
				style = "\">";
				}						
			final String fileName = "file://" + noteImageFile;
	        final String mimeType = "text/html";
	        final String encoding = "utf-8";
	        final String html = "<html><body bgcolor='#000000'> <img src=\"" + fileName + style+"</body></html>";
	        noteWebView.loadDataWithBaseURL("", html, mimeType, encoding, "");	       
		}

		// map WebView
		nLa = null;
		nLo = null;
	    noteMapView = (WebView) findViewById(R.id.noteMap);
		noteMapView.getSettings().setJavaScriptEnabled(true);
		if (noteLatitude == null && noteLocation.length() < 5) {
			Log.d("LogInfo001", "SNA - NOGeoTag");
			noteMapView.loadUrl("file:///android_asset/no-geotag.html");
		} else 	if (noteLocation.length() > 5) {						        	
	        		String[] latlngArray = noteLocation.split(",");
	            	Log.d("LogInfo001","SNA - location: " + noteLocation);
	            	Log.d("LogInfo001","SNA - latlngArray[0]: " + latlngArray[0]);
	            	Log.d("LogInfo001","SNA - latlngArray[1]: " + latlngArray[1]);
	            	nLa = latlngArray[0];
	                nLo = latlngArray[1];
			} else if (noteLatitude != null) {
				Log.d("LogInfo001", "SNA - GeoTag");
				nLa = noteLatitudeRef + dmsToDeg(noteLatitude);
				nLo = noteLongitudeRef + dmsToDeg(noteLongitude);
				Log.d("LogInfo001", "SNA - GeoTag converted");	
			}
			if (nLa != null) {
			String mapHTML = "<html><head><meta http-equiv='Content-Type'content='text/html; charset=utf-8'>"
					+"<title>Location</title></head><body bgcolor='#000000' color='#FFFFFF'>"
					+ "<iframe width='100%' height='100%' frameborder='0' scrolling='no' marginheight='0' marginwidth='0'" 
					+ " src='http://maps.google.com/maps?q="+nLa+","+nLo+"&amp;ie=UTF8&amp;z=14&amp;output=embed&amp;'>"
					+"</iframe></body></html>";			
			noteMapView.loadData(mapHTML, "text/html", null);
			noteMapView.setBackgroundColor(Color.parseColor("#000000"));			
		}

		// note TextView
		noteTextView = (TextView) findViewById(R.id.noteText);
		noteTextView.setText(noteText);
		noteDateView = (TextView) findViewById(R.id.noteDate);
		noteDateView.setText(noteDate);

	} // onCreate

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		Log.d("LogInfo001", "SNA - onResume");
		super.onResume();
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
		getMenuInflater().inflate(R.menu.activity_show_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:			
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_edit_note:
			editNote(Integer.parseInt(noteListIndex));
			return true;		
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void editNote(int listInd) {
		Intent intentEditNote = new Intent(getApplicationContext(),
				EditNoteActivity.class);
		final int listIndex = listInd;
		final String dbId = MainActivity.noteList.get(listIndex).getDbId();
		final String note = MainActivity.noteList.get(listIndex).getNote();
		final String thumb = MainActivity.noteList.get(listIndex).getThumb();
		final String thumbRotation = String.valueOf(MainActivity.noteList.get(listIndex)
				.getRotation());
		final String intentListIndex = String.valueOf(listIndex);
		intentEditNote.putExtra(MainActivity.DB_ID, dbId);
		intentEditNote.putExtra(MainActivity.SHOW_NOTE_NOTE, note);
		intentEditNote.putExtra(MainActivity.THUMB_FILE, thumb);
		intentEditNote.putExtra(MainActivity.THUMB_ROTATION, thumbRotation);
		intentEditNote.putExtra(MainActivity.EDIT_NOTE_LIST_INDEX, intentListIndex);		
		startActivityForResult(intentEditNote, SN_EDIT_NOTE_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("LogInfo001", "SNA - EditNote onActivityResult");
		
		if (requestCode == SN_EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {

			int intListIndex = Integer.parseInt(data.getExtras().getString(
					"editedNoteListIndex"));
			noteTextView.setText(data.getExtras().getString("editedNote"));
			MainActivity.noteList.get(intListIndex).setNote(
					data.getExtras().getString("editedNote"));			
			MainActivity.imageAdapter.notifyDataSetChanged();
		}
//		if (requestCode == SN_EDIT_LOCATION_REQUEST && resultCode == RESULT_OK) {
//			int intListIndex = Integer.parseInt(data.getExtras().getString(
//					"editedNoteListIndex"));
//			noteList.get(intListIndex).setLocation(
//					data.getExtras().getString("editedLocation"));
//			imageAdapter.notifyDataSetChanged();
//		}

	}
	
	private String dmsToDeg(String gpsExif) {
		
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
		return result.toString();
	} // dmsToDeg

} // ShowNoteActivity