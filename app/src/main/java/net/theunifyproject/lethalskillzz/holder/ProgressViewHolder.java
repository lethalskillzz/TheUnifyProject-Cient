package net.theunifyproject.lethalskillzz.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import net.theunifyproject.lethalskillzz.R;

/**
 * Created by Ibrahim on 19/11/2015.
 */
public class ProgressViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;

    public ProgressViewHolder(View v) {
        super(v);
        progressBar = (ProgressBar) v.findViewById(R.id.footer_progress_bar);
    }
}
