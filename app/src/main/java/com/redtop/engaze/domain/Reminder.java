package com.redtop.engaze.domain;
import android.os.Parcel; 
import android.os.Parcelable; 

public class Reminder  implements Parcelable {
	private int timeInterval;
	private String period;
	private String notificationType;
		
	public Reminder(int timeInterval, String period, String notificationType) {
		this.timeInterval = timeInterval;
		this.period = period;
		this.notificationType = notificationType;
		
	}
	public Reminder() 
	{		
	}
	public Reminder(Parcel in) 
	{ 
		readFromParcel(in); 
		} 
	
	public int getTimeInterval() {
		return timeInterval;
	}
	public void setTimeInterval(int timeInterval) {
		this.timeInterval = timeInterval;
	}
	
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	
	public String getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	
	@Override
	public String toString() {
		return notificationType + "\n";
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(timeInterval);
		dest.writeString(period); 
		dest.writeString(notificationType);
		
	}	
	
	/** * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object */ 
	private void readFromParcel(Parcel in) 
	{ 
		timeInterval = in.readInt();
		period = in.readString(); 
		notificationType = in.readString(); 
	}
	public static final Creator CREATOR = new Creator()
	{ 
		public Reminder createFromParcel(Parcel in) 
		{ 					
			return new Reminder(in); 			
			}   
		public Reminder[] newArray(int size) 
		{ 
			return new Reminder[size]; 
			} 
		}; 
}
