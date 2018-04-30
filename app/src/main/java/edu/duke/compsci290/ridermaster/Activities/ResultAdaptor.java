package edu.duke.compsci290.ridermaster.Activities;

/**
 * Created by zhengzhixu on 4/24/18.
 *
 * You have matched with:
 *
 * username (??)
 * email address (copy button ? redirect button ?- email)
 *
 * (confirm match button)
 *
 * username
 * email address
 *
 *
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResultAdaptor extends RecyclerView.Adapter<ResultAdaptor.ViewHolder> {
    String username;

    @NonNull
    @Override
    public ResultAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdaptor.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
