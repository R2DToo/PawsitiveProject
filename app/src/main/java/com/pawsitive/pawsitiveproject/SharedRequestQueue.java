package com.pawsitive.pawsitiveproject;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton pattern to handle the volley request queue
 */
public class SharedRequestQueue {
    private final String TAG = "api-req";

    private static SharedRequestQueue queue;
    private RequestQueue requestQueue;
    private static Context context;

    private SharedRequestQueue(Context ctx) {
        context = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized SharedRequestQueue getInstance(Context ctx) {
        if (queue == null) {
            queue = new SharedRequestQueue(ctx);
        }
        return queue;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
