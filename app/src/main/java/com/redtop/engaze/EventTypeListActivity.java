package com.redtop.engaze;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.redtop.engaze.adapter.NameImageAdapter;
import com.redtop.engaze.domain.NameImageItem;

import androidx.appcompat.app.AppCompatActivity;

public class EventTypeListActivity extends AppCompatActivity implements OnItemClickListener {

	/** Called when the activity is first created. */
	
	ListView listView;
	ArrayList<NameImageItem> rowItems;
	
	
	//public TypedArray images ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    String[] eventTypeName =  getResources().getStringArray(R.array.event_type_name);
	    TypedArray images = getResources().obtainTypedArray(R.array.event_type_image);	  
		
	    // TODO Auto-generated method stub
	    setContentView(R.layout.activity_event_type_list);
	    
	    rowItems = new ArrayList<NameImageItem>();
		for (int i = 0; i < eventTypeName.length; i++) {
			NameImageItem item = new NameImageItem(images.getResourceId(i, -1), eventTypeName[i], i);
			rowItems.add(item);
		}

		listView = (ListView) findViewById(R.id.eve_type_list);
		NameImageAdapter adapter = new NameImageAdapter(this,
				R.layout.item_name_image_row, rowItems);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();	
		 
		intent.putExtra("com.redtop.engaze.entity.NameImageItem", rowItems.get(position)); 	
		
		setResult(RESULT_OK, intent);        
		finish();
		
	}

}
