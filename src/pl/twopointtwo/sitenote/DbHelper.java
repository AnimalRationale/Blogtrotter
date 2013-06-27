package pl.twopointtwo.sitenote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DbHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 7;
	private static final String DATABASE_NAME = "SiteNote.db";

	public static abstract class FeedSiteNote implements BaseColumns {
		public static final String TABLE_NAME = "sitenote";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_DATE_TIME = "datetime";
		public static final String COLUMN_NAME_FILE = "file";
		public static final String COLUMN_NAME_THUMB = "thumb";
		public static final String COLUMN_NAME_NOTE = "note";
		public static final String COLUMN_NAME_LOCATION = "location";
		public static final String COLUMN_NAME_ROTATION = "rotation";
		public static final String COLUMN_NAME_EXIF_ORIENTATION = "exiforientation";
		public static final String COLUMN_NAME_EXIF_GPS_LATITUDE = "exifgpslatitude";
		public static final String COLUMN_NAME_EXIF_GPS_LATITUDEREF = "exifgpslatituderef";
		public static final String COLUMN_NAME_EXIF_GPS_LONGITUDE = "exifgpslongitude";
		public static final String COLUMN_NAME_EXIF_GPS_LONGITUDEREF = "exifgpslongituderef";

		private FeedSiteNote() {}; // aby zapobiec tworzeniu obiektu klasy typu contract tworzymy pusty konstruktor

	}

	private static final String TEXT_TYPE = " TEXT";
	private static final String REAL_TYPE = " REAL";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + FeedSiteNote.TABLE_NAME + " (" 
																	+ FeedSiteNote._ID + " INTEGER PRIMARY KEY,"
																	+ FeedSiteNote.COLUMN_NAME_ID + TEXT_TYPE + COMMA_SEP 
																	+ FeedSiteNote.COLUMN_NAME_DATE_TIME + TEXT_TYPE + COMMA_SEP 
																	+ FeedSiteNote.COLUMN_NAME_FILE + TEXT_TYPE + COMMA_SEP  
																	+ FeedSiteNote.COLUMN_NAME_THUMB + TEXT_TYPE + COMMA_SEP 
																	+ FeedSiteNote.COLUMN_NAME_NOTE + TEXT_TYPE + COMMA_SEP 
																	+ FeedSiteNote.COLUMN_NAME_LOCATION + TEXT_TYPE + COMMA_SEP
																	+ FeedSiteNote.COLUMN_NAME_ROTATION + REAL_TYPE + COMMA_SEP
																	+ FeedSiteNote.COLUMN_NAME_EXIF_ORIENTATION + TEXT_TYPE + COMMA_SEP
																	+ FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDE + TEXT_TYPE + COMMA_SEP
																	+ FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDEREF + TEXT_TYPE + COMMA_SEP
																	+ FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDE + TEXT_TYPE + COMMA_SEP
																	+ FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDEREF + TEXT_TYPE
			+" )";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FeedSiteNote.TABLE_NAME;

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
	}	

}
