package pl.twopointtwo.sitenote;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ExportNoteActivity extends Activity {
	
	private String note;
	private String location;
	private String date;
	private String thumb;
	private String photo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		
		date = intent.getStringExtra(MainActivity.SHOW_NOTE_DATE);
		location = intent.getStringExtra(MainActivity.SHOW_NOTE_LOCATION);
		photo = intent.getStringExtra(MainActivity.SHOW_NOTE_FILE);
		
		Double rotation = Double.parseDouble(thumbRotation);

		ImgLoader imgLoader = new ImgLoader(this);
		ImageView imageView = (ImageView) findViewById(R.id.imageView4);
		imageView.setRotation(rotation.floatValue());
		imgLoader.loadBitmap(thumb, imageView);

		Button button = (Button) findViewById(R.id.button_send_note);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ExportNoteActivity.this, "Trying to postData",
						Toast.LENGTH_SHORT).show();
				new Thread(new Runnable() {
					// Thread to stop network calls on the UI thread
					public void run() {
						// Request the HTML
						try {
							JSONObject json = new JSONObject();
							json.put("key", "Blogtrotter.v01");
							json.put("date", date);
							json.put("title", "No title");
							json.put("location", location);
							json.put("note", note);
							json.put("thumb", thumb);
							json.put("photo", photo);
							
							postData(json);
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}

	public void postData(JSONObject json) throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		String URL_BASE = "http://infodump.pl/note/upload01.php";

		try {
			URL url = new URL(URL_BASE + "/noteuploader01.php");
			HttpPost httppost = new HttpPost(url.toURI());

			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			nvp.add(new BasicNameValuePair("json", json.toString()));
			httppost.setHeader("Content-type", "application/json");
			httppost.setEntity(new UrlEncodedFormEntity(nvp, "UTF-8"));
			Log.d("LogInfo001", "ExNA - NVP: " + nvp);

			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				Log.d("LogInfo001",
						"ExNA - Response: " + EntityUtils.toString(entity));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendNote() {

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