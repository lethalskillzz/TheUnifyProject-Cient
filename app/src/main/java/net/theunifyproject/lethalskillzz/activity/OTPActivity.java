package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import net.theunifyproject.lethalskillzz.service.HttpService;

public class OTPActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = OTPActivity.class.getSimpleName();

    private PrefManager pref;
    public static Handler mUiHandler;

    //Connection detector class
    private ConnectionDetector cd;

    private EditText inputOtp;
    private Button btnVerifyOtp;
    private Button btnLinkResendCode;
    private TextInputLayout inputLayoutOtp;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        pref = new PrefManager(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        inputOtp = (EditText) findViewById(R.id.inputOtp);
        btnVerifyOtp = (Button) findViewById(R.id.btn_verify_otp);
        btnLinkResendCode = (Button) findViewById(R.id.btnLinkResendCode);
        inputLayoutOtp  = (TextInputLayout) findViewById(R.id.input_layout_otp);

        progressBar = (ProgressBar) findViewById(R.id.otpProgressBar);

        btnVerifyOtp.setOnClickListener(this);
        btnLinkResendCode.setOnClickListener(this);


        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {

                switch(msg.what) {

                    case AppConfig.smsHandlerOtp: {
                        inputOtp.setText(((String)msg.obj).trim());
                    }
                    break;

                    case AppConfig.httpHandlerOtp: {

                        // unhiding the OTP button
                        btnVerifyOtp.setVisibility(View.VISIBLE);
                        // hiding the progress bar
                        progressBar.setVisibility(View.GONE);

                        if(((String)msg.obj).trim().equals("success")) {
                            finish();
                        } else if(((String)msg.obj).trim().equals("fail"))
                            inputLayoutOtp.setError(getString(R.string.err_msg_otp));
                        else
                            showSnackBar(0, getString(R.string.err_network_timeout));

                    }
                    break;
                }

        }

        };

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_verify_otp: {

                //checking network connectivity
                if (cd.isConnectingToInternet()) {
                    verifyOtp();
                }else showSnackBar(0, getString(R.string.err_no_internet));
            }
            break;

            case R.id.btnLinkResendCode:
                resendOtp(pref.getMobileNumber());
                break;
        }
    }


    /**
     * sending the OTP to server and activating the user
     */
    private void verifyOtp() {
        String otp = inputOtp.getText().toString().trim();

        if (otp.length()==6) {
            inputLayoutOtp.setErrorEnabled(false);

            // hiding the OTP button
            btnVerifyOtp.setVisibility(View.GONE);
            // unhiding the progress bar
            progressBar.setVisibility(View.VISIBLE);

            Intent grapprIntent = new Intent(getApplicationContext(), HttpService.class);
            grapprIntent.putExtra("intent_type", AppConfig.httpIntentOtp);
            grapprIntent.putExtra("otp", otp);
            startService(grapprIntent);

        } else {
            inputLayoutOtp.setError(getString(R.string.err_msg_otp));
        }
    }



    /**
     * Request for code resend
     */
    private void resendOtp(final String mobile) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_RESEND_OTP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();


                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error
                    if (!error) {

                        showSnackBar(1, message);

                    } else {

                        showSnackBar(1, message);
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
                    showSnackBar(1, getString(R.string.err_network_timeout));
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
                params.put("mobile", mobile);

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


    //SnackBar function
    private void showSnackBar(final int id, String msg) {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.otpCoordinatorLayout);
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        if(id == 0){
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //checking network connectivity
                    if (cd.isConnectingToInternet()) verifyOtp();
                    else showSnackBar(0, getString(R.string.err_no_internet));
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
