package com.redtop.engaze.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.BaseEventActivity;
import com.redtop.engaze.Interface.FragmentToActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivityResults;
import com.redtop.engaze.domain.Duration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExtendEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExtendEventFragment extends DialogFragment {

    private Context mContext;
    private FragmentToActivity callBackToActivity;
    private Duration snoozeDuration = null;
    private EditText text;

    public ExtendEventFragment() {
        // Required empty public constructor
    }


    public static ExtendEventFragment newInstance() {
        ExtendEventFragment fragment = new ExtendEventFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        text = (EditText) view.findViewById(R.id.SnoozeValue);
        text.setText(Integer.toString(snoozeDuration.getTimeInterval()));

        TextView save = view.findViewById(R.id.save_event_Snooze);
        save.setOnClickListener(view1 -> {
            if (!text.getText().toString().isEmpty()) {
                try {
                    int userInput = Integer.parseInt(text.getText().toString());
                    //if(AppUtility.validateDurationInput(snoozeDuration, getBaseContext())){
                    if (userInput >= getResources().getInteger(R.integer.runningevent_min_extend_minutes) && userInput <= getResources().getInteger(R.integer.runningevent_max_extend_minutes)) {
                        snoozeDuration.setTimeInterval(userInput);
                        callBackToActivity.communicate(snoozeDuration, this);
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.message_runningEvent_extendDurationValidation),
                                Toast.LENGTH_LONG).show();

                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.message_runningEvent_extendDurationValidation),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.event_invalid_input_message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        callBackToActivity = (RunningEventActivityResults) mContext;
        snoozeDuration = new Duration(30, "minute", true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_extend_event, container, false);
    }
}