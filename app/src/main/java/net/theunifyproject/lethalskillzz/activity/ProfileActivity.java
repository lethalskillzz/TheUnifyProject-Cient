package net.theunifyproject.lethalskillzz.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import net.android.volley.toolbox.ImageLoader;
import net.android.volley.toolbox.StringRequest;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.adapter.FeedAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.FeedItem;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.util.Logout;
import net.theunifyproject.lethalskillzz.widget.ProfileImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private Logout mLogout;

    private RecyclerView rView;
    private List<FeedItem> feedItems;
    private FeedAdapter feedAdapter;
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String username;
    private boolean isRefreshing;
    private boolean isProfileSet;
    boolean isFollow, hasStore;

    private ConnectionDetector cd;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private ProfileImageView profilePic;
    private TextView acadInfo, location, following, followers, btn_follow;
    private FloatingActionButton fab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();


        Uri data = getIntent().getData();
        if(data != null){
            String uri = data.toString();
            String uname=uri.split("/")[2];
            //if(uname.length()<=15)
                username = uname;
        }else {
            Intent intent = getIntent();
            username = intent.getStringExtra("username");
        }


        profilePic = (ProfileImageView)findViewById(R.id.layout_profilePic);
        acadInfo = (TextView)findViewById(R.id.layout_profileAcadInfo);
        location = (TextView)findViewById(R.id.layout_profileLocation);
        following = (TextView)findViewById(R.id.layout_profileFollowing);
        followers = (TextView)findViewById(R.id.layout_profileFollowers);
        btn_follow = (TextView)findViewById(R.id.layout_profile_follow_btn);
        fab = (FloatingActionButton)findViewById(R.id.profile_store_fab);

        profilePic.setOnClickListener(this);
        following.setOnClickListener(this);
        followers.setOnClickListener(this);
        btn_follow.setOnClickListener(this);
        fab.setOnClickListener(this);

        feedItems  =  new ArrayList<FeedItem>();

        LinearLayoutManager lLayout = new LinearLayoutManager(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.profile_feed_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView)findViewById(R.id.profile_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        if (cd.isConnectingToInternet()) {
            loadProfileDetail(username);
        }else {
            showSnackBar(2, getString(R.string.err_no_internet));
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
             if(isProfileSet) {
                 if (cd.isConnectingToInternet()) {
                     isRefreshing = true;
                     loadFeed(username, String.valueOf(0));
                 } else {
                     mSwipeRefreshLayout.setRefreshing(false);
                     showSnackBar(0, getString(R.string.err_no_internet));
                 }
             }
            }
        });


        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {

                String data[] = ((String)msg.obj).trim().split(AppConfig.OTP_DELIMITER);

                switch(msg.what) {

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

                    case AppConfig.httpHandlerFollowUser: {

                        isFollow=Boolean.parseBoolean(data[1]);
                        if(isFollow) {
                            btn_follow.setText(R.string.btn_unfollow);
                            btn_follow.setBackgroundResource(R.drawable.btn_unfollow_blue);
                        } else {
                            btn_follow.setText(R.string.btn_follow);
                            btn_follow.setBackgroundResource(R.drawable.btn_follow_green);
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
                            loadFeed(username, String.valueOf(0));
                        } else {
                            showSnackBar(0, getString(R.string.err_no_internet));
                        }
                    }
                    break;

                    case AppConfig.httpHandlerReportUser: {

                    }
                    break;
                }
            }

        };


    }


   /* @Override
    public void onResume() {
        super.onResume();

        if (cd.isConnectingToInternet()) {
            loadProfileDetail(username);
        }else {
            showSnackBar(2, getString(R.string.err_no_internet));
        }
    }*/

    @Override
    public void onClick(View view) {
        if(isProfileSet) {
            switch (view.getId()) {

                case R.id.profile_store_fab: {

                    Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
                break;

                case R.id.layout_profileFollowing: {

                    Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("list_type", AppConfig.listFollowing);
                    startActivity(intent);

                }
                break;

                case R.id.layout_profileFollowers: {

                    Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("list_type", AppConfig.listFollowers);
                    startActivity(intent);

                }
                break;

                case R.id.layout_profile_follow_btn: {

                    Intent grapprIntent = new Intent(ProfileActivity.this, HttpService.class);
                    grapprIntent.putExtra("intent_type", AppConfig.httpIntentFollowUser);
                    grapprIntent.putExtra("username", username);

                    if(isFollow) {
                        this.btn_follow.setText(R.string.btn_follow);
                        this.btn_follow.setBackgroundResource(R.drawable.btn_follow_green);
                        grapprIntent.putExtra("follow_type", "unfollow");
                        isFollow = false;
                    } else {
                        this.btn_follow.setText(R.string.btn_unfollow);
                        this.btn_follow.setBackgroundResource(R.drawable.btn_unfollow_blue);
                        grapprIntent.putExtra("follow_type", "follow");
                        isFollow = true;
                    }
                    startService(grapprIntent);
                }
                break;

                case R.id.layout_profilePic: {
                    Intent intent = new Intent(getApplicationContext(), ProfilePicActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                }

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(pref.getUsername().equals(username))
           getMenuInflater().inflate(R.menu.menu_my_profile, menu);
        else
            getMenuInflater().inflate(R.menu.menu_profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {

            case R.id.menu_profile_edit: {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
            }
            return true;

            case R.id.menu_account_edit: {
                Intent intent = new Intent(getApplicationContext(), EditAccountActivity.class);
                startActivity(intent);
            }
            return true;

            case R.id.menu_profile_report:
                showReportUserDialog();
                return true;

            case R.id.menu_store_edit: {
                Intent intent = new Intent(getApplicationContext(), StoreSettingActivity.class);
                startActivity(intent);
            }
            return true;
        }



        return super.onOptionsItemSelected(item);
    }



    private void setupProfile(String name, String isVerify, String mobile, String acadInfo,
                              String location, String following, String followers, boolean isFollow) {

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        profilePic.setImageUrl(AppConfig.URL_PROFILE_PIC + username + ".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
        profilePic.setVisibility(View.VISIBLE);
        profilePic.setResponseObserver(new ProfileImageView.ResponseObserver() {
            @Override
            public void onError() {
                profilePic.setImageResource(R.drawable.ic_user);
            }

            @Override
            public void onSuccess() {
            }
        });

        this.acadInfo.setText(acadInfo);
        this.location.setText(location);
        this.following.setText(following+" following" );
        this.followers.setText(followers + " followers");

        if(!username.equals(pref.getUsername())) {
            this.btn_follow.setVisibility(View.VISIBLE);
            if(isFollow) {
                this.btn_follow.setText(R.string.btn_unfollow);
                this.btn_follow.setBackgroundResource(R.drawable.btn_unfollow_blue);
            } else {
                this.btn_follow.setText(R.string.btn_follow);
                this.btn_follow.setBackgroundResource(R.drawable.btn_follow_green);
            }
        }else this.btn_follow.setVisibility(View.GONE);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.profile_collapsing_toolbar);
        collapsingToolbar.setTitle(name);

        if(hasStore) {
            CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            p.setAnchorId(R.id.profile_app_bar_layout);
            fab.setLayoutParams(p);
            fab.setVisibility(View.VISIBLE);

        }

        isProfileSet = true;
        setFeedAdapter();
    }


    /**
     * function to load user's profile details in mysql db
     * @param target_username user unique name
     */
    private void loadProfileDetail(final String target_username) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_PROFILE_DETAIL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error, if not error SMS is initiated
                    // device should receive it shortly
                    if (!error) {

                        // parsing the user profile information
                        JSONObject profileObj = responseObj.getJSONObject("profile");
                        JSONArray profileArray = profileObj.getJSONArray("detail");
                        JSONObject detailObj = (JSONObject) profileArray.get(0);

                        String name = detailObj.getString("name");
                        String isVerify = detailObj.getString("isVerify");
                        String mobile = detailObj.getString("mobile");
                        String acadInfo = detailObj.getString("acadInfo");
                        String location = detailObj.getString("location");
                        String following = String.valueOf(detailObj.getInt("following"));
                        String followers = String.valueOf(detailObj.getInt("followers"));
                        hasStore = detailObj.getBoolean("hasStore");
                        isFollow = detailObj.getBoolean("isFollow");

                        setupProfile(name, isVerify, mobile, acadInfo,location,following,followers, isFollow);

                        // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();


                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());

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

                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();


            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("target_username", target_username);
                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);

    }


    private void setFeedAdapter() {
        feedAdapter = new FeedAdapter(this, rView, feedItems);
        rView.setAdapter(feedAdapter);

        feedAdapter.setOnLoadMoreListener(new FeedAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (isRefreshing == false) {
                    //add progress item
                    feedItems.add(null);
                    feedAdapter.notifyItemInserted(feedItems.size() - 1);

                    if (cd.isConnectingToInternet()) {
                        loadFeed(username, String.valueOf(feedAdapter.getItemCount() - 1));
                    } else {
                        showSnackBar(1, getString(R.string.err_no_internet));
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
            loadFeed(username, String.valueOf(0));
        }else {
            showSnackBar(0, getString(R.string.err_no_internet));
        }
    }


    private void loadFeed(final String target_username, final String feed_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_PROFILE_FEED, new Response.Listener<String>() {

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
                        parseJsonFeed(feedObj);

                    }else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (isRefreshing == false) {
                        feedItems.remove(feedItems.size() - 1);
                        feedAdapter.notifyItemRemoved(feedItems.size());
                        feedAdapter.setLoaded();
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
                    feedItems.remove(feedItems.size() - 1);
                    feedAdapter.notifyItemRemoved(feedItems.size());
                    feedAdapter.setLoaded();
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
     * */
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            if (isRefreshing == true) {
                if(feedArray.length()>0) {
                    feedItems.clear();
                }
                isRefreshing = false;
            }else {
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


    @SuppressLint("ValidFragment")
    private class ReportUserDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            //builder.setMessage(LoginActivity.noneterrormsg)
            final View reportLyout = getLayoutInflater().inflate(R.layout.fragment_report_user, null);
            builder.setView(reportLyout)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            EditText input_report = (EditText) reportLyout.findViewById(R.id.input_report_user);
                            String report = input_report.getText().toString();
                            if(report.length()!=0) {
                                Intent grapprIntent = new Intent(ProfileActivity.this, HttpService.class);
                                grapprIntent.putExtra("intent_type", AppConfig.httpIntentReportUser);
                                grapprIntent.putExtra("username", username);
                                grapprIntent.putExtra("report", report);
                                startService(grapprIntent);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


    private void showReportUserDialog() {
        DialogFragment newFragment = new ReportUserDialogFragment();
        newFragment.show(getFragmentManager(), "rateDlg");

    }



    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.profileCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cd.isConnectingToInternet()) {
                    if (id == 0)
                        loadFeed(username, String.valueOf(0));
                    else if (id == 1)
                        loadFeed(username, String.valueOf(feedAdapter.getItemCount() - 1));
                    else loadProfileDetail(username);
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
