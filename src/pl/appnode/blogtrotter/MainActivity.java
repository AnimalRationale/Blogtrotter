package pl.appnode.blogtrotter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import pl.appnode.blogtrotter.DbHelper.FeedSiteNote;
import pl.appnode.blogtrotter.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int CAMERA_REQUEST = 1888;
	private static final int EDIT_NOTE_REQUEST = 1999;
	private static final int EDIT_LOCATION_REQUEST = 1799;
	private static final int EXPORT_NOTE_REQUEST = 1699;

	public static final String EDIT_NOTE_LIST_INDEX = "pl.appnode.blogtrotter.EDIT_NOTE_INDEX";
	public static final String YOUR_PHOTO_START = "pl.appnode.blogtrotter.dodajZdjecie";
	public static final String SHOW_NOTE_FILE = "pl.appnode.blogtrotter.SHOW_NOTE_FILE";
	public static final String SHOW_NOTE_DATE = "pl.appnode.blogtrotter.SHOW_NOTE_DATE";
	public static final String SHOW_NOTE_NOTE = "pl.appnode.blogtrotter.SHOW_NOTE_NOTE";
	public static final String SHOW_NOTE_LOCATION = "pl.appnode.blogtrotter.SHOW_NOTE_LOCATION";
	public static final String SHOW_NOTE_LATITUDE = "pl.appnode.blogtrotter.SHOW_NOTE_LATITUDE";
	public static final String SHOW_NOTE_LATITUDE_REF = "pl.appnode.blogtrotter.SHOW_NOTE_LATITUDE_REF";
	public static final String SHOW_NOTE_LONGITUDE = "pl.appnode.blogtrotter.SHOW_NOTE_LONGITUDE";
	public static final String SHOW_NOTE_LONGITUDE_REF = "pl.appnode.blogtrotter.SHOW_NOTE_LONGITUDE_REF";
	public static final String SHOW_NOTE_EXIF_ORIENT = "pl.appnode.blogtrotter.SHOW_NOTE_EXIF_ORIENT";
	public static final String PHOTO_DIR = "/Blogtrotter/";
	public static final String THUMB_DIR = "/BlogtrotterThumbs/";
	public static final String DB_ID = "pl.appnode.blogtrotter.DB_ID";
	public static final String THUMB_FILE = "pl.appnode.blogtrotter.THUMB_FILE";
	public static final String THUMB_ROTATION = "pl.appnode.blogtrotter.THUMB_ROTATION";

	private static final String myURI = null;

	private Uri mImageCaptureUri1;
	private Uri inImageSendUri;
	private Boolean isAfterOnActivityResult;
	private Boolean handleSendImage;

	public static ArrayList<DataSet> noteList = new ArrayList<DataSet>();

	public static ImageAdapter imageAdapter;
	private ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("LogInfo001", "MA - onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {

			mImageCaptureUri1 = savedInstanceState.getParcelable(myURI);
		}

		Intent intentIn = getIntent();
	    String actionIn = intentIn.getAction();
	    String typeIn = intentIn.getType();
	    
	    handleSendImage = false;
	    
	    if (Intent.ACTION_SEND.equals(actionIn) && typeIn != null) {
	        if (typeIn.startsWith("image/")) {	            
	            inImageSendUri = intentIn.getParcelableExtra(Intent.EXTRA_STREAM);	            
	            if (inImageSendUri != null) {
	            	String storageState = Environment.getExternalStorageState();
	        		if (!Environment.MEDIA_MOUNTED.equals(storageState)) {
	        			Toast.makeText(this, R.string.storage_alert, Toast.LENGTH_LONG)
	        					.show();
	        			return;
	        		}
	        		File photoDir = new File(Environment.getExternalStorageDirectory()
	        				+ PHOTO_DIR);
	        		photoDir.mkdirs();
	        		mImageCaptureUri1 = Uri.fromFile(new File(photoDir, "thmb"
	        				+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
	        		File fileIn = new File(getRealPathFromURI(inImageSendUri));
	        		String fileOut = mImageCaptureUri1.toString().substring(7);
	        		copyFile (fileIn.toString(), fileOut);
  	                handleSendImage = true; // Handle single image being sent
	                Log.d("LogInfo001", "MAIntent - handle: " + handleSendImage + " --- fileIn: " + fileIn + " --- fileOut: " + fileOut);
	              
	            }
	        }
	    }	    
	    isAfterOnActivityResult = false;
	    
		if (noteList.isEmpty()) {
			Log.d("LogInfo001", "isEmpty");
			feedNoteListFromDatabase();
		}

		lv = (ListView) findViewById(R.id.listView1);

		imageAdapter = new ImageAdapter(this, noteList);
		lv.setAdapter(imageAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				DataSet item = (DataSet) parent.getItemAtPosition(position);
				Intent intent = new Intent(getApplicationContext(),
						ShowNoteActivity.class);

				// Set of parameters sent to ShowNoteActivity, it could be done
				// in one Array of Strings
				// but we are still not sure what finally we'll need to send
				intent.putExtra(EDIT_NOTE_LIST_INDEX, String.valueOf(position));				
				intent.putExtra(SHOW_NOTE_FILE, item.getFile());
				intent.putExtra(SHOW_NOTE_DATE, item.getDateTime());
				intent.putExtra(SHOW_NOTE_NOTE, item.getNote());
				intent.putExtra(SHOW_NOTE_LOCATION, item.getLocation());
				intent.putExtra(SHOW_NOTE_LATITUDE, item.getExifGpsLatitude());
				intent.putExtra(SHOW_NOTE_LATITUDE_REF,
						item.getExifGpsLatitudeRef());
				intent.putExtra(SHOW_NOTE_LONGITUDE, item.getExifGpsLongitude());
				intent.putExtra(SHOW_NOTE_LONGITUDE_REF,
						item.getExifGpsLongitudeRef());
				intent.putExtra(SHOW_NOTE_EXIF_ORIENT,
						item.getExifOrientation());
				startActivity(intent);
			}
		});
		registerForContextMenu(lv);
	}

	@Override
	protected void onResume() {
		Log.d("LogInfo001", "MA - onResume");
		super.onResume();

		if (isAfterOnActivityResult) {
			// po nacisnieciu zapisz w app aparatu, wywolywana jest
			// onActivityResult a pozniej onResume
			// dlatego przenioslem intent dla YourPhotoActivity do onResume
			isAfterOnActivityResult = false;
			Intent intent = new Intent(this, YourPhotoActivity.class);
			intent.putExtra(YOUR_PHOTO_START, mImageCaptureUri1.getPath());
			Log.d("LogInfo001", "MA YourPhoto Intent mImageCapturedUri1: " + mImageCaptureUri1.getPath());
			startActivity(intent);
		}
		
		if (handleSendImage) {			
			handleSendImage = false;
			Intent intent = new Intent(this, YourPhotoActivity.class);
			Log.d("LogInfo001", "MAIntent - plik: " + inImageSendUri);
			intent.putExtra(YOUR_PHOTO_START, mImageCaptureUri1.getPath());
			startActivity(intent);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.con_menu_edit_note:
			editNote(info.position);
			return true;
		case R.id.con_menu_add_location:
			addLocation(info.position);
			return true;
		case R.id.con_menu_restore_location:
			restoreLocation(info.position);
			return true;
		case R.id.con_menu_export_note:
			exportNote(info.position);
			return true;
		case R.id.con_menu_dell_all:
			deleteAllNotes();
			return true;
		case R.id.con_menu_del:
			deleteOneNote(info.position);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_add_button) {
			addPhoto();
		} else if (item.getItemId() == R.id.menu_big_map_button) {
			bigMap();
		}
		return super.onOptionsItemSelected(item);
	}

	private void editNote(int listInd) {
		Intent intentEditNote = new Intent(getApplicationContext(),
				EditNoteActivity.class);
		final int listIndex = listInd;
		final String dbId = noteList.get(listIndex).getDbId();
		final String note = noteList.get(listIndex).getNote();
		final String thumb = noteList.get(listIndex).getThumb();
		final String thumbRotation = String.valueOf(noteList.get(listIndex)
				.getRotation());
		final String intentListIndex = String.valueOf(listIndex);
		intentEditNote.putExtra(DB_ID, dbId);
		intentEditNote.putExtra(SHOW_NOTE_NOTE, note);
		intentEditNote.putExtra(THUMB_FILE, thumb);
		intentEditNote.putExtra(THUMB_ROTATION, thumbRotation);
		intentEditNote.putExtra(EDIT_NOTE_LIST_INDEX, intentListIndex);		
		startActivityForResult(intentEditNote, EDIT_NOTE_REQUEST);
	}

	private void addLocation(int listInd) {
		Intent intentLocationMap = new Intent(getApplicationContext(),
				LocationMapActivity.class);
		final int listIndex = listInd;
		final String dbId = noteList.get(listIndex).getDbId();
		final String intentListIndex = String.valueOf(listIndex);
		intentLocationMap.putExtra(DB_ID, dbId);
		intentLocationMap.putExtra(EDIT_NOTE_LIST_INDEX, intentListIndex);
		startActivityForResult(intentLocationMap, EDIT_LOCATION_REQUEST);
	}

	private void restoreLocation(int listInd) {
		String dbId = noteList.get(listInd).getDbId();
		DbHelper mDbHelper = new DbHelper(MainActivity.this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String location = "";
		ContentValues args = new ContentValues();
		args.put(FeedSiteNote.COLUMN_NAME_LOCATION, location);
		Log.d("LogInfo001", "MA - args: " + args.toString());

		boolean resultSQL = db.update(DbHelper.FeedSiteNote.TABLE_NAME, args,
				FeedSiteNote._ID + "='" + dbId + "'", null) > 0;
		if (!resultSQL) {
			Toast.makeText(this, R.string.sql_update_alert, Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(this, R.string.restore_confirm, Toast.LENGTH_LONG)
					.show();
		}
		db.close();
		mDbHelper.close();
		Log.d("LogInfo001", "MA - restore location db.update");

		noteList.get(listInd).setLocation(location);
		imageAdapter.notifyDataSetChanged();
	}
	
	private void exportNote(int listInd) {
		Intent intentExportNote = new Intent(getApplicationContext(),
				ExportNoteActivity.class);
		final int listIndex = listInd;
		final String dbId = noteList.get(listIndex).getDbId();
		final String note = noteList.get(listIndex).getNote();
		final String thumb = noteList.get(listIndex).getThumb();
		final String thumbRotation = String.valueOf(noteList.get(listIndex)
				.getRotation());
		final String intentListIndex = String.valueOf(listIndex);
		intentExportNote.putExtra(DB_ID, dbId);
		intentExportNote.putExtra(SHOW_NOTE_NOTE, note);
		intentExportNote.putExtra(THUMB_FILE, thumb);
		intentExportNote.putExtra(THUMB_ROTATION, thumbRotation);
		intentExportNote.putExtra(EDIT_NOTE_LIST_INDEX, intentListIndex);
		
		intentExportNote.putExtra(SHOW_NOTE_FILE, noteList.get(listIndex).getFile());
		intentExportNote.putExtra(SHOW_NOTE_DATE, noteList.get(listIndex).getDateTime());		
		intentExportNote.putExtra(SHOW_NOTE_LOCATION, noteList.get(listIndex).getLocation());
		intentExportNote.putExtra(SHOW_NOTE_LATITUDE, noteList.get(listIndex).getExifGpsLatitude());
		intentExportNote.putExtra(SHOW_NOTE_LATITUDE_REF,
				noteList.get(listIndex).getExifGpsLatitudeRef());
		intentExportNote.putExtra(SHOW_NOTE_LONGITUDE, noteList.get(listIndex).getExifGpsLongitude());
		intentExportNote.putExtra(SHOW_NOTE_LONGITUDE_REF,
				noteList.get(listIndex).getExifGpsLongitudeRef());
		intentExportNote.putExtra(SHOW_NOTE_EXIF_ORIENT,
				noteList.get(listIndex).getExifOrientation());
		
		startActivityForResult(intentExportNote, EXPORT_NOTE_REQUEST);
	}

	private void deleteAllNotes() {
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
		adb.setTitle(R.string.delete_msg);
		adb.setMessage(R.string.delete_all_msg);
		adb.setNegativeButton(R.string.neg_button, null);
		adb.setPositiveButton(R.string.pos_button,
				new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						DbHelper mDbHelper = new DbHelper(MainActivity.this);
						SQLiteDatabase db = mDbHelper.getReadableDatabase();
						db.delete(DbHelper.FeedSiteNote.TABLE_NAME, null, null);

						noteList.clear();
						imageAdapter.notifyDataSetChanged();
					}
				});
		adb.show();
	}

	private void deleteOneNote(int listInd) {
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
		adb.setTitle(R.string.delete_msg);
		adb.setMessage(R.string.delete_record_msg);
		final int listIndex = listInd;
		final String dbId = noteList.get(listIndex).getDbId();
		adb.setNegativeButton(R.string.neg_button, null);
		adb.setPositiveButton(R.string.pos_button,
				new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						DbHelper mDbHelper = new DbHelper(MainActivity.this);
						SQLiteDatabase db = mDbHelper.getReadableDatabase();

						String selection = DbHelper.FeedSiteNote._ID
								+ " LIKE ?";
						String[] selectionArgs = { dbId };

						db.delete(DbHelper.FeedSiteNote.TABLE_NAME, selection,
								selectionArgs);
						db.close();
						mDbHelper.close();

						noteList.remove(listIndex);
						imageAdapter.notifyDataSetChanged();
					}
				});
		adb.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void addPhoto() {
		String storageState = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(storageState)) {
			Toast.makeText(this, R.string.storage_alert, Toast.LENGTH_LONG)
					.show();
			return;
		}

		File photoDir = new File(Environment.getExternalStorageDirectory()
				+ PHOTO_DIR);
		photoDir.mkdirs();

		mImageCaptureUri1 = Uri.fromFile(new File(photoDir, "thmb"
				+ String.valueOf(System.currentTimeMillis()) + ".jpg"));

		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				mImageCaptureUri1);
		cameraIntent.putExtra("return-data", true);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

	public void bigMap() {
		Intent intent = new Intent(getApplicationContext(),
				BigMapActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putParcelable(myURI, mImageCaptureUri1);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("LogInfo001", "MA - onActivityResult");

		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			isAfterOnActivityResult = true;
		}
		if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK && data != null) {

			int intListIndex = Integer.parseInt(data.getExtras().getString(
					"editedNoteListIndex"));
			noteList.get(intListIndex).setNote(
					data.getExtras().getString("editedNote"));
			imageAdapter.notifyDataSetChanged();
		}
		if (requestCode == EDIT_LOCATION_REQUEST && resultCode == RESULT_OK && data != null) {
			int intListIndex = Integer.parseInt(data.getExtras().getString(
					"editedNoteListIndex"));
			noteList.get(intListIndex).setLocation(
					data.getExtras().getString("editedLocation"));
			imageAdapter.notifyDataSetChanged();
		}

	}
	
	public String getRealPathFromURI(Uri uri) {
	    Cursor cursor = getContentResolver().query(uri, null, null, null, null); 
	    cursor.moveToFirst(); 
	    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	    return cursor.getString(idx); 
	}
	
	private void copyFile(String srFile, String dtFile){
		try{
		  File f1 = new File(srFile);
		  File f2 = new File(dtFile);
		  InputStream in = new FileInputStream(f1);

		  //For Append the file.
		  //OutputStream out = new FileOutputStream(f2,true);

		  //For Overwrite the file.
		  OutputStream out = new FileOutputStream(f2);

		  byte[] buf = new byte[1024];
		  int len;
		  while ((len = in.read(buf)) > 0){
		    out.write(buf, 0, len);
		  }
		  in.close();
		  out.close();
		  
		}
		catch(FileNotFoundException ex){
			Toast.makeText(this, R.string.copy_alert,
					Toast.LENGTH_LONG).show();		  
		}
		catch(IOException e){
			Toast.makeText(this, R.string.copy_alert,
					Toast.LENGTH_LONG).show();      
		}
		}

	private void feedNoteListFromDatabase() {
		DbHelper mDbHelper = new DbHelper(this);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = { DbHelper.FeedSiteNote._ID,
				DbHelper.FeedSiteNote.COLUMN_NAME_ID,
				DbHelper.FeedSiteNote.COLUMN_NAME_DATE_TIME,
				DbHelper.FeedSiteNote.COLUMN_NAME_FILE,
				DbHelper.FeedSiteNote.COLUMN_NAME_THUMB,
				DbHelper.FeedSiteNote.COLUMN_NAME_NOTE,
				DbHelper.FeedSiteNote.COLUMN_NAME_LOCATION,
				DbHelper.FeedSiteNote.COLUMN_NAME_ROTATION,
				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_ORIENTATION,
				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDE,
				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDEREF,
				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDE,
				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDEREF,
				DbHelper.FeedSiteNote.COLUMN_NAME_TAGS,
				DbHelper.FeedSiteNote.COLUMN_NAME_PUBLISHED };

		// How you want the results sorted in the resulting Cursor
		String sortOrder = DbHelper.FeedSiteNote._ID + " DESC";

		Cursor c = db.query(DbHelper.FeedSiteNote.TABLE_NAME, // The table
				projection, // The columns to return
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);

		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				noteList.add(new DataSet(
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote._ID)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_ID)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_DATE_TIME)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_FILE)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_THUMB)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_NOTE)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_LOCATION)),
						c.getDouble(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_ROTATION)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_ORIENTATION)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDE)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDEREF)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDE)),
						c.getString(c
								.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDEREF)),
						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_TAGS)),
						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_PUBLISHED))));
				c.moveToNext();
			}
		}
		c.close();
		mDbHelper.close();
	}
}
