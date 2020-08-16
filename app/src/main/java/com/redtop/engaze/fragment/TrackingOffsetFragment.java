package com.redtop.engaze.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.redtop.engaze.BaseActivity;
import com.redtop.engaze.BaseEventActivity;
import com.redtop.engaze.Interface.FragmentToActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.domain.Duration;

import java.util.ArrayList;

public class TrackingOffsetFragment extends DialogFragment {
    private ArrayList<TextView> periods;
    private Duration tracking = null;
    private Context mContext;
    private FragmentToActivity callBackToActivity;
    public TrackingOffsetFragment() {
    }

    public static TrackingOffsetFragment newInstance(Duration duration) {

        TrackingOffsetFragment fragment = new TrackingOffsetFragment();
        Bundle args = new Bundle();
        args.putParcelable("Tracking", duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        callBackToActivity = (BaseEventActivity) mContext;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tracking = getArguments().getParcelable("Tracking");
        TextView save = view.findViewById(R.id.save_event_track);
        save.setOnClickListener(v -> {
            EditText intervalEditText = view.findViewById(R.id.TrackingValue);
            if (!intervalEditText.getText().toString().isEmpty()){
                try{
                    int userInput = Integer.parseInt(intervalEditText.getText().toString());
                    tracking.setTimeInterval(userInput);
                    if(Duration.validateTrackingInput(tracking)){
                        ((BaseActivity)mContext).hideKeyboard(v);
                        callBackToActivity.communicate(tracking, this);
                        dismiss();

                    }
                }catch(NumberFormatException e){
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.message_createEvent_trackingStartMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.event_invalid_input_message),
                        Toast.LENGTH_LONG).show();
            }
        });

        EditText text = view.findViewById(R.id.TrackingValue);
        text.setText(Integer.toString(tracking.getTimeInterval()));

        periods = new ArrayList<>();
        TextView period = null;

        period = view.findViewById(R.id.Minutes);
        setDefaultTrackingPeriod(period);
        periods.add(period);

        period = view.findViewById(R.id.Hours);
        setDefaultTrackingPeriod(period);
        periods.add(period);



        for(int i=0;i<periods.size();i++){
            TextView pr = periods.get(i);

            pr.setOnClickListener(v -> {

                for(int i1 = 0; i1 <periods.size(); i1++){
                    TextView dv = periods.get(i1);
                    dv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    String duration = dv.getText().toString();
                    if(duration.contains(getResources().getString(R.string.before)))
                    {
                        dv.setText(duration.substring(0, duration.indexOf(getResources().getString(R.string.before))));
                        dv.setTextColor(getResources().getColorStateList(R.color.primaryText));
                    }

                }
                TextView  dur = ((TextView)v);
                Drawable draw = getResources().getDrawable(R.drawable.ic_check_black_24dp);
                dur.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
                dur.setTextColor(getResources().getColorStateList(R.color.primary));
                dur.setText(dur.getText().toString().concat(getResources().getString(R.string.before)));
                tracking.setPeriod(dur.getTag().toString());
            });
        }


        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracking_offset, container, false);
    }

    private void setDefaultTrackingPeriod(TextView period) {
        if(period.getTag().equals(tracking.getPeriod()))
        {
            period.setText(period.getText().toString().concat(getResources().getString(R.string.before)));
            period.setTextColor(getResources().getColorStateList(R.color.primary));
            Drawable draw = getResources().getDrawable(R.drawable.ic_check_black_24dp);
            period.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
        }
    }
}