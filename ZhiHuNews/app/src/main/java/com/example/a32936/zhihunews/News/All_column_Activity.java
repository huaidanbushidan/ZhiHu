package com.example.a32936.zhihunews.News;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.a32936.zhihunews.Adapter.Column_newsAdapter;
import com.example.a32936.zhihunews.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class All_column_Activity extends AppCompatActivity {
    public static final int GET_DATA_SUCCESS = 1;
    private String id;
    private String url;
    private String thumbnail;
    private String title;
    private StringBuilder response;
    private String description;
    private String name;
    private int flag;
    List<Map<String,Object>> list=new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Column_newsAdapter column_newsAdapter =new Column_newsAdapter(this,list);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_column_);
        recyclerView = findViewById(R.id.recycleview);
        CreateThread();
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.SwipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.clear();
                flag = 0;
                CreateThread();
                column_newsAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
//                if (flag!=0) {
//                    Toast.makeText(All_column_Activity.this, "刷新成功", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(All_column_Activity.this, "刷新失败", Toast.LENGTH_SHORT).show();
//                }
            }
        });


    }


    public void CreateThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url_1 = new URL("https://news-at.zhihu.com/api/4/sections");
                    connection = (HttpURLConnection) url_1.openConnection();
                    connection.setRequestMethod("GET");
//                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    //读取输入流
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.d("hhh",response.toString());
                    flag++;          //用于判断刷新是否成功
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("response",response.toString());
                    message.setData(bundle);
                    message.what = GET_DATA_SUCCESS;
                    handler.sendMessage(message);

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (reader != null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_DATA_SUCCESS:
                    praseJSONWithJSONObject(response.toString());
                    break;
                default:
                    break;

            }


        }
    };


    private void praseJSONWithJSONObject(String JsonData){
        try{
            JSONObject jsonObject = new JSONObject(JsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0;i<jsonArray.length();i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                description = jsonObject1.getString("description");
                id = jsonObject1.getString("id");
                thumbnail = jsonObject1.getString("thumbnail");
                name = jsonObject1.getString("name");
                Map map=new HashMap();

                map.put("id", id);
                map.put("thumbnail", thumbnail);
                map.put("name", name);
                map.put("description", description);
                list.add(map);

                LinearLayoutManager manager=new LinearLayoutManager(this);

                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(column_newsAdapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}//栏目总览解析！！！
