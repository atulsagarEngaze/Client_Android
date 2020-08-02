package com.redtop.engaze.Interface;

import org.json.JSONObject;

public interface OnAPICallCompleteListener<T> {
	void apiCallSuccess(T response);
	void apiCallFailure();

}
