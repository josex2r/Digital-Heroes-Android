package com.josex2r.digitalheroes;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;

import java.net.MalformedURLException;
import java.net.URL;

@SuppressLint("SetJavaScriptEnabled") public class BrowserActivity extends Activity {
	
	private ProgressBar pbWebLoader;

    private ShareActionProvider mShareActionProvider;

    private Bundle data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		overridePendingTransition(R.anim.activity_slide_in, R.anim.no_anim);
		
		setContentView(R.layout.activity_browser);
		 
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		pbWebLoader = (ProgressBar) findViewById(R.id.pbWebLoader);
		pbWebLoader.setVisibility(View.VISIBLE);
		
		data = getIntent().getExtras();

		try {
			String title = data.getString("title");
			actionBar.setTitle(title);
			
			URL link = new URL( data.getString("uri") );
			
			WebView navegador = (WebView)findViewById(R.id.wvPost);
			
			WebSettings settings = navegador.getSettings();
			settings.setJavaScriptEnabled(true);
			
			navegador.setHorizontalScrollBarEnabled(false);
			
			navegador.setWebViewClient(new WebViewClient(){
		        public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        	/*if (url != null && url.matches("/gobalo/g")){
	        			return false;
		        	}else  {*/
		        		view.getContext().startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse(url)) );
		        		return true;
		        	//}
		        }
		        
		        @Override
		        public void onPageFinished(WebView view, String url) {
		        	// TODO Auto-generated method stub
		        	pbWebLoader.setVisibility(View.GONE);
		        	super.onPageFinished(view, url);
		        }
		    });
			
			navegador.loadUrl(link.toString());
			
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		overridePendingTransition(R.anim.no_anim, R.anim.activity_slide_out);
		super.onPause();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        Intent myIntent = new Intent();
        myIntent.setAction(Intent.ACTION_SEND);
        myIntent.putExtra(Intent.EXTRA_SUBJECT, data.getString("title"));
        myIntent.putExtra(Intent.EXTRA_TEXT, data.getString("uri"));
        myIntent.setType("text/plain");

        setShareIntent(myIntent);

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

}
