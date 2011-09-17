package com.shadowcraft.android;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.shadowcraft.android.R.drawable;


public class IconHandler {

    private Paint mask;
    private PorterDuffXfermode porter_SRC_IN, porter_DST_ATOP;
    private Resources res;
    private Class<drawable> classR;
    private Map<String, Integer> idCache = new HashMap<String, Integer>();
    private Map<String, Drawable> socketFrames = new HashMap<String, Drawable>();
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
        classR = R.drawable.class;
        mask = new Paint(Paint.ANTI_ALIAS_FLAG);
        mask.setColor(WHITE);
        mask.setAlpha(255);
        porter_DST_ATOP = new PorterDuffXfermode(Mode.DST_ATOP);
        porter_SRC_IN = new PorterDuffXfermode(Mode.SRC_IN);
        socketFrames.put("RED", res.getDrawable(R.drawable.sc_sockets_red));
        socketFrames.put("YELLOW", res.getDrawable(R.drawable.sc_sockets_yellow));
        socketFrames.put("BLUE", res.getDrawable(R.drawable.sc_sockets_blue));
        socketFrames.put("META", res.getDrawable(R.drawable.sc_sockets_meta));
        socketFrames.put("PRISMATIC", res.getDrawable(R.drawable.sc_sockets_prismatic));
    }

    public Bitmap getItemIcon(int id, int quality) {
        int r = 4;              // round edge
        float w = 3;
        return getIconWithBasicFrame(id, r, COLORS[quality], w);
    }

    public Bitmap getItemIcon(String name, int quality) {
        return getItemIcon(getIconId(name), quality);
    }

    public Drawable getGemIcon(int id, String socket) {
        Drawable frame = socketFrames.get(socket);
        if (frame == null)
            frame = socketFrames.get("META");

        Drawable[] layers = new Drawable[2];
        layers[0] = res.getDrawable(id);
        layers[1] = frame;
        return new LayerDrawable(layers);
    }

    public Drawable getGemIcon(String name, String socket) {
        return getGemIcon(getIconId(name), socket);
    }

    public Bitmap getTalentIcon(int id) {
        int r = 5;
        return getIconWithBasicFrame(id, r, WHITE);
    }

    public Bitmap fetchBitmap(int id) {
        InputStream is = res.openRawResource(id);
        return BitmapFactory.decodeStream(is);
    }

    /**
     * This attempts to find the id tied to a resource in the R class
     * @param name The String holding the name of the resource
     * @return The resource id, or the id of the question mark if non is found.
     */
    public int getIconId(String name) {
        if (idCache.containsKey(name)) {
            return idCache.get(name);
        }
        // apparently reflection is faster than getIdentifier().
        //return res.getIdentifier(name, "drawable", "com.shadowcraft.android");
        try {
            java.lang.reflect.Field field = classR.getField(name);
            int id = field.getInt(null);
            idCache.put(name, id);
            return id;
        }
        catch (Exception e) {
            // TODO return the question mark icon id.
            return 0;
        }
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
