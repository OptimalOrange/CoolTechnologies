/**
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//stolen from http://developer.android.com/training/volley/requestqueue.html#singleton
package com.optimalorange.cooltechnologies.network;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.optimalorange.cooltechnologies.util.LruBitmapCache;

import android.content.Context;

public class VolleySingleton {

    /** 磁盘缓存大小（单位：字节） */
    private static final int MAX_DISK_CACHE_BYTES = 50 * 1024 * 1024;

    private static VolleySingleton sInstance;

    private final RequestQueue mRequestQueue;

    private final ImageLoader mImageLoader;

    private VolleySingleton(Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        context = context.getApplicationContext();

        mRequestQueue = Volley.newRequestQueue(context, MAX_DISK_CACHE_BYTES);

        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(context));
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VolleySingleton(context);
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}
