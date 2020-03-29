package com.redtop.engaze.manager;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.redtop.engaze.R;
import com.redtop.engaze.common.AppService;
import com.redtop.engaze.constant.Constants;
import com.redtop.engaze.webservice.SMSService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SMSManager {

    private static final String SMS_PORT = "8901";
    private static final String TAG = SMSManager.class.getName();

    private static void sendSmsFromDevice(String phonenumber, String message,
                               boolean isBinary) {
        //phonenumber = "0" + phonenumber;
        Log.d("TAG", "no " + phonenumber);
        Log.d("TAG", "message " + message);
        SmsManager manager = SmsManager.getDefault();
        if (isBinary) {
            if (null != message) {
                message = message.trim();
            }
            byte[] data = new byte[message.length()];
            for (int index = 0; index < message.length(); ++index) {
                data[index] = (byte) message.charAt(index);
            }

            manager.sendDataMessage(phonenumber, null,
                    (short)Integer.parseInt(SMS_PORT), data, null, null);
            Log.d("Sending sms", "smsdata sent");

        } else {
            int length = message.length();
            if (length > Constants.MAX_SMS_MESSAGE_LENGTH) {
                ArrayList<String> messagelist = manager.divideMessage(message);
                manager.sendMultipartTextMessage(phonenumber, null,
                        messagelist, null, null);
            } else {
                manager.sendTextMessage(phonenumber, null, message, null, null);
                Log.d("Sending sms", "sms sent");
            }
        }
    }

    private static void sendSMSFromWebService(String smsText, String mobileNumber, Context context) {
        try {
            // making json object request
            JSONObject mJRequestobj = new JSONObject();

            mJRequestobj.put("CountryCodeForSMS", "+91");
            mJRequestobj.put("ContactNumberForSMS", mobileNumber);
            mJRequestobj.put("MessageForSMS", smsText);
            if(!AppService.isNetworkAvailable(context))
            {
                String message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
                Log.d(TAG, message);
                return ;

            }
            SMSService.callSMSGateway(context, mJRequestobj);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());

            Toast.makeText(context,
                    context.getResources().getString(R.string.message_smsGateway_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static void sendSMS(String countryCode, String mobileNumber, String smsText, Context context){
        if(countryCode.equals("+91")){

            sendSmsFromDevice(mobileNumber, smsText, false);
        }
        else{
            //call API
            sendSMSFromWebService(smsText, mobileNumber, context);
        }
    }
}
