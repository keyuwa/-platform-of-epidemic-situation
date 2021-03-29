package com.xiaok.conv19;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.AtmosphereEffect;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.xiaok.conv19.main.SameWayActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import utils.MyUtills;

public class NativeAppFragment extends Fragment {

    private SceneView mSceneView;
    private ArcGISScene scene;

    private FloatingActionMenu menu_blue;
    private List<FloatingActionMenu> menus = new ArrayList<>();

    private FloatingActionButton fab_same_way;
    private FloatingActionButton fab_diag_neiborhood;
    private FloatingActionButton fab_outpatient;
    private FloatingActionButton fab_lastest_news;
    private FloatingActionButton fab_real_time;

    private Handler mUiHandler = new Handler();

    private Handler realDataHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String resultStr = (String) msg.obj;
            Log.e("111111111111",resultStr);
            showRealData(resultStr);

        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_native_app, container, false);

        mSceneView = root.findViewById(R.id.scene_sceneview);
        menu_blue = root.findViewById(R.id.menu_blue);
        fab_same_way = root.findViewById(R.id.fab_same_way);
        fab_diag_neiborhood = root.findViewById(R.id.fab_diag_neiborhood);
        fab_outpatient = root.findViewById(R.id.fab_outpatient);
        fab_lastest_news = root.findViewById(R.id.fab_lastset_news);
        fab_real_time = root.findViewById(R.id.fab_real_time);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        copyAssetAndWrite("conv_feature_layers.geodatabase");

        //网络图层地址
        String MapUrl="https://map.geoq.cn/ArcGIS/rest/services/ChinaOnlineCommunity/MapServer";
        ArcGISTiledLayer mapServiceLayer = new ArcGISTiledLayer(MapUrl);

        menu_blue.setIconAnimated(false);
        menu_blue.hideMenuButton(false);
        menus.add(menu_blue);
        int delay = 400;
        for (final FloatingActionMenu menu : menus) {
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menu.showMenuButton(true);
                }
            }, delay);
            delay += 150;
        }

        //遥感影像作为三维底图
        scene = new ArcGISScene(Basemap.createTopographic());
        scene.getOperationalLayers().add(mapServiceLayer);

        mSceneView.setScene(scene);

        File cacheDir = getActivity().getCacheDir();
        if (cacheDir.exists()){
            loadFeatureLayers(cacheDir+"/conv_feature_layers.geodatabase");
        }

        Camera camera = new Camera(30.592935,103.305215,10000000, 0, 12, 0);
        mSceneView.setViewpointCamera(camera);

        mSceneView.setAtmosphereEffect(AtmosphereEffect.HORIZON_ONLY);

        mSceneView.setAttributionTextVisible(false); //关闭Esri logo
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud4449636536,none,NKMFA0PL4S0DRJE15166"); //消除水印


        fab_real_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AnalyseDataTask().execute();

            }
        });

        fab_same_way.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SameWayActivity.class));
                Toast.makeText(getContext(),"同乘查询",Toast.LENGTH_SHORT).show();
            }
        });


        fab_diag_neiborhood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"确诊小区查询",Toast.LENGTH_SHORT).show();
            }
        });


        fab_outpatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"门诊查询",Toast.LENGTH_SHORT).show();
            }
        });

        fab_lastest_news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera camera = new Camera(30.592935,103.305215,10000000, 0, 12, 0);
                mSceneView.setViewpointCamera(camera);
                Toast.makeText(getContext(),"重置地图",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        mSceneView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSceneView.resume();
    }

    @Override
    public void onDestroy() {
        mSceneView.dispose();
        super.onDestroy();
    }

    private void loadFeatureLayers(String geodatabasePath){

        final Geodatabase geodatabase = new Geodatabase(geodatabasePath);
        geodatabase.loadAsync();
        // 当geodatabase读取成功后将geodatabase加载到数据库
        geodatabase.addDoneLoadingListener(() -> {
            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {

                List<GeodatabaseFeatureTable> geodatabaseFeatureTables = geodatabase.getGeodatabaseFeatureTables();
                for (int i=geodatabaseFeatureTables.size()-1;i>=0;i--){
                    GeodatabaseFeatureTable geodatabaseFeatureTable = geodatabaseFeatureTables.get(i);
                    geodatabaseFeatureTable.loadAsync();
                    //创建要素图层
                    final FeatureLayer featureLayer = new FeatureLayer(geodatabaseFeatureTable);
                    // 添加到地图
                    scene.getOperationalLayers().add(featureLayer);
                }

            } else {
                Toast.makeText(getContext(), "Geodatabase failed to load!", Toast.LENGTH_LONG).show();
                Log.e("333333333333333333333333","Geodatabase failed to load!");
            }
        });
    }

    /**
     * 将asset文件写入缓存
     */
    private boolean copyAssetAndWrite(String fileName){
        try {
            File cacheDir = getActivity().getCacheDir();
            if (!cacheDir.exists()){
                cacheDir.mkdirs();
            }
            File outFile =new File(cacheDir,fileName);
            if (!outFile.exists()){
                boolean res=outFile.createNewFile();
                if (!res){
                    return false;
                }
            }else {
                if (outFile.length()>10){//表示已经写入一次
                    return true;
                }
            }
            InputStream is = getActivity().getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    //异步获取实时疫情数据
    @SuppressLint("StaticFieldLeak")
    private class AnalyseDataTask extends AsyncTask<Void, String, Boolean> {

        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("实时疫情数据");
            progressDialog.setMessage("检查网络连接...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMax(100);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            publishProgress("正在获取数据...");
            SystemClock.sleep(500);
            Boolean isOK = getRealData(makeRequestUrl());
            return isOK;
        }

        @Override
        protected void onProgressUpdate(String... values) {// String... 表示不定参数，即调用时可以传入多个String对象
            super.onProgressUpdate(values);
            progressDialog.setMessage(values[0] + "，请勿关闭此界面...");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            if (aBoolean) {
                Toast.makeText(getContext(), "获取成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "获取数据失败，请检查网络或稍后重试", Toast.LENGTH_SHORT).show();

            }
        }
    }

    //组拼请求URL
    private URL makeRequestUrl(){
        URL url = null;
        //请求地址
        String httpUrl = "https://route.showapi.com/2217-2?showapi_appid=169112&showapi_timestamp=20200404212817&showapi_sign=0afb717ca8344e9aa4aedb85200749d1";

        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private boolean getRealData(URL url){

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); //注意GET要大写
            conn.setConnectTimeout(5000); //设置超时时间
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200){
                InputStream in = conn.getInputStream();
                String responseStr = MyUtills.readStream(in); //将服务器返回的流转化为字符串
                Message msg = Message.obtain();
                msg.obj = responseStr;
                realDataHandler.sendMessage(msg);
            }else {
                Log.e("----------------返回码----------------:",responseCode+"");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //显示疫情实时数据
    private void showRealData(String resultStr){
        List<String> mResultAttributes = new ArrayList<>();

        //解析Json数据
        JSONObject tempObj = JSON.parseObject(resultStr);
        String tempStr = tempObj.getString("showapi_res_body");
        JSONObject temp2Obj = JSON.parseObject(tempStr);
        String temp2Str = temp2Obj.getString("todayStatictic");
        JSONObject resultObj = JSON.parseObject(temp2Str);
        String confirmedNumStr = resultObj.getString("confirmedNum"); //现有确诊
        String confirmedIncrStr = resultObj.getString("confirmedIncr"); //确诊增加
        String deadNumStr = resultObj.getString("deadNum"); //累计死亡
        String deadIncrStr = resultObj.getString("deadIncr"); //死亡增加
        String suspetedNumStr = resultObj.getString("suspectedNum"); //现有疑似
        String suspectedIncrStr = resultObj.getString("suspectedIncr"); //疑似增加
        String seriousNumStr = resultObj.getString("seriousNum"); //现有重症
        String seriousIncrStr = resultObj.getString("seriousIncr"); //重症增加
        String curedNum = resultObj.getString("curedNum"); //累计治愈
        String curedIncr = resultObj.getString("curedIncr"); //治愈增加

        //计算累计确诊
        String confirmedSumNum = String.valueOf(Integer.parseInt(confirmedNumStr)+
                Integer.parseInt(deadNumStr)+Integer.parseInt(curedNum));

        //获取数据刷新时间
        String updateTime = temp2Obj.getString("updateTime");

        mResultAttributes.add("数据更新时间："+updateTime);
        mResultAttributes.add("现有确诊："+confirmedNumStr);
        mResultAttributes.add("现有疑似："+suspetedNumStr);
        mResultAttributes.add("现有重症："+seriousNumStr);
        mResultAttributes.add("累计确诊："+confirmedSumNum);
        mResultAttributes.add("累计治愈："+curedNum);
        mResultAttributes.add("累计死亡："+deadNumStr);
        mResultAttributes.add(" ");
        mResultAttributes.add("相较于昨天数据变化量:");
        mResultAttributes.add("确诊增加："+confirmedIncrStr);
        mResultAttributes.add("死亡增加："+deadIncrStr);
        mResultAttributes.add("疑似增加："+suspectedIncrStr);
        mResultAttributes.add("重症增加："+seriousIncrStr);
        mResultAttributes.add("治愈增加："+curedIncr);



        new MaterialDialog.Builder(getContext())
                .title("实时疫情数据")
                .canceledOnTouchOutside(false)
                .items(mResultAttributes)
                .positiveText("确定")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (which == DialogAction.POSITIVE) {
                            dialog.dismiss();
                        }

                    }
                })
                .show();
    }
}