package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import net.theunifyproject.lethalskillzz.adapter.UserListAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.UserListItem;
import net.theunifyproject.lethalskillzz.util.Logout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserListActivity extends AppCompatActivity {

    public static Handler mUiHandler;
    private static final String TAG = UserListActivity.class.getSimpleName();
    private Logout mLogout;
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private ConnectionDetector cd;


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rView;
    private List<UserListItem> userListItems;
    private UserListAdapter userListAdapter;

    private String list_type;
    private String username;
    private String feedId;
    private boolean isRefreshing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.user_list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mLogout = new Logout(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();
        userListItems = new ArrayList<UserListItem>();

        Intent intent = getIntent();
        list_type  = intent.getStringExtra("list_type");
        if(list_type.equals(AppConfig.listFollowing)) {
            toolbar.setTitle("Following");
        } else if(list_type.equals(AppConfig.listFollowers)) {
            toolbar.setTitle("Followers");
        } else if(list_type.equals(AppConfig.listLike)) {
            toolbar.setTitle("Likes");
        } else {

        }


        LinearLayoutManager lLayout = new LinearLayoutManager(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.user_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView)findViewById(R.id.user_list_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        userListAdapter = new UserListAdapter(this, rView, userListItems);
        rView.setAdapter(userListAdapter);


        if(list_type.equals(AppConfig.listFollowing) || list_type.equals(AppConfig.listFollowers)) {
            username = intent.getStringExtra("username");

            if (cd.isConnectingToInternet()) {
                isRefreshing = true;
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                loadUserListFollow(username, String.valueOf(0), list_type);
            } else {
                showSnackBar(2, "No internet connection!");
            }

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    if (cd.isConnectingToInternet()) {
                        isRefreshing = true;
                        loadUserListFollow(username, String.valueOf(0), list_type);
                    }  else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        showSnackBar(0, getString(R.string.err_no_internet));
                    }
                }
            });

            userListAdapter.setOnLoadMoreListener(new UserListAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (isRefreshing == false) {
                        //add progress item
                        userListItems.add(null);
                        userListAdapter.notifyItemInserted(userListItems.size() - 1);
                        if (cd.isConnectingToInternet()) {
                            loadUserListFollow(username, String.valueOf(userListAdapter.getItemCount()-1), list_type);
                        } else {
                            showSnackBar(3, getString(R.string.err_no_internet));
                        }
                    }
                }
            });
        }else if(list_type.equals(AppConfig.listLike)) {
            feedId = intent.getStringExtra("feedId");

            if (cd.isConnectingToInternet()) {
                isRefreshing = true;
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                loadUserListFeed(feedId, String.valueOf(0), list_type);
            } else {
                showSnackBar(0, getString(R.string.err_no_internet));
            }

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    if (cd.isConnectingToInternet()) {
                        isRefreshing = true;
                        loadUserListFeed(feedId, String.valueOf(0), list_type);
                    }  else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        showSnackBar(0, getString(R.string.err_no_internet));
                    }
                }
            });

            userListAdapter.setOnLoadMoreListener(new UserListAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (isRefreshing == false) {
                        //add progress item
                        userListItems.add(null);
                        userListAdapter.notifyItemInserted(userListItems.size() - 1);
                        if (cd.isConnectingToInternet()) {
                            loadUserListFeed(feedId, String.valueOf(userListAdapter.getItemCount()-1), list_type);
                        } else {
                            showSnackBar(1, getString(R.string.err_no_internet));
                        }
                    }
                }
            });
        }

        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {

                String data[] = ((String)msg.obj).trim().split(AppConfig.OTP_DELIMITER);

                switch (msg.what) {

                    case AppConfig.httpHandlerFollowUser: {

                        for (int i = 0; i<userListItems.size(); i++) {
                            UserListItem item = userListItems.get(i);
                            if((item.getUsername()!=null))
                                if((item.getUsername().equals(data[0])))
                                    item.setIsFollow(Boolean.parseBoolean(data[1]));
                        }

                    }
                    break;

                }
            }

        };


    }





    private void loadUserListFollow(final String target_username, final String list_pos, final String list_type) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_FOLLOW, new Response.Listener<String>() {

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
                        parseJsonUser(dataObj);

                    }else {

                    }
                    mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (isRefreshing == false) {
                        userListItems.remove(userListItems.size() - 1);
                        userListAdapter.notifyItemRemoved(userListItems.size());
                        userListAdapter.setLoaded();
                    }else isRefreshing = false;
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    showSnackBar(2, getString(R.string.err_network_timeout));
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
                if (isRefreshing == false) {
                    userListItems.remove(userListItems.size() - 1);
                    userListAdapter.notifyItemRemoved(userListItems.size());
                    userListAdapter.setLoaded();
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
                params.put("target_username", target_username);
                params.put("list_pos", list_pos);
                params.put("list_type", list_type);

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



    private void loadUserListFeed(final String feedId, final String list_pos, final String list_type) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_LIKE, new Response.Listener<String>() {

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
                        parseJsonUser(feedObj);

                    }else {

                    }
                    mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (isRefreshing == false) {
                        userListItems.remove(userListItems.size() - 1);
                        userListAdapter.notifyItemRemoved(userListItems.size());
                        userListAdapter.setLoaded();
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
                if (isRefreshing == false) {
                    userListItems.remove(userListItems.size() - 1);
                    userListAdapter.notifyItemRemoved(userListItems.size());
                    userListAdapter.setLoaded();
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
                params.put("feedId", feedId);
                params.put("list_pos", list_pos);
                params.put("list_type", list_type);

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
     * Parsing json response and passing the data to user view list adapter
     * */
    private void parseJsonUser(JSONObject response) {

        try {


            JSONArray userArray = response.getJSONArray("user");

            if (isRefreshing == false) {
                userListItems.remove(userListItems.size() - 1);
                userListAdapter.notifyItemRemoved(userListItems.size());
                userListAdapter.setLoaded();
            }else {
                if (userArray.length() > 0) {
                    userListItems.clear();
                }
                isRefreshing = false;
            }



            for (int i = 0; i < userArray.length(); i++) {
                JSONObject userObj = (JSONObject) userArray.get(i);

                UserListItem item = new UserListItem();
                item.setUsername(userObj.getString("username"));
                item.setName(userObj.getString("name"));
                item.setIsVerify(userObj.getBoolean("isVerify"));
                item.setInfo(userObj.getString("info"));
                item.setIsFollow(userObj.getBoolean("isFollow"));

                userListItems.add(item);
            }


            // notify data changes to feed adapter
            userListAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.userlistCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cd.isConnectingToInternet()) {
                    if (id == 0)
                        loadUserListFeed(feedId, String.valueOf(0), list_type);
                    else if (id == 1)
                        loadUserListFeed(feedId, String.valueOf(userListAdapter.getItemCount()-1), list_type);
                    else if (id == 2)
                        loadUserListFollow(username, String.valueOf(0), list_type);
                    else
                        loadUserListFollow(username, String.valueOf(userListAdapter.getItemCount()-1), list_type);
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
