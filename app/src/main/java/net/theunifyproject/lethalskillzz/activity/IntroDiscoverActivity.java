package net.theunifyproject.lethalskillzz.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
import net.android.volley.toolbox.JsonObjectRequest;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.adapter.UserSelectAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.UserSelectItem;
import net.theunifyproject.lethalskillzz.util.Logout;

public class IntroDiscoverActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = IntroDiscoverActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private ConnectionDetector cd;
    private Logout mLogout;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rView;
    private List<UserSelectItem> userSelectItems;
    private UserSelectAdapter userSelectAdapter;
    private PrefManager pref;
    private boolean isRefreshing = false;
    public static TextView btn_follow;
    private ProgressBar progressBar;
    private boolean isReady;
    public static int selectCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_discover);


        mLogout = new Logout(this);

        // creating connection detector class instance
        cd = new ConnectionDetector(this);

        pref = new PrefManager(this);
        //profile = pref.getUserDetails();
        userSelectItems =  new ArrayList<UserSelectItem>();

        btn_follow = (TextView) findViewById(R.id.intro_discover_follow_btn);
        btn_follow.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.intro_discover_progressBar);

        LinearLayoutManager lLayout = new LinearLayoutManager(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.intro_discover_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView)findViewById(R.id.intro_discover_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        setAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadIntroDiscover(String.valueOf(0));
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showSnackBar(0, getString(R.string.err_no_internet));
                }
            }
        });


    }


    @Override
    public void onClick(View view) {
        if(isReady) {
            switch (view.getId()) {

                case R.id.intro_discover_follow_btn: {

                    /*int selectCount = 0;
                    for (int i = 0; i<userSelectItems.size(); i++) {
                        UserSelectItem item = userSelectItems.get(i);
                        if ((item.getIsSelect())) {
                            selectCount +=1;
                        }
                    }*/

                    if(selectCount>=5) {
                        btn_follow.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        followIntroDiscover();
                    }
                }
                break;
            }
        }
    }




    private void setAdapter() {
        userSelectAdapter = new UserSelectAdapter(this, rView, userSelectItems);
        rView.setAdapter(userSelectAdapter);

        userSelectAdapter.setOnLoadMoreListener(new UserSelectAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isRefreshing) {
                    //add progress item
                    userSelectItems.add(null);
                    userSelectAdapter.notifyItemInserted(userSelectItems.size() - 1);

                    if (cd.isConnectingToInternet()) {
                        loadIntroDiscover(String.valueOf(userSelectAdapter.getItemCount() - 1));
                    } else
                        showSnackBar(1, getString(R.string.err_no_internet));
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
            loadIntroDiscover(String.valueOf(0));
        }else
            showSnackBar(0, getString(R.string.err_no_internet));

    }

    public JSONObject phoneContacts(String discover_pos) {

        JSONArray arr = new JSONArray();
        JSONObject obj2 = new JSONObject();

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //Query phone here.  Covered next

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +"= ?",new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        // Do something with phones
                        String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));

                        try{
                            //obj.put("name",name);
                            JSONObject obj = new JSONObject();
                            obj.put("phone",phone);
                            arr.put(obj);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    pCur.close();
                }
            }
        }
        try {
            obj2.put("contact",arr);
            obj2.put("discover_pos",discover_pos);
            obj2.put("username",pref.getUsername());
            obj2.put("sessionId",pref.getSessionId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Response: " + obj2.toString());
        return obj2;

    }




    private void loadIntroDiscover(final String discover_pos) {

        // making fresh volley request and getting json
        JsonObjectRequest objReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_LOAD_INTRO_DISCOVER, phoneContacts(discover_pos), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response.toString());

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

                        isReady = true;

                    }else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (!isRefreshing) {
                        userSelectItems.remove(userSelectItems.size() - 1);
                        userSelectAdapter.notifyItemRemoved(userSelectItems.size());
                        userSelectAdapter.setLoaded();
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
                    userSelectItems.remove(userSelectItems.size() - 1);
                    userSelectAdapter.notifyItemRemoved(userSelectItems.size());
                    userSelectAdapter.setLoaded();
                }else isRefreshing = false;
            }
        }) {

        };

        //Setting request policy to max timeout
        objReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(objReq);
    }


    /**
     * Parsing json response and passing the data to user view list adapter
     * */
    private void parseJsonUser(JSONObject response) {

        try {

            JSONArray userArray = response.getJSONArray("user");

            if (isRefreshing == false) {
                userSelectItems.remove(userSelectItems.size() - 1);
                userSelectAdapter.notifyItemRemoved(userSelectItems.size());
                userSelectAdapter.setLoaded();
            }else {
                if (userArray.length() > 0) {
                    userSelectItems.clear();
                }
                isRefreshing = false;
            }

            for (int i = 0; i < userArray.length(); i++) {
                JSONObject userObj = (JSONObject) userArray.get(i);

                UserSelectItem item = new UserSelectItem();
                item.setUsername(userObj.getString("username"));
                item.setName(userObj.getString("name"));
                item.setIsVerify(userObj.getBoolean("isVerify"));
                item.setInfo(userObj.getString("info"));

                userSelectItems.add(item);
            }


            // notify data changes to feed adapter
            userSelectAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private JSONObject selectedUsers() {

        JSONArray arr = new JSONArray();
        JSONObject obj2 = new JSONObject();

        for (int i = 0; i<userSelectItems.size(); i++) {
            UserSelectItem item = userSelectItems.get(i);
            if ((item.getIsSelect())) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("user", item.getUsername());
                    arr.put(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        try {
            obj2.put("contact", arr);
            obj2.put("username", pref.getUsername());
            obj2.put("sessionId", pref.getSessionId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj2;
    }


    private void followIntroDiscover() {

        // making fresh volley request and getting json
        JsonObjectRequest objReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_FOLLOW_INTRO_DISCOVER, selectedUsers(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response.toString());

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error
                    if (!error) {

                        pref.setRegStage(0);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        finish();

                    }else {

                    }

                    btn_follow.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    btn_follow.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

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

                btn_follow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            }
        }) {

        };

        //Setting request policy to max timeout
        objReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(objReq);
    }

    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.introDiscoverCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cd.isConnectingToInternet()) {
                    if (id == 0)
                        loadIntroDiscover(String.valueOf(0));
                    else
                        loadIntroDiscover(String.valueOf(userSelectAdapter.getItemCount() - 1));
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
