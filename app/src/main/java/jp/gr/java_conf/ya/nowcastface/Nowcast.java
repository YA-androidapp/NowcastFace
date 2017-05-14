package jp.gr.java_conf.ya.nowcastface; // Copyright (c) 2017 YA <ya.androidapp@gmail.com> All rights reserved. This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.graphics.Canvas;
import android.graphics.Color;
// import android.graphics.LightingColorFilter;
// import android.graphics.Paint;
// import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 降水ナウキャストから情報取得を行う
 */

public class Nowcast {
    private static Context context;
    private static final int nowcastDataNum = 11;
    private static Resources r;
    private static final Pattern colorCodePattern = Pattern.compile("^#[0-9A-Fa-f]{6,8}$");
    private static final double[][] latlngRange = {{139.0, 143.5, 42.0, 45.8}, {143.5, 147.0, 42.0, 44.8}, {138.0, 144.0, 41.0, 44.0}, {138.0, 143.5, 38.0, 42.0}, {137.5, 142.8, 36.5, 40.0},
            {137.0, 142.5, 34.0, 37.0}, {137.0, 140.8, 33.5, 34.0}, {136.0, 141.5, 34.0, 38.0}, {136.0, 141.5, 36.0, 38.8}, {134.0, 139.5, 34.5, 38.0}, {135.5, 140.8, 32.8, 36.5},
            {133.0, 138.0, 36.5, 33.0}, {130.5, 135.0, 33.5, 37.5}, {131.0, 136.0, 32.0, 35.8}, {128.0, 133.0, 32.0, 35.5}, {128.0, 133.0, 30.0, 33.0}, {127.5, 132.0, 27.0, 30.0},
            {127.1, 130.5, 25.0, 28.0}, {130.0, 132.0, 25.0, 28.0}, {122.0, 126.5, 23.0, 26.5}};

    private static final double[][] latlngBasis = {{73.0, 101.0, 138.5479, 46.1881}, {73.0, 101.0, 140.8082, 45.9802}, {75.0, 101.0, 137.6133, 45.0594}, {78.0, 101.0, 137.4872, 42.3960},
            {81.0, 101.0, 137.0741, 40.4752}, {83.0, 101.0, 136.6627, 37.8119}, {83.0, 101.0, 136.6627, 37.8119}, {83.0, 101.0, 135.6506, 38.3069}, {81.0, 101.0, 135.6667, 40.0594},
            {82.0, 101.0, 133.7927, 38.8119}, {84.0, 101.0, 135.1071, 37.0594}, {83.0, 101.0, 132.3373, 37.1485}, {84.0, 101.0, 129.6786, 37.8416}, {84.0, 101.0, 130.4524, 36.2277},
            {85.0, 102.0, 127.3882, 35.8824}, {87.0, 102.0, 127.9540, 33.7157}, {89.0, 102.0, 126.8539, 30.8824}, {91.0, 102.0, 125.4066, 28.7157}, {91.0, 101.0, 126.9121, 28.5644},
            {93.0, 101.0, 121.6129, 27.0594}};

    private static final String[] latlngName = {"201", "202", "203", "204", "205", "206", "206", "207", "208", "209", "210", "211", "212", "213", "214", "215", "216", "217", "218", "219"};

    Nowcast() {
        context = ApplicationController.getInstance().getApplicationContext();
        r = context.getResources();
    }

