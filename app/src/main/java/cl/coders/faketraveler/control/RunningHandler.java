package cl.coders.faketraveler.control;

import java.util.ArrayList;

import cl.coders.faketraveler.model.CustomThread;
import cl.coders.faketraveler.model.Location;


public class RunningHandler {
    double MAGIC_NUMBER_LAT_Y = 110.574;  // [km/deg]
    double MAGIC_NUMBER_LON_X = 111.320;  // [km/deg]

    private ArrayList<Location> listLocation;
    private Location currentLocation;
    private int targetIdx;  // targetLocation = listLocation[targetIdx]

    private double speed;  // [km/h]
    private double timeInterval;  // update interval time [ms]

    private double lookaheadDistance;  // lookahead dist to consider reaching target [km]
    private boolean isFinished;

    public RunningHandler(
            ArrayList<Location> listLocation,
            double speed,
            double timeInterval
    ) {
        this.listLocation = listLocation;
        this.currentLocation = listLocation.get(0);
        this.speed = speed;
        this.timeInterval = timeInterval;
        this.isFinished = false;
        this.lookaheadDistance = 0.005;  // 5[m]
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public double getTimeInterval() {
        return timeInterval;
    }

    public ArrayList<Location> getListLocation() {
        return listLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public Location getDistanceInterval() {
        double timeInHour = (this.timeInterval / 1000) / 3600;  // ms -> sec -> hour
        double distanceInKm = this.speed * timeInHour;

        double rad_alpha = currentLocation.getAngle(listLocation.get(targetIdx));
        // projection
        double y = distanceInKm * Math.sin(rad_alpha);
        double x = distanceInKm * Math.cos(rad_alpha);

        double distanceInLat = y / MAGIC_NUMBER_LAT_Y;  // [deg]
        double distanceInLon = x / (MAGIC_NUMBER_LON_X * Math.cos(distanceInLat * Math.PI / 180));  // [deg]

        return new Location(distanceInLat, distanceInLon);
    }

    private void update() {
        Location d_latlon = getDistanceInterval();
        currentLocation.plus(d_latlon);
        updateTargetIdx();
    }

    private void updateTargetIdx() {
        // update next target pos
        // if distance between current location and target < this.lookaheadDistance [m]
        if (currentLocation.dist(listLocation.get(targetIdx)) < this.lookaheadDistance / MAGIC_NUMBER_LAT_Y) {
            if (targetIdx < listLocation.size() - 1)
                targetIdx += 1;
            else
                isFinished = true;
        }
    }

    public CustomThread run_thread() {
        return new CustomThread() {
            @Override
            public void run() {
                if (listLocation.size() == 0)
                    return;

                // set the first location as starting point
                currentLocation = listLocation.get(0);
                targetIdx = 0;
                updateTargetIdx();

                // interrupt if finished
                if (isFinished)
                    return;

                // update for each internal time
                while (!isFinished) {
                    if (!isRunning)
                        break;

                    update();
                    try {
                        Thread.sleep((long) timeInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
