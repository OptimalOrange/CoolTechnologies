// 参考自http://fernandocejas.com/2014/08/03/aspect-oriented-programming-in-android/
package com.optimalorange.cooltechnologies.util;

import java.util.concurrent.TimeUnit;

public class StopWatch {

    private long mStartTime;

    private long mStopTime;

    private void reset() {
        mStartTime = 0;
        mStopTime = 0;
    }

    public void start() {
        reset();
        mStartTime = System.nanoTime();
    }

    public void stop() {
        if (mStartTime != 0) {
            mStopTime = System.nanoTime();
        } else {
            reset();
        }
    }

    public long getTotalTimeMillis() {
        long elapsedTime =  mStopTime - mStartTime;
        return (elapsedTime != 0) ? TimeUnit.NANOSECONDS.toMillis(elapsedTime) : 0;
    }

}
