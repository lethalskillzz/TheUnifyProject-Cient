package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageButton;
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
import net.theunifyproject.lethalskillzz.adapter.GridAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.fragment.SearchDigestFragment;
import net.theunifyproject.lethalskillzz.model.GridItem;
import net.theunifyproject.lethalskillzz.util.Logout;

public class DigestActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = DigestActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private Logout mLogout;
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private ConnectionDetector cd;

    private RecyclerView rView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageButton mSearch;
    private Spinner categorySpinner;

    private List<GridItem> gridItems;
    private GridAdapter gridAdapter;
    private boolean isRefreshing = false;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.digest_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(this);
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();
        gridItems = new ArrayList<GridItem>();

        //LinearLayoutManager lLayout = new LinearLayoutManager(this);
        final GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.digest_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView) findViewById(R.id.digest_recycler_view);
        rView.setLayoutManager(mLayoutManager);
        rView.setHasFixedSize(true);
        rView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        int viewWidth = rView.getMeasuredWidth();
                        float cardViewWidth = getApplicationContext().getResources().getDimension(R.dimen.grid_item_width);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        mLayoutManager.setSpanCount(newSpanCount);
                        mLayoutManager.requestLayout();
                    }
                });

        mSearch = (ImageButton) findViewById(R.id.digest_search);
        mSearch.setOnClickListener(this);
        categorySpinner = (Spinner) findViewById(R.id.digest_category_spinner);
        categorySpinner.setOnItemSelectedListener(this);

        setGridAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadDigest(category, String.valueOf(0));
                }  else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showSnackBar(0, getString(R.string.err_no_internet));
                }

            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.digest_search: {
                onClickSearch();
            }
            break;

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;

            case R.id.grid_toolbar_search:
                onClickSearch();
                break;

            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        category = parent.getItemAtPosition(position).toString();

        if (category != null) {

            if (cd.isConnectingToInternet()) {
                isRefreshing = true;
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                loadDigest(category, String.valueOf(0));
            } else showSnackBar(0, getString(R.string.err_no_internet));
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    private void setGridAdapter() {
        gridAdapter = new GridAdapter(this, rView, gridItems);
        rView.setAdapter(gridAdapter);

        gridAdapter.setOnLoadMoreListener(new GridAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isRefreshing) {
                    //add progress item
                    gridItems.add(null);
                    gridAdapter.notifyItemInserted(gridItems.size() - 1);
                    if (cd.isConnectingToInternet()) {
                        loadDigest(category, String.valueOf(gridAdapter.getItemCount() - 1));
                    } else showSnackBar(1, getString(R.string.err_no_internet));
                }
            }
        });

       /* if (cd.isConnectingToInternet()) {
            isRefreshing = true;
            loadDigest(profile.get("user_id"), category, String.valueOf(0));
        } else showSnackBar(0, getString(R.string.err_no_internet)); */
    }



    private void loadDigest(final String category, final String digest_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_DIGEST, new Response.Listener<String>() {

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
                        parseJsonDigest(dataObj);

                    } else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);


                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (!isRefreshing) {
                        gridItems.remove(gridItems.size() - 1);
                        gridAdapter.notifyItemRemoved(gridItems.size());
                        gridAdapter.setLoaded();
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
                    gridItems.remove(gridItems.size() - 1);
                    gridAdapter.notifyItemRemoved(gridItems.size());
                    gridAdapter.setLoaded();
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
                params.put("category", category);
                params.put("digest_pos", digest_pos);

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
     * Parsing json response and passing the data to digest view grid adapter
     */
    private void parseJsonDigest(JSONObject response) {
        try {
            JSONArray digestArray = response.getJSONArray("digest");


            if (isRefreshing) {
               // if (digestArray.length() > 0) {
                    gridItems.clear();
                //}
                isRefreshing = false;
            } else {
                gridItems.remove(gridItems.size() - 1);
                gridAdapter.notifyItemRemoved(gridItems.size());
                gridAdapter.setLoaded();
            }


            for (int i = 0; i < digestArray.length(); i++) {
                JSONObject digestObj = (JSONObject) digestArray.get(i);

                GridItem item = new GridItem();
                item.setId(digestObj.getInt("id"));
                item.setType(digestObj.getInt("type"));
                item.setCategory(digestObj.getString("category"));
                item.setTitle(digestObj.getString("title"));
                item.setImage(digestObj.getString("image"));
                item.setUrl(digestObj.getString("url"));

                gridItems.add(item);

            }

            // notify data changes to feed adapater
            gridAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onClickSearch() {
        if (findViewById(R.id.digest_root_view) != null) {
            SearchDigestFragment search_digest_fragment = new SearchDigestFragment();

            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                    .add(R.id.digest_root_view, search_digest_fragment).commit();
        }
    }

    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.digestCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cd.isConnectingToInternet()) {
                            if (id == 0)
                                loadDigest(category, String.valueOf(0));
                            else
                                loadDigest(category, String.valueOf(gridAdapter.getItemCount() - 1));
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
