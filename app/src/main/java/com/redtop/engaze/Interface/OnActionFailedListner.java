package com.redtop.engaze.Interface;

import com.redtop.engaze.common.enums.Action;

public interface OnActionFailedListner {
	void actionFailed(String msg, Action action);

}
