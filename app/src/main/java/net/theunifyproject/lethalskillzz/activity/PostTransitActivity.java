package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.service.HttpService;

public class PostTransitActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static Handler mUiHandler;
    private ConnectionDetector cd;

    private Spinner spinner_bosso_means,spinner_bosso_time, spinner_gidan_kwano_means, spinner_gidan_kwano_time;
    private Button btn_submit;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_transit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.post_transit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        spinner_bosso_means = (Spinner) findViewById(R.id.transit_bosso_means);
        spinner_bosso_time = (Spinner) findViewById(R.id.transit_bosso_time);
        spinner_gidan_kwano_means = (Spinner) findViewById(R.id.transit_gidan_kwano_means);
        spinner_gidan_kwano_time = (Spinner) findViewById(R.id.transit_gidan_kwano_time);
        btn_submit = (Button) findViewById(R.id.post_transit_button);
        progressBar = (ProgressBar) findViewById(R.id.post_transit_progressBar);

        btn_submit.setOnClickListener(this);
        spinner_bosso_means.setOnItemSelectedListener(this);
        spinner_bosso_time.setOnItemSelectedListener(this);
        spinner_gidan_kwano_means.setOnItemSelectedListener(this);
        spinner_gidan_kwano_time.setOnItemSelectedListener(this);

        spinner_bosso_means.setSelection(1);
        spinner_bosso_time.setSelection(1);
        spinner_gidan_kwano_means.setSelection(1);
        spinner_gidan_kwano_time.setSelection(1);


        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case AppConfig.httpHandlerPostTransit: {

                        progressBar.setVisibility(View.GONE);
                        btn_submit.setVisibility(View.VISIBLE);

                        if (((String) msg.obj).trim().equals("success"))
                            finish();
                        else if (((String) msg.obj).trim().equals("fail"))
                            showSnackBar("Error while posting transit!");
                        else
                            showSnackBar(getString(R.string.err_network_timeout));
                    }
                    break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.post_transit_button:
                submit();
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        if(parent.getId() == R.id.transit_bosso_means) {
            if(parent.getItemAtPosition(position).toString().equals("None"))
                spinner_bosso_time.setSelection(0);
            else {
                if (spinner_bosso_time.getSelectedItem().toString().equals("None"))
                    spinner_bosso_time.setSelection(1);
            }

        } else if(parent.getId() == R.id.transit_bosso_time) {
            if(parent.getItemAtPosition(position).toString().equals("None"))
                spinner_bosso_means.setSelection(0);
            else {
                if (spinner_bosso_means.getSelectedItem().toString().equals("None"))
                    spinner_bosso_means.setSelection(1);
            }

        } else if(parent.getId() == R.id.transit_gidan_kwano_means) {
            if(parent.getItemAtPosition(position).toString().equals("None"))
                spinner_gidan_kwano_time.setSelection(0);
            else {
                if (spinner_gidan_kwano_time.getSelectedItem().toString().equals("None"))
                    spinner_gidan_kwano_time.setSelection(1);
            }

        } else if(parent.getId() == R.id.transit_gidan_kwano_time) {
            if(parent.getItemAtPosition(position).toString().equals("None"))
                spinner_gidan_kwano_means.setSelection(0);
            else {
                if (spinner_gidan_kwano_means.getSelectedItem().toString().equals("None"))
                    spinner_gidan_kwano_means.setSelection(1);
            }
        }

    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    private void submit() {


        String bosso_means = spinner_bosso_means.getSelectedItem().toString().trim();
        String bosso_time = spinner_bosso_time.getSelectedItem().toString().trim();
        String gidan_kwano_means = spinner_gidan_kwano_means.getSelectedItem().toString().trim();
        String gidan_kwano_time = spinner_gidan_kwano_time.getSelectedItem().toString().trim();


        if (cd.isConnectingToInternet()) {

            progressBar.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.GONE);

            Intent intent = new Intent(this, HttpService.class);
            intent.putExtra("intent_type", AppConfig.httpIntentPostTransit);
            intent.putExtra("bosso_means", bosso_means);
            intent.putExtra("bosso_time", bosso_time);
            intent.putExtra("gidan_kwano_means", gidan_kwano_means);
            intent.putExtra("gidan_kwano_time", gidan_kwano_time);
            startService(intent);
        } else showSnackBar(getString(R.string.err_no_internet));

    }

    //SnackBar function
    private void showSnackBar(String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.post_transit_CoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

    }

}
