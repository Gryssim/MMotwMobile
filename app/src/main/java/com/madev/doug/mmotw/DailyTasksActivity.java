package com.madev.doug.mmotw;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;


public class DailyTasksActivity extends AppCompatActivity {

    TextView selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tasks);

        selectedDate = (TextView) findViewById(R.id.selectedDate);

        selectedDate.setText(buildDateString());

        Intent intent = getIntent();

        String userName = intent.getStringExtra(MainActivity.EXTRA_NAME);
        System.out.println(userName);
        userName = userName.substring(1, userName.length() - 1);
        System.out.println(userName);

        new populateTasks().execute(userName, selectedDate.getText().toString());

    }

    public String buildDateString() {

        String result = DateFormat.getDateInstance().format(new Date());

        return result;
    }

    public String fixDateTime(String dateTime){
        String result = "";
        StringBuilder sb = new StringBuilder(result);

        String newDates[] = dateTime.split(" ");
        String monthString ="";


        switch(newDates[0]){
            case "Jan":  monthString = "01";
                         break;
            case "Feb":  monthString = "02";
                         break;
            case "Mar":  monthString = "03";
                         break;
            case "Apr":  monthString = "04";
                         break;
            case "May":  monthString = "05";
                         break;
            case "Jun":  monthString = "06";
                         break;
            case "Jul":  monthString = "07";
                         break;
            case "Aug":  monthString = "08";
                         break;
            case "Sep":  monthString = "09";
                         break;
            case "Oct":  monthString = "10";
                         break;
            case "Nov":  monthString = "11";
                         break;
            case "Dec":  monthString = "12";
                         break;
        }

        result = sb.append(newDates[2].substring(2)).append("-").append(monthString).append("-").append(newDates[1].substring(0, 2)).toString();

        return result;
    }

    public class populateTasks extends AsyncTask<String, Void, String> {
        protected void onPreExecuite(){

        }

        protected String doInBackground(String... args) {
            String userName = args[0];
            String dateTime = fixDateTime(args[1]);
            try {
                URL url = new URL("http://192.168.1.14/mmotw/API/apiGetUserTask.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("user_name", userName);
                postDataParams.put("date_time", dateTime);
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

            TextView test = new TextView(getApplicationContext());

            JsonArray jArray = parser.parse(result).getAsJsonArray();

            LinearLayout ll = (LinearLayout)findViewById(R.id.linearLayout1);

            for(int i = 0; i < jArray.size(); i++){
                CheckBox cb = new CheckBox(getApplicationContext());
                JsonObject obj;
                obj = jArray.get(i).getAsJsonObject();
                String task = obj.get("task_title").toString().substring(1, 2).toUpperCase() +
                        obj.get("task_title").toString().substring(2, obj.get("task_title").toString().length()-1)
                        + "\n" + obj.get("task_description").toString().substring(1, 2).toUpperCase() +
                        obj.get("task_description").toString().substring(2, obj.get("task_description").toString().length()-1);
                cb.setText(task);
                cb.setId(i+10);
                cb.setPadding(0, 10, 0, 10);
                ll.addView(cb);
            }

        }
    }


    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {
            String key = itr.next();
            Object value = params.get(key);

            if (first)
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