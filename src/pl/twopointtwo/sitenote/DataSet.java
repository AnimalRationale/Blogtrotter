package pl.twopointtwo.sitenote;

public class DataSet {	
	
	private String dbId;
	private String id;
	private String dateTime;
	private String file;
	private String thumb;
	private String note;	
	private String location;
	private double rotation; //wyliczona z exifOrientation, gotowa do uzycia w imageView.setRotation((float)rotation); 
	private String exifOrientation; //1-normal; 3-rotate180; 8-rotate270; 6-rotate90;
	private String exifGpsLatitude; //Format is "num1/denom1,num2/denom2,num3/denom3".
	private String exifGpsLatitudeRef; 
	private String exifGpsLongitude;
	private String exifGpsLongitudeRef; //Format is "num1/denom1,num2/denom2,num3/denom3".
		
	public DataSet(String dbId, String id, String dateTime, String file, String thumb, String note, String location, double rotation, String exifOrientation,
			String exifGpsLatitude, String exifGpsLatitudeRef, String exifGpsLongitude, String exifGpsLongitudeRef) {
		super();
		this.dbId = dbId;
		this.id = id;
		this.dateTime = dateTime;
		this.file = file;
		this.thumb = thumb;
		this.note = note;
		this.location = location;
		this.rotation = rotation;
		this.exifOrientation = exifOrientation;
		this.exifGpsLatitude = exifGpsLatitude;
		this.exifGpsLatitudeRef = exifGpsLatitudeRef;
		this.exifGpsLongitude = exifGpsLongitude;
		this.exifGpsLongitudeRef = exifGpsLongitudeRef;
	}
	
	public String getDbId() {
		return dbId;
	}
	public void setDbId(String dbId) {
		this.dbId = dbId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getThumb() {
		return thumb;
	}
	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}	

	public String getExifOrientation() {
		return exifOrientation;
	}
	public void setExifOrientation(String exifOrientation) {
		this.exifOrientation = exifOrientation;
	}
	public String getExifGpsLatitude() {
		return exifGpsLatitude;
	}
	public void setExifGpsLatitude(String exifGpsLatitude) {
		this.exifGpsLatitude = exifGpsLatitude;
	}
	public String getExifGpsLatitudeRef() {
		return exifGpsLatitudeRef;
	}
	public void setExifGpsLatitudeRef(String exifGpsLatitudeRef) {
		this.exifGpsLatitudeRef = exifGpsLatitudeRef;
	}
	public String getExifGpsLongitude() {
		return exifGpsLongitude;
	}
	public void setExifGpsLongitude(String exifGpsLongitude) {
		this.exifGpsLongitude = exifGpsLongitude;
	}
	public String getExifGpsLongitudeRef() {
		return exifGpsLongitudeRef;
	}
	public void setExifGpsLongitudeRef(String exifGpsLongitudeRef) {
		this.exifGpsLongitudeRef = exifGpsLongitudeRef;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}	
}
