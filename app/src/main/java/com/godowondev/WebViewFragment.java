package com.godowondev;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WebViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    TextView title;
    ImageButton menuBtn;
    ImageButton reloadBtn;
    String argTitle;
    String argLayout;
    String argUrl;
    WebView webView;
    ProgressBar progressBar;
    SwipeRefreshLayout refreshLayout;

    @Override
    public void onRefresh() {
        webView.reload();
    }


    public interface UiListener{
        void onMenuButtonClicked();
        void appFinishCheck();
        void openDatePicker(WebView webView);
    }

    private UiListener uiCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            uiCallback = (UiListener) activity; // check if the interface is implemented
        }catch(ClassCastException e){
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle args) {

        argTitle = getArguments().getString("Title");
        argLayout = getArguments().getString("Layout");
        argUrl = getArguments().getString("Url");

        int resID = getResources().getIdentifier(argLayout, "layout", "com.godowondev");
        View view = inflater.inflate(resID, container, false);

        // PullToRefresh 설정
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorScheme(R.color.color1);
        refreshLayout.setEnabled(false);  // 기능 사용/사용안함


        // 기본 설정
        menuBtn = (ImageButton) view.findViewById(R.id.mainpage_topLeftMenuBtn);
        reloadBtn = (ImageButton) view.findViewById(R.id.mainpage_topRightReloadBtn);
        title= (TextView) view.findViewById(R.id.mainpage_topTitle);
        title.setText(argTitle);

        if(argUrl != null)
        {
            progressBar = (ProgressBar) view.findViewById(R.id.pro);
            webView = (WebView) view.findViewById(R.id.webView);

            WebSettings set = webView.getSettings();
            set.setJavaScriptEnabled(true);
            //set.setDefaultFontSize(1);
            //set.setSupportZoom(true);
            set.setBuiltInZoomControls(true); // 확대기능 가능
            set.setDisplayZoomControls(false); // 확대기능 +,- 버튼 안보이게
            set.setLoadWithOverviewMode(true); // 한페이지에 전체화면이 다 들어가도록
            set.setUseWideViewPort(true); // html meta tag 에 설정된 뷰포트 사용
            set.setJavaScriptCanOpenWindowsAutomatically(true);

            webView.setWebViewClient(new WebViewClient() {

                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    /* alert 캐치로 변경하면서 url 캐치기능 사용하지 않음
                    if (url.startsWith("changedate://")) {
                        uiCallback.openDatePicker(webView);
                        return true;
                    }
                    */
                    view.loadUrl(url);
                    return true;
                }

                public void onPageStarted(WebView view, String url,
                                          android.graphics.Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progressBar.setVisibility(View.VISIBLE);
                    //refreshLayout.setRefreshing(true);
                }

                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.INVISIBLE);
                    refreshLayout.setRefreshing(false);
                }

                public void onReceivedError(WebView view, int errorCode,
                                            String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }

            });

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    progressBar.setProgress(newProgress);
                }

                @Override
                public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                    if (message.startsWith("[ChangeDate]")) {
                        result.confirm();
                        uiCallback.openDatePicker(webView);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            });

            webView.loadUrl(argUrl);
        }

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
                refreshLayout.setRefreshing(true);
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.isFocused() && webView.canGoBackOrForward(-3)) {
                                webView.goBack();
                                //uiCallback.onBackButtonClicked(webView);
                                return true;
                            } else {
                                uiCallback.appFinishCheck();
                            }
                            break;
                    }
                }

                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.mainpage_topLeftMenuBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uiCallback.onMenuButtonClicked();
            }
        });
    }
}
