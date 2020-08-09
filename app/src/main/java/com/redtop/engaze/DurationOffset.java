package com.redtop.engaze;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.domain.Duration;

public class DurationOffset extends BaseActivity {

    private ArrayList<TextView> periods;
    private Duration duration = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_setduration);
        duration = (Duration) this.getIntent().getParcelableExtra("com.redtop.engaze.entity.Duration");

        Button save = (Button) findViewById(R.id.save_event_duration);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText intervalEditText = (EditText) findViewById(R.id.DurationValue);

                if (!intervalEditText.getText().toString().isEmpty()) {
                    try {
                        int userInput = Integer.parseInt(intervalEditText.getText().toString());
                        duration.setTimeInterval(userInput);
                        if (Duration.validateDurationInput(duration)) {
                            hideKeyboard(v);
                            Intent intent = new Intent();
                            intent.putExtra("com.redtop.engaze.entity.Duration", (Parcelable) duration);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(AppContext.context,
                                    AppContext.context.getResources().getString(R.string.message_createEvent_durationMaxAlert),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getBaseContext(),
                                getResources().getString(R.string.message_createEvent_durationMaxAlert),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(),
                            getResources().getString(R.string.event_invalid_input_message),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        EditText text = (EditText) findViewById(R.id.DurationValue);
        text.setText(Integer.toString(duration.getTimeInterval()));

        periods = new ArrayList<TextView>();
        TextView period = null;

        period = (TextView) findViewById(R.id.DurationMinutes);
        setDefaultDurationPeriod(period);
        periods.add(period);

        period = (TextView) findViewById(R.id.DurationHours);
        setDefaultDurationPeriod(period);
        periods.add(period);

        //		period = (TextView)findViewById(R.id.Weeks);
        //		setDefaultTrackingPeriod(period);
        //		periods.add(period);
        //
        //		period = (TextView)findViewById(R.id.Days);
        //		setDefaultTrackingPeriod(period);
        //		periods.add(period);


        for (int i = 0; i < periods.size(); i++) {
            TextView pr = periods.get(i);

            pr.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    for (int i = 0; i < periods.size(); i++) {
                        TextView dv = periods.get(i);
                        dv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        String duration = dv.getText().toString();
                        dv.setTextColor(getResources().getColorStateList(R.color.primaryText));
                        //						if(duration.contains(getResources().getString(R.string.duration)))
                        //						{
                        //							dv.setText(duration.substring(0, duration.indexOf(getResources().getString(R.string.duration))));
                        //							dv.setTextColor(getResources().getColorStateList(R.color.primary_text));
                        //						}

                    }
                    // TODO Auto-generated method stub
                    TextView dur = ((TextView) v);
                    Drawable draw = ContextCompat.getDrawable(mContext, R.drawable.ic_check_black_24dp);
                    dur.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
                    dur.setTextColor(getResources().getColorStateList(R.color.primary));
                    //dur.setText(dur.getText().toString().concat(getResources().getString(R.string.duration)));
                    duration.setPeriod(dur.getTag().toString());
                }
            });

        }
    }

    private void setDefaultDurationPeriod(TextView period) {
        if (period.getTag().equals(duration.getPeriod())) {
            //period.setText(period.getText().toString().concat(getResources().getString(R.string.duration)));
            period.setTextColor(getResources().getColorStateList(R.color.primary));
            Drawable draw = ContextCompat.getDrawable(mContext, R.drawable.ic_check_black_24dp);
            period.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
        }
    }
}
