package net.theunifyproject.lethalskillzz.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RatingBar;
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
import net.theunifyproject.lethalskillzz.adapter.CustomSpinnerAdapter;
import net.theunifyproject.lethalskillzz.adapter.GridAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.fragment.SearchShopFragment;
import net.theunifyproject.lethalskillzz.model.CustomSpinnerItem;
import net.theunifyproject.lethalskillzz.model.GridItem;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.util.Logout;

public class StoreActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static Handler mUiHandler;
    private static final String TAG = StoreActivity.class.getSimpleName();
    private Logout mLogout;
    private PrefManager pref;
    private ConnectionDetector cd;

    private RecyclerView rView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Spinner categorySpinner;
    private ImageButton mSearch;
    private CustomSpinnerAdapter spinnerAdapter;
    private TextView mDescription;
    private RatingBar mRating;
    private TextView mRate;

    private static String[] titles = null;
    private List<GridItem> gridItems;
    private GridAdapter gridAdapter;
    private String username;
    private boolean isRefreshing = false;
    private String category;

    private FloatingActionButton fab;


    public static List<CustomSpinnerItem> getData() {
        List<CustomSpinnerItem> data = new ArrayList<>();

        // preparing navigation drawer items
        for (int i = 0; i < titles.length; i++) {
            CustomSpinnerItem item = new CustomSpinnerItem();
            item.setTitle(titles[i]);
            item.setIcon(R.drawable.ic_add);
           /* switch (titles[i]) {

                case "Electronics & Gadgets":
                    item.setIcon(R.drawable.);
                    break;

                case "Beauty & Fashion":
                    item.setIcon(R.drawable.);
                    break;

                case "Sports & Hobbies":
                    item.setIcon(R.drawable.);
                    break;

                case "Books & Literature":
                    item.setIcon(R.drawable.);
                    break;

                case "Food & Provisions":
                    item.setIcon(R.drawable.);
                    break;

                case "Job & Services":
                    item.setIcon(R.drawable.);
                    break;

                case "Tools & Equipments":
                    item.setIcon(R.drawable.);
                    break;

                case "Vehicles":
                    item.setIcon(R.drawable.);
                    break;

                case "Real Estate":
                    item.setIcon(R.drawable.);
                    break;

                case "Home & Furniture":
                    item.setIcon(R.drawable.);
                    break;

            }*/

            data.add(item);
        }
        return data;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        Toolbar toolbar = (Toolbar) findViewById(R.id.store_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        mLogout = new Logout(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(this);
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();
        gridItems = new ArrayList<GridItem>();

        //LinearLayoutManager lLayout = new LinearLayoutManager(this);
        final GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.store_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView) findViewById(R.id.store_recycler_view);
        rView.setLayoutManager(mLayoutManager);
        rView.setHasFixedSize(true);
        rView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        int viewWidth = rView.getMeasuredWidth();
                        float cardViewWidth = getApplicationContext().getResources().getDimension(R.dimen.shop_item_width);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        mLayoutManager.setSpanCount(newSpanCount);
                        mLayoutManager.requestLayout();
                    }
                });


        mDescription = (TextView) findViewById(R.id.layout_store_description);
        mRating = (RatingBar) findViewById(R.id.layout_store_ratebar);
        mSearch = (ImageButton) findViewById(R.id.store_search);
        mSearch.setOnClickListener(this);
        mRate = (TextView) findViewById(R.id.layout_store_rate_btn);
        mRate.setOnClickListener(this);

        // labels
        titles = getResources().getStringArray(R.array.shopping_array);
        // Create custom adapter object ( see below CustomAdapter.java )
        spinnerAdapter = new CustomSpinnerAdapter(this, R.layout.item_custom_spinner, getData());

        categorySpinner = (Spinner) findViewById(R.id.store_category_spinner);
        categorySpinner.setOnItemSelectedListener(this);
        //categorySpinner.setAdapter(spinnerAdapter);

        fab = (FloatingActionButton) findViewById(R.id.store_fab);
        fab.setOnClickListener(this);

        setGridAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        if (cd.isConnectingToInternet()) {
            loadStoreDetail(username);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            showSnackBar(0, getString(R.string.err_no_internet));
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadStore(username, category, String.valueOf(0));
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showSnackBar(0, getString(R.string.err_no_internet));
                }

            }
        });

        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case AppConfig.httpHandlerEditShop: {
                        if (cd.isConnectingToInternet()) {
                            isRefreshing = true;
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
                            });
                            loadStore(username, category, String.valueOf(0));
                        } else {
                            showSnackBar(0, getString(R.string.err_no_internet));
                        }
                    }
                    break;

                    case AppConfig.httpHandlerDeleteShop: {

                        if (((String) msg.obj).trim().equals("success")) {
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
                            });
                            loadStore(username, category, String.valueOf(0));

                        } else if (((String) msg.obj).trim().equals("fail"))
                            showSnackBar(1, "Error while deleting shop!");
                        else
                            showSnackBar(1, getString(R.string.err_network_timeout));
                    }
                    break;

                    case AppConfig.httpHandlerRateStore: {
                        if (cd.isConnectingToInternet()) {
                            loadStoreDetail(username);
                        } else {
                            mSwipeRefreshLayout.setRefreshing(false);
                            showSnackBar(0, getString(R.string.err_no_internet));
                        }
                    }
                    break;
                }
            }
        };
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.store_fab: {
                Intent intent = new Intent(getApplicationContext(), PostShopActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.store_search:
                onClickSearch();
                break;

            case R.id.layout_store_rate_btn:
                showRateDialog();
                break;
        }
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        category =  parent.getItemAtPosition(position).toString();

        if(category!=null) {

            if (cd.isConnectingToInternet()) {
                isRefreshing = true;
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                loadStore(username, category, String.valueOf(0));
            } else {
                showSnackBar(0, getString(R.string.err_no_internet));
            }
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    private void setupStore(String name, String description, String rating) {
        if(description.length()!= 0)
           mDescription.setText(description);
        mRating.setRating(Float.parseFloat(rating));

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.store_collapsing_toolbar);
        collapsingToolbar.setTitle(name);

      /* if (cd.isConnectingToInternet()) {
            isRefreshing = true;
            loadStore(username, category, String.valueOf(0));
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            showSnackBar(0, getString(R.string.err_no_internet));
        }*/
    }

    /**
     * function to load user's profile details in mysql db
     * @param target_username user unique name
     */
    private void loadStoreDetail(final String target_username) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_STORE_DETAIL, new Response.Listener<String>() {

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
                        JSONObject profileObj = responseObj.getJSONObject("data");
                        JSONArray profileArray = profileObj.getJSONArray("detail");
                        JSONObject detailObj = (JSONObject) profileArray.get(0);

                        String name = detailObj.getString("name");
                        String description = detailObj.getString("description");
                        String rating = detailObj.getString("rating");

                        setupStore(name, description, rating);

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
                        loadStore(username, category, String.valueOf(gridAdapter.getItemCount() - 1));
                    } else showSnackBar(1, getString(R.string.err_no_internet));
                }
            }
        });
    }



    private void loadStore(final String target_username, final String category, final String store_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_STORE, new Response.Listener<String>() {

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
                        parseJsonShopping(dataObj);

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
                params.put("target_username", target_username);
                params.put("category", category);
                params.put("store_pos", store_pos);

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
     * Parsing json response and passing the data to shop view grid adapter
     */
    private void parseJsonShopping(JSONObject response) {
        try {
            JSONArray shopArray = response.getJSONArray("store");


            if (isRefreshing) {
                if (shopArray.length() > 0) {
                    gridItems.clear();
                }
                isRefreshing = false;
            } else {
                gridItems.remove(gridItems.size() - 1);
                gridAdapter.notifyItemRemoved(gridItems.size());
                gridAdapter.setLoaded();
            }


            for (int i = 0; i < shopArray.length(); i++) {
                JSONObject shopObj = (JSONObject) shopArray.get(i);

                GridItem item = new GridItem();
                item.setId(shopObj.getInt("id"));
                item.setType(shopObj.getInt("type"));
                item.setPrice(shopObj.getString("price"));
                item.setTitle(shopObj.getString("title"));
                item.setImage(shopObj.getString("image"));

                gridItems.add(item);
            }

            // notify data changes to feed adapater
            gridAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void onClickSearch() {
        if (findViewById(R.id.storeCoordinatorLayout) != null) {

            SearchShopFragment search_shop_fragment = new SearchShopFragment();

            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                    .add(R.id.storeCoordinatorLayout, search_shop_fragment).commit();

        }
    }




    @SuppressLint("ValidFragment")
    private class RateDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            //builder.setMessage(LoginActivity.noneterrormsg)
            final View rateLayout = getLayoutInflater().inflate(R.layout.fragment_rate_store, null);
            builder.setView(rateLayout)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            RatingBar rateBar  = (RatingBar) rateLayout.findViewById(R.id.store_rating_bar);
                            String rating =  String.valueOf(rateBar.getRating());
                            Intent grapprIntent = new Intent(StoreActivity.this, HttpService.class);
                            grapprIntent.putExtra("intent_type", AppConfig.httpIntentRateStore);
                            grapprIntent.putExtra("username", username);
                            grapprIntent.putExtra("rating", rating);
                            startService(grapprIntent);


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


    private void showRateDialog() {
        DialogFragment newFragment = new RateDialogFragment();
        newFragment.show(getFragmentManager(), "rateDlg");

    }




    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.storeCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cd.isConnectingToInternet()) {
                            if (id == 0)
                                loadStoreDetail(username);
                            else
                                loadStore(username, category, String.valueOf(gridAdapter.getItemCount() - 1));
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
