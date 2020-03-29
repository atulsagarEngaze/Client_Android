package com.redtop.engaze;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("deprecation")
public class EULAActivity extends AppCompatActivity {
	private static final String TAG = "EULAActivity";
	private Class<?> callerClass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_eula);
		Toolbar toolbar = (Toolbar) findViewById(R.id.eula_toolbar);
		try {
			String caller     = getIntent().getStringExtra("caller");
			callerClass = Class.forName(caller);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle("End User License Agreement");
			toolbar.setTitleTextColor(getResources().getColor(R.color.icon));
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();				
				}
			});
		}
	}
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(getBaseContext(), callerClass);
		startActivity(intent);
		this.finish();
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			return true;
		
		}
		return super.onOptionsItemSelected(item);
	}
}
