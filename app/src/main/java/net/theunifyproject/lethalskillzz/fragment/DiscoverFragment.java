package net.theunifyproject.lethalskillzz.fragment;

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
import net.theunifyproject.lethalskillzz.adapter.MultiItemAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.MultiItem;
import net.theunifyproject.lethalskillzz.util.Logout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ibrahim on 07/11/2015.
 */
public class DiscoverFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private ConnectionDetector cd;
    private Logout mLogout;


    private RecyclerView rView;
    private List<MultiItem> multiItems;
    private MultiItemAdapter multiItemAdapter;
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isRefreshing = false;


    public DiscoverFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity());

        mLogout = new Logout(getActivity());

        pref = new PrefManager(getActivity());
        //profile = pref.getUserDetails();
        multiItems =  new ArrayList<MultiItem>();

        LinearLayoutManager lLayout = new LinearLayoutManager(getActivity());
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.discover_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView)rootView.findViewById(R.id.discover_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        setMultiItemAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadDiscover(String.valueOf(0));
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

                switch (msg.what) {

                    case AppConfig.httpHandlerFollowUser: {

                        for (int i = 0; i<multiItems.size(); i++) {
                            MultiItem item = multiItems.get(i);
                            if((item.getUsername()!=null))
                                if((item.getUsername().equals(data[0])))
                                    item.setIsFollow(Boolean.parseBoolean(data[1]));
                        }

                    }
                    break;


                    case AppConfig.httpHandlerLikeFeed: {

                        for (int i = 0; i<multiItems.size(); i++) {
                            MultiItem item = multiItems.get(i);
                            if(item.getId()==Integer.parseInt(data[0]))
                                item.setLikeCount(data[1]);
                        }

                    }
                    break;

                    case AppConfig.httpHandlerPostComment: {

                        for (int i = 0; i<multiItems.size(); i++) {
                            MultiItem item = multiItems.get(i);
                            if(item.getId()==Integer.parseInt(data[0]))
                                item.setCommentCount(data[1]);
                        }
                    }
                    break;

                    case AppConfig.httpHandlerDeleteFeed: {

                        if (cd.isConnectingToInternet()) {
                            isRefreshing = true;
                            loadDiscover(String.valueOf(0));
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



    private void setMultiItemAdapter() {
        multiItemAdapter = new MultiItemAdapter(getActivity(), rView, multiItems);
        rView.setAdapter(multiItemAdapter);

        multiItemAdapter.setOnLoadMoreListener(new MultiItemAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isRefreshing) {
                    //add progress item
                    multiItems.add(null);
                    multiItemAdapter.notifyItemInserted(multiItems.size() - 1);

                    if (cd.isConnectingToInternet()) {
                        loadDiscover(String.valueOf(multiItemAdapter.getItemCount()-1));
                    }else showSnackBar(1, getString(R.string.err_no_internet));
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
            loadDiscover(String.valueOf(0));
        }else showSnackBar(0, getString(R.string.err_no_internet));

    }



    private void loadDiscover(final String discover_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_DISCOVER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());

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
                        parseJsonUserFeedHash(feedObj);

                    }else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (!isRefreshing) {
                        multiItems.remove(multiItems.size() - 1);
                        multiItemAdapter.notifyItemRemoved(multiItems.size());
                        multiItemAdapter.setLoaded();
                    }else isRefreshing = false;
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
                    multiItems.remove(multiItems.size() - 1);
                    multiItemAdapter.notifyItemRemoved(multiItems.size());
                    multiItemAdapter.setLoaded();
                }else isRefreshing = false;
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
                params.put("discover_pos", discover_pos);

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
     * */
    private void parseJsonUserFeedHash(JSONObject response) {

        try {

            if (isRefreshing) {
                if ( (response.getJSONArray("user")!=null) || (response.getJSONArray("feed")!=null) || (response.getJSONArray("hashtag")!=null) ) {
                    multiItems.clear();
                }
                isRefreshing = false;
            }else {
                multiItems.remove(multiItems.size() - 1);
                multiItemAdapter.notifyItemRemoved(multiItems.size());
                multiItemAdapter.setLoaded();
            }

            if (response.getJSONArray("user")!=null) {

                JSONArray userArray = response.getJSONArray("user");
                for (int i = 0; i < userArray.length(); i++) {
                    JSONObject userObj = (JSONObject) userArray.get(i);

                    MultiItem item = new MultiItem();
                    item.setType(userObj.getInt("type"));
                    item.setUsername(userObj.getString("username"));
                    item.setName(userObj.getString("name"));
                    item.setIsVerify(userObj.getBoolean("isVerify"));
                    item.setInfo(userObj.getString("info"));
                    item.setIsFollow(userObj.getBoolean("isFollow"));

                    multiItems.add(item);
                }
            }

            if (response.getJSONArray("feed")!=null) {

                JSONArray feedArray = response.getJSONArray("feed");
                for (int i = 0; i < feedArray.length(); i++) {
                    JSONObject feedObj = (JSONObject) feedArray.get(i);

                    MultiItem item = new MultiItem();
                    item.setType(feedObj.getInt("type"));
                    item.setId(feedObj.getInt("id"));
                    item.setUsername(feedObj.getString("username"));
                    item.setName(feedObj.getString("name"));
                    item.setIsVerify(feedObj.getBoolean("isVerify"));
                    item.setImage(feedObj.getString("image"));
                    if (feedObj.getString("image").trim().length() != 0)
                        item.setIsExpand(false);
                    item.setStatus(feedObj.getString("message"));
                    item.setTimeStamp(feedObj.getString("timeStamp"));
                    item.setLikeCount(String.valueOf(feedObj.getInt("likeCount")));
                    item.setCommentCount(String.valueOf(feedObj.getInt("commentCount")));
                    item.setIsLike(feedObj.getBoolean("isLike"));

                    multiItems.add(item);
                }
            }

            if (response.getJSONArray("hashtag")!=null) {

                JSONArray hashTagArray = response.getJSONArray("hashtag");
                for (int i = 0; i < hashTagArray.length(); i++) {
                    JSONObject hashObj = (JSONObject) hashTagArray.get(i);

                    MultiItem item = new MultiItem();
                    item.setType(hashObj.getInt("type"));
                    item.setHash(hashObj.getString("hash"));
                    item.setCount(hashObj.getString("count"));

                    multiItems.add(item);
                }
            }

            // notify data changes to feed adapater
            multiItemAdapter.notifyDataSetChanged();
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
                        loadDiscover(String.valueOf(0));
                    else
                        loadDiscover(String.valueOf(multiItemAdapter.getItemCount() - 1));
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

