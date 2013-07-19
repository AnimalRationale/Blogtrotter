package pl.appnode.blogtrotter;

import pl.appnode.blogtrotter.DbHelper.FeedSiteNote;
import pl.appnode.blogtrotter.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditNoteActivity extends Activity{
	
	private TextView editNote;
	private long lDbId;
	private String listIndex;
	private String dbId;
	private boolean resultSQL;
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        // Show the Up button in the action bar.
        setupActionBar();
        
        Log.d("LogInfo001","ENA - onCreate after ActionBar");
        
        Intent intent = getIntent();
		dbId = intent.getStringExtra(MainActivity.DB_ID);
		String note = intent.getStringExtra(MainActivity.SHOW_NOTE_NOTE);
		String thumb = intent.getStringExtra(MainActivity.THUMB_FILE);
		String thumbRotation = intent.getStringExtra(MainActivity.THUMB_ROTATION);
		listIndex = intent.getStringExtra(MainActivity.EDIT_NOTE_LIST_INDEX);
		
		Double rotation = Double.parseDouble(thumbRotation);
		
		editNote = (EditText)findViewById(R.id.editNote);		
		editNote.setText(note, TextView.BufferType.EDITABLE);
		editNote.requestFocus();
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.restartInput(editNote);
		
		Log.d("LogInfo001","ENA - after EditNoteTextView");
		
		ImgLoader imgLoader = new ImgLoader(this);
		ImageView imageView = (ImageView) findViewById(R.id.imageView3);
		
		imageView.setRotation(rotation.floatValue());		
		
		imgLoader.loadBitmap(thumb, imageView);
		
		lDbId = Long.parseLong(dbId);		
	}
	
	public void saveEditedNote() {
		DbHelper mDbHelper = new DbHelper(EditNoteActivity.this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String note = editNote.getText().toString();
		
		ContentValues args = new ContentValues();
        args.put(FeedSiteNote.COLUMN_NAME_NOTE, note);		
        Log.d("LogInfo001","ENA - args: " + args.toString());
        
		resultSQL = db.update(DbHelper.FeedSiteNote.TABLE_NAME, args, FeedSiteNote._ID + "=" + lDbId, null) > 0;
		if (!resultSQL) {
			Toast.makeText(this, R.string.sql_update_alert, Toast.LENGTH_LONG).show();
		  }
		  else {Toast.makeText(this, R.string.sql_update_confirm, Toast.LENGTH_LONG).show();
		  }
		
		Log.d("LogInfo001","ENA - TableName: " + DbHelper.FeedSiteNote.TABLE_NAME);
		Log.d("LogInfo001","ENA - ColumnName: " + DbHelper.FeedSiteNote.COLUMN_NAME_ID);
		Log.d("LogInfo001","ENA - lDbId: " + lDbId);
		
		db.close();	
		mDbHelper.close();
		Log.d("LogInfo001","ENA - db.update");		

		finish();				
	}
	
	@Override
	public void finish() {
	  // Prepare data intent 
	  Intent data = new Intent();
	  data.putExtra("editedNote", editNote.getText().toString());
	  data.putExtra("editedNoteListIndex", listIndex);
	  data.putExtra("editedNoteDbId", dbId);
	  if (resultSQL) {   // Activity finished ok, return the data
		  setResult(RESULT_OK, data);
		  super.finish();
	  }
	  setResult(RESULT_CANCELED, data); // Activity finished NOT ok
	  super.finish();
	} 
		
	 // Set up the {@link android.app.ActionBar}, if the API is available.	 
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_edit_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:			
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_save_edited_note:			
			saveEditedNote();
			return true;	
		}
		return super.onOptionsItemSelected(item);
	}
}