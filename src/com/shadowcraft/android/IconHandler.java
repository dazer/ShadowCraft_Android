package com.shadowcraft.android;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;


public class IconHandler {

    private Paint mask = null;
    private PorterDuffXfermode porter_SRC_IN, porter_DST_ATOP = null;
    private Resources res;
    static final int WHITE =     0xFFFFFFFF;
    static final int POOR =      0xFF9D9D9D;
    static final int COMMON =    0xFFFFFFFF;
    static final int UNCOMMON =  0xFF1EFF00;
    static final int RARE =      0xFF0070DD;
    static final int EPIC =      0xFFA335EE;
    static final int LEGENDARY = 0xFFFF8000;
    static final int ARTIFACT =  0xFFE5CC80;
    static final int HEIRLOOM =  0xFFE5CC80;
    final int[] COLORS = new int[] {POOR, COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, ARTIFACT, HEIRLOOM};

    /**
     * Constructor: initializes basic assets, as well as the Resources, which
     * need to be passed because this activity doesn't have Context.
     * @param res The Application resources.
     */
    public IconHandler(Resources res) {
        this.res = res;
        mask = new Paint(Paint.ANTI_ALIAS_FLAG);
        mask.setColor(WHITE);
        mask.setAlpha(255);
        porter_DST_ATOP = new PorterDuffXfermode(Mode.DST_ATOP);
        porter_SRC_IN = new PorterDuffXfermode(Mode.SRC_IN);
    }

    public Bitmap getItemIcon(int id, int quality) {
        int r = 4;              // round edge
        float w = 3;
        return getIconWithBasicFrame(id, r, quality, w);
    }

    public Bitmap getTalentIcon(int id) {
        int r = 5;
        return getIconWithBasicFrame(id, r, WHITE);
    }

    public Bitmap fetchBitmap(int id) {
        InputStream is = res.openRawResource(id);
        return BitmapFactory.decodeStream(is);
    }

    public Bitmap roundCorners(Bitmap bitmap, int r) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Rect rect = new Rect(0, 0, w, h);
        RectF rectF = new RectF(rect);
        Bitmap processedIcon = Bitmap.createBitmap(w, h, Config.ARGB_8888);

        Canvas canvas = new Canvas(processedIcon);
        canvas.drawARGB(0, 0, 0, 0);

        mask.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rectF, r, r, mask);
        mask.setXfermode(porter_SRC_IN);
        canvas.drawBitmap(bitmap, null, rect, mask);

        return processedIcon;
    }

    public Bitmap getIconWithBasicFrame(int id, int r, int color, float width) {
        Bitmap bitmap = fetchBitmap(id);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Rect rect = new Rect(0, 0, w, h);
        RectF rectF = new RectF(rect);
        Bitmap processedIcon = Bitmap.createBitmap(w, h, Config.ARGB_8888);

        Canvas canvas = new Canvas(processedIcon);
        canvas.drawARGB(0, 0, 0, 0);

        mask.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rectF, r, r, mask);
        mask.setXfermode(porter_SRC_IN);
        canvas.drawBitmap(bitmap, null, rect, mask);

        mask.setColor(color);
        mask.setStrokeWidth(width); // frame stroke width
        mask.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rectF, r, r, mask);
        mask.setXfermode(porter_DST_ATOP);

        return processedIcon;
    }

    public Bitmap getIconWithBasicFrame(int id, int r, int color) {
        return getIconWithBasicFrame(id, r, color, (float) 4.5);
    }
}
