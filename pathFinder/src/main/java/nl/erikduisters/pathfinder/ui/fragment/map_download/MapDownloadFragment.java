package nl.erikduisters.pathfinder.ui.fragment.map_download;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.service.MapDownloadService;
import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragmentViewState.DisplayMessageState;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragmentViewState.ShowWebsiteState;
import nl.erikduisters.pathfinder.ui.widget.ProgressBarCompat;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 26-07-2018.
 */
public class MapDownloadFragment
        extends BaseFragment<MapDownloadFragmentViewModel> {
    @BindView(R.id.webView) WebView webView;
    @BindView(R.id.progressBar) ProgressBarCompat progressBar;

    private MapDownloadService mapDownloadService;
    private MapDownloadServiceConnection mapDownloadServiceConnection;
    private Context context;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.context = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        bindMapDownloadService();
    }

    @Override
    public void onStop() {
        super.onStop();

        unbindMapDownloadService();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapDownloadServiceConnection = new MapDownloadServiceConnection();
    }

    @Override
    public void onDestroyView() {
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        super.onDestroyView();
    }

    private void bindMapDownloadService() {
        Intent intent = new Intent(context, MapDownloadService.class);
        context.bindService(intent, mapDownloadServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMapDownloadService() {
        if (mapDownloadService != null) {
            context.unbindService(mapDownloadServiceConnection);
            mapDownloadService = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (getContext() != null && 0 != (getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
            { WebView.setWebContentsDebuggingEnabled(true); }
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                progressBar.setProgress(newProgress);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(View.GONE);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return viewModel.shouldOverrideUrlLoading(url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
            }
        });

        if (savedInstanceState == null) {
            viewModel.start();
        } else {
            webView.restoreState(savedInstanceState);
        }

        viewModel.getViewStateObservable().observe(this, this::render);
        viewModel.getScheduleMapDownloadStateObservable().observe(this, this::handleDownloadMapState);

        return v;
    }

    private void render(@Nullable MapDownloadFragmentViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof ShowWebsiteState) {
            webView.loadUrl(((ShowWebsiteState) viewState).url);
            viewModel.onWebSiteShown();
        }

        if (viewState instanceof DisplayMessageState) {
            render((DisplayMessageState) viewState);
        }
    }

    private void render(DisplayMessageState state) {
        Snackbar.make(webView, state.getMessage(getContext()), Snackbar.LENGTH_SHORT).show();
        viewModel.onMessageDisplayed();
    }

    private void handleDownloadMapState(MapDownloadFragmentViewState.ScheduleMapDownloadState state) {
        if (state == null) {
            return;
        }

        if (getContext() == null) {
            throw new RuntimeException("Context has not been set yet");
        }

        DownloadManager.Request request = new DownloadManager.Request(state.mapUri);

        //TODO: Allow/Disallow metered and or roaming connections
        request.setTitle(state.title.getString(getContext()))
                .setDescription(state.description.getString(getContext()))
                .setDestinationUri(state.destinationUri)
                .setVisibleInDownloadsUi(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        mapDownloadService.enqueue(request);

        viewModel.onMapDownloadScheduled();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_map_download;
    }

    @Override
    protected Class<MapDownloadFragmentViewModel> getViewModelClass() {
        return MapDownloadFragmentViewModel.class;
    }

    @Override
    public boolean onBackPressed() {
        Timber.d("onBackPressed()");

        if (webView.canGoBack()) {
            webView.goBack();

            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        webView.saveState(outState);
    }

    private class MapDownloadServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Timber.e("onServiceConnected");

            mapDownloadService = ((MapDownloadService.MapDownloadServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.e("onServiceDisconnected");
            mapDownloadService = null;
        }
    }
}
