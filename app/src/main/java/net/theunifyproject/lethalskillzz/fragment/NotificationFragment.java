package net.theunifyproject.lethalskillzz.fragment;

/**
 * Created by Ibrahim on 15/10/2015.
 */
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.android.volley.AuthFailureError;
import net.android.volley.DefaultRetryPolicy;
import net.android.volley.NetworkError;
import net.android.volley.NoConnectionError;
import net.android.volley.ParseError;
import net.android.volley.Request;
import net.android.volley.Response;
import net.android.volley.ServerError;
import net.android.volley.TimeoutError;
import net.android.volley.VolleyError;
import net.android.volley.toolbox.StringRequest;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.MainActivity;
import net.theunifyproject.lethalskillzz.adapter.NotificationAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.NotificationItem;
import net.theunifyproject.lethalskillzz.util.Logout;

public class NotificationFragment extends Fragment{

    private static final String TAG = MainActivity.class.getSimpleName();
    private ConnectionDetector cd;
    public static Handler mUiHandler;
    private Logout mLogout;

    private RecyclerView rView;
    private List<NotificationItem> notificationItems;
    private NotificationAdapter notificationAdapter;
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isRefreshing = false;


    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity());

        mLogout = new Logout(getActivity());

        pref = new PrefManager(getActivity());
        //profile = pref.getUserDetails();
        notificationItems = new ArrayList<NotificationItem>();

        LinearLayoutManager lLayout = new LinearLayoutManager(getActivity());
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.notification_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView) rootView.findViewById(R.id.notification_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        setNotificationAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadNotification(String.valueOf(0));
                } else showSnackBar(0, getString(R.string.err_no_internet));
            }
        });


        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AppConfig.httpHandlerSeenNotification: {
                        if(((String)msg.obj).trim().equals("fail"));
                        else {
                            for (int i = 0; i<notificationItems.size(); i++) {
                                NotificationItem item = notificationItems.get(i);
                                if(item.getId()==Integer.parseInt(((String)msg.obj).trim()))
                                    item.setIsSeen(true);
                            }
                        }

                    }
                }
            }
        };

                        return rootView;
    }


     /* @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();

    }*/



    private void setNotificationAdapter() {
        notificationAdapter = new NotificationAdapter(getActivity(), rView, notificationItems);
        rView.setAdapter(notificationAdapter);

        notificationAdapter.setOnLoadMoreListener(new NotificationAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isRefreshing) {
                    //add progress item
                    notificationItems.add(null);
                    notificationAdapter.notifyItemInserted(notificationItems.size() - 1);
                    if (cd.isConnectingToInternet()) {
                        loadNotification(String.valueOf(notificationAdapter.getItemCount() - 1));
                    }  else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        showSnackBar(0, getString(R.string.err_no_internet));
                    }
                }
            }
        });

        if (cd.isConnectingToInternet()) {
            isRefreshing = true;
            mSwipeRefreshLayout.post(new Runnable() {
                @Override public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            loadNotification(String.valueOf(0));
        } else showSnackBar(0, getString(R.string.err_no_internet));

    }



    private void loadNotification(final String notify_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_NOTIFICATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response);

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error
                    if (!error) {

                        // parsing the feed data
                        JSONObject feedObj = responseObj.getJSONObject("data");
                        parseJsonNotification(feedObj);

                    } else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);


                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (!isRefreshing) {
                        notificationItems.remove(notificationItems.size() - 1);
                        notificationAdapter.notifyItemRemoved(notificationItems.size());
                        notificationAdapter.setLoaded();
                    } else isRefreshing = false;
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    showSnackBar(0, getString(R.string.err_network_timeout));
                } else if (error instanceof AuthFailureError) {
                    //TODO
                } else if (error instanceof ServerError) {
                    //TODO
                } else if (error instanceof NetworkError) {
                    //TODO
                } else if (error instanceof ParseError) {
                    //TODO
                }

                mSwipeRefreshLayout.setRefreshing(false);
                if (!isRefreshing) {
                    notificationItems.remove(notificationItems.size() - 1);
                    notificationAdapter.notifyItemRemoved(notificationItems.size());
                    notificationAdapter.setLoaded();
                } else isRefreshing = false;
            }
        }) {

            /**
             * Passing feed parameters to our server
             *
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("notify_pos", notify_pos);

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }



    /**
     * Parsing json response and passing the data to feed view list adapter
     */
    private void parseJsonNotification(JSONObject response) {
        try {
            JSONArray notifyArray = response.getJSONArray("notification");


            if (isRefreshing) {
                if (notifyArray.length() > 0) {
                    notificationItems.clear();
                }
                isRefreshing = false;
            } else {
                notificationItems.remove(notificationItems.size() - 1);
                notificationAdapter.notifyItemRemoved(notificationItems.size());
                notificationAdapter.setLoaded();
            }


            for (int i = 0; i < notifyArray.length(); i++) {
                JSONObject notifyObj = (JSONObject) notifyArray.get(i);

                NotificationItem item = new NotificationItem();
                item.setId(notifyObj.getInt("id"));
                item.setType(notifyObj.getInt("type"));
                item.setData(notifyObj.getString("data"));
                item.setMsg(notifyObj.getString("msg"));
                item.setIsSeen(notifyObj.getBoolean("isSeen"));
                item.setTimeStamp(notifyObj.getString("timeStamp"));

                notificationItems.add(item);
            }

            // notify data changes to feed adapater
            notificationAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.mainCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cd.isConnectingToInternet()) {
                    if (id == 0)
                        loadNotification(String.valueOf(0));
                    else
                        loadNotification(String.valueOf(notificationAdapter.getItemCount() - 1));
                }
            }
        });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

    }

}
