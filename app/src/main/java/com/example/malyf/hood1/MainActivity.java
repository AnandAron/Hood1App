package com.example.malyf.hood1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static com.example.malyf.hood1.MainActivity.tv1;
import static com.example.malyf.hood1.MainActivity.tv2;

public class MainActivity extends AppCompatActivity {
    public static String sip;
    public static String monitorPort;
    public static Context mainCtx;
    public static String relBtnStatus;
    public static Button rel1Btn,rel2Btn,rel3Btn,rel4Btn;
    public static String postPort;
    public static String postIp;
    public static TextView tv1;
    public static TextView tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        sip="192.168.43.217";
        monitorPort="2000";
        mainCtx=this;
        postPort="2002";
        postIp="192.168.43.217";

        rel1Btn=(Button) findViewById(R.id.rel1Btn);
        rel2Btn=(Button) findViewById(R.id.rel2Btn);
        rel3Btn=(Button) findViewById(R.id.rel3Btn);
        rel4Btn=(Button) findViewById(R.id.rel4Btn);

        tv1=(TextView) findViewById(R.id.textView1);
        tv2=(TextView) findViewById(R.id.textView2);
        relBtnStatus="{" +
                "\"rel1\":\"0\","+
                "\"rel2\":\"0\","+
                "\"rel3\":\"0\","+
                "\"rel4\":\"0\""+
                "}";

        Log.i("Oncreate()","RealTimeUpdateTask()");

            new RealTimeUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void postRelStatus(View view) throws JSONException, ExecutionException, InterruptedException {
        JSONObject jsonObject = new JSONObject(relBtnStatus);
        switch (view.getId()){
            case R.id.rel1Btn:
                if(jsonObject.getString("rel1").equals("0")){
                    jsonObject.put("rel1","1");
                }
                else jsonObject.put("rel1","0");
                break;
            case R.id.rel2Btn:
                if(jsonObject.getString("rel2").equals("0")){
                    jsonObject.put("rel2","1");
                }
                else jsonObject.put("rel2","0");
                break;
            case R.id.rel3Btn:
                if(jsonObject.getString("rel3").equals("0")){
                    jsonObject.put("rel3","1");
                }
                else jsonObject.put("rel3","0");
                break;
            case R.id.rel4Btn:
                if(jsonObject.getString("rel4").equals("0")){
                    jsonObject.put("rel4","1");
                }
                else jsonObject.put("rel4","0");
                break;
        }
        relBtnStatus=jsonObject.toString();
        Log.i("relBtnStatus", relBtnStatus);
        Log.i("postRelStatus: ","Calling PostTask...");


        String response=new PostTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        JSONObject responseJson = new JSONObject(response);

        Drawable whiteBg=getResources().getDrawable(R.drawable.white_bg);
        Drawable blueBg=getResources().getDrawable(R.drawable.blue_bg);
        Log.i("postRelStatus: ",responseJson.getString("rel1"));
        Log.i("postRelStatus: ",responseJson.getString("rel2"));
        Log.i("postRelStatus: ",responseJson.getString("rel3"));
        Log.i("postRelStatus: ",responseJson.getString("rel4"));
        Log.i("###############",""+responseJson.getString("rel1").equals("1"));
        if(responseJson.getString("rel1").equals("1")){
            rel1Btn.setBackground(blueBg);
        }else rel1Btn.setBackground(whiteBg);

        if(responseJson.getString("rel2").equals("1")){
            rel2Btn.setBackground(blueBg);
        }else rel2Btn.setBackground(whiteBg);

        if(responseJson.getString("rel3").equals("1")){
            rel3Btn.setBackground(blueBg);
        }else rel3Btn.setBackground(whiteBg);

        if(responseJson.getString("rel4").equals("1")){
            rel4Btn.setBackground(blueBg);
        }else rel4Btn.setBackground(whiteBg);

    }


}
class RealTimeUpdateTask extends AsyncTask<URL,Void,String>{
    private int i=0;
    Exception err;
    String response;
    @Override
    protected String doInBackground(URL... urls) {
        Log.i("RealTimeUpdateTask","doinBackground");
        try{
        while(i<100){
            Log.i("RealTimeUpdateTask","calling UpdateTask");
            try {

                URL url = new URL("http://" + MainActivity.sip + ":" + MainActivity.monitorPort + "/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                String line;
                response="";
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                    Log.e("Response", response);
                }

            } catch (Exception e) {
                err = e;

            }

            i++;
            if(i>=90){i=0;}
            try {
                JSONObject jsonObject= new JSONObject(response);
                tv1.setText(jsonObject.getString("hum"));
                tv2.setText(jsonObject.getString("temp"));
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }}catch (Exception e){e.printStackTrace();}
        return response;
    }

    @Override
    protected void onPostExecute(String s) {
        }
}



class PostTask extends AsyncTask<URL, Void, String> {
    private String response;
    @Override
    protected String doInBackground(URL... urls) {
        Log.i("PostTask", "Entered ");
        try {

            URL url=new URL("http://"+MainActivity.postIp+":"+MainActivity.postPort+"/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream os = conn.getOutputStream();
            os.write(MainActivity.relBtnStatus.getBytes());
            response="";
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                response += line;
                Log.e("Response", response);



            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    protected void onPostExecute(String s) {


    }

}
