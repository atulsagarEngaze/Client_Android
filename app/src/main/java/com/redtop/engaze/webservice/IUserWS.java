package com.redtop.engaze.webservice;


import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.domain.ContactOrGroup;

import org.json.JSONObject;

import java.util.HashMap;

public interface IUserWS {

    void saveProfile(JSONObject jRequestobj,
                     final OnAPICallCompleteListener onAPICallCompleteListener);

    void sendInvite(JSONObject jsonObject, final OnAPICallCompleteListener onAPICallCompleteListener);

    void AssignUserIdToRegisteredUser(final HashMap<String, ContactOrGroup> contactsAndgroups,
                                      final OnAPICallCompleteListener onAPICallCompleteListener);

}
