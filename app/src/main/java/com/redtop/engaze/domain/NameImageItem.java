package com.redtop.engaze.domain;
import android.os.Parcel; 
import android.os.Parcelable; 

public class NameImageItem  implements Parcelable {
	private int imageId;
	private String name;
	private int imageIndex;

	public NameImageItem(int imageId, String name, int imageIndex) {
		this.imageId = imageId;
		this.name = name;
		this.imageIndex = imageIndex;
	}
	public NameImageItem(Parcel in) 
	{ 
		readFromParcel(in); 
	} 

	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	
	public int getImageIndex() {
		return imageIndex;
	}
	public void setImageIndex(int index) {
		this.imageIndex = index;
	}

	public String getName() {
		return name;
	}
	public void setName(String title) {
		this.name = title;
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
		dest.writeInt(imageId);	
		dest.writeInt(imageIndex);
	}	

	/** * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object */ 
	private void readFromParcel(Parcel in) 
	{   
		name = in.readString(); 
		imageId = in.readInt();
		imageIndex = in.readInt();
	}
	public static final Creator CREATOR = new Creator()
	{ 
		public NameImageItem createFromParcel(Parcel in) 
		{ 
			return new NameImageItem(in); 
		}   
		public NameImageItem[] newArray(int size) 
		{ 
			return new NameImageItem[size]; 
		} 
	}; 
}