    public static final int[] getNowcastPrecipitations(String[] nowcastImageUrlParts) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.JAPAN);

        final Calendar cal0 = Calendar.getInstance();
        cal0.setLenient(true);
        cal0.set(Calendar.MINUTE, (cal0.get(Calendar.MINUTE) + -5 + (-1) * ((cal0.get(Calendar.MINUTE) % 5))));
        final Date time0 = cal0.getTime();
        final Calendar cal = cal0;
        cal.setLenient(true);
        cal.add(Calendar.MINUTE, 5);

        final int[] precipitations = new int[nowcastDataNum];

        if (nowcastImageUrlParts[0].equals("-1")) {
            final Double lat2 = 35.681382, lng2 = 139.766084;
            nowcastImageUrlParts = getNowcastImageUrl(lat2, lng2);
        }

        for (int i = 0; i < nowcastDataNum; i++) {
            cal.setLenient(true);
            cal.add(Calendar.MINUTE, 5);

            final String uriString = nowcastImageUrlParts[0] + (sdf.format(time0) + "-" + String.format("%1$02d", i + 2)) + ".png";
            // Log.d("Nowcast", "uriString: "+uriString);
            final Bitmap bmp = NetworkUtil.getBitmapFromWeb(uriString);
            // Log.d("Nowcast", "bmp: "+bmp.getWidth());

            if (bmp == null) {
                precipitations[i] = -1;
            } else {
                final int[] ninePixels = getNinePixels(bmp, Integer.parseInt(nowcastImageUrlParts[1]), Integer.parseInt(nowcastImageUrlParts[2]));
                if (ninePixels == null) {
                    precipitations[i] = -1;
                    // Log.d("Nowcast", "ninePixels == null");
                } else {
                    final int precipitation = getMaxPrecipitation(ninePixels);
                    precipitations[i] = precipitation;
                }
            }
        }

        return precipitations;
    }

    public static final String getNowcastString(final int[] precipitations) {
        final StringBuilder sb = new StringBuilder();

        for (final int item : precipitations) {
            if (item == -1)
                sb.append("☀");
            else
                sb.append(item).append(" ");
        }

        return sb.toString();
    }

    public static final String[] getNowcastImageUrl(double latitude, double longitude) {
        String UrlCommon = "http://www.jma.go.jp/jp/radnowc/imgs/nowcast/";

        String num = "-1";
        double x = 0;
        double y = 0;

        if ((longitude >= latlngRange[0][0]) && (longitude <= latlngRange[0][1]) && (latitude >= latlngRange[0][2]) && (latitude <= latlngRange[0][3])) {
            num = latlngName[0];
            x = (longitude - latlngBasis[0][2]) * latlngBasis[0][0];
            y = (latlngBasis[0][3] - latitude) * latlngBasis[0][1];
        } else if ((longitude >= latlngRange[1][0]) && (longitude <= latlngRange[1][1]) && (latitude >= latlngRange[1][2]) && (latitude <= latlngRange[1][3])) {
            num = latlngName[1];
            x = (longitude - latlngBasis[1][2]) * latlngBasis[1][0];
            y = (latlngBasis[1][3] - latitude) * latlngBasis[1][1];
        } else if ((longitude >= latlngRange[2][0]) && (longitude <= latlngRange[2][1]) && (latitude >= latlngRange[2][2]) && (latitude <= latlngRange[2][3])) {
            num = latlngName[2];
            x = (longitude - latlngBasis[2][2]) * latlngBasis[2][0];
            y = (latlngBasis[2][3] - latitude) * latlngBasis[2][1];
        } else if ((longitude >= latlngRange[3][0]) && (longitude <= latlngRange[3][1]) && (latitude >= latlngRange[3][2]) && (latitude <= latlngRange[3][3])) {
            num = latlngName[3];
            x = (longitude - latlngBasis[3][2]) * latlngBasis[3][0];
            y = (latlngBasis[3][3] - latitude) * latlngBasis[3][1];
        } else if ((longitude >= latlngRange[4][0]) && (longitude <= latlngRange[4][1]) && (latitude >= latlngRange[4][2]) && (latitude <= latlngRange[4][3])) {
            num = latlngName[4];
            x = (longitude - latlngBasis[4][2]) * latlngBasis[4][0];
            y = (latlngBasis[4][3] - latitude) * latlngBasis[4][1];
        } else if ((longitude >= latlngRange[5][0]) && (longitude <= latlngRange[5][1]) && (latitude >= latlngRange[5][2]) && (latitude <= latlngRange[5][3])) {
            num = latlngName[5];
            x = (longitude - latlngBasis[5][2]) * latlngBasis[5][0];
            y = (latlngBasis[5][3] - latitude) * latlngBasis[5][1];
        } else if ((longitude >= latlngRange[6][0]) && (longitude <= latlngRange[6][1]) && (latitude >= latlngRange[6][2]) && (latitude <= latlngRange[6][3])) {
            num = latlngName[6];
            x = (longitude - latlngBasis[6][2]) * latlngBasis[6][0];
            y = (latlngBasis[6][3] - latitude) * latlngBasis[6][1];
        } else if ((longitude >= latlngRange[7][0]) && (longitude <= latlngRange[7][1]) && (latitude >= latlngRange[7][2]) && (latitude <= latlngRange[7][3])) {
            num = latlngName[7];
            x = (longitude - latlngBasis[7][2]) * latlngBasis[7][0];
            y = (latlngBasis[7][3] - latitude) * latlngBasis[7][1];
        } else if ((longitude >= latlngRange[8][0]) && (longitude <= latlngRange[8][1]) && (latitude >= latlngRange[8][2]) && (latitude <= latlngRange[8][3])) {
            num = latlngName[8];
            x = (longitude - latlngBasis[8][2]) * latlngBasis[8][0];
            y = (latlngBasis[8][3] - latitude) * latlngBasis[8][1];
        } else if ((longitude >= latlngRange[9][0]) && (longitude <= latlngRange[9][1]) && (latitude >= latlngRange[9][2]) && (latitude <= latlngRange[9][3])) {
            num = latlngName[9];
            x = (longitude - latlngBasis[9][2]) * latlngBasis[9][0];
            y = (latlngBasis[9][3] - latitude) * latlngBasis[9][1];
        } else if ((longitude >= latlngRange[10][0]) && (longitude <= latlngRange[10][1]) && (latitude >= latlngRange[10][2]) && (latitude <= latlngRange[10][3])) {
            num = latlngName[10];
            x = (longitude - latlngBasis[10][2]) * latlngBasis[10][0];
            y = (latlngBasis[10][3] - latitude) * latlngBasis[10][1];
        } else if ((longitude >= latlngRange[11][0]) && (longitude <= latlngRange[11][1]) && (latitude >= latlngRange[11][2]) && (latitude <= latlngRange[11][3])) {
            num = latlngName[11];
            x = (longitude - latlngBasis[11][2]) * latlngBasis[11][0];
            y = (latlngBasis[11][3] - latitude) * latlngBasis[11][1];
        } else if ((longitude >= latlngRange[12][0]) && (longitude <= latlngRange[12][1]) && (latitude >= latlngRange[12][2]) && (latitude <= latlngRange[12][3])) {
            num = latlngName[12];
            x = (longitude - latlngBasis[12][2]) * latlngBasis[12][0];
            y = (latlngBasis[12][3] - latitude) * latlngBasis[12][1];
        } else if ((longitude >= latlngRange[13][0]) && (longitude <= latlngRange[13][1]) && (latitude >= latlngRange[13][2]) && (latitude <= latlngRange[13][3])) {
            num = latlngName[13];
            x = (longitude - latlngBasis[13][2]) * latlngBasis[13][0];
            y = (latlngBasis[13][3] - latitude) * latlngBasis[13][1];
        } else if ((longitude >= latlngRange[14][0]) && (longitude <= latlngRange[14][1]) && (latitude >= latlngRange[14][2]) && (latitude <= latlngRange[14][3])) {
            num = latlngName[14];
            x = (longitude - latlngBasis[14][2]) * latlngBasis[14][0];
            y = (latlngBasis[14][3] - latitude) * latlngBasis[14][1];
        } else if ((longitude >= latlngRange[15][0]) && (longitude <= latlngRange[15][1]) && (latitude >= latlngRange[15][2]) && (latitude <= latlngRange[15][3])) {
            num = latlngName[15];
            x = (longitude - latlngBasis[15][2]) * latlngBasis[15][0];
            y = (latlngBasis[15][3] - latitude) * latlngBasis[15][1];
        } else if ((longitude >= latlngRange[16][0]) && (longitude <= latlngRange[16][1]) && (latitude >= latlngRange[16][2]) && (latitude <= latlngRange[16][3])) {
            num = latlngName[16];
            x = (longitude - latlngBasis[16][2]) * latlngBasis[16][0];
            y = (latlngBasis[16][3] - latitude) * latlngBasis[16][1];
        } else if ((longitude >= latlngRange[17][0]) && (longitude <= latlngRange[17][1]) && (latitude >= latlngRange[17][2]) && (latitude <= latlngRange[17][3])) {
            num = latlngName[17];
            x = (longitude - latlngBasis[17][2]) * latlngBasis[17][0];
            y = (latlngBasis[17][3] - latitude) * latlngBasis[17][1];
        } else if ((longitude >= latlngRange[18][0]) && (longitude <= latlngRange[18][1]) && (latitude >= latlngRange[18][2]) && (latitude <= latlngRange[18][3])) {
            num = latlngName[18];
            x = (longitude - latlngBasis[18][2]) * latlngBasis[18][0];
            y = (latlngBasis[18][3] - latitude) * latlngBasis[18][1];
        } else if ((longitude >= latlngRange[19][0]) && (longitude <= latlngRange[19][1]) && (latitude >= latlngRange[19][2]) && (latitude <= latlngRange[19][3])) {
            num = latlngName[19];
            x = (longitude - latlngBasis[19][2]) * latlngBasis[19][0];
            y = (latlngBasis[19][3] - latitude) * latlngBasis[19][1];
        }

        String[] ret = new String[3];
        ret[0] = UrlCommon + num + "/";
        ret[1] = Long.toString(Math.round(x));
        ret[2] = Long.toString(Math.round(y));
        return ret;
    }

