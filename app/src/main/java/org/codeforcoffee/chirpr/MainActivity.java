package org.codeforcoffee.chirpr;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static String mAccessToken;
    private ImageView mImage;
    private OkHttpClient mHttp = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // image
        mImage = (ImageView)findViewById(R.id.image);
        // get token
        mAccessToken = getIntent().getStringExtra("accessToken");




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Request request = new Request.Builder()
                        .url("https://api.instagram.com/v1/users/self/media/recent/?access_token="+mAccessToken)
                        .build();

                mHttp.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        String responseBody = response.body().string();
                        try {
                            JSONObject results = new JSONObject(responseBody);
                            JSONArray dataArray = results.getJSONArray("data");
                            JSONObject dataObject = dataArray.getJSONObject(0);
                            Log.d(InstagramLoginActivity.class.getName(),"Data Object: "+dataObject.toString());
                            JSONObject imagesObject = dataObject.getJSONObject("images");
                            JSONObject standardResObject = imagesObject.getJSONObject("standard_resolution");
                            final String imageUrl = standardResObject.getString("url");
                            Log.d(InstagramLoginActivity.class.getName(),"Image URL: "+standardResObject.getString("url"));

                            MainActivity.this.runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    Picasso.with(MainActivity.this).load(imageUrl).into(mImage);                        }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Snackbar.make(view, "Fetching most recent photo...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
