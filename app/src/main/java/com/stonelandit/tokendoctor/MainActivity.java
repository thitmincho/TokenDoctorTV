package com.stonelandit.tokendoctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import java.util.Date;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.stonelandit.tokendoctor.Interface.TVInterface;
import com.stonelandit.tokendoctor.helper.DBManager;
import com.stonelandit.tokendoctor.helper.Helpers;
import com.stonelandit.tokendoctor.helper.LocaleHelper;
import com.stonelandit.tokendoctor.helper.RetrofitInstance;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout layout;
    private LinearLayout qrView;
    private LinearLayout tokenInfoView;
    private FrameLayout idIVQrcodeLayout;

    private ImageView qrCodeIV;
    private TextView connectionText;
    private TextView currentTokenLabel;
    private TextView nextTokenLabel;
    private TextView dateDisplay;
    private TextView infoText;
    private TextView currentDoctor;
    private TextView currentToken;
    private TextView nextToken;

    private YouTubePlayerView youTubePlayerView;
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 5000;

    private DBManager dbManager;
    private String generatedToken;
    Context context;
    Resources resources;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        layout = new ConstraintLayout(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar+
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_main);

        dbManager = new DBManager(this);
        dbManager.open();

        // View elements
        qrCodeIV = findViewById(R.id.idIVQrcode);
        qrView = (LinearLayout) findViewById(R.id.qrView);
        tokenInfoView = (LinearLayout) findViewById(R.id.tokenInfoView);
        idIVQrcodeLayout = (FrameLayout) findViewById(R.id.idIVQrcodeLayout);
        connectionText = (TextView) findViewById(R.id.connectionText);
        currentTokenLabel = (TextView) findViewById(R.id.currentTokenLabel);
        nextTokenLabel = (TextView) findViewById(R.id.nextTokenLabel);
        currentDoctor = (TextView) findViewById(R.id.current_doctor);
        currentToken = (TextView) findViewById(R.id.current_token);
        nextToken = (TextView) findViewById(R.id.next_token);
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        dateDisplay = (TextView) findViewById(R.id.textDate);
        infoText = (TextView) findViewById(R.id.infoText);
        qrView.setVisibility(View.INVISIBLE);
