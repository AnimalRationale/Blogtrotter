package pl.appnode.blogtrotter;

import java.util.ArrayList;

import pl.appnode.blogtrotter.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<DataSet> mNoteList;
	private ImgLoader imgLoader;

	public ImageAdapter(Context c, String[] t) {
		mContext = c;
		imgLoader = new ImgLoader(mContext);
	}

	public ImageAdapter(Context c, ArrayList<DataSet> nl) {
		mContext = c;
		mNoteList = nl;
		imgLoader = new ImgLoader(mContext);
	}

	@Override
	public int getCount() {
		return mNoteList.size();
	}

	@Override
	public Object getItem(int position) {
		return mNoteList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mNoteList.get(position).hashCode();
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View mView;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mView = inflater.inflate(R.layout.single_note, null);
		} else {
			mView = (View) convertView;
		}

		if (mNoteList.isEmpty()) {
			return mView;
		}

		ImageView imageView = (ImageView) mView.findViewById(R.id.imageView1);
		imageView.setRotation((float) mNoteList.get(position).getRotation());

		imgLoader.loadBitmap(mNoteList.get(position).getThumb(), imageView);

		TextView textViewNote = (TextView) mView.findViewById(R.id.textViewNote);
		textViewNote.setText(mNoteList.get(position).getNote());

		TextView textViewId = (TextView) mView.findViewById(R.id.textView_gps);
		if (mNoteList.get(position).getExifGpsLatitude() != null || mNoteList.get(position).getLocation().length() > 5) {
			setFontFace(textViewId);
			textViewId.setText("H");
		} else {
			textViewId.setText("");
		}
		
		TextView textViewDate = (TextView) mView.findViewById(R.id.textViewDate2);
		textViewDate.setText(mNoteList.get(position).getDateTime());

		return mView;
	}

	public static void setFontFace(TextView textView) {
		Typeface font = Typeface.createFromAsset(textView.getContext().getAssets(), "fonts/AndroidIcons.ttf");
		textView.setTypeface(font);
	}
}
