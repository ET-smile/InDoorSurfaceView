package com.karonl.surfaceinstance;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.karonl.surfaceinstance.Adapter.DataAdapter;
import com.karonl.surfaceinstance.Unit.DataJson;
import com.karonl.surfaceinstance.Unit.PathUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textview;
    private DataAdapter adapter =  new DataAdapter();;
    private Bitmap bmp;
    List<PathUnit> unitList = new ArrayList<>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    float fr = (float) msg.obj;
                    int fps = (int) (1000f / fr);
                    textview.setText("FPS: " + fps + "");
                    break;
                case 2:
                    findViewById(R.id.tip).setVisibility(View.GONE);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InDoorSurfaceView view = (InDoorSurfaceView)findViewById(R.id.surface);

        //延迟展区区域数据加载
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }catch (InterruptedException e){}

                //背景图
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.zxc, opt);//图片资源
                adapter.setBmp(bmp);//设置图片
                bmp = null;
                getUnitList();//设置数组
                adapter.setList(unitList);//设置数组
                adapter.refreshData();
                Message message = handler.obtainMessage();
                message.what = 2;
                message.sendToTarget();
            }
        }).start();

        textview = (TextView)findViewById(R.id.frames);

        view.setAdapter(adapter);//初始化

        view.setOnClickMapListener(new InDoorSurfaceView.onClickMapListener() {
            @Override
            public void onClick(PathUnit region) {
                Log.e(this.getClass().getName(),"click");
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("商店介绍");
                dialog.setMessage(""+region.getName());
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }
        });


        view.onFramesListener(new InDoorSurfaceView.FramesListener() {
            @Override
            public void onRefresh(float number) {
                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = number;
                message.sendToTarget();
            }
        });

    }


    //图案列表
    private void getUnitList(){
        DataJson data = new DataJson();
        for(int i = 0; i < data.size(); i++){
            JSONObject jsonObject = data.getArray(i);
            PathUnit unit = new PathUnit(getList(jsonObject));
            try {
                unit.setName(jsonObject.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            unitList.add(unit);
        }
    }

    //每个图案的坐标组合
    private List<PointF> getList(JSONObject jsonObject){
        float density = getResources().getDisplayMetrics().density;
        List<PointF> pointList = new ArrayList<>();
        JSONArray array;
        try {
            array = jsonObject.getJSONArray("area");
            for(int r = 0; r < array.length(); r++){
                float x = ((JSONObject)(array.get(r))).getInt("x");
                float y = ((JSONObject)(array.get(r))).getInt("y");
                pointList.add(new PointF( x * density, y * density));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pointList;
    }



}
