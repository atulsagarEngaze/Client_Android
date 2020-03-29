package com.redtop.engaze.domain;

public class AutoCompletePlace
{
	public AutoCompletePlace(String placeId, String description)
	{
		this.mPlaceId = placeId;
		this.mDescription = description;
	}
	private String mPlaceId;
	private String mDescription;
	
	public String getPlaceId()
	{
		return mPlaceId;
	}
	
	public void setPlaceId(String placeId)
	{
		this.mPlaceId = placeId;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public void setDescription(String description)
	{
		this.mDescription = description;
	}
	
	@Override
	public String toString() {
	   return mDescription;
	}
}