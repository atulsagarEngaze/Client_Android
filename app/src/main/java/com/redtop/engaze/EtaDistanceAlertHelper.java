package com.redtop.engaze;

import java.util.ArrayList;
import java.util.UUID;

import  kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.ReminderFrom;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.service.EventDistanceReminderService;

@SuppressLint("NewApi")
public class EtaDistanceAlertHelper {

	private Event mEvent;
	private NumericWheelAdapter kmsAdapter;
	private ArrayWheelAdapter<String> metersAdapter;
	private Dialog reminderDialog;
	private boolean scrolling = false;
	private WheelView unit;
	private WheelView kms;	
	private String mUserName;
	private String mUserId;

	public EtaDistanceAlertHelper(String eventId, String userName, String userId, IActionHandler actionHandler){

		mEvent = InternalCaching.getEventFromCache(eventId);
		mUserName = userName;
		mUserId = userId;
	}	

	@SuppressLint({"NewApi", "WrongConstant"})
	public void showSetAlertDialog(){	

		final WheelView from;		
		//Create a custom dialog with the dialog_date.xml file
		reminderDialog = new Dialog(AppContext.context);
		reminderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		reminderDialog.setContentView(R.layout.activity_etadistancereminder);
		final Button cancelRemove = (Button)reminderDialog.findViewById(R.id.eta_cancel);
		TextView etaExisting = (TextView) reminderDialog.findViewById(R.id.eta_existing);
		etaExisting.setVisibility(View.GONE);
		ArrayList<EventParticipant> reminderMembers = mEvent.getReminderEnabledMembers();
		if(reminderMembers != null){
			for(EventParticipant em : reminderMembers){
				if(em.getUserId().equals(mUserId)){									
					etaExisting.setVisibility(View.VISIBLE);
					etaExisting.setText("Reminder set to : " + em.getDistanceReminderDistance() + " Mtrs.");					
					cancelRemove.setText("Remove");
					cancelRemove.setTag(em);
				}
			}
		}

		unit = (WheelView) reminderDialog.findViewById(R.id.eta_unit);
		ArrayWheelAdapter<String> unitAdapter =
				new ArrayWheelAdapter<String>(AppContext.context, new String[] {"Kms", "Mtrs"});
		/*unitAdapter.setItemResource(R.layout.wheel_text_item);
		unitAdapter.setItemTextResource(R.id.wheel_text);*/
		unit.setViewAdapter(unitAdapter);
		unit.setTextAlignment(Gravity.CENTER);

		//unit.setCyclic(true);

		//Configure kms Column
		kms = (WheelView) reminderDialog.findViewById(R.id.eta_values);
		
		metersAdapter =	new ArrayWheelAdapter<String>(AppContext.context, new String[] {"100","250","500","750"});
		/*metersAdapter.setItemResource(R.layout.wheel_item);
		metersAdapter.setItemTextResource(R.id.distance_item);*/
		
		kmsAdapter = new NumericWheelAdapter(AppContext.context, 1, 10);
		/*kmsAdapter.setItemResource(R.layout.wheel_item);
		kmsAdapter.setItemTextResource(R.id.distance_item);*/
		kms.setViewAdapter(kmsAdapter);    
		kms.setCyclic(true);

		//Configure From 
		from = (WheelView) reminderDialog.findViewById(R.id.eta_from);
		ArrayWheelAdapter<String> fromAdapter;
				
		if(mEvent.getDestinationAddress() != null && !mEvent.getDestinationAddress().isEmpty())
		{
			fromAdapter = new ArrayWheelAdapter<String>(AppContext.context, new String[] {"Me", "Dest"});
		}
		else{
			fromAdapter = new ArrayWheelAdapter<String>(AppContext.context, new String[] {"Me"});
		}
	/*	fromAdapter.setItemResource(R.layout.wheel_item_time);
		fromAdapter.setItemTextResource(R.id.wheel_text);*/
		from.setViewAdapter(fromAdapter);
		from.setTextAlignment(Gravity.CENTER);

		unit.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView kms, int oldValue, int newValue) {
				if (!scrolling) {
					updateValues(kms, newValue);
				}
			}
		});

		unit.addScrollingListener( new OnWheelScrollListener() {
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
			}
			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;
				updateValues(kms, unit.getCurrentItem());
			}
		});


		Button set = (Button)reminderDialog.findViewById(R.id.eta_set);
		set.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int finalMeters = readvalues(R.id.eta_unit, R.id.eta_values, mUserName, from.getCurrentItem());

				EventParticipant mem = mEvent.getMember(mUserId);
				mem.setDistanceReminderId(UUID.randomUUID().toString());
				mem.setDistanceReminderDistance(finalMeters);
				mem.setReminderFrom(ReminderFrom.getDistanceReminderFrom(from.getCurrentItem()));
				mEvent.isDistanceReminderSet = true;

				ArrayList<EventParticipant> emList = mEvent.getReminderEnabledMembers();
				if(emList == null){
					emList = new ArrayList<EventParticipant>();
					emList.add(mem);
					mEvent.setReminderEnabledMembers(emList);
				}else{
					if(!emList.contains(mem)){
						emList.add(mem);
				}
				}		

				InternalCaching.saveEventToCache(mEvent);
				reminderDialog.cancel();
				AppContext.actionHandler.actionComplete(Action.SETTIMEBASEDALERT);
				Intent eventDistanceReminderServiceIntent = new Intent(AppContext.context, EventDistanceReminderService.class);
				eventDistanceReminderServiceIntent.putExtra("EventId", mEvent.getEventId());
				eventDistanceReminderServiceIntent.putExtra("MemberId", mUserId);
				AppContext.context.startService(eventDistanceReminderServiceIntent);
			}
		});

		cancelRemove.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {					
				if(v.getTag() == null){
					reminderDialog.cancel();
				}else{
					EventParticipant mem = mEvent.getReminderEnabledMembers().get(mEvent.getReminderEnabledMembers().indexOf(cancelRemove.getTag()));
					mEvent.getReminderEnabledMembers().remove(mem);
					InternalCaching.saveEventToCache(mEvent);
					reminderDialog.cancel();
					Toast.makeText(AppContext.context,
							"Proximity Reminder removed!",
							Toast.LENGTH_LONG).show();	
				}
				AppContext.actionHandler.actionCancelled(Action.SETTIMEBASEDALERT);
			}
		});
		reminderDialog.show();		

	}	

	private WheelView getWheel(int id) {
		return (WheelView) reminderDialog.findViewById(id);
	}

	private void updateValues(WheelView kms, int index) {
		if(index ==1){			
			kms.setViewAdapter(metersAdapter);
		}else{
			kms.setViewAdapter(kmsAdapter); 
		}		
	}

	protected int readvalues(int unitwheel, int valueswheel, String username, int from) {
		WheelView wheel1 = getWheel(unitwheel);
		WheelView wheel2 = getWheel(valueswheel);
		String units = "Kilo Meters" ;
		int value = 0, finalMeters = 0;
		if(wheel1.getCurrentItem() != 0){
			units = "Meters";
			switch (wheel2.getCurrentItem()) {
			case 0:
				value = 100; 				
				break;
			case 1:
				value = 250;
				break;
			case 2:
				value = 500;
				break;
			case 3:
				value = 750;
				break;
			default:
				break;
			}
			finalMeters = value;
		}
		else{
			value = wheel2.getCurrentItem() + 1;
			finalMeters = value * 1000;
		}
		String fromvalue = "";
		if(from == 0){
			fromvalue  = "you";
		}else{
			fromvalue = "Destination";
		}
		Toast.makeText(AppContext.context,
				"Sit back and Relax. We will remind you when " + username + " is around " + value + " " + units + " away from " + fromvalue +".",
				Toast.LENGTH_LONG).show();		

		return finalMeters;
	}
}