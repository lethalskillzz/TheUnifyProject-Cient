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
import android.widget.Spinner;
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

public class RegisterProfileActivity extends AppCompatActivity implements View.OnClickListener {

    //logcat tag
    private static String TAG = RegisterProfileActivity.class.getSimpleName();

    //Connection detector class
    private ConnectionDetector cd;

    //view & widget declarations
    private ViewPager viewPager;
    private ViewPagerAdapter viewAdapter;
    private Button btnRegister, btnNext, btnLinkToLogin, btnLinkToLogin2;
    private EditText reg_inputName;
    private Spinner locSpinner, courseSpinner, levelSpinner;
    private ProgressBar progressBar;
    private TextInputLayout inputLayoutRegName;
    private PrefManager pref;
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        pref = new PrefManager(this);

        viewPager = (ViewPager) findViewById(R.id.viewPagerVertical);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        inputLayoutRegName = (TextInputLayout) findViewById(R.id.input_layout_reg_name);

        reg_inputName = (EditText) findViewById(R.id.input_reg_name);


        //Spinners

        locSpinner = (Spinner) findViewById(R.id.location_spinner);
        //locSpinner.setOnItemChosenListener(this);

        courseSpinner = (Spinner) findViewById(R.id.course_spinner);
        //deptSpinner.setOnItemChosenListener(this);

        levelSpinner = (Spinner) findViewById(R.id.level_spinner);
        //levelSpinner.setOnItemChosenListener(this);

        progressBar = (ProgressBar) findViewById(R.id.registerProgressBar);


        reg_inputName.addTextChangedListener(new MyTextWatcher(reg_inputName));

        btnRegister = (Button) findViewById(R.id.btn_Register);
        btnNext = (Button) findViewById(R.id.btn_Next);

        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        btnLinkToLogin2 = (Button) findViewById(R.id.btnLinkToLoginScreen2);



        // view click listeners
        btnRegister.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        btnLinkToLogin.setOnClickListener(this);
        btnLinkToLogin2.setOnClickListener(this);

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

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.registerCoordinatorLayout);
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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_Register:
                submitRegister();
                break;

            case R.id.btnLinkToLoginScreen:
                gotoLogin();
                break;

            case R.id.btnLinkToLoginScreen2:
                gotoLogin();
                break;


            case R.id.btn_Next:
                next();
                break;
        }
    }





    private void next() {

        viewPager.setCurrentItem(1);
    }

    private void gotoLogin() {
        Intent intent = new Intent(RegisterProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    private void submitRegister() {

        if (!validateName(reg_inputName,inputLayoutRegName))
        {
            return;
        }
        //checking network connectivity
        if (cd.isConnectingToInternet()) {
            String name = reg_inputName.getText().toString().trim();
            String location = locSpinner.getSelectedItem().toString().trim();
            String department = courseSpinner.getSelectedItem().toString().trim();
            String level = levelSpinner.getSelectedItem().toString().trim();

            // hiding the register button
            btnRegister.setVisibility(View.INVISIBLE);
            // showing the progress
            progressBar.setVisibility(View.VISIBLE);

            // request for sms
            registerProfileDetail(name, location, department, level);
        }
        else
            showSnackBar(0, getString(R.string.err_no_internet));


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
     * Method submits and store user details to the server
     *
     *@param name user name
     *@param location user valid location
     *@param department user school department
     *@param level user level
     */
    private void registerProfileDetail( final String name, final String location, final String department, final String level) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER_PROFILE_DETAIL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                //Toast.makeText(getApplicationContext(),  response, Toast.LENGTH_SHORT).show();


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
                        String username = profileObj.getString("username");

                        // saving in shared preferences
                        pref.setRegStage(AppConfig.REG_STAGE_ONE);
                        pref.setSessionId(sessionId);
                        
                        Intent intent = new Intent(RegisterProfileActivity.this, RegisterAccountActivity.class);
                        intent.putExtra("username", username);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        finish();


                    } else {

                        showSnackBar(1, message);
                    }

                    // unhiding the register button
                    btnRegister.setVisibility(View.VISIBLE);
                    // hiding the progress bar
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    btnRegister.setVisibility(View.VISIBLE);
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

                btnRegister.setVisibility(View.VISIBLE);
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
                params.put("name", name);
                params.put("location", location);
                params.put("course", department);
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

                case R.id.input_reg_name:
                    validateName(reg_inputName,inputLayoutRegName);
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
                    resId = R.id.layout_registration;
                    break;

                case 1:
                    resId = R.id.layout_registration2;
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
                            if(cd.isConnectingToInternet()) submitRegister();
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
