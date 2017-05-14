package jp.gr.java_conf.ya.nowcastface; // Copyright (c) 2017 YA <ya.androidapp@gmail.com> All rights reserved. This software includes the work that is distributed in the Apache License 2.0

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
// import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtil {

    public static final Bitmap getBitmapFromWeb(String uri) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        Bitmap bmp = null;
        try {
            URLConnection conn = new URL(uri).openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            final BufferedInputStream bis = new BufferedInputStream(is);
            bmp = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            bmp = null;
            // Log.d("Nowcast", "IOException: "+e.getLocalizedMessage());
        } catch (Exception e) {
            bmp = null;
            // Log.d("Nowcast", "Exception: "+e.getLocalizedMessage());
        }

        return bmp;
    }
}
