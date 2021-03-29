package com.xiaok.conv19;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WebAppFragment extends Fragment {

    private WebView wv_main;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_web_app, container, false);
        wv_main = root.findViewById(R.id.wv_main);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wv_main.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                if(url!=null) {
                    String fun="javascript:function getClass(parent,sClass) { var aEle=parent.getElementsByTagName('div'); var aResult=[]; var i=0; for(i<0;i<aEle.length;i++) { if(aEle[i].className==sClass) { aResult.push(aEle[i]); } }; return aResult; } ";

                    view.loadUrl(fun);

                    String fun2="javascript:function hideOther() {getClass(document,'cover_logo')[0].style.display='none';getClass(document,'qt-body')[0].style.top=35;}";

                    view.loadUrl(fun2);

                    view.loadUrl("javascript:hideOther();");
                }

                super.onPageFinished(view, url);
            }


            private WebResourceResponse editResponse() {
                try {
                    Log.i("result", "加载本地js");
                    return new WebResourceResponse("application/x-javascript", "utf-8", getContext().getAssets().open("webapp.js"));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("result", "加载本地js错误："+e.toString());
                }
                //需处理特殊情况
                return null;
            }

        });
        //支持javascript
        wv_main.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        wv_main.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        wv_main.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        wv_main.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        wv_main.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        wv_main.getSettings().setLoadWithOverviewMode(true);
        wv_main.loadUrl("https://wp.m.163.com/163/page/news/virus_report/index.html?_nw_=1&_anw_=1");
    }
}