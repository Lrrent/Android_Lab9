package com.example.da.lab9;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private Button search;
    private EditText citySearch;
    private TextView city;
    private TextView time;
    private TextView temp_above,temp_below;
    private TextView humidity,air,wind;
    private RelativeLayout update;
    private LinearLayout tody;
    private ListView indexList;
    private RecyclerView recyclerView;
    private String url = "http://ws.webxml.com.cn/WebServices/WeatherWS.asmx/getWeather";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllView();
        update.setVisibility(GONE);  //没有结果时隐藏控件
        tody.setVisibility(GONE);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = citySearch.getText().toString();
                new searchAsyncTask().execute(input);  //执行异步线程,查询天气并更新UI
            }
        });
    }
    private void findAllView(){
        search = (Button) findViewById(R.id.search);
        citySearch = (EditText) findViewById(R.id.citySearch);
        city = (TextView) findViewById(R.id.city);
        time = (TextView) findViewById(R.id.time);
        temp_above = (TextView) findViewById(R.id.temp_above);
        temp_below = (TextView) findViewById(R.id.temp_below);
        humidity = (TextView) findViewById(R.id.humidity);
        humidity = (TextView) findViewById(R.id.humidity);
        air = (TextView) findViewById(R.id.air);
        wind = (TextView) findViewById(R.id.wind);
        update = (RelativeLayout) findViewById(R.id.update);
        tody = (LinearLayout) findViewById(R.id.tody);
        indexList = (ListView) findViewById(R.id.data);
        recyclerView = (RecyclerView) findViewById(R.id.nextSeven);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
    }
    //测试网络连接是否可用,返回boolean值
    public boolean checkNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){ //找到网络并且网络可连接
            return true;
        }
        else {
            return false;
        }
    }
    //新建一个线程进行网络访问,返回并解析网址数据,最后更新到控件上
    class searchAsyncTask extends AsyncTask<String,Integer,String>{
        //处理异步任务,即从网络中获得数据
        @Override
        protected String doInBackground(String... input) {
            String weatherInfo = "";
            if (!checkNetwork()){  //网络不可用的情况
                //Log.i("当前没有可用网络", "doInBackground: ");
//                Toast.makeText(getApplicationContext(),"当前没有可用网络",Toast.LENGTH_LONG).show();
            }
            else{ //网络可用
                 weatherInfo = getWeather(input[0]);  //获得天气详情
            }
                return weatherInfo;
        }

        @Override
        protected void onPostExecute(String s) {  //之后doInBackground return的数据会传入这里,然后对控件进行更新
            if (s.equals("")){
                Toast.makeText(getApplicationContext(),"当前没有可用网络",Toast.LENGTH_LONG).show();
            }
            else{
               if (s.indexOf("查询结果为空") != -1){  //判断返回的是否含有查询为空字符,有的话说明没有查询结果
                   Toast.makeText(getApplicationContext(),"查询结果为空" , Toast.LENGTH_SHORT).show();
               }
               else if (s.indexOf("免费用户不能使用高速访问") != -1){
                   Toast.makeText(getApplicationContext(), "您的手速过快,二次查询间隔<600ms", Toast.LENGTH_SHORT).show();
               }
               else if (s.indexOf("访问超过规定数量") != -1){
                   Toast.makeText(getApplicationContext(), "免费用户24 小时内访问超过规定数量50次", Toast.LENGTH_SHORT).show();
               }
               else{ //解析查询得到的xml文件的字符串,并更新到空间上
                   update.setVisibility(View.VISIBLE);
                   tody.setVisibility(View.VISIBLE);
                   try {
                       List parseResult = parseQuery(s);
                       for(int i = 0;i<parseResult.size();i++){
                            Log.i("parseResult",parseResult.get(i).toString());
                        }
                       String cityRs = parseResult.get(1).toString();  //获得天气
                       String timeRs = parseResult.get(3).toString().split(" ")[1]+" 更新";
                       String tempBelowRs = parseResult.get(8).toString();  //获得温度
                       String[] weatherTody = parseResult.get(4).toString().split("；"); //获得今天的天气情况
                       String tempAboveRs = weatherTody[0].split("：")[2]; 
                       String windRs = weatherTody[1].split("：")[1];   //风力
                       String humidityRs = weatherTody[2];              //湿度
                       String airRs = parseResult.get(5).toString().split("。")[1];  //获得空气质量
                       List<Map<String,String>> list = getIndex(parseResult.get(6).toString());  //获得相关指数
                       List<Map<String,String>> nextSeven = getNextSeven(parseResult);   //存储最近7天的天气情况
                       updateView(cityRs,timeRs,tempAboveRs,tempBelowRs,humidityRs,airRs,windRs,list,nextSeven);//更新UI
                   } catch (XmlPullParserException e) {
                       e.printStackTrace();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
           }
    }
    //更新控件内容
    private  void updateView(String cityRs,String timeRs,String tempAboveRs,String tempBelowRs,String humidityRs,String airRs,
    						String windRs,List<Map<String,String>> list,List<Map<String,String>> nextSevenList){
        city.setText(cityRs);
        time.setText(timeRs);
        temp_above.setText(tempAboveRs);
        temp_below.setText(tempBelowRs);
        air.setText(airRs);
        humidity.setText(humidityRs);
        wind.setText(windRs);
        //更新列表
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,list,R.layout.item,new String[]{"index","content"},
        												new int[]{R.id.index,R.id.content});
        indexList.setAdapter(simpleAdapter);
        //最近7天天气
        RecyleViewAdapter adapter = new RecyleViewAdapter(this,nextSevenList);
        recyclerView.setAdapter(adapter);
    }
    //获得各种指数,存入list中,之后使用ListView显示
    private List<Map<String,String>> getIndex(String index){
        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        String []s = index.split("。");
        for (int i = 0;i<5;i++){
            Map<String,String> map = new HashMap<String,String>();
            map.put("index",s[i].split("：")[0]);
            map.put("content",s[i].split("：")[1]);
            list.add(map);
        }
        return list;
    }
    //获得RecycleView的数据
    private List<Map<String,String>> getNextSeven(List list){
            List<Map<String,String>> data = new ArrayList<>();
            for (int i = 0;i<7;i++){
                Map<String,String> map = new HashMap<String,String>();
                map.put("date",list.get(7 + i*5).toString().split(" ")[0]);
                map.put("weather",list.get(7 + i*5).toString().split(" ")[1]);
                map.put("temperature",list.get(8 + i*5).toString());
                data.add(map);
            }
        return data;
    }
    //当网络可用时,根据输入的城市名查询天气情况
    private String getWeather(String city){
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        try{
            connection = (HttpURLConnection) ((new URL(url.toString()).openConnection()));
            connection.setRequestMethod("POST");
            connection.setReadTimeout(8000); //设置读取数据超时
            connection.setConnectTimeout(80000); //设置连接超时
            DataOutput out = new DataOutputStream(connection.getOutputStream());
            city = URLEncoder.encode(city,"utf-8");
            out.writeBytes("theCityCode="+city+"&theUserID=635639eef9dc42f4abe8e4c23a8fb932");
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line=reader.readLine())!=null){  //获得返回的数据,并读入字符串
                response.append(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (connection != null){
                connection.disconnect();  //结束后断开网络连接
            }
        }
        return response.toString();
    }
    //解析查询结果
    private List<String> parseQuery(String response) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(response));
        int eventType = parser.getEventType(); //?
        List<String>  result = new ArrayList<>();
        while (eventType != XmlPullParser.END_DOCUMENT){
            switch (eventType){
                case XmlPullParser.START_TAG:    //位置在标签的开始
                    if ("string".equals(parser.getName())){ //标签为string
                        String str = parser.nextText();   //获得标签之中的内容
                        result.add(str);     //加入到list中
                    }
                    break;
                case XmlPullParser.END_TAG:break;
                default:break;
            }
            eventType = parser.next();
        }
        return result;
    }
}

