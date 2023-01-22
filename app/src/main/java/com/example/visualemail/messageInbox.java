package com.example.visualemail;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Locale;

import io.paperdb.Paper;

public class messageInbox extends AppCompatActivity {

    private static final int REQ_CODE = 1;
    SwipeRefreshLayout refreshLayout;
    String webURL = "https://mail.google.com/";
    private Button assistantButton;
    private Intent intent;
    WebView web_view;
    String cookies = "";
    private TextToSpeech textToSpeech;
    boolean logout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_inbox);
        refreshLayout = findViewById(R.id.swipe);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data...");
        progressDialog.setCancelable(false);
        web_view = findViewById(R.id.web_view);
        assistantButton = findViewById(R.id.assist);
        String userEmail = "";
        String userPassword = "Paper.book().read(Prevalent.password)";
        cookies = CookieManager.getInstance().getCookie(webURL);
        textToSpeech = new TextToSpeech(messageInbox.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result;
                result = textToSpeech.setLanguage(new Locale("en", "in"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported");
                    textToSpeech.setLanguage(new Locale("en", "us"));
                } else {
                    textToSpeech.setLanguage(new Locale("en", "in"));
                }

            }
        });

        assistantButton.setOnClickListener(view -> {
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            try {
                startActivityForResult(intent, REQ_CODE);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        web_view.requestFocus();

        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        web_view.getSettings().setDomStorageEnabled(true);
        web_view.getSettings().setUseWideViewPort(true);
        web_view.setWebChromeClient(new WebChromeClient());


        web_view.getSettings().setAppCachePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/cache");
        web_view.getSettings().setDatabasePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/databases");

        web_view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                 /*    javaScript = "javascript:(function() {var userNameField = document.getElementById('identifierId');" +
                        "            userNameField.value = 'safwan.naqvi@gmail.com';" +
                        "            const myNodeList = document.querySelectorAll('button');" +
                        "            myNodeList[2].click();})()";

                javaScript = "javascript:(function work(){const myNodeList = document.querySelector('input[name='password']');}" +
                        "function main() {" +
                        "var userNameField = document.getElementById('identifierId');" +
                        "userNameField.value = 'eyetesting69@gmail.com';" +
                        "const myNodeList = document.querySelectorAll('button');" +
                        "myNodeList[2].click();" +
                        "setTimeout(function(){work},3000);})main()";
*/
                web_view.loadUrl("javascript:setTimeout(function(){const myNodeList = document.querySelectorAll('a.button');myNodeList[1].click()},4000);");
                web_view.loadUrl("javascript:setTimeout(function(){var userNameField = document.getElementById('identifierId');userNameField.value = '" + userEmail + "';const myNodeList = document.querySelectorAll('button');myNodeList[2].click();},6000);");
                web_view.loadUrl("javascript:setTimeout(function(){const myNodeList = document.querySelector('input[name=" + '"' + "password" + '"' + "]');myNodeList.value = '" + userPassword + "';const myNodeList2 = document.querySelectorAll('button');myNodeList2[1].click();},8000);");
                textToSpeech.speak("Loading", TextToSpeech.QUEUE_FLUSH, null, null);
            }

        });

        web_view.loadUrl(webURL);

        web_view.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (!logout) {
                    if (progress < 100) {
                        progressDialog.show();
                    }
                    if (progress == 100) {
                        progressDialog.dismiss();
                    }

                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                web_view.reload();

                refreshLayout.setRefreshing(false);
            }
        });

        web_view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction()
                        == MotionEvent.ACTION_UP && web_view.canGoBack()) {
                    //handler.sendEmptyMessage(1);
                    web_view.goBack();
                    return true;
                }

                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String input = result.get(0);
                    String search = input.toLowerCase().trim();
                    if (containsWords(search, new String[]{"compose"}) || containsWords(search, new String[]{"send"})) {
                        Intent intent = new Intent(messageInbox.this, messageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        //finish(); Will see if requires to finish or not
                    } else if (containsWords(search, new String[]{"logout"}) || containsWords(search, new String[]{"log out"})) {
                        clearCookies();
                        logout = true;
                        Intent intent = new Intent(messageInbox.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
                break;
        }

    }

    public static boolean containsWords(String inputString, String[] items) {
        boolean found = true;
        for (String item : items) {
            if (!inputString.contains(item)) {
                found = false;
                break;
            }
        }
        Log.i("app", String.valueOf(found));
        return found;
    }


    @Override
    public void onDestroy() {
        //Don't forget to shut down text to speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        Paper.book().destroy();
        super.onDestroy();
    }

    public void clearCookies() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }
}