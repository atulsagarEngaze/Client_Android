package com.redtop.engaze;

import com.redtop.engaze.R;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

@SuppressWarnings("deprecation")
public class AboutActivity extends AppCompatActivity {
	private static final String TAG = "AboutActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			getSupportActionBar().setTitle(R.string.title_about);
			//toolbar.setSubtitle(R.string.title_about);
			toolbar.setNavigationOnClickListener(new OnClickListener() {
				@Override 
				public void onClick(View v) {
					onBackPressed();
				} 
			}); 
		}
		setversionInfo();
		
		TextView eulaTextView = (TextView)findViewById(R.id.linktermsandservice_about);
		//checkbox.setText("");
		eulaTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), EULAActivity.class);
				intent.putExtra("caller", getIntent().getComponent().getClassName());
				startActivity(intent);	
				finish();
			}
		});

		TextView privacyPolicyTextView = (TextView)findViewById(R.id.linkprivacypolicy_about);
		//checkbox.setText("");
		privacyPolicyTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), PrivacyPolicyActivity.class);
				intent.putExtra("caller", getIntent().getComponent().getClassName());
				startActivity(intent);	
				finish();
			}
		});

	}

	private void setversionInfo() {
		try {
			PackageInfo pinfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			String versiontext =  "Version "  + pinfo.versionName ;
			TextView txversion = (TextView)findViewById(R.id.txt_version);
			txversion.setText(versiontext);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Log.d("pinfoCode",pinfo.versionCode);
		//Log.d("pinfoName",pinfo.versionName);

	}


}
