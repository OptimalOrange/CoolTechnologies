package com.optimalorange.cooltechnologies.network;

import com.android.volley.Request;
import com.android.volley.Response;

/**
 * A request for retrieving a T type response body at a given URL.
 *
 * @param <T> The type of parsed response this request expects.
 */
public abstract class SimpleRequest<T> extends Request<T> {

    private final Response.Listener<T> mListener;

    public SimpleRequest(int method, String url, Response.Listener<T> listener,
            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

}
