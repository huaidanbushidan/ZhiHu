package com.example.a32936.zhihunews.News;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.a32936.zhihunews.Adapter.NewsAdapter;
import com.example.a32936.zhihunews.R;
import com.example.a32936.zhihunews.SQLiteDatabase.Edit_informationActivity;
import com.example.a32936.zhihunews.SQLiteDatabase.Like_Activity;
import com.example.a32936.zhihunews.SQLiteDatabase.Login_Activity;
import com.example.a32936.zhihunews.SQLiteDatabase.MyDatabaseHelper;

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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
        private ImageView imageView;
        private String id;
        private String url;
        private String thumbnail;
        private String title;
        private MyDatabaseHelper dbHelper;
        private int flag;
        private SwipeRefreshLayout swipeRefreshLayout;
        List<Map<String,Object>> list=new ArrayList<>();
        private StringBuilder response;
        public static final int GET_DATA_SUCCESS = 1;
        private RecyclerView recyclerView;
        private NewsAdapter newsAdapter=new NewsAdapter(this,list);   //改成全局变量方便刷新数据时调用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new MyDatabaseHelper(this);               //显示头像时调用数据库

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.inflateHeaderView(R.layout.nav_header_main);
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        View navHeaderView = navigationView.getHeaderView(0);
        ImageView imageView = navHeaderView.findViewById(R.id.imageView);
        recyclerView = findViewById(R.id.recycleview);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.SwipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                list.clear();
                flag = 0;
                CreateThread();
                newsAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
//                if (flag!= 0){
//                Toast.makeText(MainActivity.this,"刷新成功",Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(MainActivity.this,"刷新失败",Toast.LENGTH_SHORT).show();
//                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);


        CreateThread();
//        recyclerView.setLayoutManager(linearLayoutManager);
//        NewsAdapter newsAdapter = new NewsAdapter(this,list);
//        recyclerView.setAdapter(newsAdapter);

        String Uers_name;
        byte[] head_image=null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (Login_Activity.signer != null){
        Cursor cursor = db.query("User",new String[]{"UserCount","Head_image","UserName"},"UserCount = ?",new String[]{Login_Activity.signer},null,null,"id");
        if (cursor.moveToFirst()) {
            do {
                 Uers_name = cursor.getString(cursor.getColumnIndex("UserCount"));
                head_image = cursor.getBlob(cursor.getColumnIndex("Head_image"));

//                if (Login_Activity.signer == null){
//                    imageView.setImageResource(R.drawable.logo);
//                }else {
//                    imageView.setImageBitmap(Edit_informationActivity.bitmap);
//                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
            if (new String(head_image).equals("1")) {
                imageView.setImageResource(R.drawable.logo);
            } else {
                Bitmap bitmap = Bytes2Bimap(head_image);
                imageView.setImageBitmap(bitmap);
            }
        }else imageView.setImageResource(R.drawable.logo);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Login_Activity.signer==null){
                    Intent intent = new Intent(MainActivity.this,Login_Activity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(MainActivity.this, Edit_informationActivity.class);
                    startActivity(intent);
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            // Handle the camera action
        } else if (id == R.id.nav_like) {
            if (Login_Activity.signer!=null){
            Intent intent = new Intent(MainActivity.this,Like_Activity.class);
            startActivity(intent);
            }else{
                Toast.makeText(MainActivity.this,"请先登录",Toast.LENGTH_SHORT).show();
            }
        }else if (id == R.id.nav_column){
            Intent intent = new Intent(MainActivity.this,All_column_Activity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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


    public void CreateThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url_1 = new URL("https://news-at.zhihu.com/api/4/news/hot");
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
                    flag++;                 //用于判断刷新成功与否
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


    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                praseJSONWithJSONObject(response);

            }
        });
    }

    private void praseJSONWithJSONObject(String JsonData){
        try{
            JSONObject jsonObject = new JSONObject(JsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("recent");
            for (int i = 0;i<jsonArray.length();i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                id = jsonObject1.getString("news_id");
                url = jsonObject1.getString("url");
                thumbnail = jsonObject1.getString("thumbnail");
                title = jsonObject1.getString("title");
                Map map=new HashMap();

                map.put("id",id);
                map.put("url",url);
                map.put("title",title);
                map.put("thumbnail",thumbnail);
                list.add(map);

                LinearLayoutManager manager=new LinearLayoutManager(this);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(newsAdapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
