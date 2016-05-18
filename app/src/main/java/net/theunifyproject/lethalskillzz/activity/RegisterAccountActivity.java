package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterAccountActivity extends AppCompatActivity implements View.OnClickListener {

    //logcat tag
    private static String TAG = RegisterAccountActivity.class.getSimpleName();

    //Connection detector class
    private ConnectionDetector cd;

    //view & widget declarations
    private ViewPager viewPager;
    private ViewPagerAdapter viewAdapter;
    private Button btnNext,btnSubmit;
    private EditText inputUsername, inputPassword, inputConfirmPassword, inputMobile;
    private ProgressBar progressBar;
    private TextInputLayout inputLayoutUsername, inputLayoutPassword, inputLayoutConfirmPassword,  inputLayoutMobile;
    private PrefManager pref;
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        Intent intent = getIntent();
        String username  = intent.getStringExtra("username");

        pref = new PrefManager(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        viewPager = (ViewPager) findViewById(R.id.viewPagerVertical2);

        inputLayoutUsername = (TextInputLayout) findViewById(R.id.input_layout_reg_username);
        inputLayoutMobile = (TextInputLayout) findViewById(R.id.input_layout_reg_mobile);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_reg_password);
        inputLayoutConfirmPassword = (TextInputLayout) findViewById(R.id.input_layout_reg_confirm_password);


        inputUsername = (EditText) findViewById(R.id.input_reg_username);
        inputPassword = (EditText) findViewById(R.id.input_reg_password);
        inputConfirmPassword = (EditText) findViewById(R.id.input_reg_confirm_password);
        inputMobile = (EditText) findViewById(R.id.input_reg_mobile);

        progressBar = (ProgressBar) findViewById(R.id.confirmProgressBar);

        inputUsername.addTextChangedListener(new MyTextWatcher(inputUsername));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
        inputConfirmPassword.addTextChangedListener(new MyTextWatcher(inputConfirmPassword));
        inputMobile.addTextChangedListener(new MyTextWatcher(inputMobile));

        btnNext = (Button) findViewById(R.id.btnConfirmNext);
        btnSubmit = (Button) findViewById(R.id.btnConfirm);

        // view click listeners
        btnNext.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        inputUsername.setText(username);

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

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.confirmCoordinatorLayout);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnConfirm:
                submitConfirm();
                break;


            case R.id.btnConfirmNext:
                next();
                break;

            case R.id.btnLinkToLoginScreen:
                gotoLogin();
                break;

            case R.id.btnLinkToLoginScreen2:
                gotoLogin();
                break;

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK :
                viewPager.setCurrentItem(0);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    private void next() {

        if (!validateUsername(inputUsername, inputLayoutUsername))
        {
            return;
        }

        if (!validatePassword(inputPassword, inputLayoutPassword))
        {
            return;
        }

        if (!validateConfirmPassword(inputConfirmPassword, inputPassword, inputLayoutConfirmPassword))
        {
            return;
        }


        viewPager.setCurrentItem(1);
    }


    private void submitConfirm() {
        if (!validateMobile(inputMobile, inputLayoutMobile))
        {
            return;
        }


        //checking network connectivity
        if (cd.isConnectingToInternet()) {
            String mobile = inputMobile.getText().toString().trim();
            String username = inputUsername.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();


            // hiding the login button
            btnSubmit.setVisibility(View.INVISIBLE);
            // showing progress bar
            progressBar.setVisibility(View.VISIBLE);

            //connect to Server for confirmation
            registerAccountDetail(username, password, mobile);
        }
        else
            showSnackBar(0, getString(R.string.err_no_internet));


    }


    private void gotoLogin() {
        Intent intent = new Intent(RegisterAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }



    private boolean validateMobile(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_mobile));
            requestFocus(EdTxt);
            return false;
        } else {
            if (isValidPhoneNumber(EdTxt.getText().toString().trim())) {
                inputLayout.setErrorEnabled(false);
            }
            else
            {
                inputLayout.setError(getString(R.string.err_msg_mobile));
                requestFocus(EdTxt);
                return false;
            }
        }

        return true;
    }


    private boolean validateUsername(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_username));
            requestFocus(EdTxt);
            return false;
        } else {

            if(isValidUsername(EdTxt.getText().toString().trim())) {
                inputLayout.setError(getString(R.string.err_msg_username));
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


    /**
     * function to register user details
     * @param mobile user valid mobile number
     * @param username  user unique name
     */
    private void registerAccountDetail(final String username,  final String password, final String mobile) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER_ACCOUNT_DETAIL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error, if not error SMS is initiated
                    // device should receive it shortly
                    if (!error) {

                        pref.setMobileNumber(mobile);
                        pref.setRegStage(2);

                        Intent intent = new Intent(RegisterAccountActivity.this, OTPActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        finish();

                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    } else {

                        String error_type = responseObj.getString("error_type");
                        switch (error_type) {

                            case "Username": {
                                viewPager.setCurrentItem(0);
                                inputLayoutUsername.setError(getString(R.string.err_msg_username_taken));
                                requestFocus(inputUsername);
                            }
                            break;

                            case "Mobile": {
                                viewPager.setCurrentItem(1);
                                inputLayoutMobile.setError(getString(R.string.err_msg_mobile_taken));
                                requestFocus(inputMobile);
                            }
                            break;

                            default:
                                showSnackBar(1, message);
                                break;
                        }

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
                params.put("sessionId", pref.getSessionId());
                params.put("username", username);
                params.put("password", password);
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


    /**
     * Regex to validate the mobile number
     * mobile number should be of 11 digits length
     * @param mobile
     * @return
     */
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{11}$";
        return mobile.matches(regEx);
    }

    /**
     * Regex to validate the username
     * @param username
     * @return
     */
    private static boolean isValidUsername(String username) {
        Pattern p = Pattern.compile("[^A-Za-z0-9_]");
        Matcher m = p.matcher(username);
        return m.find();
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {

                case R.id.input_reg_mobile:
                    validateMobile(inputMobile, inputLayoutMobile);
                    break;

                case R.id.input_reg_username:
                    validateUsername(inputUsername, inputLayoutUsername);
                    break;

                case R.id.input_reg_password:
                    validatePassword(inputPassword, inputLayoutPassword);
                    break;

                case R.id.input_reg_confirm_password:
                    validateConfirmPassword(inputConfirmPassword, inputPassword, inputLayoutConfirmPassword);
                    break;
            }
        }
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
                    resId = R.id.layout_confirmation;
                    break;

                case 1:
                    resId = R.id.layout_confirmation2;
                    break;

            }
            return findViewById(resId);
        }

    }



    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        if(id == 0){
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submitConfirm();
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
