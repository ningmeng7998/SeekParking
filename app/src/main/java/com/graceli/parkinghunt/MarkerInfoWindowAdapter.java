package com.graceli.parkinghunt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;
    public MarkerInfoWindowAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    @Override
    public View getInfoContents(Marker arg0) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v =  inflater.inflate(R.layout.activity_marker_info_window_adapter, null);
        TextView spt = (TextView) v.findViewById(R.id.sp_title);
        TextView sps = (TextView) v.findViewById(R.id.sp_snippet);
        spt.setText("Status : " + arg0.getTitle());
        sps.setText(arg0.getSnippet());
        return v;
    }
}
