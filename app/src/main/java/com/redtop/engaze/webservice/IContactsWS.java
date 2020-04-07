package com.redtop.engaze.webservice;


import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.domain.ContactOrGroup;
import org.json.JSONObject;

import java.util.HashMap;

public interface IContactsWS {



    void sendInvite(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess,
                                  final OnAPICallCompleteListner listnerOnFailure);

    void AssignUserIdToRegisteredUser(final HashMap<String, ContactOrGroup> contactsAndgroups,
                                                    final OnAPICallCompleteListner listnerOnSuccess,
                                                    final OnAPICallCompleteListner listnerOnFailure) ;

}
