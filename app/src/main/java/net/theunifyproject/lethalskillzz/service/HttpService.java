package net.theunifyproject.lethalskillzz.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
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
import net.theunifyproject.lethalskillzz.activity.CommentActivity;
import net.theunifyproject.lethalskillzz.activity.DisplayFeedActivity;
import net.theunifyproject.lethalskillzz.activity.EditShopActivity;
import net.theunifyproject.lethalskillzz.activity.HashActivity;
import net.theunifyproject.lethalskillzz.activity.IntroDiscoverActivity;
import net.theunifyproject.lethalskillzz.activity.OTPActivity;
import net.theunifyproject.lethalskillzz.activity.PostFeedActivity;
import net.theunifyproject.lethalskillzz.activity.PostShopActivity;
import net.theunifyproject.lethalskillzz.activity.PostTransitActivity;
import net.theunifyproject.lethalskillzz.activity.ProfileActivity;
import net.theunifyproject.lethalskillzz.activity.ProfilePicActivity;
import net.theunifyproject.lethalskillzz.activity.SearchContactActivity;
import net.theunifyproject.lethalskillzz.activity.ShoppingActivity;
import net.theunifyproject.lethalskillzz.activity.StoreActivity;
import net.theunifyproject.lethalskillzz.activity.StoreSettingActivity;
import net.theunifyproject.lethalskillzz.activity.UserListActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.fragment.DiscoverFragment;
import net.theunifyproject.lethalskillzz.fragment.FeedFragment;
import net.theunifyproject.lethalskillzz.fragment.NotificationFragment;
import net.theunifyproject.lethalskillzz.fragment.SearchFragment;
import net.theunifyproject.lethalskillzz.util.Logout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Ibrahim on 27/10/2015.
 */
public class HttpService extends IntentService {

