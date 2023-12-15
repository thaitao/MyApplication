package com.example.employeecheckin;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.display.DisplayManager;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
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
import android.view.Display;
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

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    private static final String ACTION_USB_PERMISSION="com.jack.rfiddemo.USB_PERMISSION";

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1;
    //    private FragmentFirstBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    RFIDPnPacket mRFIDPacket;
    int[] myCardNoData = new int[32];
    private String lastId;
    private String tempId;
    private LocalTime time;

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

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        Display secondDisplay = null;
        for (int i = 0; i<displays.length; i++)
        {
            if (displays[i].getDisplayId() != Display.DEFAULT_DISPLAY)
            {
                secondDisplay = displays[i];
                break;
            }
        }

        if (secondDisplay != null)
        {
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchDisplayId(secondDisplay.getDisplayId());
            Intent intent = new Intent(this, MainActivity2.class);
            startActivity(intent, options.toBundle());
        }

//        setupRFID();

        Intent intent = new Intent(getApplicationContext(), TagReceiveService.class);
        startForegroundService(intent);
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

    private void setupRFID()
    {
        mRFIDPacket=new RFIDPnPacket((UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);

        new AsyncTask<Integer, Object, Integer>()
        {
            @Override
            protected void onPostExecute(Integer integer)
            {
                if (integer > 0)
                {
                    scan();
                }
            }

            @Override
            protected Integer doInBackground(Integer... integers)
            {
                return mRFIDPacket.PN_RF_Set(0);
            }
        }.execute(0);



    }

    private void scan()
    {
        new AsyncTask<Integer, Object, String>()
        {
            @Override
            protected void onPostExecute(String id)
            {
                if (id != null && !id.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Read Card successful " + lastId, Toast.LENGTH_SHORT).show();
                }
                scan();
            }

            @Override
            protected String doInBackground(Integer... integers)
            {
                Arrays.fill(myCardNoData,0);
                int irr = mRFIDPacket.PN_RF_M1_Search(myCardNoData,5);

                tempId = USBOp.convert2String(myCardNoData, irr);

                if (tempId == null || tempId.isEmpty() || !tempId.equals(lastId))
                {
                    lastId = tempId;
                    time = LocalTime.now();
                    return lastId;
                }
                else
                {
                    return null;
                }
            }
        }.execute(0);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}