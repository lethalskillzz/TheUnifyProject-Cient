package net.theunifyproject.lethalskillzz.activity;

import android.graphics.Color;
import android.os.Handler;
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
import android.widget.AdapterView;
import android.widget.Spinner;
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
import net.theunifyproject.lethalskillzz.adapter.UserListAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.UserListItem;
import net.theunifyproject.lethalskillzz.util.Logout;

public class TaxiActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static Handler mUiHandler;
    private static final String TAG = TaxiActivity.class.getSimpleName();
    private Logout mLogout;
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private ConnectionDetector cd;

    private Spinner campusSpinner, timeSpinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rView;
    private List<UserListItem> userListItems;
    private UserListAdapter userListAdapter;

    private String campus, time;
    private boolean isRefreshing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi);

        Toolbar toolbar = (Toolbar) findViewById(R.id.taxi_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();
        userListItems = new ArrayList<UserListItem>();

        campusSpinner = (Spinner) findViewById(R.id.taxi_campus_spinner);
        timeSpinner = (Spinner) findViewById(R.id.taxi_time_spinner);

        campusSpinner.setOnItemSelectedListener(this);
        timeSpinner.setOnItemSelectedListener(this);


        LinearLayoutManager lLayout = new LinearLayoutManager(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.taxi_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView)findViewById(R.id.taxi_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        userListAdapter = new UserListAdapter(this, rView, userListItems);
        rView.setAdapter(userListAdapter);

        setTaxiAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadTaxiTransit(campus, time, String.valueOf(0));
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showSnackBar(0, getString(R.string.err_no_internet));
                }
            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

         /*   case R.id.taxi_search:
                onClickSearch();
                break; */
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        if(parent.getId() == R.id.taxi_campus_spinner)
            campus = parent.getItemAtPosition(position).toString();
        else
            time = parent.getItemAtPosition(position).toString();

        if((campus!=null && time!=null)) {

            if (cd.isConnectingToInternet()) {
                isRefreshing = true;
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                loadTaxiTransit(campus, time, String.valueOf(0));
            } else
                showSnackBar(0, getString(R.string.err_no_internet));

        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }




    private void setTaxiAdapter() {
        userListAdapter = new UserListAdapter(this, rView, userListItems);
        rView.setAdapter(userListAdapter);

        userListAdapter.setOnLoadMoreListener(new UserListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isRefreshing) {
                    //add progress item
                    userListItems.add(null);
                    userListAdapter.notifyItemInserted(userListItems.size() - 1);
                    if (cd.isConnectingToInternet()) {
                        loadTaxiTransit(campus, time, String.valueOf(userListAdapter.getItemCount() - 1));
                    } else
                        showSnackBar(1, getString(R.string.err_no_internet));
                }
            }
        });

     /*   if (cd.isConnectingToInternet()) {
            isRefreshing = true;
            mSwipeRefreshLayout.post(new Runnable() {
                @Override public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            loadTaxi(String.valueOf(0));
        } else
            showSnackBar(0, getString(R.string.err_no_internet));*/
    }


    private void loadTaxiTransit(final String campus, final String time, final String transit_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_TAXI_TRANSIT, new Response.Listener<String>() {

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
                params.put("campus", campus);
                params.put("time", time);
                params.put("transit_pos", transit_pos);

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

            if (!isRefreshing) {
                userListItems.remove(userListItems.size() - 1);
                userListAdapter.notifyItemRemoved(userListItems.size());
                userListAdapter.setLoaded();
            }else {
               // if (userArray.length() > 0) {
                    userListItems.clear();
              //  }
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

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.taxiCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cd.isConnectingToInternet()) {
                    if (id == 0)
                        loadTaxiTransit(campus, time, String.valueOf(0));
                    else
                        loadTaxiTransit(campus, time, String.valueOf(userListAdapter.getItemCount() - 1));
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
