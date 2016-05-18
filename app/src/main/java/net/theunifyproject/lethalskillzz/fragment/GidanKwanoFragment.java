package net.theunifyproject.lethalskillzz.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
import net.theunifyproject.lethalskillzz.activity.TaxiActivity;
import net.theunifyproject.lethalskillzz.activity.TransitActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.util.Logout;

/**
 * Created by Ibrahim on 28/01/2016.
 */
public class GidanKwanoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = TransitActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private PrefManager pref;
    private ConnectionDetector cd;
    private Logout mLogout;

    protected BarChart mChart;
    private ProgressBar progressBar;
    private TextView taxiCount;
    private Typeface mTf;
    private static String campus = "Gidan Kwano";
    private static int rows = 12;

    protected String[] mTime = new String[] {
            "06-07", "07-08", "08-09", "09-10", "10-11", "11-12", "12-01", "01-02", "02-03", "03-04", "04-05", "05-06"
    };

    public GidanKwanoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gidan_kwano, container, false);

        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity());
        mLogout = new Logout(getActivity());
        pref = new PrefManager(getActivity());

        progressBar = (ProgressBar) rootView.findViewById(R.id.fragment_gidan_kwano_progressBar);
        taxiCount =  (TextView) rootView.findViewById(R.id.gidan_kwano_taxi_count_btn);
        taxiCount.setOnClickListener(this);
        mChart = (BarChart) rootView.findViewById(R.id.gidan_kwano_chart);
        //mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("Gidan Kwano bus park");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        mChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });

        mChart.getAxisRight().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });

        mTf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        //YAxisValueFormatter custom = new MyYAxisValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTf);

        //leftAxis.setLabelCount(8, false);
        //leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(mTf);
        //rightAxis.setLabelCount(8, false);
        //rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        //l.setEnabled(false);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

        if (cd.isConnectingToInternet()) {
            progressBar.setVisibility(View.VISIBLE);
            loadTransit(campus);
        } else {
            showSnackBar(0, getString(R.string.err_no_internet));
        }

        //mChart.setDrawLegend(false);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.gidan_kwano_taxi_count_btn: {
                Intent intent = new Intent(getActivity(), TaxiActivity.class);
                intent.putExtra("campus", campus);
                startActivity(intent);
            }
            break;
        }
    }

    private void loadTransit(final String campus) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_BUS_TRANSIT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response);

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if (!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error
                    if (!error) {

                        // parsing the feed data
                        JSONObject dataObj = responseObj.getJSONObject("data");
                        setData(dataObj, rows);

                    } else {

                    }

                    mChart.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    mChart.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
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
                mChart.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
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
                params.put("campus", campus);

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


    private void setData(JSONObject dataObj, int count) {

        try {

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < count; i++) {
                xVals.add(mTime[i % 12]);
            }

            JSONArray transitArray = dataObj.getJSONArray("transit");
            JSONObject transitObj = (JSONObject) transitArray.get(0);

            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

            BarEntry v1e1 = new BarEntry(transitObj.getInt("06:00 - 07:00"), 0); // 06 - 07
            yVals1.add(v1e1);
            BarEntry v1e2 = new BarEntry(transitObj.getInt("07:00 - 08:00"), 1); // 07 - 08
            yVals1.add(v1e2);
            BarEntry v1e3 = new BarEntry(transitObj.getInt("08:00 - 09:00"), 2); // 08 - 09
            yVals1.add(v1e3);
            BarEntry v1e4 = new BarEntry(transitObj.getInt("09:00 - 10:00"), 3); // 09 - 10
            yVals1.add(v1e4);
            BarEntry v1e5 = new BarEntry(transitObj.getInt("10:00 - 11:00"), 4); // 10 - 11
            yVals1.add(v1e5);
            BarEntry v1e6 = new BarEntry(transitObj.getInt("11:00 - 12:00"), 5); // 11 - 12
            yVals1.add(v1e6);
            BarEntry v1e7 = new BarEntry(transitObj.getInt("12:00 - 01:00"), 6); // 12 - 01
            yVals1.add(v1e7);
            BarEntry v1e8 = new BarEntry(transitObj.getInt("01:00 - 02:00"), 7); // 01 - 02
            yVals1.add(v1e8);
            BarEntry v1e9 = new BarEntry(transitObj.getInt("02:00 - 03:00"), 8); // 02 - 03
            yVals1.add(v1e9);
            BarEntry v1e10 = new BarEntry(transitObj.getInt("03:00 - 04:00"), 9); // 03 - 04
            yVals1.add(v1e10);
            BarEntry v1e11 = new BarEntry(transitObj.getInt("04:00 - 05:00"), 10); // 04 - 05
            yVals1.add(v1e11);
            BarEntry v1e12 = new BarEntry(transitObj.getInt("05:00 - 06:00"), 11); // 05 - 06
            yVals1.add(v1e12);

            BarDataSet set1 = new BarDataSet(yVals1, "Passengers");
            set1.setColors(ColorTemplate.COLORFUL_COLORS);
            set1.setBarSpacePercent(35);
            set1.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) Math.floor(value));
                }
            });

            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10);
            data.setValueTypeface(mTf);

            mChart.setData(data);
            mChart.animateXY(2000, 2000);

            String append;
            if(transitObj.getInt("taxi_count")==1)
                append = " person is going by taxi";
            else
                append = " people are going by taxi";

            taxiCount.setText(transitObj.getString("taxi_count") + append);
            taxiCount.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.mainCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cd.isConnectingToInternet()) {
                            progressBar.setVisibility(View.VISIBLE);
                            loadTransit(campus);
                        } else {
                            showSnackBar(0, getString(R.string.err_no_internet));
                        }
                    }
                }

        );

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

}