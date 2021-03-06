package edu.duke.compsci290.ridermaster.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.duke.compsci290.ridermaster.R;

public class MainActivity extends AppCompatActivity {
    private Button mLogInButton;
    private Button mSignUpButton;

    private LoginButton mLogInWithFacebookButton;

    private EditText mEmailField;
    private EditText mPasswordField;

    private TextView mForgotPasswordText;

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    private final String TAG = "Sign in activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: huhhb");

        mLogInButton = this.findViewById(R.id.log_in_button);
        mSignUpButton = this.findViewById(R.id.sign_up_button);
        mLogInWithFacebookButton = this.findViewById(R.id.log_in_with_facebook_button);
        mEmailField = this.findViewById(R.id.email_field_edit_text);
        mPasswordField = this.findViewById(R.id.password_field_edit_text);
        mForgotPasswordText = this.findViewById(R.id.forgot_password_text_view);

        mAuth = FirebaseAuth.getInstance();

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createAccount(
                        mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
        mForgotPasswordText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = mEmailField.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please input a valid email address.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this,
                                            "A reset password email is sent",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this,
                                            "Failed to send reset password email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Initializes Facebook Login button.
        mCallbackManager = CallbackManager.Factory.create();
        mLogInWithFacebookButton.setReadPermissions("email", "public_profile");
        mLogInWithFacebookButton.registerCallback(
                mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Checks if user is signed in (non-null) and updates UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Passes the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void createAccount(String email, String password) {
        if (!validateForm(email, password)) {
            Toast.makeText(MainActivity.this, "Please input a valid email address and password.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Creates account succeeded, updates UI with the signed-in user's
                            // information.
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If create account fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm(email, password)) {
            Toast.makeText(MainActivity.this, "Please input a valid email address and password.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in succeeded, update UI with the signed-in user's information.
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in succeeded, update UI with the signed-in user's information.
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    // Verifies email and password format.
    private boolean validateForm(String email, String password) {
        if (email.isEmpty() || !email.contains("@")) {
            return false;
        }
        // TODO: validates password with some more criteria.
        if (password.isEmpty()) {
            return false;
        }
        return true;
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            // TODO: updates UI if no user is signed in.
        } else {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            getApplicationContext().startActivity(intent);
        }
    }
}
