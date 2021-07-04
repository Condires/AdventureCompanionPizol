package com.condires.adventure.companion.audio;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import com.condires.adventure.companion.setting.ACSettings;

public class CompanionAudioService {

    private static CompanionAudioService instance;
    private Activity activity;
    public static int theVolume;  // das eingestellte Volume, auf das wird automatisch zurückgestellt
    AudioManager am;
    public int maxVolume = 15;
    public int referenceVolume;
    public int silentVolume = 1;
    public int loudVolume = 0;
    public boolean isSilent = false;



    public CompanionAudioService(Activity activity) {
        //
        // AudioService.disableSafeMediaVolume();
        this.activity = activity;
        if (instance == null) {
            instance =  this;
        }
        isSilent = false;
        am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
    }

    public static CompanionAudioService getInstance(Activity activity) {
        if (instance == null) {
            instance = new CompanionAudioService(activity);
        }
        return instance;
    }
    public static CompanionAudioService getInstance() {
        return instance;
    }

    /*
    *  wenn der Player auf still gesetzt wird, muss das eingestellt Volumen gesichert werden
    */
    public void setSilent() {
        //loudVolume = ACSettings.getInstance().getVolume();
        //theVolume = silentVolume;
        setVolume(silentVolume);
        instance.isSilent = true;
    }
    /*
     * Wenn der Player wieder auf Laut gesetzt wird
     * Falls wir in einem temporär leisergestellten Stück sind, geht das verloren
     */
    public void setLoud() {
        loudVolume = ACSettings.getInstance().getVolume();
        //theVolume = referenceVolume;
        setVolume(loudVolume);
        instance.isSilent = false;
    }


    /*
     * verändert die Lautstärke für ein Stück
     */
    public void changeVolumeTemp(int delta) {
        // im Silent Mode wird nichts verändert
        if (!instance.isSilent) {
            int newVolume = referenceVolume + delta;
            // wenn wir schon auf dem gewünschten Volume sind, nichts machen, sinst ändern
            if (newVolume != theVolume) {
                theVolume = newVolume;
                if (theVolume > maxVolume) theVolume = maxVolume;
                am.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
                // reference Volume wird nicht verändert.
                if (theVolume >= silentVolume) instance.isSilent = false; else instance.isSilent = true;
            }
        }

    }

    // verändert die Lautstärke des Gerätes
    public void changeVolume(int delta) {
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        currVolume = currVolume + delta;
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        theVolume = currVolume;
        if (theVolume > maxVolume) theVolume = maxVolume;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0);
        referenceVolume = theVolume;
        if (theVolume >= silentVolume) instance.isSilent = false; else instance.isSilent = true;
    }

    // verändert die Lautstärke des Gerätes
    public void setVolume(int newVolume) {
        theVolume = newVolume;
        if (theVolume > maxVolume) theVolume = maxVolume;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        referenceVolume = theVolume;
        if (theVolume >= silentVolume) instance.isSilent = false; else instance.isSilent = true;
    }

    public int getVolume() {
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currVolume;
    }
}
