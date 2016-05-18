package net.theunifyproject.lethalskillzz.activity;

import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.glomadrian.codeinputlib.CodeInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.util.Logout;

public class ChangeNumberActivity extends AppCompatActivity implements View.OnClickListener{

    //logcat tag
    private static String TAG = ChangeNumberActivity.class.getSimpleName();

    private Logout mLogout;
    private PrefManager pref;
    //Connection detector class
    private ConnectionDetector cd;

    //view & widget declarations
    private Button btnSubmit, btnSubmit2;
    private EditText inputMobile, inputPassword, inputOtp;
    private ProgressBar progressBar;
    private TextInputLayout inputLayoutMobile, inputLayoutPassword, inputLayoutOtp;
    private ViewPager viewPager;
    private ViewPagerAdapter viewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_number);

        Toolbar toolbar = (Toolbar) findViewById(R.id.change_number_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        pref = new PrefManager(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        viewPager = (ViewPager) findViewById(R.id.change_number_viewPager);

        inputLayoutMobile = (TextInputLayout) findViewById(R.id.change_number_input_layout_mobile);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.change_number_input_layout_password);
        inputLayoutOtp = (TextInputLayout) findViewById(R.id.change_number_input_layout_otp);

        inputMobile = (EditText) findViewById(R.id.change_number_input_mobile);
        inputPassword = (EditText) findViewById(R.id.change_number_input_password);
        inputOtp = (EditText) findViewById(R.id.change_number_inputOtp);


        progressBar = (ProgressBar) findViewById(R.id.change_number_progressBar);

        btnSubmit = (Button) findViewById(R.id.change_number_btn_submit);
        btnSubmit2 = (Button) findViewById(R.id.change_number_btn_submit2);

        // view click listeners
        btnSubmit.setOnClickListener(this);
        btnSubmit2.setOnClickListener(this);

        viewAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(viewAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.change_number_btn_submit:
                submit();
                break;

            case R.id.change_number_btn_submit2:
                verifyOtp();
                break;

        }
    }

    private void submit() {
        if (!validateMobile(inputMobile, inputLayoutMobile)) {
            return;
        }

        if (!validatePassword(inputPassword, inputLayoutPassword)) {
            return;
        }
        //checking network connectivity
        if (cd.isConnectingToInternet()) {
            String mobile = inputMobile.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // hiding the login button
            btnSubmit.setVisibility(View.GONE);
            // showing progress bar
            progressBar.setVisibility(View.VISIBLE);

            pref.setNewMobileNumber(mobile);
            //connect to Server for credential authentication
            changeNumber(mobile, password);
        }
        else
            showSnackBar(0, getString(R.string.err_no_internet));

    }

    private void verifyOtp() {
        String otp = inputOtp.getText().toString().trim();

        if (otp.length()==6) {
            inputLayoutOtp.setErrorEnabled(false);

            //checking network connectivity
            if (cd.isConnectingToInternet()) {

                // hiding the login button
                btnSubmit2.setVisibility(View.GONE);
                // showing progress bar
                progressBar.setVisibility(View.VISIBLE);

                //connect to Server for credential authentication
                changeNumberVerifyOtp(pref.getNewMobileNumber(), otp);
            } else
                showSnackBar(0, getString(R.string.err_no_internet));

        }else
            inputLayoutOtp.setError(getString(R.string.err_msg_otp));
    }


    private void changeNumber(final String mobile, final String password) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CHANGE_NUMBER, new Response.Listener<String>() {

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

                    // checking for error
                    if (!error) {

                        //JSONObject feedbackObj = responseObj.getJSONObject("data");

                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        viewPager.setCurrentItem(1);

                    } else {
                        showSnackBar(1, message);

                    }

                    // unhiding the login button
                    btnSubmit.setVisibility(View.VISIBLE);
                    // hiding the progress bar
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    btnSubmit.setVisibility(View.VISIBLE);
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

                btnSubmit.setVisibility(View.VISIBLE);
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
                params.put("mobile", mobile);
                params.put("password", password);

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


    private void changeNumberVerifyOtp(final String mobile, final String otp) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CHANGE_NUMBER_VERIFY_OTP, new Response.Listener<String>() {

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

                    // checking for error
                    if (!error) {

                        JSONObject feedbackObj = responseObj.getJSONObject("data");
                        pref.setMobileNumber( feedbackObj.getString("mobile"));

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        finish();

                    } else {
                        showSnackBar(1, message);

                    }

                    // unhiding the login button
                    btnSubmit2.setVisibility(View.VISIBLE);
                    // hiding the progress bar
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    btnSubmit2.setVisibility(View.VISIBLE);
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

                btnSubmit2.setVisibility(View.VISIBLE);
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


    private boolean validateMobile(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_mobile));
            requestFocus(EdTxt);
            return false;
        } else {
            if (isValidPhoneNumber(EdTxt.getText().toString().trim())!=true) {
                //inputLayout.setErrorEnabled(false);
                inputLayout.setError(getString(R.string.err_msg_mobile));
                requestFocus(EdTxt);
                return false;
            }
            inputLayout.setErrorEnabled(false);
        }

        return true;
    }


    private boolean validatePassword(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_password));
            requestFocus(EdTxt);
            return false;
        } else {
            inputLayout.setErrorEnabled(false);
        }

        return true;
    }



    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     *
     * @param mobile
     * @return
     */
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{11}$";
        return mobile.matches(regEx);
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    //SnackBar function
    private void showSnackBar(final int id, String msg) {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.changeNumberCoordinatorLayout);
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        if(id == 0){
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cd.isConnectingToInternet()) submit();
                    else  showSnackBar(0, getString(R.string.err_no_internet));
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

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        public Object instantiateItem(View collection, int position) {

            int resId = 0;
            switch (position) {


                case 0:
                    resId = R.id.layout_change_number;
                    break;

                case 1:
                    resId = R.id.layout_change_number2;
                    break;

            }
            return findViewById(resId);
        }

    }


}
