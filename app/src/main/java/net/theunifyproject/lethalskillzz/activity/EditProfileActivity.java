package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import net.android.volley.toolbox.StringRequest;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.util.Logout;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    //logcat tag
    private static String TAG = EditProfileActivity.class.getSimpleName();
    private PrefManager pref;
    HashMap<String, String> profile;
    //Connection detector class
    private ConnectionDetector cd;
    private Logout mLogout;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private FeedImageView profilePic;
    private EditText inputName;
    private TextInputLayout inputLayoutName;
    private Spinner locSpinner, courseSpinner, levelSpinner;
    private Button btn_done;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        pref = new PrefManager(this);
        profile = pref.getUserDetails();
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        profilePic = (FeedImageView) findViewById(R.id.edit_profile_pic);
        inputName = (EditText) findViewById(R.id.edit_profile_name);
        locSpinner = (Spinner) findViewById(R.id.edit_profile_location);
        courseSpinner = (Spinner) findViewById(R.id.edit_profile_course);
        levelSpinner = (Spinner) findViewById(R.id.edit_profile_level);
        btn_done = (Button) findViewById(R.id.edit_profile_button);
        progressBar = (ProgressBar) findViewById(R.id.edit_profile_progressBar);
        inputLayoutName = (TextInputLayout) findViewById(R.id.edit_profile_input_layout_name);


        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        profilePic.setImageUrl(AppConfig.URL_PROFILE_PIC + pref.getUsername() + ".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
        profilePic.setVisibility(View.VISIBLE);
        profilePic.setResponseObserver(new FeedImageView.ResponseObserver() {
            @Override
            public void onError() {
                profilePic.setImageResource(R.drawable.ic_user);
            }

            @Override
            public void onSuccess() {
            }
        });

        inputName.setText(profile.get("name"));

        String[] locTitles = getApplicationContext().getResources().getStringArray(R.array.location_array);
        for (int i = 0; i < locTitles.length; i++) {
            if(locTitles[i].equals(profile.get("location"))) {
                locSpinner.setSelection(i);
            }
        }

        String[] courseTitles = getApplicationContext().getResources().getStringArray(R.array.course_array);
        for (int i = 0; i < courseTitles.length; i++) {
            if(courseTitles[i].equals(profile.get("course"))) {
                courseSpinner.setSelection(i);
            }
        }

        String[] levelTitles = getApplicationContext().getResources().getStringArray(R.array.level_array);
        for (int i = 0; i < levelTitles.length; i++) {
            if(levelTitles[i].equals(profile.get("level"))) {
                levelSpinner.setSelection(i);
            }
        }


        profilePic.setOnClickListener(this);
        btn_done.setOnClickListener(this);

        requestFocus(inputName);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        AppController.getInstance().getRequestQueue().getCache().remove(AppConfig.URL_PROFILE_PIC + pref.getUsername() + ".png");

        profilePic.setImageUrl(AppConfig.URL_PROFILE_PIC + pref.getUsername() + ".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
        profilePic.setVisibility(View.VISIBLE);
        profilePic.setResponseObserver(new FeedImageView.ResponseObserver() {
            @Override
            public void onError() {
                profilePic.setImageResource(R.drawable.ic_user);
            }

            @Override
            public void onSuccess() {
            }
        });

    }

    /*@Override
    public void onPause() {
        super.onPause();

    }*/


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.edit_profile_button:
                submit();
                break;

            case R.id.edit_profile_pic: {
                Intent intent = new Intent(getApplicationContext(), ProfilePicActivity.class);
                intent.putExtra("username", pref.getUsername());
                startActivity(intent);
            }
            break;
        }
    }


    private void submit() {

        if (!validateName(inputName,inputLayoutName)) {
            return;
        }

        String name = inputName.getText().toString().trim();
        String location = locSpinner.getSelectedItem().toString().trim();
        String course = courseSpinner.getSelectedItem().toString().trim();
        String level = levelSpinner.getSelectedItem().toString().trim();

        if (cd.isConnectingToInternet()) {

            progressBar.setVisibility(View.VISIBLE);
            btn_done.setVisibility(View.GONE);
            editProfile(name, location, course, level);

        } else
            showSnackBar(0,getString(R.string.err_no_internet));


    }

    private void editProfile(final String name, final String location, final String course, final String level) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EDIT_PROFILE, new Response.Listener<String>() {

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
                        pref.storeUserDetails(sessionId, name, username, mobile, location, course, level, false);

                        finish();

                    } else {
                        showSnackBar(1, message);
                    }

                    // unhiding the done button
                    btn_done.setVisibility(View.VISIBLE);
                    // hiding the progress bar
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    btn_done.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());

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

                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();

                btn_done.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
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
                params.put("name", name);
                params.put("location", location);
                params.put("course", course);
                params.put("level", level);

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


    private boolean validateName(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_name));
            requestFocus(EdTxt);
            return false;
        } else {
            if(isValidName(EdTxt.getText().toString().trim())) {
                inputLayout.setError(getString(R.string.err_msg_name));
                requestFocus(EdTxt);
                return false;
            }
            inputLayout.setErrorEnabled(false);
        }

        return true;
    }



    /**
     * Regex to validate the name
     * name should not contain any special character
     * @param name
     * @return
     */
    private static boolean isValidName(String name) {
        Pattern p = Pattern.compile("[^A-Za-z\\s]");
        Matcher m = p.matcher(name);
        return m.find();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.editProfileCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        if(id == 0){
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cd.isConnectingToInternet())
                        submit();
                    else
                        showSnackBar(0, getString(R.string.err_no_internet));
                }
            });
        }

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

    }

}
