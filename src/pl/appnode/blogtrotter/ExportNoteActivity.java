package pl.appnode.blogtrotter;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ExportNoteActivity extends Activity {
	
	private TextView editNoteTitle;
	private String note_id;
	private String note;
	private String title;
	private String location;
	private String date;
	private String time;
	private String thumb;
	private String photo;
	private String encodedImage;
	private Double rotation;
	private String blogAuthor;
	private String blogAuthorID;
	private String blogAddress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		blogAuthor = sharedPrefs.getString("blog_author", "");
		blogAuthorID = sharedPrefs.getString("blog_author_id", "");
		
		setContentView(R.layout.activity_export_note);
		// Show the Up button in the action bar.
		setupActionBar();
		Log.d("LogInfo001", "ExNA - onCreate after ActionBar");
		// sendNote(getIntent());
		Intent intent = getIntent();
		String dbId = intent.getStringExtra(MainActivity.DB_ID);
		note = intent.getStringExtra(MainActivity.SHOW_NOTE_NOTE);
		thumb = intent.getStringExtra(MainActivity.THUMB_FILE);
		String thumbRotation = intent
				.getStringExtra(MainActivity.THUMB_ROTATION);
		String listIndex = intent
				.getStringExtra(MainActivity.EDIT_NOTE_LIST_INDEX);
		
		String date_time = intent.getStringExtra(MainActivity.SHOW_NOTE_DATE);
		String[] date_time_split = date_time.split(" ");
		date = date_time_split[0];
		time = date_time_split[1];
		
		location = intent.getStringExtra(MainActivity.SHOW_NOTE_LOCATION);
		
		photo = intent.getStringExtra(MainActivity.SHOW_NOTE_FILE);
		note_id = photo.substring(photo.length() - 17);
		note_id = note_id.replace(".jpg", "");
		
		rotation = Double.parseDouble(thumbRotation);

		TextView exportNoteDateView = (TextView) findViewById(R.id.exportNoteDate);
		exportNoteDateView.setText(date + "   " + time);		
		
		ImgLoader imgLoader = new ImgLoader(this);
		ImageView imageView = (ImageView) findViewById(R.id.imageView4);
		imageView.setRotation(rotation.floatValue());
		imgLoader.loadBitmap(thumb, imageView);

		editNoteTitle = (EditText)findViewById(R.id.enterTitle);		
		// editNoteTitle.setText("", TextView.BufferType.EDITABLE);
		editNoteTitle.requestFocus();
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.restartInput(editNoteTitle);
	    
		Button button = (Button) findViewById(R.id.button_send_note);		
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ExportNoteActivity.this, R.string.note_to_blog,
						Toast.LENGTH_SHORT).show();
				new Thread(new Runnable() {
					// Thread to stop network calls on the UI thread
					public void run() {
						
						BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
						int scaleFactor = 4;
						decodeBitmapOptions.inSampleSize = scaleFactor;
						decodeBitmapOptions.inJustDecodeBounds = false;
						decodeBitmapOptions.inPurgeable = true;
						Bitmap bm = BitmapFactory.decodeFile(photo, decodeBitmapOptions);						
				        ByteArrayOutputStream baos = new ByteArrayOutputStream();
				        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				        byte[] byteArray = baos.toByteArray();
				        int flag = Base64.NO_WRAP;
				        encodedImage = Base64.encodeToString(byteArray, flag);
				        // Log.d("LogInfo001", "ExNA - BASE64: " + encodedImage);
						title = editNoteTitle.getText().toString();
						if (title == null) {title = "--";}
						// Request HTML
						try {
							JSONObject json = new JSONObject();
							json.put("check_uploader", "yes");
							json.put("pass", "Blogtrotter.v01.236");
							json.put("note_id", note_id);
							json.put("author", blogAuthor);
							json.put("author_id", blogAuthorID);
							json.put("date", date);
							json.put("time", time);
							json.put("title", title);
							json.put("location", location);
							json.put("note", note);							
							json.put("photo", encodedImage);
							if (rotation != 0) {
								json.put("rotation", "yes");
							} else {
								json.put("rotation", "no");
							}
							postData(json);							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();
				finish();
			}			
		});     
	}

	@Override
	public void finish() {
		// Prepare data intent
		Intent data = new Intent();
		setResult(RESULT_OK, data);
		super.finish();		
	}
	
	public void postData(JSONObject json) throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		String URL_BASE = "http://infodump.pl/note";

		try {
			URL url = new URL(URL_BASE + "/noteuploader01.php");
			HttpPost httppost = new HttpPost(url.toURI());

			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			nvp.add(new BasicNameValuePair("json", json.toString()));
			// httppost.setHeader("Content-type", "application/json");
			httppost.setEntity(new UrlEncodedFormEntity(nvp, "UTF-8"));
			// Log.d("LogInfo001", "ExNA - NVP: " + nvp);

			HttpResponse response = httpclient.execute(httppost);
			StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();
	        Log.d("LogInfo001", "StatusCode " + statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				Log.d("LogInfo001",
						"ExNA - Response: " + EntityUtils.toString(entity));
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("LogInfo001", "ExNA - Exception: " + e.toString());
		}
	}	

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_export_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}