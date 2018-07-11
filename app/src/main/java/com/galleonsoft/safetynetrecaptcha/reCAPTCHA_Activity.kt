package com.galleonsoft.safetynetrecaptcha

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class reCAPTCHA_Activity : AppCompatActivity() {

    lateinit var et_message: EditText
    lateinit var linearLayoutForm: LinearLayout
    lateinit var Tv_Message: TextView
    lateinit var btn_send: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar = supportActionBar
        if (actionBar != null)
            actionBar.title = "reCAPTCHA"

        et_message = findViewById(R.id.et_message)
        linearLayoutForm = findViewById(R.id.linearLayoutForm)
        Tv_Message = findViewById(R.id.Tv_Message)
        btn_send = findViewById(R.id.btn_send)

        btn_send.setOnClickListener { CheckSafetynetreCAPTCHA() }

    }


    fun CheckSafetynetreCAPTCHA() {

        val feedback = et_message.text.toString().trim()
        // checking for empty text message
        if (TextUtils.isEmpty(feedback)) {
            Toast.makeText(applicationContext, "Enter feedback!", Toast.LENGTH_LONG).show()
            return
        }

        // Showing SafetyNet reCAPTCHA dialog
        SafetyNet.getClient(this).verifyWithRecaptcha(SAFETY_NET_API_KEY)
                .addOnSuccessListener(this) { response ->
                    Log.d(TAG, "onSuccess")

                    if (!response.tokenResult.isEmpty()) {

                        // Received reCaptcha token and This token still needs to be validated on
                        // the server using the SECRET key api.
                        verifyTokenFromServer(response.tokenResult, feedback).execute()
                        Log.i(TAG, "onSuccess: " + response.tokenResult)
                    }
                }
                .addOnFailureListener(this) { e ->
                    if (e is ApiException) {
                        Log.d(TAG, "SafetyNet Error: " + CommonStatusCodes.getStatusCodeString(e.statusCode))
                    } else {
                        Log.d(TAG, "Unknown SafetyNet error: " + e.message)
                    }
                }
    }

    /**
     * Verifying the captcha token on the server
     * Server makes call to https://www.google.com/recaptcha/api/siteverify
     * with SECRET Key and SafetyNet token.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class verifyTokenFromServer(internal var sToken: String, internal var msg: String) : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg args: String): String {

            // object to hold the information, which is sent to the server
            val hashMap = HashMap<String, String>()
            hashMap["recaptcha-response"] = sToken
            // Optional params you can use like this
            // hashMap.put("feedback-message", msg)

            // Send the recaptcha response token and receive a Result in return
            return ApiPostHelper.SendParams(VERIFY_ON_API_URL_SERVER, hashMap)
        }

        override fun onPostExecute(result: String?) {

            if (result == null)
                return

            Log.i("onPost::: ", result)
            try {
                val jsonObject = JSONObject(result)
                val success = jsonObject.getBoolean("success")
                val message = jsonObject.getString("message")

                if (success) {
                    // reCaptcha verified successfully.
                    linearLayoutForm.visibility = View.GONE
                    Tv_Message.visibility = View.VISIBLE
                } else {
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.i("Error: ", e.message)
            }

        }
    }

    companion object {
        private val TAG = "reCAPTCHA_Activity"

        // TODO: replace the reCAPTCHA KEY with yours
        private val SAFETY_NET_API_KEY = "6Le7bloUAAAAALdVrWl5RUynjZoJAF3ZllhlM5Kc"

        // TODO: replace the SERVER API URL with yours
        private val VERIFY_ON_API_URL_SERVER = "http://apps.galleonsoft.com/api-example/safetynet-recaptcha-verfication.php"
    }

}
