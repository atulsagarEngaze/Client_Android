package com.redtop.engaze.common.utility;

import java.util.Comparator;

import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.UsersLocationDetail;

public class Comparer implements Comparator<UsersLocationDetail> {

	@Override
	public int compare(UsersLocationDetail lhs, UsersLocationDetail rhs) {
		if(lhs.getAcceptanceStatus()==rhs.getAcceptanceStatus()){
			if (lhs.getAcceptanceStatus()== AcceptanceStatus.ACCEPTED ){
				if(lhs.currentAddress!=null && lhs.currentAddress!=""){
					if(rhs.currentAddress!=null && rhs.currentAddress!=""){
						return  lhs.getUserName().compareToIgnoreCase(rhs.getUserName());
					}
					else {
						return -1;
					}

				}
				else {
					if(rhs.currentAddress!=null && rhs.currentAddress!=""){
						return 1;
					}
					else
					{
						return  lhs.getUserName().compareToIgnoreCase(rhs.getUserName());
					}
				}
			}

			return  lhs.getUserName().compareToIgnoreCase(rhs.getUserName());
		}
		else {
			if (lhs.getAcceptanceStatus()==AcceptanceStatus.ACCEPTED ){
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}	
}
