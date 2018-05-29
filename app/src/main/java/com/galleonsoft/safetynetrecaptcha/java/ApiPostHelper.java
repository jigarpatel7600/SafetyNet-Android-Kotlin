package com.galleonsoft.safetynetrecaptcha.java;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Author: JIGAR PATEL.
 * Tutorial_URL: https://galleonsoft.com/tutorial/
 */
 
public class ApiPostHelper {


    // Send Parameters method
    public static String SendParams(String reqURL, HashMap<String, String> postDataParams) {

        URL gsUrl;
        StringBuilder response = new StringBuilder();
        String resultString = "";
        try {
            gsUrl = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) gsUrl.openConnection();
            conn.setReadTimeout(7000);
            conn.setConnectTimeout(7000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // For Post encoded Parameters
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.i("responseCode: ", responseCode + "");
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                // convert content stream to a String
                resultString = response.toString();
            } else {
                resultString = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultString;

    }


    // Collect Params from HashMap and encode with url.
    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
