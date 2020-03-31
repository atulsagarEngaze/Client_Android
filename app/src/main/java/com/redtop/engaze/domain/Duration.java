package com.redtop.engaze.domain;
import android.os.Parcel; 
import android.os.Parcelable; 

public class Duration  implements Parcelable {
	private int timeInterval;
	private String period;
	private Boolean enabled;


	public Duration(int timeInterval, String period, Boolean enabled ) {
		this.timeInterval = timeInterval;
		this.period = period;
		this.enabled = enabled;


	}
	public Duration() 
	{		
	}
	public Duration(Parcel in) 
	{ 
		readFromParcel(in); 
	} 
	
	public Boolean getTrackingState()
	{
		return enabled;
	}
	
	public void setTrackingState(Boolean enabled)
	{
		this.enabled = enabled;
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


	@Override
	public String toString() {
		return period + "\n";
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

	}	

	/** * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object */ 
	private void readFromParcel(Parcel in) 
	{ 
		timeInterval = in.readInt();
		period = in.readString(); 		
	}
	public static final Creator CREATOR = new Creator()
	{ 
		public Duration createFromParcel(Parcel in) 
		{ 					
			return new Duration(in); 			
		}   
		public Duration[] newArray(int size) 
		{ 
			return new Duration[size]; 
		} 
	}; 
}
