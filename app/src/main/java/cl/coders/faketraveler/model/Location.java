package cl.coders.faketraveler.model;

import androidx.annotation.NonNull;

public class Location {
    private double lat;
    private double lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double dist(@NonNull Location that) {
        double lat_2 = Math.pow(this.lat - that.lat, 2);
        double lon_2 = Math.pow(this.lon - that.lon, 2);
        return Math.sqrt(lat_2 + lon_2);
    }

    public double getAngle(@NonNull Location that) {
        double d_lat = that.lat - this.lat;
        double d_lon = that.lon - this.lon;
        return Math.atan2(d_lat, d_lon);
    }

    public void plus(@NonNull Location that) {
        this.lat += that.lat;
        this.lon += that.lon;
    }

}
