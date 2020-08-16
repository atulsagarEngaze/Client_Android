package com.redtop.engaze.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.BaseActivity;
import com.redtop.engaze.BaseEventActivity;
import com.redtop.engaze.Interface.FragmentToActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.domain.Duration;

import java.util.ArrayList;

public class DurationOffsetFragment extends DialogFragment {
    private ArrayList<TextView> periods;
    private Duration duration = null;
    private Context mContext;
    private FragmentToActivity callBackToActivity;
    public DurationOffsetFragment() {
        // Required empty public constructor
    }

    public static DurationOffsetFragment newInstance(Duration duration) {

        DurationOffsetFragment fragment = new DurationOffsetFragment();
        Bundle args = new Bundle();
        args.putParcelable("Duration", duration);
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

        duration = getArguments().getParcelable("Duration");
        TextView save = view.findViewById(R.id.save_event_duration);
        save.setOnClickListener(v -> {
            EditText intervalEditText = view.findViewById(R.id.DurationValue);

            if (!intervalEditText.getText().toString().isEmpty()) {
                try {
                    int userInput = Integer.parseInt(intervalEditText.getText().toString());
                    duration.setTimeInterval(userInput);
                    if (Duration.validateDurationInput(duration)) {
                        ((BaseActivity)mContext).hideKeyboard(v);
                        callBackToActivity.communicate(duration, this);
                        dismiss();
                    } else {
                        Toast.makeText(AppContext.context,
                                AppContext.context.getResources().getString(R.string.message_createEvent_durationMaxAlert),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(mContext,
                            getResources().getString(R.string.message_createEvent_durationMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mContext,
                        getResources().getString(R.string.event_invalid_input_message),
                        Toast.LENGTH_LONG).show();
            }
        });

        EditText text = view.findViewById(R.id.DurationValue);
        text.setText(Integer.toString(duration.getTimeInterval()));

        periods = new ArrayList<>();
        TextView period = null;

        period = view.findViewById(R.id.DurationMinutes);
        setDefaultDurationPeriod(period);
        periods.add(period);

        period = view.findViewById(R.id.DurationHours);
        setDefaultDurationPeriod(period);
        periods.add(period);

        for (int i = 0; i < periods.size(); i++) {
            TextView pr = periods.get(i);

            pr.setOnClickListener(v -> {

                for (int i1 = 0; i1 < periods.size(); i1++) {
                    TextView dv = periods.get(i1);
                    dv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    dv.setTextColor(getResources().getColorStateList(R.color.primaryText));
                }
                TextView dur = ((TextView) v);
                Drawable draw = ContextCompat.getDrawable(mContext, R.drawable.ic_check_black_24dp);
                dur.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
                dur.setTextColor(getResources().getColorStateList(R.color.primary));
                duration.setPeriod(dur.getTag().toString());
            });
        }


        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_duration_offset, container, false);
    }

    private void setDefaultDurationPeriod(TextView period) {
        if (period.getTag().equals(duration.getPeriod())) {
            period.setTextColor(getResources().getColorStateList(R.color.primary));
            Drawable draw = ContextCompat.getDrawable(mContext, R.drawable.ic_check_black_24dp);
            period.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
        }
    }
}