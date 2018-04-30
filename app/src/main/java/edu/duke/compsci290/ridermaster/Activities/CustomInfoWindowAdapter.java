package edu.duke.compsci290.ridermaster.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import edu.duke.compsci290.ridermaster.R;

/**
 * Created by shreyasingh on 4/28/18.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        this.mContext = mContext;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void renderWindowText(Marker marker, View view){

        String title = marker.getTitle();
        TextView tvTitle = (TextView) view.findViewById(R.id.title);

        if (!title.equals("")){
            tvTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView tvSnippet = (TextView) view.findViewById(R.id.snippet);

        if (!snippet.equals("")){
            tvSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker,mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker,mWindow);
        return mWindow;
    }
}
