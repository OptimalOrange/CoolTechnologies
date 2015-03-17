// 参考自http://fernandocejas.com/2014/08/03/aspect-oriented-programming-in-android/
package com.optimalorange.cooltechnologies.debug;

import com.optimalorange.cooltechnologies.util.StopWatch;

import org.aspectj.lang.reflect.MethodSignature;

import android.app.Activity;
import android.util.Log;

public aspect PerformanceMonitor {

    private static final String LOG_TAG = "Performance";

    private final StopWatch mStopWatch = new StopWatch();

    private pointcut monitoredClass():
            this(Activity) || this(android.support.v4.app.Fragment) || this(android.app.Fragment);

    private pointcut monitoredMethod(): monitoredClass() && execution(* on*(..));

    Object around(): monitoredMethod() {
        mStopWatch.start();
        Object result = proceed();
        mStopWatch.stop();
        final String methodName = ((MethodSignature) thisJoinPointStaticPart.getSignature())
                .getMethod().toGenericString();
        final String logMessage = buildLogMessage(methodName, mStopWatch.getTotalTimeMillis());
        if (mStopWatch.getTotalTimeMillis() <= 100) {
            Log.d(LOG_TAG, logMessage);
        } else {
            Log.w(LOG_TAG, logMessage);
        }
        return result;
    }

    private static String buildLogMessage(String methodName, long methodDuration) {
        return String.format("[%dms]%s", methodDuration, methodName);
    }

}
