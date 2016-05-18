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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import net.theunifyproject.lethalskillzz.adapter.CommentAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.CommentItem;
import net.theunifyproject.lethalskillzz.service.HttpService;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener {

    public static Handler mUiHandler;
    private static final String TAG = CommentActivity.class.getSimpleName();
    private PrefManager pref;
    //private HashMap<String, String> profile;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rView;
    private List<CommentItem> commentItems;
    private CommentAdapter commentAdapter;

    private boolean isRefreshing = false;
    private String feedId;
    private EditText edt_comment;
    private ImageButton btn_comment;
    private ProgressBar progressBar;

    private ConnectionDetector cd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.comment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        feedId  = intent.getStringExtra("feedId");

        // creating connection detector class instance
        cd = new ConnectionDetector(this);
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();
        commentItems = new ArrayList<CommentItem>();

        edt_comment = (EditText) findViewById(R.id.input_post_comment);
        btn_comment = (ImageButton) findViewById(R.id.btn_post_comment);
        progressBar = (ProgressBar) findViewById(R.id.post_comment_progressBar);

        btn_comment.setOnClickListener(this);

        LinearLayoutManager lLayout = new LinearLayoutManager(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.comment_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        rView = (RecyclerView) findViewById(R.id.comment_recycler_view);
        rView.setLayoutManager(lLayout);
        rView.setHasFixedSize(true);

        setCommentAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (cd.isConnectingToInternet()) {
                    isRefreshing = true;
                    loadComment(feedId, String.valueOf(0));
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showSnackBar(0, getString(R.string.err_no_internet));
                }
            }
        });

        requestFocus(edt_comment);

        mUiHandler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg) {
                switch(msg.what) {

                    case AppConfig.httpHandlerPostComment: {

                        progressBar.setVisibility(View.GONE);
                        btn_comment.setVisibility(View.VISIBLE);
                        edt_comment.setText("");

                        if (cd.isConnectingToInternet()) {
                            isRefreshing = true;
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
                            });
                            loadComment(feedId, String.valueOf(0));
                        } else {
                            showSnackBar(0, getString(R.string.err_no_internet));
                        }
                    }
                    break;

                }
            }

        };
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_post_comment:
                submitComment();
                break;

        }
    }



    private void submitComment() {
        if (cd.isConnectingToInternet()) {
            if (validateComment(edt_comment)) {

                progressBar.setVisibility(View.VISIBLE);
                btn_comment.setVisibility(View.GONE);

                String comment = edt_comment.getText().toString().trim();

                Intent grapprIntent = new Intent(CommentActivity.this, HttpService.class);
                grapprIntent.putExtra("intent_type", AppConfig.httpIntentPostComment);
                grapprIntent.putExtra("feedId", feedId);
                grapprIntent.putExtra("comment", comment);
                startService(grapprIntent);
            }
        } else {
            showSnackBar(2, getString(R.string.err_no_internet));
        }
}


    private boolean validateComment(EditText EdTxt)  {
      if(EdTxt.getText().toString().trim().isEmpty())  {
          return false;
      }
        return true;
    }


    private void setCommentAdapter() {
        commentAdapter = new CommentAdapter(this, rView, commentItems);
        rView.setAdapter(commentAdapter);

        commentAdapter.setOnLoadMoreListener(new CommentAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (isRefreshing == false) {
                    //add progress item
                    commentItems.add(null);
                    commentAdapter.notifyItemInserted(commentItems.size() - 1);
                    if (cd.isConnectingToInternet()) {
                        loadComment(feedId, String.valueOf(commentAdapter.getItemCount() - 1));
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
            loadComment(feedId, String.valueOf(0));
        } else {
            showSnackBar(0, getString(R.string.err_no_internet));
        }
    }



    private void loadComment(final String feedId, final String comment_pos) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_COMMENT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error
                    if (!error) {

                        // parsing the feed data
                        JSONObject commentObj = responseObj.getJSONObject("data");
                        parseJsonComment(commentObj);

                    } else {

                    }

                    mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (isRefreshing == false) {
                        commentItems.remove(commentItems.size() - 1);
                        commentAdapter.notifyItemRemoved(commentItems.size());
                        commentAdapter.setLoaded();
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
                if (isRefreshing == false) {
                    commentItems.remove(commentItems.size() - 1);
                    commentAdapter.notifyItemRemoved(commentItems.size());
                    commentAdapter.setLoaded();
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
                params.put("feedId", feedId);
                params.put("comment_pos", comment_pos);

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
     * Parsing json response and passing the data to comment view list adapter
     */
    private void parseJsonComment(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("comment");


            if (isRefreshing == true) {
                if (feedArray.length() > 0) {
                    commentItems.clear();
                }
                isRefreshing = false;
            } else {
                commentItems.remove(commentItems.size() - 1);
                commentAdapter.notifyItemRemoved(commentItems.size());
                commentAdapter.setLoaded();
            }


            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                CommentItem item = new CommentItem();
                item.setId(feedObj.getInt("id"));
                item.setUsername(feedObj.getString("username"));
                item.setName(feedObj.getString("name"));
                item.setIsVerify(feedObj.getBoolean("isVerify"));
                item.setComment(feedObj.getString("comment"));
                item.setTimeStamp(feedObj.getString("timeStamp"));

                commentItems.add(item);

            }

            // notify data changes to feed adapater
            commentAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.commentCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cd.isConnectingToInternet()) {
                    if (id == 0)
                        loadComment(feedId, String.valueOf(0));
                    else if (id == 1)
                        loadComment(feedId, String.valueOf(commentAdapter.getItemCount() - 1));
                    else
                        submitComment();
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
