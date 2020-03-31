package com.redtop.engaze.common.utility;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;


public class AppUtility {
	private final static String TAG = AppUtility.class.getName();

	private static  Context appContext;

	public static int deviceDensity;



	public static void setApplicationContext(Context context){
		appContext =  context;
	}
	public static Context getApplicationContext(){
		return appContext;
	}



	public static void showAlert(Context context, String title, String message) {
		if (((Activity) context).isFinishing() == false) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(title).setMessage(message).setCancelable(false);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.show();
		}
	}



	public static int getRandamNumber() {
		Random r = new Random(System.currentTimeMillis());
		int x = 10000 + r.nextInt(20000);
		return x;
	}


	public static String convertNullToEmptyString(String str) {
		if (str == null) {
			str = "";
		}
		return str;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager
		= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public int pxToDp(int px, Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return dp;
	}

	public static int dpToPx(int dp, Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static  CharSequence createTextForDisplay(CharSequence description, int maxLength) {
		//HOME_ACTIVITY_LOCATION_TEXT_LENGTH
		int maxLengthForDevice = maxLength  + (int)5*(AppUtility.deviceDensity/320 -1 );// TODO Auto-generated method stub
		if(description.length()<= maxLengthForDevice)
		{
			return description;
		}
		else
		{
			return description.toString().substring(0,maxLengthForDevice-3) + "...";
		}
	}

	public static  CharSequence createTextForLocationDisplay(CharSequence description, int lineLength , int numLines) {
		int lineCount = 0;
		int totalLength = lineLength * numLines;
		String descriptionStr = description.toString();
		String tmpStr = "";
		String singlieLinedisplayText ="";
		String[] lines = descriptionStr.split(" ");

		String resultantlines = "";
		for (String line : lines){
			tmpStr = singlieLinedisplayText + line + " ";
			if(tmpStr.length()< lineLength){
				singlieLinedisplayText = tmpStr;
			}
			else{
				lineCount += 1;
				if(lineCount <= numLines){
					resultantlines += tmpStr.substring(0, lineLength);
					singlieLinedisplayText = tmpStr.substring(lineLength, tmpStr.length());
				}
				else{
					break;
				}
			}
		}

		if(lineCount < numLines){
			resultantlines += tmpStr;
		}

		if(descriptionStr.length()<=totalLength){
			return resultantlines;
		}
		else{
			return resultantlines.substring(0,resultantlines.length()-3) + "...";
		}
	}

	/*public static void sendMsgViaWatsApp(Context context) {

		PackageManager pm=context.getPackageManager();
		try {

			Intent waIntent = new Intent(Intent.ACTION_SEND);
			waIntent.setType("text/plain");
			String text = "YOUR TEXT HERE";

			pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
			//Check if package exists or not. If not then code 
			//in catch block will be called
			waIntent.setPackage("com.whatsapp");

			waIntent.putExtra(Intent.EXTRA_TEXT, text);
			context.startActivity(Intent.createChooser(waIntent, "Share with"));

		} catch (NameNotFoundException e) {
			Toast.makeText(context, "WhatsApp not Installed", Toast.LENGTH_SHORT)
			.show();
		}
	}

	public static boolean validateDurationInput(Duration duration, Context context) {
		int userInput = duration.getTimeInterval(); 
		switch(duration.getPeriod()){
		case "minute" :
			if(userInput >=context.getResources().getInteger(R.integer.event_creation_duration_min_minutes) && userInput <= context.getResources().getInteger(R.integer.event_creation_duration_max_minutes)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_durationMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		case "hour" :
			if(userInput >0 && userInput <= context.getResources().getInteger(R.integer.event_creation_duration_max_hours)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_durationMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		return false;		
	}

	public static boolean validateTrackingInput(Duration duration, Context context) {	
		int userInput = duration.getTimeInterval(); 
		switch(duration.getPeriod()){
		case "minute" :
			if(userInput >0 && userInput <= context.getResources().getInteger(R.integer.event_tracking_start_max_minutes)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_trackingStartMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		case "hour" :
			if(userInput >0 && userInput <= context.getResources().getInteger(R.integer.event_tracking_start_max_hours)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_trackingStartMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		return false;		
	}

	public static boolean validateReminderInput(Reminder reminder, Context context) {	
		int userInput = reminder.getTimeInterval(); 
		switch(reminder.getPeriod()){
		case "minute" :
			if(userInput >0 && userInput <= context.getResources().getInteger(R.integer.event_reminder_start_max_minutes)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		case "hour" :
			if(userInput >0 && userInput <= context.getResources().getInteger(R.integer.event_reminder_start_max_hours)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		case "day" :
			if(userInput >0 && userInput <= context.getResources().getInteger(R.integer.event_reminder_start_max_days)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		case "week" :
			if(userInput >0 && userInput <= context.getResources().getInteger(R.integer.event_reminder_start_max_weeks)){
				return true;
			}
			else{
				Toast.makeText(context,							
						context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		return false;		
	}*//*

	//	public static double getDistanceBetweenTwoLocations(Location initialLoc, Location finalLoc) {
	//		//double theta =  lon1 - lon2;
	//		double theta =  initialLoc.getLongitude() - finalLoc.getLongitude();
	//		//double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	//		double dist = Math.sin(deg2rad(initialLoc.getLatitude())) * Math.sin(deg2rad(finalLoc.getLatitude())) 
	//				+ Math.cos(deg2rad(initialLoc.getLatitude()) * Math.cos(deg2rad(finalLoc.getLatitude())) * Math.cos(deg2rad(theta)));
	//		dist = Math.acos(Math.min(dist,1));
	//		dist = rad2deg(dist);
	//		dist = dist * 60 * 1.1515;
	//		return (dist);
	//	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}*/
/*



	public static void setBackgrounOfRecycleViewItem( CardView view, int colorId){
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {			
			view.setCardBackgroundColor(colorId);
			view.setRadius(0);	
			view.setMaxCardElevation(0);
			view.setPreventCornerOverlap(false);
			view.setUseCompatPadding(false);
			view.setContentPadding(-15, -15, -15, -15);
		} else {
			view.setBackgroundColor(colorId);
		}
	}
	@SuppressLint("NewApi")

	

	public static ProgressDialog showProgressBar(String title, String message, Context context ){
		ProgressDialog dialog = new ProgressDialog(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		if(!(title==null || title.equals(""))){
			dialog.setTitle(title);
		}
		dialog.setMessage(message);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setIndeterminate(true);
		dialog.show();
		return dialog;
	}
	public static void hideProgressBar( ProgressDialog dialog){
		if(dialog!=null && dialog.isShowing()){
			dialog.dismiss();
		}
	}*/
}
