package com.madev.doug.mmotw;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_NAME = "com.madev.doug.mmotw.daily";

    EditText userNameText, userPasswordText;
    Button testButton;
    Boolean Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        userNameText = (EditText) findViewById(R.id.userNameText);
        userPasswordText = (EditText) findViewById(R.id.userPasswordText);

        testButton = (Button) findViewById(R.id.testButton);

        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String userName = userNameText.getText().toString();
                String userPassword = userPasswordText.getText().toString();

                new userLogin().execute(userName, userPassword);
            }
        });
    }

    public class userLogin extends AsyncTask<String, Void, String>{
        protected void onPreExecuite(){

        }

        protected String doInBackground(String... args) {
            String userName = args[0];
            String userPassword = args[1];
            try {
                URL url = new URL("http://192.168.1.14/mmotw/API/apilogin.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("user_name", userName);
                postDataParams.put("password", userPassword);
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* ms */);
                conn.setConnectTimeout(15000 /* ms */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK){
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";
                    while((line = in.readLine()) != null){
                        sb.append(line);
                        break;
                    }
                    in.close();
                    return sb.toString();
                } else {
                    return "false: " + responseCode;
                }

            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result){
            JsonParser parser = new JsonParser();

            JsonObject obj = parser.parse(result).getAsJsonObject();

            System.out.println(obj.get("auth").toString());

            if(Boolean.parseBoolean(obj.get("auth").toString())){
                Toast.makeText(getApplicationContext(),"Success!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), DailyTasksActivity.class);
                String userName = obj.get("user_name").toString();
                intent.putExtra(EXTRA_NAME, userName);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Incorrect User Name or Password!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){
            String key = itr.next();
            Object value = params.get(key);

            if(first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();

    }
}

