package com.redtop.engaze.adapter;

import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.SmsManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.customeviews.CircularImageView;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.ViewHelper;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.manager.ParticipantManager;
import com.redtop.engaze.fragment.ParticipantInfoFragment;

@SuppressLint("SimpleDateFormat")
public class CustomParticipantsInfoList extends BaseAdapter {

	private Context context;
	private final ArrayList<EventParticipant> eventMembers;
	private String initiatorID;
	private String source;
	private String eventId;
	ParticipantInfoFragment parentFragment;
	//private final Integer[] imageId;
	private static LayoutInflater inflater = null;

	public CustomParticipantsInfoList(ParticipantInfoFragment parentFragment, Context context,
									  ArrayList<EventParticipant> arrayList, String initiatorID, String eventId, String source) {
		//super(context, R.layout.event_participant_listitem, arrayList);
		this.parentFragment = parentFragment;
		this.context = context;
		this.eventMembers = arrayList;
		this.initiatorID = initiatorID;
		this.source = source;
		this.eventId = eventId;
		inflater = (LayoutInflater) context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public class Holder {
		TextView tv;
		CircularImageView img_profile;
		ImageView img_call;
		//ImageView img_sms;
		RelativeLayout rl_parent;
		ImageView img_status;
		ImageView img_poke;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView;

		final EventParticipant member = eventMembers.get(position);
		final String mobileno = member.contactOrGroup.getRegisteredMobileNumber();


		rowView = inflater.inflate(R.layout.item_event_participant_list, null);
		holder.tv = (TextView) rowView.findViewById(R.id.txt_participant);
		holder.img_profile = (CircularImageView) rowView.findViewById(R.id.img_participant);
		holder.rl_parent = (RelativeLayout) rowView.findViewById(R.id.rl_participant);
		//holder.img.setImageResource(imageId[position]);  
		holder.img_call = (ImageView) rowView.findViewById(R.id.img_call);
		//holder.img_sms = (ImageView) rowView.findViewById(R.id.img_sms);		
		holder.img_status = (ImageView) rowView.findViewById(R.id.img_status);
		holder.img_poke = (ImageView) rowView.findViewById(R.id.img_poke);

		if (source != null && source.equals(RunningEventActivity.class.getName())) {
			holder.img_profile.setBackground(member.contactOrGroup.getImageDrawable(context));
			holder.img_status.setVisibility(View.GONE);
		} else {
			holder.img_profile.setVisibility(View.GONE);

			holder.img_status.setVisibility(View.VISIBLE);
			if (member.acceptanceStatus == AcceptanceStatus.Accepted) {
				ViewHelper.setRippleDrawable(holder.img_status, context, R.drawable.ripple_lightgreen);
				//holder.img_status.setBackground(context.getResources().getDrawable(R.drawable.ic_check_green_48));
			} else if (member.acceptanceStatus == AcceptanceStatus.Rejected) {
				ViewHelper.setRippleDrawable(holder.img_status, context, R.drawable.ripple_red);
				//holder.img_status.setBackground(context.getResources().getDrawable(R.drawable.ic_decline_red_48));
			} else {
				ViewHelper.setRippleDrawable(holder.img_status, context, R.drawable.ripple_amber);
				//holder.ll_status.setBackgroundColor(context.getResources().getColor(R.color.event_pending));
				//holder.img_status.setBackground(context.getResources().getDrawable(R.drawable.ic_exclam));
			}
		}
		if (ParticipantManager.isParticipantCurrentUser(member.userId)) {
			holder.img_call.setVisibility(View.GONE);
			//holder.img_sms.setVisibility(holder.img_sms.GONE);
			holder.tv.setText("You");
			holder.img_poke.setVisibility(View.GONE);
		} else {
			final String participanName = member.profileName;

			holder.tv.setText(participanName);

			if (member.acceptanceStatus != AcceptanceStatus.Accepted) {
				holder.img_poke.setOnClickListener(v -> ParticipantManager.pokeParticipant(member.userId, member.profileName, eventId, AppContext.actionHandler));
			} else {
				holder.img_poke.setVisibility(View.GONE);
			}

			holder.img_call.setOnClickListener(v -> parentFragment.onCallClick(mobileno));
		}

		return rowView;
	}

	protected void sendSMSMessage(String mobileno, Editable editable) {
		// TODO Auto-generated method stub
		//Log.i("Send SMS", "");

		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(mobileno, null, editable.toString(), null, null);
			Toast.makeText(context, "SMS sent.", Toast.LENGTH_LONG).show();
		} 

		catch (Exception e) {
			Toast.makeText(context, "SMS faild, please try again.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	protected void showSMSInputDialog(final String mobileno) {

		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View promptView = layoutInflater.inflate(R.layout.input_sms_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(promptView);

		final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
		// setup a dialog window
		alertDialogBuilder.setCancelable(false)
		.setPositiveButton("Send", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendSMSMessage(mobileno, editText.getText());
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		// create an alert dialog
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return eventMembers.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

}