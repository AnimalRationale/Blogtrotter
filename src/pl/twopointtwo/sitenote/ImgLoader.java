package pl.twopointtwo.sitenote;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImgLoader {

	private Context mContext;
	private Bitmap mPlaceHolderBitmap;

	public ImgLoader(Context mContex) {
		this.mContext = mContex;
		this.mPlaceHolderBitmap = BitmapFactory.decodeResource(mContex.getResources(), android.R.drawable.ic_menu_gallery);
	}

	public void loadBitmap(String file, ImageView imageView) {
		if (cancelPotentialWork(file, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(file);
		}		
	}

	public static boolean cancelPotentialWork(String file, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.data;
			if (bitmapData != file) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was cancelled
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	public static Bitmap decodeBitmap(String pathPhotoName, int targetWidth, int targetHeight) {
		// Ustawienie wlasciwosci inJustDecodeBounds = true
		// zapobiega alokacji pamieci podczas dekodowania
		// obiekt bitmapy zwraca null, ale ustawia outWidth, outHeight i
		// outMimeType.
		// Pozwala to odczytac wymiary i typ danych obrazu

		// Pierwszy decode z inJustDecodeBounds = true aby sprawdzic wymiary
		BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
		decodeBitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathPhotoName, decodeBitmapOptions);
		int photoWidth = decodeBitmapOptions.outWidth;
		int photoHeight = decodeBitmapOptions.outHeight;

		// Wyliczamy jak bardzo mamy zmniejszyc zdjecie
		int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);
		decodeBitmapOptions.inSampleSize = scaleFactor;

		// Kompresujemy obrazek zgodnie z wyliczonymi opcjami
		decodeBitmapOptions.inJustDecodeBounds = false;
		decodeBitmapOptions.inPurgeable = true;
		return BitmapFactory.decodeFile(pathPhotoName, decodeBitmapOptions);
	}
	
	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}
	
	
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		String data;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			data = params[0];
			return decodeBitmap(data, 100, 100);
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
		
	}

}