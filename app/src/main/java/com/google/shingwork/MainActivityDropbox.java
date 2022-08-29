package com.google.shingwork;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class MainActivityDropbox extends AppCompatActivity {

    private Button btn_login;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dropbox);

        editText = (EditText) findViewById(R.id.accessToken);

        btn_login = (Button)findViewById(R.id.btn_submitAccessToken);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // return intent and send access token
                getIntent().putExtra("dropboxAccessToken", editText.getText().toString());
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new Callback());
        myWebView.loadUrl("https://www.dropbox.com/oauth2/authorize?client_id=1ckve8qcexznt91&redirect_uri=https://eloquent-bhabha-2f9da4.netlify.app/&response_type=token");
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return false;
        }
    }
}