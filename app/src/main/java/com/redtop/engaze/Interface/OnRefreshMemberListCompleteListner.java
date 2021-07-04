package com.redtop.engaze.Interface;

import java.util.HashMap;

import com.redtop.engaze.domain.ContactOrGroup;


public interface OnRefreshMemberListCompleteListner {
	void RefreshMemberListComplete(HashMap<String, ContactOrGroup> memberList);

}
