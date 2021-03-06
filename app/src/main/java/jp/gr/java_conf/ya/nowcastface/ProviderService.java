package jp.gr.java_conf.ya.nowcastface; // Copyright (c) 2017 YA <ya.androidapp@gmail.com> All rights reserved. This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;
import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.Network;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

//import static android.graphics.drawable.Icon.createWithBitmap;

/**
 * 降水ナウキャストの情報を取得し、ウォッチフェイスに提供する
 */

public class ProviderService extends ComplicationProviderService {
    private ConnectivityManager mConnectivityManager;
    private final int MIN_BANDWIDTH_KBPS = 320;

    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        // ネットワーク接続状態確認
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final Network activeNetwork = mConnectivityManager.getActiveNetwork();

        // 出力内容
        ComplicationData complicationData = null;

        if (activeNetwork == null) {
            // ネットワークにアクセスできない
            complicationData = new ComplicationData.Builder(ComplicationData.TYPE_LONG_TEXT)
                    .setLongTitle(ComplicationText.plainText(getApplicationContext().getString(R.string.app_name)))
                    .setLongText(ComplicationText.plainText(getApplicationContext().getString(R.string.network_not_available)))
                    .build();
        } else {
            // TODO: GPSから情報取得
            final Double latitude = 35.681382, longitude = 139.766084;

            // 情報取得先の画像ファイルURLを取得
            final String[] nowcastImageUrlParts = Nowcast.getNowcastImageUrl(latitude, longitude);

            // 降水予報値の配列を取得
            final int[] precipitations = Nowcast.getNowcastPrecipitations(nowcastImageUrlParts);

            // アイコンを描画
            // final Bitmap resultIcon = Nowcast.drawPrecipitationsIcon(precipitations);

            // 降水予報値の文字列
            final String resultString = Nowcast.getNowcastString(precipitations);
            // Log.d("Nowcast", "resultString: "+resultString);

            // 提供内容の整形
            // if(dataType == ComplicationData.TYPE_ICON) {
            // complicationData = new ComplicationData.Builder(ComplicationData.TYPE_ICON)
            // .setIcon(createWithBitmap(resultIcon))
            // .build();
            // complicationManager.updateComplicationData(complicationId, complicationData);
            // } else
            if (dataType == ComplicationData.TYPE_LONG_TEXT) {
                complicationData = new ComplicationData.Builder(ComplicationData.TYPE_LONG_TEXT)
                        .setLongTitle(ComplicationText.plainText(getApplicationContext().getString(R.string.app_name)))
                        .setLongText(ComplicationText.plainText(resultString))
                        .build();
            } else if (dataType == ComplicationData.TYPE_SHORT_TEXT) {
                complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                        .setShortText(ComplicationText.plainText(resultString))
                        .build();
            } else {
                return;
            }
        }

        // Complicationsへ更新
        complicationManager.updateComplicationData(complicationId, complicationData);
    }
}
