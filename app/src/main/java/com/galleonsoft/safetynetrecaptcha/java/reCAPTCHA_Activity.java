package com.galleonsoft.safetynetrecaptcha.java;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.galleonsoft.safetynetrecaptcha.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class reCAPTCHA_Activity extends AppCompatActivity {

    private static final String TAG = "reCAPTCHA_Activity";

    // TODO: replace the reCAPTCHA KEY with yours
    private static final String SAFETY_NET_API_KEY = "6LdYZFoUAAAAAAXxuGh0OAnQ_8moTpkzhEtcqARB";

    // TODO: replace the SERVER API URL with yours
    private static final String VERIFY_ON_API_URL_SERVER = "http://apps.galleonsoft.com/api-example/safetynet-recaptcha-verfication.php";

    EditText et_message;
    LinearLayout linearLayoutForm;
    TextView Tv_Message;
    Button btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("Message");

        et_message = findViewById(R.id.et_message);
        linearLayoutForm = findViewById(R.id.linearLayoutForm);
        Tv_Message = findViewById(R.id.Tv_Message);
        btn_send = findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckSafetynetreCAPTCHA();
            }
        });

    }


    public void CheckSafetynetreCAPTCHA() {

        final String feedback = et_message.getText().toString().trim();
        // checking for empty text message
        if (TextUtils.isEmpty(feedback)) {
            Toast.makeText(getApplicationContext(), "Enter feedback!", Toast.LENGTH_LONG).show();
            return;
        }

        // Showing SafetyNet reCAPTCHA dialog
        SafetyNet.getClient(this).verifyWithRecaptcha(SAFETY_NET_API_KEY)
                .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        Log.d(TAG, "onSuccess");

                        if (!response.getTokenResult().isEmpty()) {

                            // Received reCaptcha token and This token still needs to be validated on
                            // the server using the SECRET key api.
                            new verifyTokenFromServer(response.getTokenResult(), feedback).execute();
                            Log.i(TAG, "onSuccess: " + response.getTokenResult());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.d(TAG, "SafetyNet Error: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                        } else {
                            Log.d(TAG, "Unknown SafetyNet error: " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * Verifying the captcha token on the server
     * Server makes call to https://www.google.com/recaptcha/api/siteverify
     * with SECRET Key and SafetyNet token.
     */
    @SuppressLint("StaticFieldLeak")
    private class verifyTokenFromServer extends AsyncTask<String, String, String> {

        String sToken;
        String msg;

        public verifyTokenFromServer(String token, String message) {
            this.sToken = token;
            this.msg = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {

            // object to hold the information, which is sent to the server
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("recaptcha-response", sToken);
            // Optional params you can use like this
            // hashMap.put("feedback-message", msg);

            // Send the recaptcha response token and receive a Result in return
            return ApiPostHelper.SendParams(VERIFY_ON_API_URL_SERVER, hashMap);
        }

        @Override
        protected void onPostExecute(String result) {

            if (result == null)
                return;

            Log.i("onPost::: ", result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                boolean success = jsonObject.getBoolean("success");
                String message = jsonObject.getString("message");

                if (success) {
                    // reCaptcha verified successfully.
                    linearLayoutForm.setVisibility(View.GONE);
                    Tv_Message.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("Error: ", e.getMessage());
            }

        }
    }

}
