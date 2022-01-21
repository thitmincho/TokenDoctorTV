package com.stonelandit.tokendoctor.helper;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Helpers {

    private static final String ALLOWED_CHARACTERS = "0123456789QWERTYUIOPASDFGHJKLZXCVBNM";
    private static final int MAX_LENGTH = 10;
    private static QRGEncoder qrgEncoder;
    private static Bitmap bitmap;

    private static AlertDialog.Builder builder;
    private static int selectedLang;

    public static void setSelectedLang(int selectedLang) {
        Helpers.selectedLang = selectedLang;
    }

    public static int getSelectedLang() {
        return selectedLang;
    }

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

    public static boolean checkIP(Context context) {
        if (Helpers.IpAddress(context).equals("0.0.0.0") || Helpers.IpAddress(context).equals("127.0.0.1")) {
            return false;
        }
        return true;
    }

    public static void showNetworkStatus(Context context, ImageView qrCodeIV, String generatedToken) {
        builder = new AlertDialog.Builder(context);
        if (!Helpers.checkIP(context)) {
//            builder.setMessage("No Internet Connect") .setTitle("Network Status");

            //Setting message manually and performing action on button click
            builder.setMessage("Wifi ချိတ်ဆက်ပီးလျှင် Refresh ပြန်နှိပ်ပါ။")
                    .setCancelable(false)
                    .setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            if (!Helpers.checkIP(context)) {

                                showNetworkStatus(context, qrCodeIV, generatedToken);
                            }
                            Helpers.qrGenerate(context, qrCodeIV, generatedToken);
                        }
                    });
            //Creating dialog box
            AlertDialog alert = builder.create();
            //Setting the title manually
            alert.setTitle("Wifi မချိတ်ဆက်ထားပါ");

            alert.show();
        } else {
            Helpers.qrGenerate(context, qrCodeIV, generatedToken);
        }
    }

    public static void qrGenerate(Context context, ImageView qrCodeIV, String generatedToken) {
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
        System.out.println(cm.getActiveNetworkInfo());
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static void blinkAnim(TextView textView, int primaryBgColor, int secondaryBgColor, int primaryTxtColor, int secondaryTxtColor, int delayMillis) {

        ObjectAnimator textAnim = ObjectAnimator.ofInt(textView, "textColor", primaryTxtColor, secondaryTxtColor, primaryTxtColor);

        GradientDrawable gd = (GradientDrawable) textView.getBackground();

        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), primaryBgColor, secondaryBgColor, primaryBgColor);
        animator.setRepeatCount(Animation.REVERSE);
        animator.setRepeatCount(Animation.INFINITE);
        animator.setDuration(2000).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                gd.setColor(value);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animStart(textAnim), animator);
        set.start();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                set.end();
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
