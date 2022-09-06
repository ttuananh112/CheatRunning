package cl.coders.faketraveler.control;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import cl.coders.faketraveler.model.CustomThread;
import cl.coders.faketraveler.model.Location;


public class RunningHandler {
    double MAGIC_NUMBER_LAT_Y = 110.574;  // [km/deg]
    double MAGIC_NUMBER_LON_X = 111.320;  // [km/deg]

    private ArrayList<Location> listLocation;
    private Location currentLocation;
    private int targetIdx;  // targetLocation = listLocation[targetIdx]

    private double currentSpeed;  // [km/h]
    private ArrayList<Double> rangeSpeed;  // max-min [km/h]

    private double timeInterval;  // update interval time [ms]

    private double lookaheadDistance;  // lookahead dist to consider reaching target [km]
    private boolean isFinished;

    private ArrayList<Double> jitterLat;  // [deg]
    private ArrayList<Double> jitterLon;  // [deg]

    private Random rand;

    public RunningHandler(
            ArrayList<Location> listLocation,
            double timeInterval
    ) {
        this.listLocation = listLocation;
        this.currentLocation = null;
        this.timeInterval = timeInterval;
        this.isFinished = false;
        this.lookaheadDistance = 0.005;  // 5[m]

        this.rand = new Random();

        // define jitter
        ArrayList<Double> jitterScaleInKms = new ArrayList<>(Arrays.asList(0.0, 0.0005, 0.001));
        this.jitterLat = new ArrayList<>();
        this.jitterLon = new ArrayList<>();
        for (int i = 0; i < jitterScaleInKms.size(); i++) {
            this.jitterLat.add(jitterScaleInKms.get(i) / MAGIC_NUMBER_LAT_Y);
            this.jitterLon.add(jitterScaleInKms.get(i) /
                    (MAGIC_NUMBER_LON_X * Math.cos(this.jitterLat.get(i) * Math.PI / 180)));
        }

        // define speed
        this.rangeSpeed = new ArrayList<>(Arrays.asList(5.5, 7.2));
        this.currentSpeed = 0;
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
        // get interval time in hour
        double timeInHour = (timeInterval / 1000) / 3600;  // ms -> sec -> hour
        // add random in speed
        // there's 1% chance to change speed
        if (currentSpeed == 0 || rand.nextDouble() < 0.01)
            currentSpeed = scale(rand.nextDouble(), rangeSpeed.get(0), rangeSpeed.get(1));

        // estimate distance in kms
        double distanceInKm = currentSpeed * timeInHour;

        double rad_alpha = currentLocation.getAngle(listLocation.get(targetIdx));
        // projection
        double y = distanceInKm * Math.sin(rad_alpha);
        double x = distanceInKm * Math.cos(rad_alpha);

        double distanceInLat = y / MAGIC_NUMBER_LAT_Y;  // [deg]
        double distanceInLon = x / (MAGIC_NUMBER_LON_X * Math.cos(distanceInLat * Math.PI / 180));  // [deg]

        return new Location(distanceInLat, distanceInLon);
    }

    private double scale(double value, double min, double max) {
        return value * (max - min) + min;
    }

    private void update() {
        Location d_latlon = getDistanceInterval();
        // add jitter to location
        // the partition should be
        // 0 -> 0.7: no jiggle
        // 0.7 -> 0.9: little jiggle
        // 0.9 -> 1: much jiggle
        double jitterType = rand.nextDouble();
        double jLat;
        double jLon;
        if (jitterType < 0.7) {
            jLat = scale(rand.nextDouble(), -1, 1) * jitterLat.get(0);
            jLon = scale(rand.nextDouble(), -1, 1) * jitterLon.get(0);
        } else if (jitterType < 0.9) {
            jLat = scale(rand.nextDouble(), -1, 1) * jitterLat.get(1);
            jLon = scale(rand.nextDouble(), -1, 1) * jitterLon.get(1);
        } else {
            jLat = scale(rand.nextDouble(), -1, 1) * jitterLat.get(2);
            jLon = scale(rand.nextDouble(), -1, 1) * jitterLon.get(2);
        }

        d_latlon.plus(new Location(jLat, jLon));
        currentLocation.plus(d_latlon);
        updateTargetIdx();
    }

    private void updateTargetIdx() {
        // update next target pos
        // if distance between current location and target < this.lookaheadDistance [m]
        if (currentLocation.dist(listLocation.get(targetIdx)) < lookaheadDistance / MAGIC_NUMBER_LAT_Y) {
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
