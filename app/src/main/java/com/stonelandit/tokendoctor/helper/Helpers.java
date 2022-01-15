package com.stonelandit.tokendoctor.helper;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.stonelandit.tokendoctor.R;

import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Helpers {

    private static final String ALLOWED_CHARACTERS = "0123456789QWERTYUIOPASDFGHJKLZXCVBNM";
    private static final int MAX_LENGTH = 10;
    private static QRGEncoder qrgEncoder;
    private static Bitmap bitmap;
    public static String tokenGenerate() {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(MAX_LENGTH);
        for (int i = 0; i < MAX_LENGTH; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public static String IpAddress(Context cont) {
        Context context = cont.getApplicationContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public static boolean checkToken(DBManager dbManager, String token, String used) {
        if (dbManager.get("1").getString(1).equals(token) && dbManager.get("1").getString(2).equals(used)) {
            return true;
        } else {
            return false;
        }
    }

    public static void initSection(String generatedToken, DBManager dbManager) {
        dbManager.truncate();
//        generatedToken = "ABCDEFG";
        dbManager.insert(generatedToken, false);
    }

    public static void qrGenerate(Context context,ImageView qrCodeIV,String generatedToken) {
        // below line is for getting
        // the windowmanager service.
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder("{\n" +
                "    \"ip\": \"" + Helpers.IpAddress(context) + "\",\n" +
                "    \"port\": \"5000\",\n" +
                "    \"tvToken\": \"" + generatedToken + "\"\n" +
                "}", null, QRGContents.Type.TEXT, dimen);
        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder.encodeAsBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            qrCodeIV.setImageBitmap(bitmap);
        } catch (WriterException e) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static void blinkAnim(TextView textView,int primaryBgColor,int secondaryBgColor,int primaryTxtColor,int secondaryTxtColor, int delayMillis) {

        ObjectAnimator bgAnim = ObjectAnimator.ofInt(textView, "backgroundColor", primaryBgColor, secondaryBgColor,primaryBgColor);
        ObjectAnimator textAnim = ObjectAnimator.ofInt(textView, "textColor", primaryTxtColor, secondaryTxtColor,primaryTxtColor);

        animStart(bgAnim).start();
        animStart(textAnim).start();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bgAnim.end();
                textAnim.end();
                textView.setBackgroundColor(primaryBgColor);
                textView.setTextColor(primaryTxtColor);
            }
        }, delayMillis);
    }

    private static ObjectAnimator animStart(ObjectAnimator animator) {

        // duration of one color
        animator.setDuration(2000);
        animator.setEvaluator(new ArgbEvaluator());
        // color will be show in reverse manner
        animator.setRepeatCount(Animation.REVERSE);
        // It will be repeated up to infinite time
        animator.setRepeatCount(Animation.INFINITE);
        return animator;
    }

    public static String responseSuccess(String message) {
        return "{\n" +
                "    \"status\": \"success\",\n" +
                "    \"message\": \"" + message + "\"\n" +
                "}";
    }

    public static String responseFail(String message) {
        return "{\n" +
                "    \"status\": \"fail\",\n" +
                "    \"message\": \"" + message + "\"\n" +
                "}";
    }
}
