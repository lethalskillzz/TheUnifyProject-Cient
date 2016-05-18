package net.theunifyproject.lethalskillzz.activity;

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
import net.theunifyproject.lethalskillzz.util.Logout;

public class ChangePasswordActivity extends AppCompatActivity  implements View.OnClickListener{

    //logcat tag
    private static String TAG = ChangeNumberActivity.class.getSimpleName();

    private Logout mLogout;

    //Connection detector class
    private ConnectionDetector cd;

    //view & widget declarations
    private Button btnSubmit;
    private EditText inputOldPassword, inputNewPassword, inputConfirmPassword;
    private ProgressBar progressBar;
    private TextInputLayout inputLayoutOldPassword, inputLayoutNewPassword, inputLayoutConfirmPassword;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Toolbar toolbar = (Toolbar) findViewById(R.id.change_password_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        pref = new PrefManager(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        inputLayoutOldPassword = (TextInputLayout) findViewById(R.id.input_layout_change_old_password);
        inputLayoutNewPassword = (TextInputLayout) findViewById(R.id.input_layout_change_new_password);
        inputLayoutConfirmPassword = (TextInputLayout) findViewById(R.id.input_layout_change_confirm_password);

        inputOldPassword = (EditText) findViewById(R.id.input_change_old_password);
        inputNewPassword = (EditText) findViewById(R.id.input_change_new_password);
        inputConfirmPassword = (EditText) findViewById(R.id.input_change_confirm_password);

        progressBar = (ProgressBar) findViewById(R.id.change_password_progressBar);

        btnSubmit = (Button) findViewById(R.id.change_password_btn_submit);

        // view click listeners
        btnSubmit.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.change_password_btn_submit:
                submit();
                break;

        }
    }

    private void submit() {

        if (!validatePassword(inputOldPassword, inputLayoutOldPassword)) {
            return;
        }

        if (!validatePassword(inputNewPassword, inputLayoutNewPassword)) {
            return;
        }

        if (!validateConfirmPassword(inputConfirmPassword, inputNewPassword, inputLayoutConfirmPassword)) {
            return;
        }
        //checking network connectivity
        if (cd.isConnectingToInternet()) {
            String oldPassword = inputOldPassword.getText().toString().trim();
            String newPassword = inputNewPassword.getText().toString().trim();

            // hiding the login button
            btnSubmit.setVisibility(View.GONE);
            // showing progress bar
            progressBar.setVisibility(View.VISIBLE);

            //connect to Server for credential authentication
            changePassword(oldPassword, newPassword);
        }
        else
            showSnackBar(0, getString(R.string.err_no_internet));

    }

    private void changePassword(final String oldPassword, final String newPassword) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CHANGE_PASSWORD, new Response.Listener<String>() {

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

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        finish();

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
                params.put("oldPassword", oldPassword);
                params.put("newPassword", newPassword);

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


    private boolean validatePassword(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_password));
            requestFocus(EdTxt);
            return false;
        } else {
            if (EdTxt.getText().toString().trim().length() < 6) {
                inputLayout.setError(getString(R.string.err_msg_password_length));
                requestFocus(EdTxt);
                return false;
            }

            inputLayout.setErrorEnabled(false);

        }

        return true;
    }


    private boolean validateConfirmPassword(EditText EdTxtC, EditText EdTxtP, TextInputLayout inputLayout) {
        if (EdTxtC.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_confirm));
            requestFocus(EdTxtC);
            return false;
        } else {
            if(EdTxtC.getText().toString().trim().equals(EdTxtP.getText().toString().trim())) {
                inputLayout.setErrorEnabled(false);
            }
            else {
                inputLayout.setError(getString(R.string.err_msg_confirm));
                requestFocus(EdTxtC);
                return false;
            }
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    //SnackBar function
    private void showSnackBar(final int id, String msg) {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.changePasswordCoordinatorLayout);
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

}
