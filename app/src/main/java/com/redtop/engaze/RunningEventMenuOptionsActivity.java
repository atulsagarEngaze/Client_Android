package com.redtop.engaze;

import java.util.ArrayList;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.adapter.NameImageAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.service.ParticipantService;

import androidx.core.app.ActivityCompat;

@SuppressWarnings("deprecation")
public class RunningEventMenuOptionsActivity extends BaseActivity implements OnItemClickListener, IActionHandler {

	protected ArrayList<NameImageItem> mUserMenuItems;
	private String mEventId;
	private String mUserName;
	private String mUserId;
	private String mobileno;
	private Event mEvent;
	private EventParticipant member;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_running_event_menu_options);
		mContext = this;
		mUserName = this.getIntent().getStringExtra("UserName");
		mUserId = this.getIntent().getStringExtra("UserId");
		mEventId = this.getIntent().getStringExtra("EventId");

		mEvent = EventManager.getEvent(mEventId, true);
		member = mEvent.getParticipant(mUserId);
		mobileno = member.mobileNumber;

		Integer acceptanceStatusId = this.getIntent().getIntExtra("AcceptanceStatus", 0);

		AcceptanceStatus status = AcceptanceStatus.getStatus(acceptanceStatusId);
		mUserMenuItems = new ArrayList<NameImageItem>();

		String[] userOptions = getResources().getStringArray(R.array.running_event_user_options);
		TypedArray images = getResources().obtainTypedArray(R.array.running_event_user_options_image);
		for (int i = 0; i < userOptions.length; i++) {
			NameImageItem item = new NameImageItem(images.getResourceId(i, -1), userOptions[i], i);
			mUserMenuItems.add(item);
		}
		if (status == AcceptanceStatus.Declined || status == AcceptanceStatus.Pending) {
			mUserMenuItems.remove(1);
		} else {
			mUserMenuItems.remove(0);
		}

		NameImageAdapter adapter = new NameImageAdapter(this,
				R.layout.item_name_image_row, mUserMenuItems);
		ListView listView = (ListView) findViewById(R.id.user_menu_options);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		View view = getWindow().getDecorView();
		WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
		lp.gravity = Gravity.BOTTOM;
		lp.y = AppUtility.dpToPx(70, this);
		getWindowManager().updateViewLayout(view, lp);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		NameImageItem item = (NameImageItem) arg0.getItemAtPosition(position);
		switch (item.getImageIndex()) {
			case 0:
				onUserLocationItemMenuItemPokeClicked();
				break;

			case 1:
				onUserLocationItemMenuItemAlertClicked();
				break;
			case 2:
				onUserLocationItemMenuItemWhatsappClicked();
				break;

			case 3:
				onUserLocationItemMenuItemCallClicked();
				break;
			default:
				break;
		}
	}

	private void onUserLocationItemMenuItemWhatsappClicked() {
		boolean isWhatsappInstalled = whatsappInstalledOrNot("com.whatsapp");
		if (isWhatsappInstalled) {
			Uri uri = Uri.parse("smsto:" + mobileno);
			Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
			sendIntent.putExtra("sms_body", "Your text here!");
			sendIntent.setPackage("com.whatsapp");
			startActivity(sendIntent);
		} else {
			Toast.makeText(this, "WhatsApp not Installed",
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean whatsappInstalledOrNot(String uri) {
		PackageManager pm = getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	private void onUserLocationItemMenuItemCallClicked() {
		// TODO Auto-generated method stub	
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + mobileno));
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		mContext.startActivity(callIntent);
	}

	public void onUserLocationItemMenuItemAlertClicked(){

		EtaDistanceAlertHelper etaHelper = new EtaDistanceAlertHelper(mEventId, mUserName, mUserId, this );
		etaHelper.showSetAlertDialog();
	}

	public void onUserLocationItemMenuItemPokeClicked(){

		ParticipantService.pokeParticipant(mUserId, mUserName,mEventId, this);
	}

	@Override
	public void actionFailed(String msg, Action action){
		AppContext.actionHandler.actionFailed(msg, action);
		this.finish();
	}
	@Override
	public void actionComplete(Action action) {
		if(action!=Action.SETTIMEBASEDALERT){
			AppContext.actionHandler.actionComplete(action);
		}
		this.finish();
	}
	@Override
	public void actionCancelled(Action action){
		if(action!=Action.SETTIMEBASEDALERT){
			AppContext.actionHandler.actionCancelled(action);
		}
		this.finish();
	}
}
