package cl.coders.faketraveler;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import static cl.coders.faketraveler.MainActivity.SourceChange.CHANGE_FROM_MAP;

import cl.coders.faketraveler.control.RunningHandler;
import cl.coders.faketraveler.model.Location;

import java.util.ArrayList;


public class WebAppInterface {
    MainActivity mainActivity;
    ArrayList<Location> listLoc = new ArrayList<>();
    RunningHandler rh;

    WebAppInterface(Context c, MainActivity mA) {
        mainActivity = mA;
    }

    public void resetRunningHandler() {
        rh = new RunningHandler(listLoc, 100);
    }


    /**
     * Set position in GUI. This method is called by javascript when there is a long press in the map.
     *
     * @param str String containing lat and lng
     * @return Void
     */
    @JavascriptInterface
    public void setPosition(final String str) {

        mainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String lat = str.substring(str.indexOf('(') + 1, str.indexOf(','));
                String lng = str.substring(str.indexOf(',') + 2, str.indexOf(')'));

                MainActivity.setLatLng(lat, lng, CHANGE_FROM_MAP);
            }
        });
    }

    @JavascriptInterface
    public void addListLocation(String lat, String lon) {
        double d_lat = Double.parseDouble(lat);
        double d_lon = Double.parseDouble(lon);
        listLoc.add(new Location(d_lat, d_lon));
    }

    @JavascriptInterface
    public void resetListLocation() {
        listLoc = new ArrayList<Location>();
    }


    /**
     * Get last latitude. This method is called by javascript at page load.
     *
     * @return The last latitude or 0 if it haven't been set.
     */
    @JavascriptInterface
    public double getLat() {

        String lat = MainActivity.getLat();

        if (lat.isEmpty()) {
            return (0);
        } else {
            return (Double.parseDouble(lat));
        }
    }

    /**
     * Get last longitude. This method is called by javascript at page load.
     *
     * @return The last longitude or 0 if it haven't been set.
     */
    @JavascriptInterface
    public double getLng() {

        String lng = MainActivity.getLng();

        if (lng.isEmpty()) {
            return (0);
        } else {
            return (Double.parseDouble(lng));
        }

    }

}