package com.redtop.engaze.domain;

import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;

import android.os.Parcel;
import android.os.Parcelable;

// this can be a person from contact list or can be a group which will be resolved to actual contact at server
public class EventPlace  implements Parcelable, Serializable{
	@Expose
	private String name;
	@Expose
	private String address;
	@Expose
	private transient  LatLng latLang;
	@Expose
	private double latitude;
	@Expose
	private double longitude;
	
	public EventPlace( String name, String address, LatLng latLang) {
		
		this.name = name;
		this.address = address;
		this.latLang = latLang;
		this.latitude= latLang.latitude;
		this.longitude =latLang.longitude;
	}	
	 
	public EventPlace(Parcel in) 
	{ 
		readFromParcel(in); 
	}
	
	public String getName(){
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	};
	
	public String getAddress(){
		return this.address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	};
	
	public LatLng getLatLang(){
		return this.latLang;
	}

	public void setLatLang(LatLng latlang)
	{
		this.latLang = latlang;
		this.latitude = latlang.latitude;
		this.longitude = latlang.longitude;
	};
	
	public double getLatitude(){
		return this.latitude;
	}	

	public double getLongitude()
	{
		return this.longitude;
	};
	

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
		dest.writeString(this.name); 
		dest.writeString(this.address);
		dest.writeParcelable(this.latLang, flags);			
	}	
	
	/** * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object */ 
	private void readFromParcel(Parcel in) 
	{   
		this.name = in.readString(); 
		this.address = in.readString();
		this.latLang = in.readParcelable(LatLng.class.getClassLoader());
		this.latitude = this.latLang.latitude;
		this.longitude = this.latLang.longitude;		
	}
	
	public void createLatLangFromLatLangField(){
		this.setLatLang(new LatLng(this.latitude, this.longitude));
	}
	
	public static final Creator CREATOR = new Creator()
	{ 
		public EventPlace createFromParcel(Parcel in) 
		{ 
			return new EventPlace(in); 
		}   
		public EventPlace[] newArray(int size) 
		{ 
			return new EventPlace[size]; 
		} 
	};
	
}
