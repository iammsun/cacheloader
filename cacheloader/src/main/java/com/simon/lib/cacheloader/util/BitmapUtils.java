/**
 * @author mengsun
 * @date 2015-11-17 17:00:54
 */

package com.simon.lib.cacheloader.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BitmapUtils {

    public static final int MAXMIMUM_BITMAP_SIZE = 2048;

    public static Uri takePhoto(Activity activity, int resultCode) {
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ContentValues values = new ContentValues();
            Uri uri = activity.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            activity.startActivityForResult(intent, resultCode);
            return uri;
        }
        return null;
    }

    public static boolean takeVideo(Activity activity, int resultCode) {
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            ContentValues values = new ContentValues();
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    activity.getContentResolver()
                            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));
            activity.startActivityForResult(intent, resultCode);
            return true;
        }
        return false;
    }

    public static void pickPhoto(Activity activity, int resultCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, resultCode);
    }

    public static void pickAudio(Activity activity, int resultCode) {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, resultCode);
    }

    public static void pickVideo(Activity activity, int resultCode) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, resultCode);
    }

    private static Bitmap decodeByteArray(byte[] data, BitmapFactory.Options options) {
        if (data == null) {
            return null;
        }
        if (options == null) {
            options = new BitmapFactory.Options();
        }
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (OutOfMemoryError e) {
            if (options.inSampleSize < 1) {
                options.inSampleSize = 1;
            }
            options.inSampleSize *= 2;
            return decodeByteArray(data, options);
        }
    }

    private static Bitmap decodeStream(InputStream is, BitmapFactory.Options options) {
        if (options == null) {
            options = new BitmapFactory.Options();
        }
        try {
            return BitmapFactory.decodeStream(is, null, options);
        } catch (OutOfMemoryError e) {
            if (options.inSampleSize < 1) {
                options.inSampleSize = 1;
            }
            options.inSampleSize *= 2;
            return decodeStream(is, options);
        }
    }

    private static Bitmap decodeResource(Context context, int resourceId,
            BitmapFactory.Options options) {
        if (options == null) {
            options = new BitmapFactory.Options();
        }
        try {
            return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        } catch (OutOfMemoryError e) {
            if (options.inSampleSize < 1) {
                options.inSampleSize = 1;
            }
            options.inSampleSize *= 2;
            return decodeResource(context, resourceId, options);
        }
    }

    public static Bitmap roundCorner(Bitmap bitmap, float cornerRate) {
        if (cornerRate == 0 || bitmap == null) {
            return bitmap;
        }
        if (cornerRate < 0 || cornerRate > 1) {
            throw new IllegalArgumentException(
                    "rate should be [0, 1] and the value is " + cornerRate);
        }
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, width, height);
            final RectF rectF = new RectF(rect);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, (float) width / 2 * cornerRate,
                    (float) height / 2 * cornerRate, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            return output;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static Bitmap decodeBitmap(byte[] data) {
        return decodeByteArray(data, null);
    }

    public static Bitmap decodeBitmap(byte[] data, float width, float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap temp = decodeByteArray(data, options);
        if (temp != null) {
            temp.recycle();
        }
        if (options.outMimeType == null) {
            return null;
        }
        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }
        float scale = Math.max(height / options.outHeight, width / options.outWidth);
        while ((float) 1 / options.inSampleSize > scale
                || MAXMIMUM_BITMAP_SIZE < (float) options.outHeight / options.inSampleSize
                || MAXMIMUM_BITMAP_SIZE < (float) options.outWidth / options.inSampleSize) {
            options.inSampleSize *= 2;
        }
        if ((float) 1 / options.inSampleSize < scale
                && MAXMIMUM_BITMAP_SIZE >= (float) options.outHeight * 2 / options.inSampleSize
                && MAXMIMUM_BITMAP_SIZE >= (float) options.outWidth * 2 / options.inSampleSize) {
            options.inSampleSize /= 2;
        }
        options.inJustDecodeBounds = false;
        return decodeByteArray(data, options);
    }

    public static Bitmap decodeCroppedBitmap(byte[] data, float width, float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap temp = decodeByteArray(data, options);
        if (options.outMimeType == null) {
            return null;
        }
        Bitmap result = crop(temp, width, height);
        if (result != temp && temp != null) {
            temp.recycle();
        }
        return result;
    }

    public static Bitmap decodeCroppedRoundBitmap(byte[] data, float width, float height,
            float cornerRate) {
        Bitmap temp = decodeCroppedBitmap(data, width, height);
        Bitmap result = temp;
        if (temp != null && cornerRate != 0) {
            result = roundCorner(temp, cornerRate);
            if (result != temp && temp != null) {
                temp.recycle();
            }
        }
        return result;
    }

    public static Bitmap decodeScaledBitmap(InputStream is, float width, float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap temp = decodeStream(is, options);
        if (options.outMimeType == null) {
            return null;
        }
        Bitmap result = scale(temp, Math.max(height / options.outHeight, width / options.outWidth));
        if (result != temp && temp != null) {
            temp.recycle();
        }
        return result;
    }

    public static Bitmap decodeCroppedBitmap(InputStream is, float width, float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap temp = decodeStream(is, options);
        if (options.outMimeType == null) {
            return null;
        }
        Bitmap result = crop(temp, width, height);
        if (result != temp && temp != null) {
            temp.recycle();
        }
        return result;
    }

    public static Bitmap decodeScaledBitmap(Context context, int resourceId, float width,
            float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap temp = decodeResource(context, resourceId, options);
        if (options.outMimeType == null) {
            return null;
        }
        Bitmap result = scale(temp, Math.max(height / options.outHeight, width / options.outWidth));
        if (result != temp && temp != null) {
            temp.recycle();
        }
        return result;
    }

    public static Bitmap decodeCroppedBitmap(Context context, int resourceId, float width,
            float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap temp = decodeResource(context, resourceId, options);
        if (options.outMimeType == null) {
            return null;
        }
        Bitmap result = crop(temp, width, height);
        if (result != temp && temp != null) {
            temp.recycle();
        }
        return result;
    }

    public static Bitmap decodeCroppedRoundBitmap(Context context, int resId, float width,
            float height,
            float roundPx) {
        Bitmap temp = decodeCroppedBitmap(context, resId, width, height);
        Bitmap result = temp;
        if (temp != null && roundPx != 0) {
            result = roundCorner(temp, roundPx);
            if (result != temp && temp != null) {
                temp.recycle();
            }
        }
        return result;
    }

    public static Bitmap crop(Bitmap bitmap, float width, float height) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scale1 = width / height;
        float scale2 = (float) w / h;
        if (scale1 < scale2) {
            width = (int) (h * scale1);
            height = h;
        } else {
            width = w;
            height = (int) (w / scale1);
        }
        int retX = (int) (width > w ? 0 : (w - width) / 2);
        int retY = (int) (height > h ? 0 : (h - height) / 2);
        return Bitmap.createBitmap(bitmap, retX, retY, (int) width, (int) height, null, false);
    }

    public static Bitmap scale(Bitmap bitmap, float scale) {
        if (bitmap == null) {
            return null;
        }
        int width = Math.max(1, (int) (bitmap.getWidth() * scale));
        int height = Math.max(1, (int) (bitmap.getHeight() * scale));
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    public static byte[] decodeBytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                : Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}
