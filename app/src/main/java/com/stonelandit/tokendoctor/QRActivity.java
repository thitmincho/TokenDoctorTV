package com.stonelandit.tokendoctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stonelandit.tokendoctor.helper.DBManager;
import com.stonelandit.tokendoctor.helper.Helpers;
import com.stonelandit.tokendoctor.helper.LocaleHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


public class QRActivity extends AppCompatActivity {
    private ImageView qrCodeIV;
    private DBManager dbManager;

    private Animation blink_anim;
    private TextView textview;
    private TextView qrScanText;
    private String generatedToken;
    private Button btnMMFont;
    private ServerSocket serverSocket;
    private Handler updateConversationHandler;
    private Thread serverThread = null;
    public static final int SERVERPORT = 5000;

    int lang_selected;
    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar+
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_qractivity);

        btnMMFont = (Button) findViewById(R.id.btnMMFont);
        qrScanText = (TextView) findViewById(R.id.qrScanText);
        // qrCodeIV view element Call
        qrCodeIV = findViewById(R.id.idIVQrcode);
        // Socket Thread Handler start

        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

        dbManager = new DBManager(this);
        dbManager.open();

        generatedToken = Helpers.tokenGenerate();
        Helpers.initSection(generatedToken, dbManager);

        Helpers.showNetworkStatus(this, qrCodeIV, generatedToken);

        textview = findViewById(R.id.textView3);

        btnMMFont.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String[] Language = {"Unicode\t: ဖတ်၍ရသော Font ကို ရွေးချယ်ပါ။", "Zawgyi\t: ဖတ္၍ရေသာ Font ကို ေ႐ြးခ်ယ္ပါ"};;
                final int checkItem;
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(QRActivity.this);
                dialogBuilder.setTitle("Select a Language")
                        .setSingleChoiceItems(Language, lang_selected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0){
                                    context = LocaleHelper.setLocale(QRActivity.this, "en");
                                    resources = context.getResources();
                                    lang_selected = 0;
                                    qrScanText.setText(resources.getString(R.string.scan_qr));
                                }
                                if(i==1){
                                    context = LocaleHelper.setLocale(QRActivity.this, "");
                                    resources = context.getResources();
                                    lang_selected = 1;
                                    qrScanText.setText(resources.getString(R.string.scan_qr));
                                }
                                Helpers.setSelectedLang(lang_selected);
//                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                dialogBuilder.create().show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;
        private PrintWriter out;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

                this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    // read the data sent. We basically ignore it,
                    // stop reading once a blank line is hit. This
                    // blank line signals the end of the client HTTP
                    // headers.

                    int contentLength = -1;
                    while (true) {
                        final String line = input.readLine();
                        final String contentLengthStr = "Content-Length: ";
                        if (line.startsWith(contentLengthStr)) {
                            contentLength = Integer.parseInt(line.substring(contentLengthStr.length()));
                        }

                        if (line.length() == 0) {
                            break;
                        }
                    }
                    final char[] content = new char[contentLength];
                    input.read(content);
                    String payloadData = URLDecoder.decode(new String(content), String.valueOf(StandardCharsets.UTF_8));
                    System.out.println(payloadData);

                    // Send the response
                    // Send the headers
                    out.println("HTTP/1.0 200 OK");
                    out.println("Content-Type: application/json");
                    out.println("Access-Control-Allow-Origin: *");
                    out.println("Access-Control-Allow-Headers: *");

                    out.println("Server: QRCheck");
                    // this blank line signals the end of the headers
                    out.println("");
                    JSONObject parseData = new JSONObject(payloadData);

                    if (Helpers.checkToken(dbManager, parseData.getString("tvToken"), "0") && parseData.has("method") && parseData.getString("method").equals("handshake")) {

                        out.println(Helpers.responseSuccess("Handshake Success"));
                        updateConversationHandler.post(new updateUIThread(payloadData));
                    } else if (Helpers.checkToken(dbManager, parseData.getString("tvToken"), "0")) {
                        out.println(Helpers.responseSuccess("Login Success"));
                    } else {
                        out.println(Helpers.responseFail("Connection Fail"));
                    }

                    out.flush();
                    clientSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            try {
                JSONObject parseData = new JSONObject(this.msg);

                if (Helpers.checkToken(dbManager, parseData.getString("tvToken"), "0")) {
                    System.out.println(parseData.getString("tvToken"));
                    dbManager.update(1, parseData.getString("tvToken"), true);

                    serverSocket.close();
                    finish();
                    Intent homepage = new Intent(QRActivity.this, MainActivity.class);
                    startActivity(homepage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}