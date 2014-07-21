package com.tbocek.sunclock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;


public class TideStationSelectorView extends LinearLayout {

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
                        findViewById(R.id.lookup_tide_station_station_list_loading).setVisibility(
                                View.GONE
                        );
                        findViewById(R.id.lookup_tide_station_station_list).setVisibility(
                                View.VISIBLE
                        );


                    }
        });
    }
}
