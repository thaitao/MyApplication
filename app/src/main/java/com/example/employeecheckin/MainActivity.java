package com.example.employeecheckin;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.example.employeecheckin.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1;
    //    private FragmentFirstBinding binding;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_first);

        SignInButton signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                signIn();

            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("831312052137-4fdnch4vk7irqi8ibvru22gmq9tmscr1.apps.googleusercontent.com")
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
        GetSignInIntentRequest request =
                GetSignInIntentRequest.builder()
                        .setServerClientId("831312052137-4fdnch4vk7irqi8ibvru22gmq9tmscr1.apps.googleusercontent.com")
                        .build();

        Identity.getSignInClient(this)
                .getSignInIntent(request)
                .addOnSuccessListener(
                        result -> {
                            try {
                                startIntentSenderForResult(
                                        result.getIntentSender(),
                                        REQUEST_CODE_GOOGLE_SIGN_IN,
                                        /* fillInIntent= */ null,
                                        /* flagsMask= */ 0,
                                        /* flagsValue= */ 0,
                                        /* extraFlags= */ 0,
                                        /* options= */ null);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e("TAG", "Google Sign-in failed");
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Log.e("TAG", "Google Sign-in failed", e);
                            updateUI(e);
                        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
//        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
//            // The Task returned from this call is always completed, no need to attach
//            // a listener.
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            handleSignInResult(task);
//        }

        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
                try {
                    SignInCredential credential = Identity.getSignInClient(this).getSignInCredentialFromIntent(data);
                    // Signed in successfully - show authenticated UI
                    updateUI(credential);
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                }
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
            updateUI(e);
        }
    }

    private void updateUI(ApiException error)
    {
        TextView textView = findViewById(R.id.textview_first);
        textView.setText("signInResult:failed code=" + error.getStatusCode());
        Toast.makeText(this, "signInResult:failed code=" + error.getStatusCode(), Toast.LENGTH_SHORT).show();
    }

    private void updateUI(Exception error)
    {
        TextView textView = findViewById(R.id.textview_first);
        textView.setText("signInResult:failed code=" + error);
        Toast.makeText(this, "signInResult:failed code=" + error, Toast.LENGTH_SHORT).show();
    }

    private void updateUI(GoogleSignInAccount account)
    {
        if (account == null)
        {
            Log.w("TAG", "google account is null");
        }
        else
        {
            TextView textView = findViewById(R.id.textview_first);
            textView.setText(account.getEmail());
            Toast.makeText(this, account.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(SignInCredential account)
    {
        if (account == null)
        {
            Log.w("TAG", "google account is null");
        }
        else
        {
            TextView textView = findViewById(R.id.textview_first);
            textView.setText(account.getId());
            Toast.makeText(this, account.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}