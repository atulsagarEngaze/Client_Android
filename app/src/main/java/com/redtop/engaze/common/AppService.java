package com.redtop.engaze.common;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;


public class AppService {
	private final static String TAG = AppService.class.getName();

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

	public static int getIncrementedNotificationId(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		int notificationId = preferences.getInt("notificationId",0);
		if(notificationId == 0 || notificationId == 99999999 ){
			notificationId =1;
		}
		else
		{
			notificationId = notificationId +1;
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("notificationId", notificationId);
		editor.commit();

		return notificationId;
	}



	public static String checkNull(String str) {
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

	public static Bitmap overlayBitmapToCenter(Bitmap bitmap1, Bitmap bitmap2) {
		int bitmap1Width = bitmap1.getWidth();
		int bitmap1Height = bitmap1.getHeight();
		int bitmap2Width = bitmap2.getWidth();
		int bitmap2Height = bitmap2.getHeight();

		float marginLeft = (float) (bitmap1Width * 0.5 - bitmap2Width * 0.5);
		float marginTop = (float) (bitmap1Height * 0.5 - bitmap2Height * 0.664);

		Bitmap overlayBitmap = Bitmap.createBitmap(bitmap1Width, bitmap1Height, bitmap1.getConfig());
		Canvas canvas = new Canvas(overlayBitmap);
		canvas.drawBitmap(bitmap1, new Matrix(), null);
		canvas.drawBitmap(bitmap2, marginLeft, marginTop, null);
		return overlayBitmap;
	}

	public static Bitmap overlayBitmapToCenterOfPin(Bitmap bitmap1, Bitmap bitmap2) {
		int bitmap1Width = bitmap1.getWidth();
		int bitmap1Height = bitmap1.getHeight();
		int bitmap2Width = bitmap2.getWidth();
		int bitmap2Height = bitmap2.getHeight();

		float marginLeft = (float) (bitmap1Width * 0.5 - bitmap2Width * 0.5);
		float marginTop = (float) (bitmap1Height * 0.5 - bitmap2Height * 1.00);

		Bitmap overlayBitmap = Bitmap.createBitmap(bitmap1Width, bitmap1Height, bitmap1.getConfig());
		Canvas canvas = new Canvas(overlayBitmap);
		canvas.drawBitmap(bitmap1, new Matrix(), null);
		canvas.drawBitmap(bitmap2, marginLeft, marginTop, null);
		return overlayBitmap;
	}
	public static Bitmap getCroppedBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
				bitmap.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		//Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
		//return _bmp;
		return output;
	}	



	public static Bitmap generateCircleBitmapForText(Context context, int circleColor, float diameterDP, String text){
		final int textColor = 0xffffffff;
		DisplayMetrics metrics =Resources.getSystem().getDisplayMetrics();
		float diameterPixels = diameterDP * (metrics.densityDpi / 160f);
		float radiusPixels = diameterPixels/2;

		// Create the bitmap
		Bitmap output = Bitmap.createBitmap((int) diameterPixels, (int) diameterPixels,
				Config.ARGB_8888);

		// Create the canvas to draw on
		Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);

		// Draw the circle
		final Paint paintC = new Paint();
		paintC.setAntiAlias(true);
		paintC.setColor(circleColor);
		canvas.drawCircle(radiusPixels, radiusPixels, radiusPixels, paintC);	   

		// Draw the text
		if (text != null && text.length() > 0) {
			final Paint paintT = new Paint();
			paintT.setColor(textColor);
			paintT.setAntiAlias(true);
			paintT.setTextSize(radiusPixels * (float)1.2);
			Typeface typeFace = Typeface.DEFAULT;// Typeface.createFromAsset(context.getAssets(),"fonts/Roboto-Thin.ttf");
			paintT.setTypeface(typeFace);
			final Rect textBounds = new Rect();
			paintT.getTextBounds(text, 0, text.length(), textBounds);
			canvas.drawText(text, radiusPixels - textBounds.exactCenterX(), radiusPixels - textBounds.exactCenterY(), paintT);
		}

		return output;
	}


