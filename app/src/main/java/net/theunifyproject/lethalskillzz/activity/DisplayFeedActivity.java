package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.os.Message;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rockerhieu.emojicon.EmojiconTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.android.volley.toolbox.RoundNetworkImageView;
import net.android.volley.toolbox.StringRequest;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.util.Logout;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

public class DisplayFeedActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = DisplayFeedActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private ConnectionDetector cd;
    private Logout mLogout;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private PrefManager pref;
    //private HashMap<String, String> profile;
    private boolean isReady;
    private String username;
    private String feedId;
    private String likeCount;
    private String commentCount;
    private boolean isLike;


    private TextView name, mUsername, timestamp, mLikeCount, mCommentCount;
    private EmojiconTextView statusMsg;
    private ImageView isVerify;
    private RoundNetworkImageView profilePic;
    private FeedImageView feedImageView;
    private ImageButton btnLike, btnOption;
    private LinearLayout clickCommentCount, clickLikeCount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_feed);

        mLogout = new Logout(this);
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();

        Intent intent = getIntent();
        feedId = intent.getStringExtra("feedId");

        // creating connection detector class instance
        cd = new ConnectionDetector(this);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        name = (TextView) findViewById(R.id.display_feed_name);
        mUsername = (TextView) findViewById(R.id.display_feed_username);
        timestamp = (TextView) findViewById(R.id.display_feed_timestamp);
        statusMsg = (EmojiconTextView) findViewById(R.id.display_feed_message);
        profilePic = (RoundNetworkImageView) findViewById(R.id.display_feed_profilePic);
        isVerify = (ImageView) findViewById(R.id.display_feed_isVerify);
        feedImageView = (FeedImageView) findViewById(R.id.display_feed_image);
        btnLike = (ImageButton) findViewById(R.id.display_feed_btnLike);
        btnOption = (ImageButton) findViewById(R.id.display_feed_btnOption);
        mLikeCount = (TextView) findViewById(R.id.display_feed_likeCount);
        mCommentCount = (TextView) findViewById(R.id.display_feed_commentCount);
        clickLikeCount = (LinearLayout) findViewById(R.id.display_feed_click_like_count);
        clickCommentCount = (LinearLayout) findViewById(R.id.display_feed_click_comment_count);


        name.setOnClickListener(this);
        profilePic.setOnClickListener(this);
        btnLike.setOnClickListener(this);
        btnOption.setOnClickListener(this);
        clickLikeCount.setOnClickListener(this);
        clickCommentCount.setOnClickListener(this);


        if (cd.isConnectingToInternet()) {
            displayFeed(feedId);
        } else {
            showSnackBar(0, getString(R.string.err_no_internet));
        }


        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {

                String data[] = ((String) msg.obj).trim().split(AppConfig.OTP_DELIMITER);

                switch (msg.what) {

                    case AppConfig.httpHandlerLikeFeed: {

                        likeCount=data[1];
                        mLikeCount.setText(likeCount);
                    }
                    break;

                    case AppConfig.httpHandlerPostComment: {

                        commentCount = data[1];
                        mCommentCount.setText(commentCount);

                    }
                    break;
                }
            }

        };

    }

    @Override
    public void onClick(View view) {
        if(isReady) {
            switch (view.getId()) {
                case R.id.display_feed_btnLike:
                    clickLike(view);
                    break;

                case R.id.display_feed_btnOption:
                    clickOption(view);
                    break;

                case R.id.display_feed_click_like_count:
                    clickLikeCount(view);
                    break;

                case R.id.display_feed_click_comment_count:
                    clickCommentCount(view);
                    break;

                case R.id.display_feed_profilePic:
                    clickUser(view);
                    break;

                case R.id.display_feed_name:
                    clickUser(view);
                    break;


                default:
                    //clickItem(view);
                    break;
            }
        }
    }


    private void clickLike(View v) {

        int count = Integer.parseInt(likeCount);

        Intent grapprIntent = new Intent(v.getContext(), HttpService.class);
        grapprIntent.putExtra("intent_type", AppConfig.httpIntentLikeFeed);
        grapprIntent.putExtra("feedId", feedId);

        if (isLike) {
            btnLike.setImageResource(R.mipmap.ic_heart_outline_grey);
            grapprIntent.putExtra("like_type", "unlike");
            isLike=false;
            count -= 1;
            likeCount=String.valueOf(count);
            mLikeCount.setText(likeCount);
        } else {
            btnLike.setImageResource(R.mipmap.ic_heart_red);
            grapprIntent.putExtra("like_type", "like");
            isLike=true;
            count += 1;
            likeCount=String.valueOf(count);
            mLikeCount.setText(String.valueOf(likeCount));
        }

        v.getContext().startService(grapprIntent);

    }



    private void clickLikeCount(View v) {

        Intent intent = new Intent(v.getContext(), UserListActivity.class);
        intent.putExtra("list_type", AppConfig.listLike);
        intent.putExtra("feedId", feedId);
        v.getContext().startActivity(intent);
    }


    private void clickCommentCount(View v) {

        Intent intent = new Intent(v.getContext(), CommentActivity.class);
        intent.putExtra("feedId", feedId);
        v.getContext().startActivity(intent);
    }


    private void clickUser(View v) {

        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);
    }



    private void clickOption(final View v) {


        OnMenuItemClickListener onMenuClick = new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.feed_popup_edit: {
                        Intent intent = new Intent(v.getContext(), PostFeedActivity.class);
                        intent.putExtra("intent_type", "edit");
                        intent.putExtra("feedId", feedId);
                        v.getContext().startActivity(intent);
                    }
                    return true;

                    case R.id.feed_popup_delete: {
                        Intent intent = new Intent(v.getContext(), HttpService.class);
                        intent.putExtra("intent_type", AppConfig.httpIntentDeleteFeed);
                        intent.putExtra("feedId", feedId);
                        v.getContext().startService(intent);
                    }
                    return true;

                    case R.id.feed_popup_report: {
                        Intent intent = new Intent(v.getContext(), HttpService.class);
                        intent.putExtra("intent_type", AppConfig.httpIntentReportFeed);
                        intent.putExtra("feedId", feedId);
                        v.getContext().startService(intent);
                    }
                    return true;

                    case R.id.feed_popup_cancel:
                        return true;

                    default:
                        return false;
                }
            }
        };


        if (!pref.getUsername().equals(username)) {

            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.setOnMenuItemClickListener(onMenuClick);
            popupMenu.inflate(R.menu.menu_feed_popup);
            popupMenu.show();

        } else {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.setOnMenuItemClickListener(onMenuClick);
            popupMenu.inflate(R.menu.menu_my_feed_popup);
            popupMenu.show();
        }
    }



    private void setUpFeed(String profilePic, String name, String username, boolean isVerify, String message,
                           String image, String timestamp, String likeCount, String commentCount, boolean isLike) {

        this.name.setText(name);
        this.mUsername.setText("@"+username);

        this.timestamp.setText(timestamp);

        // Chcek for empty status message
        if (!TextUtils.isEmpty(message)) {

            Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
            String atMentionScheme = "mention://";

            Pattern HashPattern = Pattern.compile("#([A-Za-z0-9_]+)");
            String HashScheme = "hash://";

            Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
                //skip the first character to filter out '@'
                public String transformUrl(final Matcher match, String url) {
                    return match.group(1);
                }
            };

            this.statusMsg.setText(message);
            this.statusMsg.setVisibility(View.VISIBLE);

            Linkify.addLinks(this.statusMsg, Linkify.ALL);
            Linkify.addLinks(this.statusMsg, atMentionPattern, atMentionScheme, null, transformFilter);
            Linkify.addLinks(this.statusMsg, HashPattern, HashScheme, null, transformFilter);

            //StripUnderline.stripUnderlines(this.statusMsg);
        } else {
            // status is empty, remove from view
            this.statusMsg.setVisibility(View.GONE);
        }

        // user profile pic
        if(profilePic.length()!=0) {
            this.profilePic.setImageUrl(profilePic+AppConfig.AUTO_REF_HACK(), imageLoader);
        }
        this.profilePic.setDefaultImageResId(R.drawable.ic_user);

        // Feed image
        if (image.length()!=0) {
            this.feedImageView.setImageUrl(image+AppConfig.AUTO_REF_HACK(), imageLoader);
            this.feedImageView.setVisibility(View.VISIBLE);
            this.feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {

                        }
                    });


        } else {
            this.feedImageView.setVisibility(View.GONE);
        }

        if(isLike) {
            this.btnLike.setImageResource(R.mipmap.ic_heart_red);
        }else {
            this.btnLike.setImageResource(R.mipmap.ic_heart_outline_grey);
        }



        this.mLikeCount.setText(likeCount);
        this.mCommentCount.setText(commentCount);


        if(isVerify)
            this.isVerify.setVisibility(View.VISIBLE);
        else
            this.isVerify.setVisibility(View.GONE);

        isReady = true;

    }


    private void displayFeed(final String feedId) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DISPLAY_FEED, new Response.Listener<String>() {

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
                        JSONArray feedArray = dataObj.getJSONArray("feed");
                        JSONObject feedObj = (JSONObject) feedArray.get(0);

                        int id = feedObj.getInt("id");
                        username = feedObj.getString("username");
                        String name = feedObj.getString("name");
                        String profilePic = feedObj.getString("profilePic");
                        boolean isVerify = feedObj.getBoolean("isVerify");
                        String feed_message = feedObj.getString("message");
                        String feed_image = feedObj.getString("image");
                        String timeStamp = feedObj.getString("timeStamp");
                        likeCount = feedObj.getString("likeCount");
                        commentCount = feedObj.getString("commentCount");
                        isLike = feedObj.getBoolean("isLike");

                        setUpFeed(profilePic, name, username, isVerify, feed_message,
                                  feed_image, timeStamp, likeCount, commentCount, isLike);

                    } else {

                    }



                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();


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


    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.display_feed_CoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(id == 0){
                    if (cd.isConnectingToInternet()) {
                        displayFeed(feedId);
                    } else {
                        showSnackBar(0, getString(R.string.err_no_internet));
                    }
                }else {

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