//    public static final Bitmap drawPrecipitationsIcon(int[] precipitations) {
//        final int pref_icon_dif = 5;
//
//        Bitmap sun = BitmapFactory.decodeResource(r, R.drawable.sun);
//        Bitmap umb = BitmapFactory.decodeResource(r, R.drawable.umb);
//
//        final Bitmap bitmap =
//                Bitmap.createBitmap(nowcastDataNum * pref_icon_dif + 16, nowcastDataNum * pref_icon_dif + 16, Bitmap.Config.ARGB_8888);
//        final Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.TRANSPARENT);
//
//        for (int i = nowcastDataNum - 1; i >= 0; i--) {
//            final Paint paint = new Paint();
//            if (precipitations[i] > -1) {
//                final LightingColorFilter lightingColorFilter = new LightingColorFilter(Color.parseColor(precipitationToColorcode(precipitations[i])), 0);
//                paint.setFilterBitmap(true);
//                paint.setColorFilter(lightingColorFilter);
//                if(umb==null)
//                    umb = BitmapFactory.decodeResource(r, R.drawable.umb);
//                canvas.drawBitmap(umb, i * (pref_icon_dif), i * pref_icon_dif, paint);
//            } else {
//                if(sun==null)
//                    sun = BitmapFactory.decodeResource(r, R.drawable.sun);
//                canvas.drawBitmap(sun, i * (pref_icon_dif), i * pref_icon_dif, paint);
//            }
//        }
//
//        return bitmap;
//    }

    private static final int getMaxPrecipitation(final int[] colors) {
        int maxPrecipitation = -1;
        for (final int color : colors) {
            final int precipitation = colorToPrecipitations(color);
            if (precipitation > maxPrecipitation)
                maxPrecipitation = precipitation;
        }
        return maxPrecipitation;
    }

    private static final int[] getNinePixels(final Bitmap bmp, final int x, final int y) {
        if (bmp == null)
            return null;

        final int[] ninePixels = new int[9];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                try {
                    ninePixels[3 * i + j] = bmp.getPixel(x + (j - 1), y + (i - 1));
                } catch (Exception e) {
                    ninePixels[3 * i + j] = -1;
                    // Log.d("Nowcast", "getNinePixels Exception: "+e.getLocalizedMessage());
                }
            }
        }

        return ninePixels;
    }

    private static final String precipitationToColorcode(final int precipitation) {
        String colorCode;

        if (precipitation == 80)
            colorCode = "#b40068";
        else if (precipitation == 50)
            colorCode = "#ff2800";
        else if (precipitation == 30)
            colorCode = "#ff9900";
        else if (precipitation == 20)
            colorCode = "#faf500";
        else if (precipitation == 10)
            colorCode = "#0041ff";
        else if (precipitation == 5)
            colorCode = "#218cff";
        else if (precipitation == 1)
            colorCode = "#c0ffff";
        else if (precipitation == 0)
            colorCode = "#000000";
        else
            colorCode = "#000000";

        final Matcher m = colorCodePattern.matcher(colorCode);
        if (m.find()) {
            return colorCode;
        } else {
            final String[] colorCodes = {"red", "blue", "green", "black", "white", "gray", "cyan", "magenta", "yellow", "lightgray", "darkgray", "grey", "lightgrey", "darkgrey", "aqua", "fuschia", "lime", "maroon", "navy", "olive", "purple", "silver", "teal"};
            for (final String cc : colorCodes)
                if (colorCode.equals(cc))
                    return colorCode;
        }

        return "#000000";
    }

    private static final int colorToPrecipitations(final int color) {
        final int red = Color.red(color);
        final int green = Color.green(color);
        final int blue = Color.blue(color);
        if (red == 180 && green == 0 && blue == 104)
            return 80;
        else if (red == 255 && green == 40 && blue == 0)
            return 50;
        else if (red == 255 && green == 153 && blue == 0)
            return 30;
        else if (red == 250 && green == 245 && blue == 0)
            return 20;
        else if (red == 0 && green == 65 && blue == 255)
            return 10;
        else if (red == 33 && green == 140 && blue == 255)
            return 5;
        else if (red == 160 && green == 210 && blue == 255)
            return 1;
        else if (red == 242 && green == 242 && blue == 255)
            return 0;
        else
            return -1;
    }
}
