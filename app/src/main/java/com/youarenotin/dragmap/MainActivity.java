package com.youarenotin.dragmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyScrollView mScrollview;
    private MyContainer mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        MyListView listview = (MyListView) findViewById(R.id.listview);
        TextView tv_top = (TextView) findViewById(R.id.tv_search);
        mContainer = (MyContainer) findViewById(R.id.container);
        mContainer.addListView(listview);
        mContainer.setTopView(tv_top);
        String[] strings = {"title"};//Map的key集合数组
        int[] ids = {android.R.id.text1};//对应布局文件的id
        SimpleAdapter adapter = new SimpleAdapter(this, getData(), android.R.layout.simple_list_item_1, strings, ids);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "位置: " + position, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private List<HashMap<String, Object>> getData() {
        // 新建一个集合类，用于存放多条数据
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        for (int i = 1; i <= 40; i++) {
            map = new HashMap<String, Object>();
            map.put("title", "人物" + i);
            map.put("time", "9月20日");
            list.add(map);
        }
        return list;
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


}
