package edu.duke.compsci290.ridermaster.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.duke.compsci290.ridermaster.R;

public class AccountInfoActivity extends AppCompatActivity {

    private TextView uidTextView;
    private TextView emailTextView;
    private ConstraintLayout signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        uidTextView = findViewById(R.id.userid_text_view);
        emailTextView = findViewById(R.id.useremail_text_view);
        signOutButton = findViewById(R.id.sign_out_button);

        //uidTextView.setText(uid);
        emailTextView.setText(email);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
                Log.d("tag", "tapped here");
                startActivity(intent3);
            }
        });




    }

}
