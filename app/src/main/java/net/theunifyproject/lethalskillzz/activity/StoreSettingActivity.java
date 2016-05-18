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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import net.theunifyproject.lethalskillzz.util.Logout;

import mp.MpUtils;
import mp.PaymentRequest;
import mp.PaymentResponse;

public class StoreSettingActivity extends AppCompatActivity implements View.OnClickListener {

    //logcat tag
    private static String TAG = StoreSettingActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private PrefManager pref;
    //Connection detector class
    private ConnectionDetector cd;
    private Logout mLogout;

    private EditText inputName, inputDesc;
    private TextInputLayout inputLayoutName, inputLayoutDesc;
    private Button btn_submit;
    private CheckBox activate;
    private ProgressBar progressBar;

    private boolean isReady = false;

    private static String SERVICE_ID = "...";
    private static String APP_SECRET = "...";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.store_setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        pref = new PrefManager(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        inputName = (EditText) findViewById(R.id.store_setting_input_name);
        inputDesc = (EditText) findViewById(R.id.store_setting_input_description);
        inputLayoutName = (TextInputLayout) findViewById(R.id.store_setting_input_layout_name);
        inputLayoutDesc = (TextInputLayout) findViewById(R.id.store_setting_input_layout_description);
        activate = (CheckBox) findViewById(R.id.store_setting_activate);
        btn_submit = (Button) findViewById(R.id.store_setting_button);
        progressBar = (ProgressBar) findViewById(R.id.store_setting_progressBar);

        btn_submit.setOnClickListener(this);

        if (cd.isConnectingToInternet()) {

            progressBar.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.GONE);
            loadStoreSetting();
        } else
            showSnackBar(0,getString(R.string.err_no_internet));


        requestFocus(inputName);

        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case AppConfig.httpHandlerUpdateStore: {

                        progressBar.setVisibility(View.GONE);
                        btn_submit.setVisibility(View.VISIBLE);

                        if (((String) msg.obj).trim().equals("success"))
                            finish();
                        else if (((String) msg.obj).trim().equals("fail"))
                            showSnackBar(1,"Error while posting shop!");
                        else
                            showSnackBar(1,getString(R.string.err_network_timeout));
                    }
                    break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        if(isReady)
        switch (view.getId()) {

            case R.id.store_setting_button: {

                PaymentRequest.PaymentRequestBuilder builder = new PaymentRequest.PaymentRequestBuilder();
                builder.setService(SERVICE_ID, APP_SECRET);
                builder.setDisplayString("StoreFee");
                builder.setProductName("StoreFee");  // non-consumable purchases are restored using this value
                builder.setType(MpUtils.PRODUCT_TYPE_SUBSCRIPTION);              // non-consumable items can be later restored
                builder.setIcon(R.mipmap.ic_launcher);
                PaymentRequest pr = builder.build();
                makePayment(pr);
            }
            break;
        }
    }


    private void setupSettings(String name, String description, boolean isActive) {

        if(name.length()!=0) {
            inputName.setText(name);
        }

        if(description.length()!=0) {
            inputDesc.setText(description);
        }

        if(isActive)
            activate.setChecked(true);
        else
            activate.setChecked(false);

        isReady = true;
    }


    private void loadStoreSetting() {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_STORE_SETTING, new Response.Listener<String>() {

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
                        JSONArray settingArray = dataObj.getJSONArray("setting");

                        JSONObject settingObj = (JSONObject) settingArray.get(0);

                        String name = settingObj.getString("name");
                        String description = settingObj.getString("description");
                        boolean isActive = settingObj.getBoolean("isActive");

                        setupSettings(name, description, isActive);

                    } else {

                    }

                    progressBar.setVisibility(View.GONE);
                    btn_submit.setVisibility(View.VISIBLE);


                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    progressBar.setVisibility(View.GONE);
                    btn_submit.setVisibility(View.VISIBLE);
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

                progressBar.setVisibility(View.GONE);
                btn_submit.setVisibility(View.VISIBLE);

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


    // Fortumo related glue-code
    private static final int REQUEST_CODE = 1234; // Can be anything

    protected final void makePayment(PaymentRequest payment) {
        startActivityForResult(payment.toIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if(data == null) {
                return;
            }

            // OK
            if (resultCode == RESULT_OK) {
                PaymentResponse response = new PaymentResponse(data);

                switch (response.getBillingStatus()) {
                    case MpUtils.MESSAGE_STATUS_BILLED:
                        submit();
                        break;
                    case MpUtils.MESSAGE_STATUS_FAILED:
                        // ...
                        break;
                    case MpUtils.MESSAGE_STATUS_PENDING:
                        // ...
                        break;
                }
                // Cancel
            } else {
                // ..
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    private void submit() {

        if (!validateInput(inputName, inputLayoutName)) {
            return;
        }

        if (!validateInput(inputDesc, inputLayoutDesc)) {
            return;
        }

        String name = inputName.getText().toString().trim();
        String description = inputDesc.getText().toString().trim();
        boolean isActive = false;
        if (activate.isChecked())
            isActive = true;

        if (cd.isConnectingToInternet()) {

            progressBar.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.GONE);

            Intent intent = new Intent(this, HttpService.class);
            intent.putExtra("intent_type", AppConfig.httpIntentUpdateStore);
            intent.putExtra("name", name);
            intent.putExtra("description", description);
            intent.putExtra("isActive", isActive);
            startService(intent);
        } else
            showSnackBar(1,getString(R.string.err_no_internet));

    }


    private boolean validateInput(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_input));
            requestFocus(EdTxt);
            return false;
        } else {
            inputLayout.setErrorEnabled(false);
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

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.edit_shop_CoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(id == 0){
                    if (cd.isConnectingToInternet()) {
                        loadStoreSetting();
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
