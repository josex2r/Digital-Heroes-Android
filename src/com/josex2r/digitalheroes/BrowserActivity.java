package com.josex2r.digitalheroes;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.josex2r.digitalheroes.R;

public class BrowserActivity extends Activity {
	
	private ProgressBar pbWebLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);
		
		pbWebLoader = (ProgressBar) findViewById(R.id.pbWebLoader);
		pbWebLoader.setVisibility(View.VISIBLE);
		
		Bundle data = getIntent().getExtras();

		try {
			String title = data.getString("title");
			this.getActionBar().setTitle(title);
			
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

}
