package com.redtop.engaze.Interface;

import java.util.Hashtable;

import com.redtop.engaze.domain.ContactOrGroup;


public interface OnRefreshMemberListCompleteListner {
	void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList);

}
