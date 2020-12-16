package com.redtop.engaze.Interface;

import com.redtop.engaze.common.enums.Action;

import org.json.JSONException;

public interface OnActionCompleteListner {
	void actionComplete(Action action) throws JSONException;
}