//        connectionText.setVisibility(View.INVISIBLE);
//        idIVQrcodeLayout.setVisibility(View.INVISIBLE);

        // Data Display
        SimpleDateFormat sdf = new SimpleDateFormat("EEE / dd MMM h:ma");
        String currentDateandTime = sdf.format(new Date());
        dateDisplay.setText(currentDateandTime);

        // Socket Thread Handler start
        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

        if(Helpers.getSelectedLang()==0){
            context = LocaleHelper.setLocale(MainActivity.this, "en");
            resources = context.getResources();
            currentTokenLabel.setText(resources.getString(R.string.current_token_label_text));
            nextTokenLabel.setText(resources.getString(R.string.next_token_label_text));
            infoText.setText(resources.getString(R.string.info_text));
        }else{
            context = LocaleHelper.setLocale(MainActivity.this, "");
            resources = context.getResources();
            currentTokenLabel.setText(resources.getString(R.string.current_token_label_text));
            nextTokenLabel.setText(resources.getString(R.string.next_token_label_text));
            infoText.setText(resources.getString(R.string.info_text));
        }
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

    private void hideShowTokenInfos(int status) {
        tokenInfoView.setVisibility(status);
//        currentTokenLabel.setVisibility(status);
//        currentToken.setVisibility(status);
//        nextTokenLabel.setVisibility(status);
//        nextToken.setVisibility(status);
//        dateDisplay.setVisibility(status);
        currentDoctor.setVisibility(status);
        infoText.setVisibility(status);
        if (status == View.VISIBLE) {
            qrView.setVisibility(View.INVISIBLE);
//            connectionText.setVisibility(View.INVISIBLE);
//            idIVQrcodeLayout.setVisibility(View.INVISIBLE);
        } else {
            qrView.setVisibility(View.VISIBLE);
//            connectionText.setVisibility(View.VISIBLE);
//            idIVQrcodeLayout.setVisibility(View.VISIBLE);
        }
    }

    private void changeInfos() {
        currentDoctor.setText("Nil");
        currentToken.setText("Nil");
        nextToken.setText("Nil");
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

                out = new PrintWriter(this.clientSocket.getOutputStream());

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
                    System.out.println(payloadData);// Send the response
                    // Send the headers
                    out.println("HTTP/1.0 200 OK");
                    out.println("Content-Type: application/json");
                    out.println("Access-Control-Allow-Origin: *");
                    out.println("Server: TVScreen");
                    // this blank line signals the end of the headers
                    out.println("");

                    JSONObject parseData = new JSONObject(payloadData);
                    if (Helpers.checkToken(dbManager, parseData.getString("tvToken"), "1") && parseData.getString("method").equals("handshake")) {
                        out.println(Helpers.responseSuccess("Handshake Success"));
//                        updateConversationHandler.post(new updateUIThread(payloadData));
                    } else if (Helpers.checkToken(dbManager, parseData.getString("tvToken"), "1") && parseData.getString("method").equals("tv-change")) {
                        out.println(Helpers.responseSuccess("Data Send Success"));
                        updateConversationHandler.post(new updateUIThread(payloadData));
                    } else if (Helpers.checkToken(dbManager, parseData.getString("tvToken"), "1") && parseData.getString("method").equals("disconnect")) {
                        out.println(Helpers.responseSuccess("Disconnect Success"));
                        updateConversationHandler.post(new updateUIThread(payloadData));
                    } else if (Helpers.checkToken(dbManager, parseData.getString("tvToken"), "0")) {
                        out.println(Helpers.responseSuccess("Login Success"));
                        updateConversationHandler.post(new updateUIThread(payloadData));
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
                if (parseData.getString("method").equals("tv-change")) {
                    JSONObject dataValues = parseData.getJSONObject("data");

                    String current = "";
                    String next = "";

                    if (dataValues.has("currentToken")) {
                        current = dataValues.getJSONObject("currentToken").getString("token");
                    }
                    if (dataValues.has("nextToken")) {
                        next = dataValues.getJSONObject("nextToken").getString("token");
                    }

                    if (!current.equals(currentToken.getText())) {

                        Helpers.blinkAnim(currentToken, Color.WHITE, Color.parseColor("#5271FF"), Color.parseColor("#7DD957"), Color.WHITE, 30000);
                    }
                    if (!next.equals(nextToken.getText())) {

                        Helpers.blinkAnim(nextToken, Color.WHITE, Color.parseColor("#CFCECE"), Color.parseColor("#FF1616"), Color.WHITE, 30000);
                    }
                    currentDoctor.setText(dataValues.getJSONObject("currentDoctor").getString("doctor_name"));
//                    currentToken.setText(curret);
//                    nextToken.setText(next);
                    currentToken.setText(current);
                    nextToken.setText(next);


                    getTvResponse(parseData.getString("accessToken"));
                } else if (parseData.getString("method").equals("disconnect")) {
                    System.out.println("Disconnecting");
                    dbManager.delete(1);

                    hideShowTokenInfos(View.INVISIBLE);
                    changeInfos();
                    generatedToken = Helpers.tokenGenerate();
                    Helpers.initSection(generatedToken, dbManager);
                    Helpers.qrGenerate(getBaseContext(), qrCodeIV, generatedToken);
                } else {
                    hideShowTokenInfos(View.VISIBLE);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getTvResponse(String token) {
        TVInterface api = RetrofitInstance.getRetrofitInstance(token).create(TVInterface.class);

        Call<String> call = api.getString();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", response.body().toString());

                        String jsonresponse = response.body().toString();
                        System.out.println(jsonresponse);
                        writeTv(jsonresponse);

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }


    private int play = 0;

    private void writeTv(String response) {
        try {

            //getting the whole json object from the response
            JSONObject obj = new JSONObject(response);
            if (obj.optString("status").equals("true")) {

                JSONArray dataArray = obj.getJSONObject("data").getJSONArray("banner");

                getLifecycle().addObserver(youTubePlayerView);
                youTubePlayerView.initialize(new YouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        try {
                            youTubePlayer.loadVideo(dataArray.getJSONObject(play).getString("youtube_id"), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState playerState) {
                        if (playerState.toString().equals("ENDED")) {
                            play++;
                            if (play < dataArray.length()) {
                                System.out.println("============================================================================" + play);
                            } else {
                                play = 0;
                            }

                            youTubePlayerView.refreshDrawableState();

                            try {
                                youTubePlayer.loadVideo(dataArray.getJSONObject(play).getString("youtube_id"), 0);
                            } catch (Exception e) {
                            }

                        }
                    }

                    @Override
                    public void onPlaybackQualityChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackQuality playbackQuality) {
                    }

                    @Override
                    public void onPlaybackRateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackRate playbackRate) {
                    }

                    @Override
                    public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError playerError) {
                    }

                    @Override
                    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float v) {
                    }

                    @Override
                    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float v) {
                    }

                    @Override
                    public void onVideoLoadedFraction(@NonNull YouTubePlayer youTubePlayer, float v) {
                    }

                    @Override
                    public void onVideoId(@NonNull YouTubePlayer youTubePlayer, @NonNull String s) {
                    }

                    @Override
                    public void onApiChange(@NonNull YouTubePlayer youTubePlayer) {
                    }
                });

                System.out.println(play);
//
            } else {
//                Toast.makeText(QRActivity.this, obj.optString("message")+"", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}