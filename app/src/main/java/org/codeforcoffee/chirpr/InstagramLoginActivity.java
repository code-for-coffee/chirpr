package org.codeforcoffee.chirpr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InstagramLoginActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_login);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")    // method signature trips false api check
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("code=")){
                    System.out.println(url);
                    int index = url.indexOf("=");
                    System.out.println(url.substring(index+1));
                    String code = url.substring(index+1);
                    getAccessToken(code);
                    return true;
                }else {
                    return false;
                }
            }
        });
        mWebView.loadUrl("https://instagram.com/oauth/authorize/?client_id="+ InstagramIdentity.CLIENT_ID +"&redirect_uri="+ InstagramIdentity.CALLBACK_URL  +"&response_type=code&scope=public_content");
    }

    private void getAccessToken(String code){
        Log.d(InstagramLoginActivity.class.getName(),"Trying to get access token");
        final OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("client_secret", InstagramIdentity.CLIENT_SECRET)
                .add("client_id",InstagramIdentity.CLIENT_ID)
                .add("grant_type","authorization_code")
                .add("redirect_uri", InstagramIdentity.CALLBACK_URL)
                .add("code",code)
                .build();

        Request request = new Request.Builder()
                .url("https://api.instagram.com/oauth/access_token")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                String responseBody = response.body().string();
                Log.d(InstagramLoginActivity.class.getName(),responseBody);
                try {
                    JSONObject result = new JSONObject(responseBody);
                    JSONObject user = result.getJSONObject("user");

                    Intent intent = new Intent(InstagramLoginActivity.this,MainActivity.class);
                    intent.putExtra("accessToken",result.getString("access_token"));
                    intent.putExtra("userId",user.getString("id"));
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
