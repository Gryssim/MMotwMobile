package com.madev.doug.mmotw;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
    ImageButton dateLeftButton, dateRightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tasks);

        selectedDate = (TextView) findViewById(R.id.selectedDate);
        dateLeftButton = (ImageButton) findViewById(R.id.dateLeftButton);
        dateRightButton = (ImageButton) findViewById(R.id.dateRightButton);

        selectedDate.setText(buildDateString());

        Intent intent = getIntent();

        String userName = intent.getStringExtra(MainActivity.EXTRA_NAME);
        System.out.println(userName);
        userName = userName.substring(1, userName.length() - 1);
        System.out.println(userName);

        final String innerFunctionUserName = userName;

        new populateTasks().execute(userName, selectedDate.getText().toString());

        dateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDate = decrementDate(selectedDate.getText().toString());
                selectedDate.setText(newDate);
                new populateTasks().execute(innerFunctionUserName, newDate);
            }
        });

        dateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDate = incrementDate(selectedDate.getText().toString());
                selectedDate.setText(newDate);
                new populateTasks().execute(innerFunctionUserName, newDate);
            }
        });

    }

    public String buildDateString() {

        return DateFormat.getDateInstance().format(new Date());
    }

    public String decrementDate(String dateTime){
        String result = "";
        StringBuilder sb = new StringBuilder(result);

        String newDates[] = dateTime.split(" ");

        int day = Integer.parseInt(newDates[1].substring(0, newDates[1].length() - 1));
        day--;

        result = sb.append(newDates[0]).append(" ").append(String.valueOf(day)).append(", ").append(newDates[2]).toString();


        return result;
    }

    public String incrementDate(String dateTime){
        String result = "";
        StringBuilder sb = new StringBuilder(result);

        String newDates[] = dateTime.split(" ");

        int day = Integer.parseInt(newDates[1].substring(0, newDates[1].length() - 1));
        day++;

        result = sb.append(newDates[0]).append(" ").append(String.valueOf(day)).append(", ").append(newDates[2]).toString();


        return result;
    }

    public String convertMonthStringToNum(String monthString){

        String result = "";

        switch(monthString){
            case "Jan":  result = "01";
                break;
            case "Feb":  result = "02";
                break;
            case "Mar":  result = "03";
                break;
            case "Apr":  result = "04";
                break;
            case "May":  result = "05";
                break;
            case "Jun":  result = "06";
                break;
            case "Jul":  result = "07";
                break;
            case "Aug":  result = "08";
                break;
            case "Sep":  result = "09";
                break;
            case "Oct":  result = "10";
                break;
            case "Nov":  result = "11";
                break;
            case "Dec":  result = "12";
                break;
        }
        return result;
    }

    public String fixDateTime(String dateTime){
        String result = "";
        StringBuilder sb = new StringBuilder(result);

        String newDates[] = dateTime.split(" ");
        String monthString = convertMonthStringToNum(newDates[0]);

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
            ll.removeAllViewsInLayout();

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