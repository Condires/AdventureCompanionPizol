package com.condires.adventure.companion;

/**
 * Allows {@link MainActivity} to control media playback of {@link LocationTrackerActivity}.
 */
public interface LocationTracker {


    //public void initializeLocationTracker();

    //void release();

    double getLongitude();

    double getLatitude();

    float getSpeed();

    double getAltitude();

    float getDirection();



    //void initializeProgressCallback();




}
