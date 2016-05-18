package net.theunifyproject.lethalskillzz.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.adapter.ListAdapter;
import net.theunifyproject.lethalskillzz.model.ListItem;

public class EditAccountActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private static String[] titles = null;
    private ListAdapter adapter;

    public static List<ListItem> getData() {
        List<ListItem> data = new ArrayList<>();
        // preparing navigation drawer items
        for (int i = 0; i < titles.length; i++) {
            ListItem item = new ListItem();
            item.setType(1);
            item.setTitle(titles[i]);
            data.add(item);
        }
        return data;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_account_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Labels
        titles = getResources().getStringArray(R.array.edit_account_labels);
        adapter = new ListAdapter(this,getData());
        LinearLayoutManager lLayout = new LinearLayoutManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.edit_account_recycler_view);
        recyclerView.setLayoutManager(lLayout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }


}