	public static Bitmap generateCircleBitmapForIcon(Context context, int circleColor, float diameterDP, Uri uri){
		DisplayMetrics metrics =Resources.getSystem().getDisplayMetrics();
		float diameterPixels = diameterDP * (metrics.densityDpi / 160f);
		float radiusPixels = diameterPixels/2;
		// Create the bitmap	    

		Bitmap output = Bitmap.createBitmap((int) diameterPixels, (int) diameterPixels,
				Config.ARGB_8888);

		// Create the canvas to draw on
		Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);

		// Draw the circle
		final Paint paintC = new Paint();
		paintC.setAntiAlias(true);
		paintC.setColor(circleColor);
		canvas.drawCircle(radiusPixels, radiusPixels, radiusPixels, paintC);

		Bitmap bitmap=null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int canWidth = canvas.getWidth();
		int canheight = canvas.getHeight();
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,canWidth-35, canheight-35, true);

		canvas.drawBitmap(scaledBitmap, (canWidth- scaledBitmap.getWidth())/2, (canheight- scaledBitmap.getHeight())/2, paintC);

		return output;
	}

	public static Bitmap generateCircleBitmapForImage(Context context,  float diameterDP, Uri uri) {

		DisplayMetrics metrics =Resources.getSystem().getDisplayMetrics();
		float diameterPixels = diameterDP * (metrics.densityDpi / 160f);	   

		Bitmap bitmap=null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Bitmap output = Bitmap.createBitmap((int) diameterPixels, (int) diameterPixels,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(output);		

		Bitmap roundBitmap = getRoundedCroppedBitmap(bitmap, (int) diameterPixels);
		canvas.drawBitmap(roundBitmap, 0, 0, null); 
		return output;

	}

	public static Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
		Bitmap finalBitmap;
		if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
			finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
					false);
		else
			finalBitmap = bitmap;
		Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
				finalBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
				finalBitmap.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawCircle(finalBitmap.getWidth() / 2 + 0.7f,
				finalBitmap.getHeight() / 2 + 0.7f,
				finalBitmap.getWidth() / 2 + 0.1f, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(finalBitmap, rect, rect, paint);

		return output;
	}

	private static List<Integer> materialColors = Arrays.asList(
			0xffe57373,
			0xfff06292,
			0xffba68c8,
			0xff9575cd,
			0xff7986cb,
			0xff64b5f6,
			0xff4fc3f7,
			0xff4dd0e1,
			0xff4db6ac,
			0xff81c784,
			0xffaed581,
			0xffff8a65,
			0xffd4e157,
			0xffffd54f,
			0xffffb74d,
			0xffa1887f,
			0xff90a4ae
			);

	public static int getMaterialColor(Object key) {
		return materialColors.get(Math.abs(key.hashCode()) % materialColors.size());
	}

	public static boolean isParticipantCurrentUser(String userId, Context context) {
		if(PreffManager.getPref(Constants.LOGIN_ID, context).equalsIgnoreCase(userId)){
			return true;
		}
		// TODO Auto-generated method stub
		return false;
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
	*//*public static Boolean shouldShareLocation(Context context){
		List<EventDetail> events = InternalCaching.getEventListFromCache(context);
		List<EventDetail> trackingEvents = InternalCaching. getTrackEventListFromCache(context);
		if(events==null){
			return false;
		}
		for(EventDetail ed : events){
			if(ed.getCurrentMember().getAcceptanceStatus()==AcceptanceStatus.ACCEPTED 
					&& ed.getState().equals(Constants.TRACKING_ON)
					){
				return true;
			}
		}
		if(trackingEvents==null){
			return false;
		}
		for(EventDetail ed : trackingEvents){
			if(AppUtility.IsEventShareMyLocationEventForCurrentuser(ed, context)){
				return true;
			}
		}
		return false;
	}

	public static Boolean isAnyEventInState(Context context, String state, Boolean checkOnlyWhenEventAccepted){
		List<EventDetail> events = InternalCaching.getEventListFromCache(context);
		if(events==null){
			return false;
		}
		for(EventDetail ed : events){
			if(ed.getState().equals(state)){
				if(checkOnlyWhenEventAccepted)
				{

					if(ed.getCurrentMember().getAcceptanceStatus()==AcceptanceStatus.ACCEPTED 
							){
						return true;
					}
				}
				else
				{
					return true;
				}
			}
		}
		return false;

	}	

	public static boolean isCurrentUserInitiator(String initiatorId, Context context){
		if(PreffManager.getPref(Constants.LOGIN_ID, context).equalsIgnoreCase(initiatorId)){
			return true;
		}
		return false;	
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
	public static Boolean isNotifyUser( EventDetail event){
		if(event!=null && event.getCurrentMember().getAcceptanceStatus()== AcceptanceStatus.DECLINED){
			return false;
		}
		return true;		
	}

	public static Boolean isValidForLocationSharing(EventDetail event, EventMember mem, Context context){
		Boolean isValid = true;		
		if(Integer.parseInt(event.getEventTypeId())==200 && 
				AppUtility.isCurrentUserInitiator(event.getInitiatorId(), context)&&
				AppUtility.isParticipantCurrentUser(mem.getUserId(), context)
				){
			isValid = false;
		}

		if(Integer.parseInt(event.getEventTypeId())==100 &&
				!AppUtility.isCurrentUserInitiator(event.getInitiatorId(), context) &&
				!mem.getUserId().equalsIgnoreCase(event.getInitiatorId())){
			isValid=false;
		}
		return isValid;
	}
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
	public static void setRippleDrawable(ImageView view, Context context, int rippleResourceDrawableId){
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {	
			view.setBackground(context.getResources().getDrawable(rippleResourceDrawableId));
		} else {
			view.setBackground(context.getDrawable(rippleResourceDrawableId));
		}
	}
	
	public static boolean IsEventTrackBuddyEventForCurrentuser(EventDetail mEvent,
			Context context) {
		int eventTypeId = Integer.parseInt(mEvent.getEventTypeId());
		boolean isCurrentUserInitiator = isCurrentUserInitiator(mEvent.getInitiatorId(), context);

		if((isCurrentUserInitiator && eventTypeId==200) ||
				(!isCurrentUserInitiator && eventTypeId==100)){
			return true; 
		}
		return false;
	}
	
	public static boolean IsEventShareMyLocationEventForCurrentuser(EventDetail mEvent,
			Context context) {
		int eventTypeId = Integer.parseInt(mEvent.getEventTypeId());
		boolean isCurrentUserInitiator = isCurrentUserInitiator(mEvent.getInitiatorId(), context);

		if((isCurrentUserInitiator && eventTypeId==100) ||
				(!isCurrentUserInitiator && eventTypeId==200)){
			return true; 
		}
		return false;
	}
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

	public static void sendSms(String phonenumber, String message,
							   boolean isBinary) {
		//phonenumber = "0" + phonenumber;
		Log.d("TAG", "no " + phonenumber);
		Log.d("TAG", "message " + message);
		SmsManager manager = SmsManager.getDefault();
		if (isBinary) {
			if (null != message) {
				message = message.trim();
			}
			byte[] data = new byte[message.length()];
			for (int index = 0; index < message.length(); ++index) {
				data[index] = (byte) message.charAt(index);
			}

			manager.sendDataMessage(phonenumber, null,
					(short)Integer.parseInt(Constants.SMS_PORT), data, null, null);
			Log.d("Sending sms", "smsdata sent");

		} else {
			int length = message.length();
			if (length > Constants.MAX_SMS_MESSAGE_LENGTH) {
				ArrayList<String> messagelist = manager.divideMessage(message);
				manager.sendMultipartTextMessage(phonenumber, null,
						messagelist, null, null);
			} else {
				manager.sendTextMessage(phonenumber, null, message, null, null);
				Log.d("Sending sms", "sms sent");
			}
		}
	}
}
