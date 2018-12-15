package com.example.a32936.zhihunews.SQLiteDatabase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.a32936.zhihunews.Adapter.Column_newsAdapter;
import com.example.a32936.zhihunews.Adapter.NewsAdapter;
import com.example.a32936.zhihunews.News.MainActivity;
import com.example.a32936.zhihunews.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Like_Activity extends AppCompatActivity {
    List<Map<String,Object>> list=new ArrayList<>();
    List<Map<String,Object>> list_1=new ArrayList<>();
    private StringBuilder response;
    private String thumbnail;
    private String name;
    private String description;
    private String id;
    private MyDatabaseHelper dbHelper;
    private NewsAdapter newsAdapter = new NewsAdapter(this,list_1);
    private Column_newsAdapter column_newsAdapter =new Column_newsAdapter(this,list);
    private RecyclerView recyclerView_news,recyclerView_column;
    private String title;
    private String news_image;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String column_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_);
        CreateThread();
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.SwipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                list_1.clear();
                list.clear();
                CreateThread();
                column_newsAdapter.notifyDataSetChanged();
                newsAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(Like_Activity.this,"刷新成功",Toast.LENGTH_SHORT).show();
            }
        });


    }


    public void CreateThread(){

    recyclerView_column = findViewById(R.id.recycleview_column);
    recyclerView_news = findViewById(R.id.recycleview_news);
    dbHelper = new MyDatabaseHelper(this);


    //查数据库
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.query("Like_news",new String[]{"Owner","News_id","News_title","News_image"},"Owner = ?",new String[]{Login_Activity.signer},null,null,"id");
                if (cursor.moveToFirst()){
        do {
            id = cursor.getString(cursor.getColumnIndex("News_id"));
            title = cursor.getString(cursor.getColumnIndex("News_title"));
            news_image = cursor.getString(cursor.getColumnIndex("News_image"));
            Map map=new HashMap();
            map.put("id",id);
            map.put("title", title);
            map.put("thumbnail", news_image);
            list_1.add(map);

            LinearLayoutManager manager=new LinearLayoutManager(this);
            recyclerView_news.setLayoutManager(manager);
            recyclerView_news.setAdapter(newsAdapter);
        }while (cursor.moveToNext());
    }
                cursor.close();
    LinearLayoutManager manager=new LinearLayoutManager(this);
                recyclerView_news.setLayoutManager(manager);
                recyclerView_news.setAdapter(newsAdapter);



    //查数据库
    SQLiteDatabase db1 = dbHelper.getWritableDatabase();
    Cursor cursor1 = db1.query("Like_column",new String[]{"Owner","Column_id","Column_description","Column_image","Column_name"},"Owner = ?",new String[]{Login_Activity.signer},null,null,"id");
                if (cursor1.moveToFirst()){
        do {
            column_id = cursor1.getString(cursor1.getColumnIndex("Column_id"));
            thumbnail = cursor1.getString(cursor1.getColumnIndex("Column_image"));
            description = cursor1.getString(cursor1.getColumnIndex("Column_description"));
            name = cursor1.getString(cursor1.getColumnIndex("Column_name"));
            Map map1=new HashMap();
            map1.put("id", column_id);
            map1.put("thumbnail", thumbnail);
            map1.put("name", name);
            map1.put("description", description);
            list.add(map1);

            LinearLayoutManager manager1=new LinearLayoutManager(this);
            recyclerView_column.setLayoutManager(manager1);
            recyclerView_column.setAdapter(column_newsAdapter);

        }while (cursor1.moveToNext());
    }
                cursor1.close();
    LinearLayoutManager manager1=new LinearLayoutManager(this);
        recyclerView_column.setLayoutManager(manager1);
        recyclerView_column.setAdapter(column_newsAdapter);
}

}