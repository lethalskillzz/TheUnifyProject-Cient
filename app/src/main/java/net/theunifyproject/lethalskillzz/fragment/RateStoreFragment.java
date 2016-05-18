package net.theunifyproject.lethalskillzz.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.theunifyproject.lethalskillzz.R;

/**
 * Created by Ibrahim on 10/02/2016.
 */
public class RateStoreFragment extends Fragment {

    public RateStoreFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_rate_store, container, false);

        return rootView;

    }

}