    private static String TAG = HttpService.class.getSimpleName();
    private Logout mLogout;
    PrefManager pref;
    public HttpService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        pref = new PrefManager(getApplicationContext());
        mLogout = new Logout(this);
        if (intent != null) {
            String intentType = intent.getStringExtra("intent_type");
            switch (intentType) {

                case AppConfig.httpIntentOtp: {
                    String otp = intent.getStringExtra("otp");
                    //String device = android.os.Build.DEVICE;

                    verifyOtp(pref.getMobileNumber(), otp);
                }
                break;

                case AppConfig.httpIntentPostFeed: {
                    String msg = intent.getStringExtra("msg");
                    String img = intent.getStringExtra("img_path");
                    if(img!=null) {
                        String base64_img = decodeFilePath(img);
                        postFeed(msg, base64_img);
                    }else
                        postFeed(msg, "");
                }
                break;

                case AppConfig.httpIntentEditFeed: {
                    String feedId = intent.getStringExtra("feedId");
                    String msg = intent.getStringExtra("msg");
                    String img = intent.getStringExtra("img_path");
                    if(img!=null) {
                        String base64_img = decodeFilePath(img);
                        editFeed(feedId, msg, base64_img);
                    }else
                        editFeed(feedId, msg, "");
                }
                break;

                case AppConfig.httpIntentPostComment: {
                    String feedId = intent.getStringExtra("feedId");
                    String comment = intent.getStringExtra("comment");
                    postComment(feedId, comment);
                }
                break;

                case AppConfig.httpIntentLikeFeed: {
                    String feedId = intent.getStringExtra("feedId");
                    String like_type = intent.getStringExtra("like_type");
                    likeFeed(feedId, like_type);
                }
                break;

                case AppConfig.httpIntentDeleteFeed: {
                    String feedId = intent.getStringExtra("feedId");
                    deleteFeed(feedId);
                }
                break;

                case AppConfig.httpIntentReportFeed: {
                    String feedId = intent.getStringExtra("feedId");
                    reportFeed(feedId);
                }
                break;

                case AppConfig.httpIntentFollowUser: {
                    String target_username = intent.getStringExtra("username");
                    String follow_type = intent.getStringExtra("follow_type");
                    followUser(target_username, follow_type);
                }
                break;

                case AppConfig.httpIntentUpdateStore: {
                    String name = intent.getStringExtra("name");
                    String description = intent.getStringExtra("description");
                    boolean isActive = intent.getBooleanExtra("isActive", false);
                    updateStore(name,description,isActive);
                }
                break;

                case AppConfig.httpIntentRateStore: {
                    String username = intent.getStringExtra("username");
                    String rating = intent.getStringExtra("rating");
                    rateStore(username,rating);
                }
                break;

                case AppConfig.httpIntentPostShop: {
                    String title = intent.getStringExtra("title");
                    String description = intent.getStringExtra("description");
                    String price = intent.getStringExtra("price");
                    String category = intent.getStringExtra("category");
                    String condition = intent.getStringExtra("condition");
                    String img = intent.getStringExtra("img_path");
                    if(img!=null) {
                        String base64_img = decodeFilePath(img);
                        postShopping(title, description, price, category, condition, base64_img);
                    }else
                        postShopping(title, description, price, category, condition, "");
                }
                break;


                case AppConfig.httpIntentEditShop: {
                    String shopId = intent.getStringExtra("shopId");
                    String title = intent.getStringExtra("title");
                    String description = intent.getStringExtra("description");
                    String price = intent.getStringExtra("price");
                    String category = intent.getStringExtra("category");
                    String condition = intent.getStringExtra("condition");
                    String img = intent.getStringExtra("img_path");
                    if(img!=null) {
                        String base64_img = decodeFilePath(img);
                        editShopping(shopId, title, description, price, category, condition, base64_img);
                    }else
                        editShopping(shopId, title, description, price, category, condition, "");
                }
                break;

                case AppConfig.httpIntentDeleteShop: {
                    String shopId = intent.getStringExtra("shopId");
                    deleteShopping(shopId);
                }
                break;

                case AppConfig.httpIntentReportShop: {
                    String shopId = intent.getStringExtra("shopId");
                    reportShop(shopId);
                }
                break;

                case AppConfig.httpIntentPostTransit: {
                    String bosso_means = intent.getStringExtra("bosso_means");
                    String bosso_time = intent.getStringExtra("bosso_time");
                    String gidan_kwano_means = intent.getStringExtra("gidan_kwano_means");
                    String gidan_kwano_time = intent.getStringExtra("gidan_kwano_time");
                    postTransit(bosso_means, bosso_time, gidan_kwano_means, gidan_kwano_time);
                }
                break;

                case AppConfig.httpIntentChangeProfilePic: {
                    String img = intent.getStringExtra("img_path");
                    if(img!=null) {
                        String base64_img = decodeFilePath(img);
                        changeProfilePic(base64_img);
                    }else
                        changeProfilePic("");
                }
                break;

                case AppConfig.httpIntentSeenNotification: {
                    String notifyId = intent.getStringExtra("notifyId");
                    seenNotification(notifyId);
                }
                break;

                case AppConfig.httpIntentReportUser: {
                    String target_username = intent.getStringExtra("username");
                    String report = intent.getStringExtra("report");
                    reportUser(target_username,report);
                }
                break;


            }

        }
    }

    /**
     * Posting the OTP to server and activating the user
     *
     * @param mobile user valid mobile number
     * @param otp otp received in the SMS
     */
    private void verifyOtp(final String mobile, final String otp) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_VERIFY_OTP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        // parsing the user profile information
                        JSONObject profileObj = responseObj.getJSONObject("detail");

                        String sessionId = profileObj.getString("sessionId");
                        String name = profileObj.getString("name");
                        String username = profileObj.getString("username");
                        String mobile = profileObj.getString("mobile");
                        String location = profileObj.getString("location");
                        String course = profileObj.getString("course");
                        String level = profileObj.getString("level");

                        // passing profile details to prefManager
                        pref.storeUserDetails(sessionId, name, username, mobile, location, course, level, true);


                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        sendMessage(AppConfig.httpHandlerOtp, "success");

                        // saving in shared preferences
                        pref.setRegStage(AppConfig.REG_STAGE_THREE);

                        Intent grapprIntent = new Intent(getApplicationContext(), GcmRegistrationService.class);
                        startService(grapprIntent);

                        Intent intent = new Intent(HttpService.this, IntroDiscoverActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        sendMessage(AppConfig.httpHandlerOtp, "fail");
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
                    sendMessage(AppConfig.httpHandlerOtp, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile", mobile);
                params.put("otp", otp);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }



    /**
     * Posting the OTP to server and activating the user
     *
     * @param feed_msg message content of feed
     * @param feed_img image attached to feed
     */
    private void postFeed(final String feed_msg, final String feed_img) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_POST_FEED, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerPostFeed, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerPostFeed, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerPostFeed, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("feed_msg", feed_msg);
                params.put("feed_img", feed_img);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }


    /**
     * Posting the OTP to server and activating the user
     *
     * @param feed_msg message content of feed
     * @param feed_img image attached to feed
     */
    private void editFeed(final String feed_id, final String feed_msg, final String feed_img) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EDIT_FEED, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerPostFeed, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerPostFeed, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerPostFeed, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("feedId", feed_id);
                params.put("feed_msg", feed_msg);
                params.put("feed_img", feed_img);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }



    private void likeFeed(final String feedId, final String like_type) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LIKE_FEED, new Response.Listener<String>() {

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

                        // parsing the feedback data
                        JSONObject feedbackObj = responseObj.getJSONObject("data");

                        String dil = AppConfig.OTP_DELIMITER;
                        String data = feedbackObj.getString("feedId")+dil+feedbackObj.getString("count");

                        sendMessage(AppConfig.httpHandlerLikeFeed, data);

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
                params.put("like_type", like_type);

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



    private void postComment(final String feedId, final String comment) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_POST_COMMENT, new Response.Listener<String>() {

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


                        // parsing the feedback data
                        JSONObject feedbackObj = responseObj.getJSONObject("data");

                        String dil = AppConfig.OTP_DELIMITER;
                        String data = feedbackObj.getString("feedId")+dil+feedbackObj.getString("count");

                        sendMessage(AppConfig.httpHandlerPostComment, data);

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                params.put("comment", comment);

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




    private void deleteFeed(final String feedId) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETE_FEED, new Response.Listener<String>() {

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

                        sendMessage(AppConfig.httpHandlerDeleteFeed, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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



    private void reportFeed(final String feedId) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REPORT_FEED, new Response.Listener<String>() {

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

                        sendMessage(AppConfig.httpHandlerReportFeed, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                Log.d(TAG, "Error: " + error.getMessage());

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



    private void followUser(final String target_username, final String follow_type) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_FOLLOW_USER, new Response.Listener<String>() {

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

                        // parsing the feedback data
                        JSONObject feedbackObj = responseObj.getJSONObject("data");

                        String dil = AppConfig.OTP_DELIMITER;
                        String data = feedbackObj.getString("target_username")+dil+feedbackObj.getString("isFollow");

                        sendMessage(AppConfig.httpHandlerFollowUser, data);

                    } else {
                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                params.put("follow_type", follow_type);

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


    private void updateStore(final String name, final String description, final boolean isActive) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_STORE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerUpdateStore, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerUpdateStore, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerUpdateStore, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("name", name);
                params.put("description", description);
                params.put("isActive", String.valueOf(isActive));
                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }


    private void rateStore(final String username, final String rating) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_RATE_STORE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerRateStore, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerRateStore, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerRateStore, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("target_username", username);
                params.put("rating", rating);
                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }



    private void postShopping(final String title, final String description, final String price,
                              final String category, final String condition,  final String shop_img) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_POST_SHOP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerPostShop, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerPostShop, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerPostShop, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("title", title);
                params.put("description", description);
                params.put("price", price);
                params.put("category", category);
                params.put("condition", condition);
                params.put("shop_img", shop_img);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }


    private void editShopping(final String shopId, final String title, final String description, final String price,
                              final String category, final String condition,  final String shop_img) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EDIT_SHOP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerEditShop, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerEditShop, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerEditShop, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("shopId", shopId);
                params.put("title", title);
                params.put("description", description);
                params.put("price", price);
                params.put("category", category);
                params.put("condition", condition);
                params.put("shop_img", shop_img);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }


    private void deleteShopping(final String shopId) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETE_SHOP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerDeleteShop, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerDeleteShop, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerDeleteShop, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("shopId", shopId);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }


    private void reportShop(final String shopId) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REPORT_SHOP, new Response.Listener<String>() {

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

                        sendMessage(AppConfig.httpHandlerReportShop, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                Log.d(TAG, "Error: " + error.getMessage());

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
                params.put("shopId", shopId);

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



    private void postTransit(final String bosso_means, final String bosso_time,
                             final String gidan_kwano_means, final String gidan_kwano_time) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_POST_TRANSIT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerPostTransit, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerPostTransit, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerPostTransit, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("bosso_means", bosso_means);
                params.put("bosso_time", bosso_time);
                params.put("gidan_kwano_means", gidan_kwano_means);
                params.put("gidan_kwano_time", gidan_kwano_time);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }


    private void changeProfilePic(final String profilePic) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CHANGE_PROFILE_PIC, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        sendMessage(AppConfig.httpHandlerChangeProfilePic, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerChangeProfilePic, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerChangeProfilePic, getString(R.string.err_network_timeout));
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("profilePic", profilePic);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);

    }


    private void seenNotification(final String notifyId) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SEEN_NOTIFICATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {

                        JSONObject feedbackObj = responseObj.getJSONObject("data");
                        sendMessage(AppConfig.httpHandlerSeenNotification, feedbackObj.getString("notifyId"));

                        /* Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();*/


                    } else {

                       /* Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();*/
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("notifyId", notifyId);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);

    }


    private void reportUser(final String target_username, final String report) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REPORT_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();


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
                        sendMessage(AppConfig.httpHandlerReportUser, "success");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();


                    } else {
                        sendMessage(AppConfig.httpHandlerReportUser, "fail");

                        Toast.makeText(getApplicationContext().getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
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
                    sendMessage(AppConfig.httpHandlerReportUser, getString(R.string.err_network_timeout));
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
                params.put("report", report);

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);


    }



    private void sendMessage(int id, String data) {

        //Handler httpServiceHandler = new Handler();

        switch (id) {

            case AppConfig.httpHandlerOtp:{

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerOtp;
                msg.obj = data;

                if(OTPActivity.mUiHandler != null)
                   OTPActivity.mUiHandler.sendMessage(msg);
            }
            break;

            case AppConfig.httpHandlerPostFeed:{

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerPostFeed;
                msg.obj = data;

                if(PostFeedActivity.mUiHandler != null)
                    PostFeedActivity.mUiHandler.sendMessage(msg);

                Message msg1 = new Message();
                msg1.what = AppConfig.httpHandlerPostFeed;
                msg1.obj = data;

                if(FeedFragment.mUiHandler != null)
                    FeedFragment.mUiHandler.sendMessage(msg1);
            }
            break;

            case AppConfig.httpHandlerPostComment:{

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerPostComment;
                msg.obj = data;

                if(CommentActivity.mUiHandler != null)
                    CommentActivity.mUiHandler.sendMessage(msg);

                Message msg1 = new Message();
                msg1.what = AppConfig.httpHandlerPostComment;
                msg1.obj = data;

                if(HashActivity.mUiHandler != null)
                    HashActivity.mUiHandler.sendMessage(msg1);

                Message msg2 = new Message();
                msg2.what = AppConfig.httpHandlerPostComment;
                msg2.obj = data;

                if(ProfileActivity.mUiHandler != null)
                    ProfileActivity.mUiHandler.sendMessage(msg2);

                Message msg3 = new Message();
                msg3.what = AppConfig.httpHandlerPostComment;
                msg3.obj = data;

                if(FeedFragment.mUiHandler != null)
                    FeedFragment.mUiHandler.sendMessage(msg3);

                Message msg4 = new Message();
                msg4.what = AppConfig.httpHandlerPostComment;
                msg4.obj = data;

                if(DiscoverFragment.mUiHandler != null)
                    DiscoverFragment.mUiHandler.sendMessage(msg4);

                Message msg5 = new Message();
                msg5.what = AppConfig.httpHandlerPostComment;
                msg5.obj = data;

                if(SearchFragment.mUiHandler != null)
                    SearchFragment.mUiHandler.sendMessage(msg5);

                Message msg6 = new Message();
                msg6.what = AppConfig.httpHandlerPostComment;
                msg6.obj = data;

                if(DisplayFeedActivity.mUiHandler != null)
                    DisplayFeedActivity.mUiHandler.sendMessage(msg6);

            }
            break;

            case AppConfig.httpHandlerLikeFeed:{

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerLikeFeed;
                msg.obj = data;

                if(HashActivity.mUiHandler != null)
                    HashActivity.mUiHandler.sendMessage(msg);

                Message msg1 = new Message();
                msg1.what = AppConfig.httpHandlerLikeFeed;
                msg1.obj = data;

                if(ProfileActivity.mUiHandler != null)
                    ProfileActivity.mUiHandler.sendMessage(msg1);

                Message msg2 = new Message();
                msg2.what = AppConfig.httpHandlerLikeFeed;
                msg2.obj = data;

                if(FeedFragment.mUiHandler != null)
                    FeedFragment.mUiHandler.sendMessage(msg2);

                Message msg3 = new Message();
                msg3.what = AppConfig.httpHandlerLikeFeed;
                msg3.obj = data;

                if(DiscoverFragment.mUiHandler != null)
                    DiscoverFragment.mUiHandler.sendMessage(msg3);

                Message msg4 = new Message();
                msg4.what = AppConfig.httpHandlerLikeFeed;
                msg4.obj = data;

                if(SearchFragment.mUiHandler != null)
                    SearchFragment.mUiHandler.sendMessage(msg4);

                Message msg5 = new Message();
                msg5.what = AppConfig.httpHandlerLikeFeed;
                msg5.obj = data;

                if(DisplayFeedActivity.mUiHandler != null)
                    DisplayFeedActivity.mUiHandler.sendMessage(msg5);

            }
            break;

            case AppConfig.httpHandlerDeleteFeed: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerDeleteFeed;
                msg.obj = data;

                if(HashActivity.mUiHandler != null)
                    HashActivity.mUiHandler.sendMessage(msg);

                Message msg1 = new Message();
                msg1.what = AppConfig.httpHandlerDeleteFeed;
                msg1.obj = data;

                if(ProfileActivity.mUiHandler != null)
                    ProfileActivity.mUiHandler.sendMessage(msg1);

                Message msg2 = new Message();
                msg2.what = AppConfig.httpHandlerDeleteFeed;
                msg2.obj = data;

                if(FeedFragment.mUiHandler != null)
                    FeedFragment.mUiHandler.sendMessage(msg2);

                Message msg3 = new Message();
                msg3.what = AppConfig.httpHandlerDeleteFeed;
                msg3.obj = data;

                if(DiscoverFragment.mUiHandler != null)
                    DiscoverFragment.mUiHandler.sendMessage(msg3);

                Message msg4 = new Message();
                msg4.what = AppConfig.httpHandlerDeleteFeed;
                msg4.obj = data;

                if(SearchFragment.mUiHandler != null)
                    SearchFragment.mUiHandler.sendMessage(msg4);

              /*  Message msg5 = new Message();
                msg5.what = AppConfig.httpHandlerDeleteFeed;
                msg5.obj = data;

                if(DisplayFeedActivity.mUiHandler != null)
                    SearchFragment.mUiHandler.sendMessage(msg5); */
            }
            break;

            case AppConfig.httpHandlerFollowUser:{

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerFollowUser;
                msg.obj = data;

                if(UserListActivity.mUiHandler != null)
                    UserListActivity.mUiHandler.sendMessage(msg);

                Message msg1 = new Message();
                msg1.what = AppConfig.httpHandlerFollowUser;
                msg1.obj = data;

                if(DiscoverFragment.mUiHandler != null)
                    DiscoverFragment.mUiHandler.sendMessage(msg1);

                Message msg2 = new Message();
                msg2.what = AppConfig.httpHandlerFollowUser;
                msg2.obj = data;

                if(SearchFragment.mUiHandler != null)
                    SearchFragment.mUiHandler.sendMessage(msg2);

                Message msg3 = new Message();
                msg3.what = AppConfig.httpHandlerFollowUser;
                msg3.obj = data;

                if(ProfileActivity.mUiHandler != null)
                    ProfileActivity.mUiHandler.sendMessage(msg3);

                Message msg4 = new Message();
                msg4.what = AppConfig.httpHandlerFollowUser;
                msg4.obj = data;

                if(SearchContactActivity.mUiHandler != null)
                    SearchContactActivity.mUiHandler.sendMessage(msg4);
            }
            break;

            case AppConfig.httpHandlerUpdateStore: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerUpdateStore;
                msg.obj = data;

                if(StoreSettingActivity.mUiHandler != null)
                    StoreSettingActivity.mUiHandler.sendMessage(msg);
            }
            break;

            case AppConfig.httpHandlerRateStore: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerRateStore;
                msg.obj = data;

                if(StoreActivity.mUiHandler != null)
                    StoreActivity.mUiHandler.sendMessage(msg);
            }
            break;

            case AppConfig.httpHandlerPostShop: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerPostShop;
                msg.obj = data;

                if(PostShopActivity.mUiHandler != null)
                    PostShopActivity.mUiHandler.sendMessage(msg);
            }
            break;

            case AppConfig.httpHandlerEditShop: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerEditShop;
                msg.obj = data;

                if(EditShopActivity.mUiHandler != null)
                    EditShopActivity.mUiHandler.sendMessage(msg);


                Message msg1 = new Message();
                msg1.what = AppConfig.httpHandlerEditShop;
                msg1.obj = data;

                if(StoreActivity.mUiHandler != null)
                    StoreActivity.mUiHandler.sendMessage(msg1);


                Message msg2 = new Message();
                msg2.what = AppConfig.httpHandlerEditShop;
                msg2.obj = data;

                if(ShoppingActivity.mUiHandler != null)
                    ShoppingActivity.mUiHandler.sendMessage(msg2);



            }
            break;

            case AppConfig.httpHandlerDeleteShop: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerDeleteShop;
                msg.obj = data;

                if(StoreActivity.mUiHandler != null)
                    StoreActivity.mUiHandler.sendMessage(msg);


                Message msg1 = new Message();
                msg1.what = AppConfig.httpHandlerDeleteShop;
                msg1.obj = data;

                if(ShoppingActivity.mUiHandler != null)
                    ShoppingActivity.mUiHandler.sendMessage(msg1);

            }
            break;

            case AppConfig.httpHandlerReportShop: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerReportShop;
                msg.obj = data;

                if(ShoppingActivity.mUiHandler != null)
                    ShoppingActivity.mUiHandler.sendMessage(msg);

                Message msg2 = new Message();
                msg2.what = AppConfig.httpHandlerReportShop;
                msg2.obj = data;

                if(StoreActivity.mUiHandler != null)
                    StoreActivity.mUiHandler.sendMessage(msg2);
            }
            break;

            case AppConfig.httpHandlerPostTransit: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerPostTransit;
                msg.obj = data;

                if(PostTransitActivity.mUiHandler != null)
                    PostTransitActivity.mUiHandler.sendMessage(msg);
            }
            break;


            case AppConfig.httpHandlerChangeProfilePic: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerChangeProfilePic;
                msg.obj = data;

                if(ProfilePicActivity.mUiHandler != null)
                    ProfilePicActivity.mUiHandler.sendMessage(msg);
            }
            break;

            case AppConfig.httpHandlerSeenNotification: {

                Message msg = new Message();
                msg.what = AppConfig.httpHandlerSeenNotification;
                msg.obj = data;

                if(NotificationFragment.mUiHandler != null)
                    NotificationFragment.mUiHandler.sendMessage(msg);
            }
            break;
        }
    }



    /**
     * The method decodes the image file to avoid out of memory issues.
     *
     * @param filePath
     */
    public String decodeFilePath(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 512;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp <= REQUIRED_SIZE && height_tmp <= REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, o2);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] data = bos.toByteArray();

        return Base64.encodeToString(data, 0);//encodeBytes(data);
    }

}
