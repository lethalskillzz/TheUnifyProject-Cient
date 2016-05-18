package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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
import net.theunifyproject.lethalskillzz.service.GcmRegistrationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    //logcat tag
    private static String TAG = LoginActivity.class.getSimpleName();

    //Connection detector class
    private ConnectionDetector cd;

    //view & widget declarations
    private ViewPager viewPager;
    private ViewPagerAdapter viewAdapter;
    private Button btnLogin, btnRecoverPassword, btnLinkToRegister, btnLinkForgotPassword;
    private EditText inputLogin, inputPassword, inputForgotPassword;
    private ProgressBar progressBar;
    private TextInputLayout inputLayoutLogin, inputLayoutPassword, inputLayoutForgotPassword;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = new PrefManager(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        viewPager = (ViewPager) findViewById(R.id.viewPagerVertical3);

        inputLayoutLogin = (TextInputLayout) findViewById(R.id.input_layout_login);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputLayoutForgotPassword = (TextInputLayout) findViewById(R.id.input_layout_forgot_password);


        inputLogin = (EditText) findViewById(R.id.input_login);
        inputPassword = (EditText) findViewById(R.id.input_password);
        inputForgotPassword = (EditText) findViewById(R.id.input_forgot_password);


        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);


        inputLogin.addTextChangedListener(new MyTextWatcher(inputLogin));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
        inputForgotPassword.addTextChangedListener(new MyTextWatcher(inputForgotPassword));



        btnLogin = (Button) findViewById(R.id.btn_login);
        btnRecoverPassword = (Button) findViewById(R.id.btn_recover_password);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnLinkForgotPassword = (Button) findViewById(R.id.btnForgotPassword);



        // view click listeners
        btnLogin.setOnClickListener(this);
        btnRecoverPassword.setOnClickListener(this);
        btnLinkToRegister.setOnClickListener(this);
        btnLinkForgotPassword.setOnClickListener(this);

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

            case R.id.btn_login:
                submitLogin();
                break;

            case R.id.btn_recover_password:
                submitRecoverPassword();
                break;


            case R.id.btnForgotPassword:
                viewPager.setCurrentItem(1);
                break;

            case R.id.btnLinkToRegisterScreen: {
                Intent intent = new Intent(LoginActivity.this, RegisterProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
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



    private void submitLogin() {

        if (!validateInput(inputLogin, inputLayoutLogin))
        {
            return;
        }

        if (!validatePassword(inputPassword, inputLayoutPassword))
        {
            return;
        }

        //checking network connectivity
        if (cd.isConnectingToInternet()) {
            String login = inputLogin.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // hiding the login button
            btnLogin.setVisibility(View.GONE);
            // showing progress bar
            progressBar.setVisibility(View.VISIBLE);

            //connect to Server for credential authentication
            if (isValidPhoneNumber(login))
                loginUser("mobile",login, password);
            else
                loginUser("username",login, password);
        } else
            showSnackBar(0, getString(R.string.err_no_internet));


    }

    private void submitRecoverPassword() {

        if (!validateMobile(inputForgotPassword, inputLayoutForgotPassword))
        {
            return;
        }

        //checking network connectivity
        if (cd.isConnectingToInternet()) {
            String login = inputForgotPassword.getText().toString().trim();

            // hiding the login button
            btnRecoverPassword.setVisibility(View.GONE);
            // showing progress bar
            progressBar.setVisibility(View.VISIBLE);

            //connect to Server for credential authentication
            if (isValidPhoneNumber(login))
                recoverPassword("mobile", login);
            else
                recoverPassword("username",login);
        } else
            showSnackBar(1, getString(R.string.err_no_internet));

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


    private boolean validateInput(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_username_mobile));
            requestFocus(EdTxt);
            return false;
        } else {
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
     * function to verify login details in mysql db
     * @param login valid username or  mobile number
     * @param password  user password address
     */
    private void loginUser(final String type, final String login, final String password) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_USER_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
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
                        pref.storeUserDetails(sessionId, name, username, mobile, location, course, level, true);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        Intent grapprIntent = new Intent(getApplicationContext(), GcmRegistrationService.class);
                        startService(grapprIntent);

                        finish();

                       // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    } else {
                        showSnackBar(0, message);

                    }

                    // unhiding the login button
                    btnLogin.setVisibility(View.VISIBLE);
                    // hiding the progress bar
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    btnLogin.setVisibility(View.VISIBLE);
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

                btnLogin.setVisibility(View.VISIBLE);
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
                params.put("type", type);
                params.put("login", login);
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


    /**
     * function to recover passwords
     * @param login valid username or  mobile number
     */
    private void recoverPassword(final String type, final String login) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_RECOVER_PASSWORD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error
                    if (!error) {

                         Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                         viewPager.setCurrentItem(0);

                    } else {
                        showSnackBar(1, message);

                    }

                    // unhiding the login button
                    btnRecoverPassword.setVisibility(View.VISIBLE);
                    // hiding the progress bar
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    btnRecoverPassword.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
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

                btnRecoverPassword.setVisibility(View.VISIBLE);
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
                params.put("type", type);
                params.put("login", login);

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

                case R.id.input_login:
                    validateInput(inputLogin, inputLayoutLogin);
                    break;

                case R.id.input_password:
                    validatePassword(inputPassword, inputLayoutPassword);
                    break;

                case R.id.input_forgot_password:
                    validateMobile(inputForgotPassword,inputLayoutForgotPassword);
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
                    resId = R.id.layout_login;
                    break;

                case 1:
                    resId = R.id.layout_forgot_password;
                    break;

            }
            return findViewById(resId);
        }

    }



    //SnackBar function
    private void showSnackBar(final int id, String msg) {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.loginCoordinatorLayout);
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        if(id == 0){
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (id == 0) {
                        if (cd.isConnectingToInternet())
                            submitLogin();
                        else
                            showSnackBar(0, getString(R.string.err_no_internet));
                    } else {
                        if (cd.isConnectingToInternet())
                            submitRecoverPassword();
                        else
                            showSnackBar(1, getString(R.string.err_no_internet));
                    }
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
