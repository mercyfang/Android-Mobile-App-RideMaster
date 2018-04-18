package edu.duke.compsci290.ridermaster.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import edu.duke.compsci290.ridermaster.R;

public class MatchResultActivity extends BaseNavDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets activity main view.
        FrameLayout activityContainer = findViewById(R.id.activity_content);
        View.inflate(this, R.layout.activity_match_result, activityContainer);
    }
}
