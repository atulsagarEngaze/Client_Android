package com.redtop.engaze.common.utility;

import java.util.Comparator;

import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.UsersLocationDetail;

public class Comparer implements Comparator<UsersLocationDetail> {

	@Override
	public int compare(UsersLocationDetail lhs, UsersLocationDetail rhs) {
		if(lhs.acceptanceStatus==rhs.acceptanceStatus){
			if (lhs.acceptanceStatus== AcceptanceStatus.ACCEPTED ){
				if(lhs.currentAddress!=null && lhs.currentAddress!=""){
					if(rhs.currentAddress!=null && rhs.currentAddress!=""){
						return  lhs.userName.compareToIgnoreCase(rhs.userName);
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
						return  lhs.userName.compareToIgnoreCase(rhs.userName);
					}
				}
			}

			return  lhs.userName.compareToIgnoreCase(rhs.userName);
		}
		else {
			if (lhs.acceptanceStatus==AcceptanceStatus.ACCEPTED ){
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}	
}
