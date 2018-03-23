package com.example.android.trainingtask4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by TQ on 20-Mar-18.
 */

public class ChatMessage {

    private String mText;
    private Calendar mCalendar;
    private String mCurrentMediaPath;
    private String mCurrentThumbnailPath;
    private boolean isSent;
    private int mStatus;
    private int mMediaType;

    public static final int STATUS_SENDING = R.drawable.msg_status_gray_waiting;
    public static final int STATUS_SENT = R.drawable.msg_status_client_received;
    public static final int STATUS_READ = R.drawable.msg_status_client_read;

    public static final int MEDIA_NONE = 0;
    public static final int MEDIA_PHOTO = 1;
    public static final int MEDIA_VIDEO = 2;

    private ChatMessage(Calendar calendar, boolean isSent) {
        mCalendar = calendar;
        this.isSent = isSent;
        mStatus = STATUS_SENDING;
    }

    public ChatMessage(String text, Calendar calendar, boolean isSent) {
        this(calendar, isSent);
        mText = text;
        mMediaType = MEDIA_NONE;
    }

    public ChatMessage(Calendar calendar, boolean isSent, int mediaType) {
        this(calendar, isSent);
        mMediaType = mediaType;
    }

    public String getText() {
        return mText;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public boolean isSent() {
        return isSent;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    public int getMediaType() {
        return mMediaType;
    }

    public String getCurrentMediaPath() {
        return mCurrentMediaPath;
    }

    public void setCurrentMediaPath(String mCurrentMediaPath) {
        this.mCurrentMediaPath = mCurrentMediaPath;
    }

    public String getCurrentThumbnailPath() {
        return mCurrentThumbnailPath;
    }

    public void setCurrentThumbnailPath(String mCurrentThumbnailPath) {
        this.mCurrentThumbnailPath = mCurrentThumbnailPath;
    }

    public File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String imageThumbFileName = "JPEG_" + timeStamp + "thumbnail" + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        File imageThumb = File.createTempFile(
                imageThumbFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentMediaPath = image.getAbsolutePath();
        mCurrentThumbnailPath = imageThumb.getAbsolutePath();

        return image;
    }

    public File createVideoFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        String videoThumbFileName = "JPEG_" + timeStamp + "thumbnail" + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
        File videoThumb = File.createTempFile(
                videoThumbFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentMediaPath = video.getAbsolutePath();
        mCurrentThumbnailPath = videoThumb.getAbsolutePath();

        return video;
    }

    public void setPic(Context context) {
        Bitmap thumbnail = BitmapFactory.decodeFile(mCurrentMediaPath);
        setThumbnail(context, thumbnail);
    }

    public void setVideo(Context context) {
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mCurrentMediaPath, MediaStore.Video.Thumbnails.MINI_KIND);
        setThumbnail(context, thumbnail);
    }

    private void setThumbnail(Context context, Bitmap thumbnail) {
        if (thumbnail == null) {
            Toast.makeText(context, "ERROR: FAILED TO GET THUMBNAIL", Toast.LENGTH_SHORT).show();
            return;
        }
        int width = thumbnail.getWidth();
        int height = thumbnail.getHeight();
        float scaleWidth = ((float) context.getResources().getDimension(R.dimen.image_width)) / width;
        float scaleHeight = ((float) context.getResources().getDimension(R.dimen.image_height)) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                thumbnail, 0, 0, width, height, matrix, false);
        thumbnail.recycle();

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(mCurrentThumbnailPath);

        try {
            fOut = new FileOutputStream(file);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
