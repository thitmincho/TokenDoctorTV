package com.stonelandit.tokendoctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.stonelandit.tokendoctor.helper.DBManager;
import com.stonelandit.tokendoctor.helper.Helpers;

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
    private String generatedToken;

    private ServerSocket serverSocket;
    private Handler updateConversationHandler;
    private Thread serverThread = null;
    public static final int SERVERPORT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar+
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_qractivity);
        // qrCodeIV view element Call
        qrCodeIV = findViewById(R.id.idIVQrcode);
        // Socket Thread Handler start

        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

        dbManager = new DBManager(this);
        dbManager.open();
//        textView.setText(Helpers.random());
        generatedToken = Helpers.tokenGenerate();
        Helpers.initSection(generatedToken,dbManager);
        Helpers.qrGenerate(this,qrCodeIV,generatedToken);
//        textView.startAnimation(blink_anim);

//Helpers.blinkAmin(textView);
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