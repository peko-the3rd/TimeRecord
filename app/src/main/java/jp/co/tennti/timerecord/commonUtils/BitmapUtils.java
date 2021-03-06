package jp.co.tennti.timerecord.commonUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

/**
 * Created by TENNTI on 2016/04/15.
 */
public class BitmapUtils {
    /**
     * 画像生成
     * 表示サイズ合わせて画像生成時に可能なかぎり縮小して生成します。
     *
     * @param path パス
     * @param width 幅
     * @param height 高さ
     * @return 生成Bitmap
     */
    public static Bitmap createBitmap(String path, int width, int height) {

        BitmapFactory.Options option = new BitmapFactory.Options();

        // 情報のみ読み込む
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, option);

        if (option.outWidth < width || option.outHeight < height) {
            // 縦、横のどちらかが指定値より小さい場合は普通にBitmap生成
            return BitmapFactory.decodeFile(path);
        }

        float scaleWidth = ((float) width) / option.outWidth;
        float scaleHeight = ((float) height) / option.outHeight;

        int newSize = 0;
        int oldSize = 0;
        if (scaleWidth > scaleHeight) {
            newSize = width;
            oldSize = option.outWidth;
        } else {
            newSize = height;
            oldSize = option.outHeight;
        }

        // option.inSampleSizeに設定する値を求める
        // option.inSampleSizeは2の乗数のみ設定可能
        int sampleSize = 1;
        int tmpSize = oldSize;
        while (tmpSize > newSize) {
            sampleSize = sampleSize * 2;
            tmpSize = oldSize / sampleSize;
        }
        if (sampleSize != 1) {
            sampleSize = sampleSize / 2;
        }

        option.inJustDecodeBounds = false;
        option.inSampleSize = sampleSize;
        option.inPurgeable=true;
        return BitmapFactory.decodeFile(path, option);
    }

    /**
     * 画像リサイズ
     * @param bitmap 変換対象ビットマップ
     * @param newWidth 変換サイズ横
     * @param newHeight 変換サイズ縦
     * @return 変換後Bitmap
     */
    public static Bitmap resize(Bitmap bitmap, int newWidth, int newHeight) {

        if (bitmap == null) {
            return null;
        }

        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();

        if (oldWidth < newWidth && oldHeight < newHeight) {
            // 縦も横も指定サイズより小さい場合は何もしない
            return bitmap;
        }

        float scaleWidth = ((float) newWidth) / oldWidth;
        float scaleHeight = ((float) newHeight) / oldHeight;
        float scaleFactor = Math.min(scaleWidth, scaleHeight);

        Matrix scale = new Matrix();
        scale.postScale(scaleFactor, scaleFactor);

        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, scale, false);
        bitmap.recycle();

        return resizeBitmap;

    }
    /**
     * Activity以外でのディスプレイ情報の取得
     * @param Context context
     */
    public static final DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager winMan   = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display disp           = winMan.getDefaultDisplay();
        DisplayMetrics dispMet = new DisplayMetrics();
        disp.getMetrics(dispMet);
        return dispMet;
    }

    /**
     * 指定したビュー階層内のドローワブルをクリアする。
     * （ドローワブルをのコールバックメソッドによるアクティビティのリークを防ぐため）
     * @param view
     */
    public static final void cleanupView(View view) {
        if (view instanceof ImageButton) {
            ImageButton ib = (ImageButton) view;
            ib.setImageDrawable(null);
            ib.setImageBitmap(null);
            ib.destroyDrawingCache();
        } else if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.setImageDrawable(null);
            iv.setImageBitmap(null);
            iv.destroyDrawingCache();
        } else if (view instanceof View) {
            view.setOnClickListener(null);
            view.setBackground(null);
            view.destroyDrawingCache();
        }
//        } else if (view instanceof SeekBar) {
//            SeekBar sb = (SeekBar) view;
//            sb.setProgressDrawable(null);
//            sb.setThumb(null);
//            // } else if(view instanceof( xxxx )) {  -- 他にもDrawableを使用するUIコンポーネントがあれば追加
//        }
        view.setBackgroundDrawable(null);
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int size = vg.getChildCount();
            for (int i = 0; i < size; i++) {
                cleanupView(vg.getChildAt(i));
            }
        }
    }
//    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//
//        // 画像の元サイズ
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//            if (width > height) {
//                inSampleSize = Math.round((float)height / (float)reqHeight);
//            } else {
//                inSampleSize = Math.round((float)width / (float)reqWidth);
//            }
//        }
//        return inSampleSize;
//    }
//    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
//
//        // inJustDecodeBounds=true で画像のサイズをチェック
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, options);
//
//        // inSampleSize を計算
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        // inSampleSize をセットしてデコード
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(filePath, options);
//    }
    /**
     * Bitmap画像を丸に切り抜いて返す
     * @param Bitmap bitmap
     * @return Bitmap output 変換画像
     */
    public static final Bitmap toRoundBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);
        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        c.drawBitmap(bitmap, 4, 4, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        //p.setColor(Color.WHITE);
        p.setStrokeWidth(3);
        p.setColor(Color.alpha(0));
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);
        return output;
    }
}
