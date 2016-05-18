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
import net.theunifyproject.lethalskillzz.adapter.FeedAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.FeedItem;
import net.theunifyproject.lethalskillzz.util.Logout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private ConnectionDetector cd;
    private Logout mLogout;
    private RecyclerView rView;
    private List<FeedItem> feedItems;
    private FeedAdapter feedAdapter;
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isRefreshing = false;


    public FeedFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);


        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity());

        mLogout = new Logout(getActivity());

        pref = new PrefManager(getActivity());
        feedItems = new ArrayList<FeedItem>();

        LinearLayoutManager lLayout = new LinearLayoutManager(getActivity());
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.feed_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        setFeedAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadFeed(String.valueOf(0));
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showSnackBar(0, getString(R.string.err_no_internet));
                }
            }
        });

        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {

                String data[] = ((String)msg.obj).trim().split(AppConfig.OTP_DELIMITER);

                switch(msg.what) {

                    case AppConfig.httpHandlerPostFeed: {

                        if (cd.isConnectingToInternet()) {
                            isRefreshing = true;
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
                            });
                            loadFeed(String.valueOf(0));
                        } else {
                            showSnackBar(0, getString(R.string.err_no_internet));
                        }
                    }
                    break;

                    case AppConfig.httpHandlerLikeFeed: {

                        for (int i = 0; i<feedItems.size(); i++) {
                            FeedItem item = feedItems.get(i);
                            if(item.getId()==Integer.parseInt(data[0]))
                                item.setLikeCount(data[1]);
                        }

                    }
                    break;

                    case AppConfig.httpHandlerPostComment: {

                        for (int i = 0; i<feedItems.size(); i++) {
                            FeedItem item = feedItems.get(i);
                            if(item.getId()==Integer.parseInt(data[0]))
                                item.setCommentCount(data[1]);
                        }
                    }
                    break;

                    case AppConfig.httpHandlerDeleteFeed: {

                        if (cd.isConnectingToInternet()) {
                            isRefreshing = true;
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
                            });
                            loadFeed(String.valueOf(0));
                        } else {
                            showSnackBar(0, getString(R.string.err_no_internet));
                        }
                    }
                    break;

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



    private void setFeedAdapter() {
        feedAdapter = new FeedAdapter(getActivity(), rView, feedItems);
        rView.setAdapter(feedAdapter);

        feedAdapter.setOnLoadMoreListener(new FeedAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isRefreshing) {
                    //add progress item
                    feedItems.add(null);
                    feedAdapter.notifyItemInserted(feedItems.size() - 1);
                    if (cd.isConnectingToInternet()) {
                        loadFeed(String.valueOf(feedAdapter.getItemCount() - 1));
                    } else showSnackBar(1, getString(R.string.err_no_internet));
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
            loadFeed(String.valueOf(0));
        } else showSnackBar(0, getString(R.string.err_no_internet));
    }


    private void loadFeed(final String feed_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_FEED, new Response.Listener<String>() {

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
                        JSONObject dataObj = responseObj.getJSONObject("data");
                        parseJsonFeed(dataObj);

                    } else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);


                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (!isRefreshing) {
                        feedItems.remove(feedItems.size() - 1);
                        feedAdapter.notifyItemRemoved(feedItems.size());
                        feedAdapter.setLoaded();
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
                    feedItems.remove(feedItems.size() - 1);
                    feedAdapter.notifyItemRemoved(feedItems.size());
                    feedAdapter.setLoaded();
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
                params.put("feed_pos", feed_pos);

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
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");


            if (isRefreshing) {
                if (feedArray.length() > 0) {
                    feedItems.clear();
                }
                isRefreshing = false;
            } else {
                feedItems.remove(feedItems.size() - 1);
                feedAdapter.notifyItemRemoved(feedItems.size());
                feedAdapter.setLoaded();
            }


            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                item.setId(feedObj.getInt("id"));
                item.setUsername(feedObj.getString("username"));
                item.setName(feedObj.getString("name"));
                item.setIsVerify(feedObj.getBoolean("isVerify"));
                item.setImage(feedObj.getString("image"));
                if(feedObj.getString("image").trim().length()!=0)
                    item.setIsExpand(false);
                item.setStatus(feedObj.getString("message"));
                item.setTimeStamp(feedObj.getString("timeStamp"));
                item.setLikeCount(String.valueOf(feedObj.getInt("likeCount")));
                item.setCommentCount(String.valueOf(feedObj.getInt("commentCount")));
                item.setIsLike(feedObj.getBoolean("isLike"));

                feedItems.add(item);

            }

            // notify data changes to feed adapater
            feedAdapter.notifyDataSetChanged();
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
                        loadFeed(String.valueOf(0));
                    else
                        loadFeed(String.valueOf(feedAdapter.getItemCount() - 1));
                }
            }
        }

            );

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();

        }

    }


