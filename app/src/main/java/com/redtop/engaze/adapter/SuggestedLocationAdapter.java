package com.redtop.engaze.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.redtop.engaze.R;
import com.redtop.engaze.common.utility.AppLocationService;
import com.redtop.engaze.domain.AutoCompletePlace;

public class SuggestedLocationAdapter extends ArrayAdapter<AutoCompletePlace> {

    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private AppLocationService appLocationService;


    public SuggestedLocationAdapter(Context context, int resourceId,
                                    List<AutoCompletePlace> items,
                                    GoogleApiClient googleApiClient,
                                    AppLocationService appLocationService) {
        super(context, resourceId, items);
        this.context = context;
        this.appLocationService = appLocationService;
        this.mGoogleApiClient = googleApiClient;
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView txtName;


    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                if (constraint == null) {
                    return null;
                }
                //clear();

                displayPredictiveResults(constraint.toString());

                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (!constraint.toString().endsWith(" ")) {
                    notifyDataSetChanged();
                }
            }
        };
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        AutoCompletePlace rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_suggested_location_list, null);
            holder = new ViewHolder();

            holder.txtName = (TextView) convertView.findViewById(R.id.suggested_location_name);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();


        holder.txtName.setText(rowItem.getDescription());

        return convertView;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;

    }

    private void displayPredictiveResults(String query) {}
        //Southwest corner to Northeast corner.
       /* LatLngBounds bounds = appLocationService.getLatLongBounds
                (appLocationService.getMyLocation2(mGoogleApiClient));*/

        //Filter: https://developers.google.com/places/supported_types#table3
        List<Integer> filterTypes = new ArrayList<Integer>();
        //filterTypes.add( Place.TYPE_ESTABLISHMENT );
        ///**/filterTypes.add( Place. );

/*

        Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query, bounds,
                new AutocompleteFilter.Builder()
                        //.setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                        // .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                        .build())
                .setResultCallback(
                        new ResultCallback<AutocompletePredictionBuffer>() {
                            @Override
                            public void onResult(AutocompletePredictionBuffer buffer) {
                                OnAutocomleteSuccess(buffer);
                            }
                        }, 60, TimeUnit.SECONDS);
    }
*/

    /*private void OnAutocomleteSuccess(AutocompletePredictionBuffer buffer) {
        if (buffer == null)
            return;
        clear();

        if (buffer.getStatus().isSuccess()) {
            for (AutocompletePrediction prediction : buffer) {
                //Add as a new item to avoid IllegalArgumentsException when buffer is released
                add(new AutoCompletePlace(prediction.getPlaceId(), prediction.getFullText(null).toString()));
            }
        }

        //Prevent memory leak by releasing buffer
        buffer.release();
    }*/


}