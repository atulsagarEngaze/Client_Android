package com.redtop.engaze.domain;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;

import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;

// this can be a person from contact list or can be a group which will be resolved to actual contact at server
public class ContactOrGroup  implements Parcelable, Serializable{

	private String name;
	private String userId;
	private ArrayList<String> numbers;	
	private String number;
	private int groupId;
	private String bitmap;
	private static String appUserIconBitmap;
	private String iconBitmap;
	private String thumbnailuri;

	public ContactOrGroup( String name, int groupId, String userId) {
		this.numbers = new ArrayList<String>();
		this.name = name;
		this.groupId = groupId;
		this.userId = userId;
	}

	public ContactOrGroup() 
	{ 	
		this.numbers = new ArrayList<String>();
	} 
	public ContactOrGroup(Parcel in) 
	{ 
		readFromParcel(in); 
	}


	public static Drawable getAppUserIconDrawable()
	{
		return new BitmapDrawable(AppContext.context.getResources(),getAppUserIconBitmap());
	}

	public static void setAppUserIconBitmap(Bitmap bm)
	{
		ByteArrayOutputStream baos=new  ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG,100, baos);
		byte [] b=baos.toByteArray();
		appUserIconBitmap=Base64.encodeToString(b, Base64.DEFAULT);
	}

	public static Bitmap getAppUserIconBitmap()
	{
		try
		{

			if(appUserIconBitmap==null || appUserIconBitmap.equals(""))
			{
				Bitmap bm = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(AppContext.context.getContentResolver(),Uri.parse("android.resource://com.redtop.engaze/drawable/profile27")),100,100, true);
				Paint paint = new Paint();
				ColorFilter filter = new PorterDuffColorFilter(AppContext.context.getResources().getColor(R.color.primaryLight), android.graphics.PorterDuff.Mode.SRC_ATOP );
				paint.setColorFilter(filter);

				Canvas canvas = new Canvas(bm);
				canvas.drawBitmap(bm, 0, 0, paint);

				setAppUserIconBitmap(bm);

				//setAppUserIconBitmap(AppUtility.generateCircleBitmapForIcon(context, context.getResources().getColor(R.color.primary_light), 40, Uri.parse("android.resource://com.redtop.engaze/drawable/profile27")));								
			}

			byte [] encodeByte=Base64.decode(appUserIconBitmap,Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);	
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Drawable getIconImageDrawable(Context context)
	{
		try
		{
			return new BitmapDrawable(context.getResources(),getIconImageBitmap(context));			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void setIconImageBitmap(Bitmap bm)
	{
		ByteArrayOutputStream baos=new  ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG,100, baos);
		byte [] b=baos.toByteArray();
		this.iconBitmap=Base64.encodeToString(b, Base64.DEFAULT);

	}

	public Bitmap getIconImageBitmap(Context context)
	{
		byte [] encodeByte=Base64.decode(this.iconBitmap,Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);		
	}

	public Drawable getImageDrawable(Context context)
	{
		try
		{
			return new BitmapDrawable(context.getResources(),getImageBitmap(context));
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void setImageBitmap(Bitmap bm)
	{
		ByteArrayOutputStream baos=new  ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG,100, baos);
		byte [] b=baos.toByteArray();
		this.bitmap=Base64.encodeToString(b, Base64.DEFAULT);

	}

	public Bitmap getImageBitmap(Context context)
	{
		byte [] encodeByte=Base64.decode(this.bitmap,Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);		
	}

	public String getUserId(){
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	};

	public String getName(){
		return name;
	}

	public void setName(String contactName)
	{
		name = contactName;
	};
	
	public String getMobileNumber(){
		return number;
	}

	public void setMobileNumber(String mobileNumber)
	{
		number = mobileNumber;
	};

	public ArrayList<String> getNumbers(){
		return numbers;
	}

	public void setNumbers(ArrayList<String> mobileNumbers)
	{
		numbers = mobileNumbers;
	};

	public void setNumber(String mobileNumber)
	{
		numbers.add(mobileNumber);
	};

	public int getGroupId(){
		return groupId;
	}

	public void setGroupId(int groudId)
	{
		this.groupId = groudId;
	}
	
	public String getThumbnailUri(){
		return thumbnailuri;
	}

	public void setThumbnailUri(String uri)
	{
		this.thumbnailuri = uri;
	}

	@Override
	public String toString() {
		return name + "\n";
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(name); 
		dest.writeStringList(numbers);
		dest.writeInt(groupId);	
		dest.writeString(userId);
		dest.writeString(bitmap);
		dest.writeString(iconBitmap);
	}	

	/** * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object */ 
	private void readFromParcel(Parcel in) 
	{   
		name = in.readString(); 
		in.readStringList(numbers);
		groupId = in.readInt();
		userId = in.readString();
		bitmap = in.readString();
		iconBitmap = in.readString();


	}
	public static final Creator CREATOR = new Creator()
	{ 
		public ContactOrGroup createFromParcel(Parcel in) 
		{ 
			return new ContactOrGroup(in); 
		}   
		public ContactOrGroup[] newArray(int size) 
		{ 
			return new ContactOrGroup[size]; 
		} 
	};

}
