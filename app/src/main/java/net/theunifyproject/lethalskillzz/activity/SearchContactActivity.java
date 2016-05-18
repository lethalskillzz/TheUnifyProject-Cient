package net.theunifyproject.lethalskillzz.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
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
import net.theunifyproject.lethalskillzz.adapter.UserListAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.UserListItem;
import net.theunifyproject.lethalskillzz.util.Logout;

public class SearchContactActivity extends AppCompatActivity {

    public static Handler mUiHandler;
    private static final String TAG = SearchContactActivity.class.getSimpleName();
    private Logout mLogout;
    private ConnectionDetector cd;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rView;
    private List<UserListItem> userListItems;
    private UserListAdapter userListAdapter;
    private PrefManager pref;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_contact_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        pref = new PrefManager(this);
        userListItems = new ArrayList<UserListItem>();

        LinearLayoutManager lLayout = new LinearLayoutManager(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.search_contact_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView)findViewById(R.id.search_contact_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        userListAdapter = new UserListAdapter(this, rView, userListItems);
        rView.setAdapter(userListAdapter);

        setAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadSearchContact(String.valueOf(0));
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


    private void setAdapter() {
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
                        loadSearchContact(String.valueOf(userListAdapter.getItemCount() - 1));
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
            loadSearchContact(String.valueOf(0));
        } else
            showSnackBar(0, getString(R.string.err_no_internet));
    }


    public JSONObject phoneContacts(String contact_pos) {

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
            obj2.put("contact_pos",contact_pos);
            obj2.put("username",pref.getUsername());
            obj2.put("sessionId",pref.getSessionId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Response: " + obj2.toString());
        return obj2;
    }


    private void loadSearchContact(final String contact_pos) {

        // making fresh volley request and getting json
        JsonObjectRequest objReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_LOAD_CONTACT_SEARCH, phoneContacts(contact_pos), new Response.Listener<JSONObject>() {

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

                    }else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (!isRefreshing) {
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
                if (!isRefreshing) {
                    userListItems.remove(userListItems.size() - 1);
                    userListAdapter.notifyItemRemoved(userListItems.size());
                    userListAdapter.setLoaded();
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

            if (!isRefreshing) {
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

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.introDiscoverCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cd.isConnectingToInternet()) {
                    if (id == 0)
                        loadSearchContact(String.valueOf(0));
                    else
                        loadSearchContact(String.valueOf(userListAdapter.getItemCount() - 1));
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
