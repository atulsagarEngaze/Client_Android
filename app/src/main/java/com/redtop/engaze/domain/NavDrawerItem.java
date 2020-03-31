package com.redtop.engaze.domain;

import android.graphics.drawable.Drawable;

public class NavDrawerItem {
	private boolean showNotify;
	private String title;
	private int titleIcon;
	private int titleIconFont;

	public NavDrawerItem() {

	}

	public NavDrawerItem(boolean showNotify, String title) {
		this.showNotify = showNotify;
		this.title = title;
	}

	public boolean isShowNotify() {
		return showNotify;
	}

	public void setShowNotify(boolean showNotify) {
		this.showNotify = showNotify;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTitleIcon() {
		return titleIcon;
	}

	public void setTitleIcon(int titleIcon) {
		this.titleIcon = titleIcon;
	}
	
	public int getTitleIconFont() {
		return titleIconFont;
	}

	public void setTitleIconFont(int titleIconFont) {
		this.titleIconFont = titleIconFont;
	}

	
	
	
}