package com.lomocoin.map.amap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.WindowManager;

import com.airbnb.android.react.maps.R;

/**
 * 处理用户头像问题
 *
 * @author qiaojiayan
 * @date 17/8/17 下午5:20
 */

public class UserHeadUtils {
    public static Bitmap createUserIcon(Bitmap bitmap, Context context) {
        try {
            //1.圆角
            bitmap = toRoundCorner(bitmap, 360);

            //2.调整图片大小
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            int w = wm.getDefaultDisplay().getWidth();
            int mW = (int) (w * 0.12);
            bitmap = getBitmapWithSize(bitmap, mW, mW);
            //3.添加光圈

            Bitmap bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_user_wrapper);
            int bgW = (int) (mW * (221.0/145.0));
            bg = getBitmapWithSize(bg, bgW, bgW);
            int left = (int) (bgW * (38.0 / 221.0));
            return toConformBitmap(bg, bitmap, left, left);
        } catch (Exception e) {
            return null;
        }
    }


    private static Bitmap toConformBitmap(Bitmap bg, Bitmap user, int left, int top) {
        if (bg == null) {
            return null;
        }

        int bgWidth = bg.getWidth();
        int bgHeight = bg.getHeight();
        //int fgWidth = foreground.getWidth();
        //int fgHeight = foreground.getHeight();
        //create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newbmp);
        //draw bg into
        cv.drawBitmap(bg, 0, 0, null);//在 0，0坐标开始画入bg
        //draw fg into
        cv.drawBitmap(user, left, top, null);//在 0，0坐标开始画入fg ，可以从任意位置画入
        //save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);//保存
        //store
        cv.restore();//存储
        return newbmp;
    }

    private static Bitmap getBitmapWithSize(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = w * 1.0f / width;
        float scaleHeight = h * 1.0f / height;
        // 取得想要缩放的matrix參數
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的圖片
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * @param bitmapOld 需要切的图
     * @param pixels    角的弧度  360 圆角
     * @return 返回切割的图片
     */
    private static Bitmap toRoundCorner(Bitmap bitmapOld, int pixels) {
        int size;
        //取最短边为宽高
        if (bitmapOld.getWidth() > bitmapOld.getHeight()) {
            size = bitmapOld.getHeight();
        } else {
            size = bitmapOld.getWidth();
        }

        Bitmap bitmap = Bitmap.createBitmap(bitmapOld, 0, 0, size, size);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);
        final float roundPx = size / 2;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
