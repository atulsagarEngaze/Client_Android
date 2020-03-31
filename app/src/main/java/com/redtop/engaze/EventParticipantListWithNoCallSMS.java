package com.redtop.engaze;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.adapter.ParticipantsWithNoCallSMSListAdapter;

@SuppressWarnings("deprecation")
public class EventParticipantListWithNoCallSMS extends Activity {
	private TextView tvHeader;
	private String action;
	private HashMap<String, LatLng> mEndPoints = new HashMap<String, LatLng>();
	private ArrayList<String>mDisplayNameList = new ArrayList<String>();
	private ParticipantsWithNoCallSMSListAdapter mAdapter;
	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_participants_list_nocallsms);
		tvHeader  = (TextView)findViewById(R.id.ChooseCategoryHeader);		
		action = this.getIntent().getStringExtra("action");		
		if(action.equals("loadroute")){
			tvHeader.setText("Choose Route End point");
			mEndPoints = (HashMap<String,LatLng>) this.getIntent().getSerializableExtra("endpoints");
			mDisplayNameList = new ArrayList<String>(mEndPoints.keySet());
		}
		ListView list = (ListView)findViewById(R.id.list_event_participants);
		 mAdapter= new ParticipantsWithNoCallSMSListAdapter(EventParticipantListWithNoCallSMS.this, R.layout.event_participant_simple_listitem ,mDisplayNameList);	

		list.setAdapter(mAdapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) {
				String  endointName = (String)adapter.getItemAtPosition(position);
				Intent intent = new Intent();		
				intent.putExtra("endpoint", mEndPoints.get(endointName)); 			
				setResult(RESULT_OK, intent);  
				finish();
			}
		});
	}
}
