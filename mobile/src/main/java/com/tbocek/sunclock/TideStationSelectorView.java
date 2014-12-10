package com.tbocek.sunclock;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class TideStationSelectorView extends LinearLayout implements TextWatcher {

    private EditText mDistance;
    private EditText mFilter;
    private ListView mStationsList;

    public TideStationSelectorView(Context context) {
        super(context);
        init();
    }

    public TideStationSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TideStationSelectorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater layoutInflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.lookup_tide_station, this);

        TideStationLibrary.instance().requestTideStations(getContext(),
                new TideStationLibrary.TideStationsLoadedCallback() {

                    @Override
                    public void stationsLoaded(TideStationLibrary stations, boolean loadSuccess) {
                        setLoading(false);
                    }
        });

        mDistance = (EditText) findViewById(R.id.lookup_tide_station_distance);
        mFilter = (EditText) findViewById(R.id.lookup_tide_station_filter);
        mStationsList = (ListView) findViewById(R.id.lookup_tide_station_station_list);

        mDistance.addTextChangedListener(this);
        mFilter.addTextChangedListener(this);
    }

    public void setLocation(final Location location) {
        setLoading(true);
        TideStationLibrary.instance().requestTideStations(getContext(),
                new TideStationLibrary.TideStationsLoadedCallback() {

                    @Override
                    public void stationsLoaded(TideStationLibrary stations, boolean loadSuccess) {
                        stations.requestComputeDistances(location,
                                new TideStationLibrary.TideStationsLoadedCallback() {
                                    @Override
                                    public void stationsLoaded(
                                            TideStationLibrary stations, boolean loadSuccess) {
                                        startFilterStations();
                                    }
                                });
                    }
                });
    }

    private void setStations(List<TideStationLibrary.StationStub> stations) {
        mStationsList.setAdapter(new TideStationAdapter(getContext(), stations));

    }

    private void setLoading(boolean loading) {
        findViewById(R.id.lookup_tide_station_station_list_loading).setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
        findViewById(R.id.lookup_tide_station_station_list).setVisibility(
                loading ? View.GONE : View.VISIBLE
        );
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        startFilterStations();
    }

    private void startFilterStations() {
        TideStationLibrary.instance().requestFilter(
                Float.parseFloat(mDistance.getText().toString()) * 1000,
                mFilter.getText().toString(),
                new TideStationLibrary.TideStationsFilterCallback() {
                    @Override
                    public void stationsLoaded(List<TideStationLibrary.StationStub> foundStations) {
                        setLoading(false);
                        setStations(foundStations);
                    }
                });
    }

    private static class TideStationAdapter extends ArrayAdapter<TideStationLibrary.StationStub> {
        public TideStationAdapter(Context context, List<TideStationLibrary.StationStub> objects) {
            super(context, R.layout.lookup_tide_station_listview_item, objects);
        }


        @Override public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
                row = inflater.inflate(R.layout.lookup_tide_station_listview_item, parent, false);
            }

            ((TextView)row.findViewById(R.id.lookup_tide_station_listview_item_station_name))
                    .setText(getItem(position).getName());
            ((TextView)row.findViewById(R.id.lookup_tide_station_listview_item_distance))
                    .setText(String.format("%,.1f km", getItem(position).getDistance() / 1000));

            return row;
        }

    }
}
