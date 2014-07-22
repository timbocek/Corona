package com.tbocek.sunclock;

import android.content.Context;
import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;


public class TideStationSelectorView extends LinearLayout implements TextWatcher {

    private EditText mDistance;
    private EditText mFilter;

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

        TideStationLibrary.instance().requestTideStations(
                new TideStationLibrary.TideStationsLoadedCallback() {

                    @Override
                    public void stationsLoaded(TideStationLibrary stations, boolean loadSuccess) {
                        setLoading(false);
                    }
        });

        mDistance = (EditText) findViewById(R.id.lookup_tide_station_distance);
        mFilter = (EditText) findViewById(R.id.lookup_tide_station_filter);

        mDistance.addTextChangedListener(this);
        mFilter.addTextChangedListener(this);
    }

    public void setLocation(final Location location) {
        setLoading(true);
        TideStationLibrary.instance().requestTideStations(
                new TideStationLibrary.TideStationsLoadedCallback() {

                    @Override
                    public void stationsLoaded(TideStationLibrary stations, boolean loadSuccess) {
                        stations.requestComputeDistances(location,
                                new TideStationLibrary.TideStationsLoadedCallback() {
                                    @Override
                                    public void stationsLoaded(
                                            TideStationLibrary stations, boolean loadSuccess) {
                                        setLoading(false);
                                        setStations(stations.getAllStations());
                                    }
                                });
                    }
                });
    }

    private void setStations(List<TideStationLibrary.StationStub> stations) {

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
        TideStationLibrary.instance().requestFilter(
                Float.parseFloat(mDistance.getText().toString()),
                mFilter.getText().toString(),
                new TideStationLibrary.TideStationsFilterCallback() {
                    @Override
                    public void stationsLoaded(List<TideStationLibrary.StationStub> foundStations) {
                        setStations(foundStations);
                    }
                });
    }
}
