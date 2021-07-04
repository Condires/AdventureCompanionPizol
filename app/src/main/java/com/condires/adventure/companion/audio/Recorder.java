package com.condires.adventure.companion.audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Admin on 13-9-10.
 */
public class Recorder {

    private MediaRecorder mRecorder = null;
    private Context mContext;

    public Recorder(Context applicationContext) {
        mContext = applicationContext;
    }

    private void RecorderErr()
    {
        mRecorder = null;
        Toast.makeText(mContext, mContext.getString(R.string.msg_mic_error), Toast.LENGTH_LONG).show();
    }

    public void RecorderInit()
    {
        float bak = new CalAvg().Cal(3.0f);
        Log.d("SoundMeter", String.valueOf(bak));
        
        if (mRecorder != null)
            return;

        try
        {
            mRecorder = new MediaRecorder();
            //mRecorder.setAudioSource(1);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(1);
            mRecorder.setAudioEncoder(1);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();

        }
        catch (IllegalStateException e) {
            e.printStackTrace();
            RecorderErr();
        }
        catch (IOException e) {
            e.printStackTrace();
            RecorderErr();
        }
        catch (Exception e) {
            e.printStackTrace();
            RecorderErr();
        }

        return;
    }

    public void RecorderRelease() {

        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public float SoundDB()
    {
        float f1 = mRecorder.getMaxAmplitude();
        float f2 = 0;
        TextView localTextView;
        StringBuilder localStringBuilder;

        if (f1 > 0.0F)
        {
            f2 = (float)(20.0D * Math.log10(f1));
            Log.d("SoundMeter", "SoundDB: " + f2);
        }
        return f2;
    }

    class CalAvg {

        final int a = 4;
        float[] b = new float[a];
        int c = 0;

        private float Cal(float paramFloat)
        {
            float f3;
            if (paramFloat == 0.0F)
            {
                f3 = 0.0F;
                return f3;
            }
            c = (1 + c);
            if (c > -1 + b.length)
                c = 0;
            b[c] = paramFloat;
            float[] arrayOfFloat = b;
            int i = arrayOfFloat.length;
            int j = 0;
            float f1 = 0.0F;
            while (true)
            {
                if (j >= i)
                {
                    f3 = f1 / b.length;
                    break;
                }
                float f2 = arrayOfFloat[j];
                if (f2 == 0.0F)
                    f2 = paramFloat;
                f1 += f2;
                j++;
            }

            return f3;
        }
    }

}
