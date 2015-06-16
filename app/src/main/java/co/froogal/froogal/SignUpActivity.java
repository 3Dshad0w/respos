package co.froogal.froogal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import co.froogal.froogal.library.UserFunctions;
import co.froogal.froogal.services.location_service;
import co.froogal.froogal.util.basic_utils;
import co.froogal.froogal.view.FloatLabeledEditText;

/**
 * Created by akhil on 10/3/15.
 */
public class SignUpActivity extends ActionBarActivity {


    /**
     *  JSON Response node names.
     **/


    private static String KEY_SUCCESS = "success";
    private static String KEY_UID = "uid";
    private static String KEY_FIRSTNAME = "fname";
    private static String KEY_LASTNAME = "lname";
    private static String KEY_MOBILE = "mobile";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";
    private static String KEY_ERROR = "error";

    /**
     * Defining layout items.
     **/

    FloatLabeledEditText inputFirstName;
    FloatLabeledEditText inputLastName;
    FloatLabeledEditText inputMobile;
    FloatLabeledEditText inputEmail;
    FloatLabeledEditText inputPassword;
    TextView btnRegister;
    SharedPreferences sharedpreferences;
    String first_name="";
    String last_name="";
    String image_url="";
    String ip_address="";
    String imei="";
    String registered_at = "";
    String registered_through = "s";
    String latitude = "";
    String longitude = "";
    String gcm_token = "";
    basic_utils bu;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        SpannableString s = new SpannableString("SignUp");
        Typeface myfont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        s.setSpan(myfont, 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setTitle(s);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /**
         * Defining all layout items
         **/
        inputFirstName = (FloatLabeledEditText) findViewById(R.id.fname);
        inputLastName = (FloatLabeledEditText) findViewById(R.id.lname);
        inputMobile = (FloatLabeledEditText) findViewById(R.id.umobile);
        inputEmail = (FloatLabeledEditText) findViewById(R.id.email);
        inputPassword = (FloatLabeledEditText) findViewById(R.id.pword);
        btnRegister = (TextView) findViewById(R.id.register);



/**
 * Button which Switches back to the login screen on clicked
 **/



        /**
         * Register Button click event.
         * A Toast is set to alert when the fields are empty.
         * Another toast is set to alert Username must be 5 characters.
         **/

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                clearErrors();

                boolean cancel = false;
                View focusView = null;

                // Store values at the time of the login attempt.
                String mEmail = inputEmail.getText().toString();
                String mFirstName = inputFirstName.getText().toString();
                String mLastName = inputLastName.getText().toString();
                String mPassword = inputPassword.getText().toString();
                String mPhoneNumber = inputMobile.getText().toString();



                // Check for a valid password.
                if (TextUtils.isEmpty(mPassword)) {
                    inputPassword.setError(getString(R.string.error_field_required));
                    focusView = inputPassword;
                    cancel = true;
                } else if (mPassword.length() < 4) {
                    inputPassword.setError(getString(R.string.error_invalid_password));
                    focusView = inputPassword;
                    cancel = true;
                }

                // Check for a valid email address.
                if (TextUtils.isEmpty(mEmail)) {
                    inputEmail.setError(getString(R.string.error_field_required));
                    focusView = inputEmail;
                    cancel = true;
                } else if (!mEmail.contains("@") || !mEmail.contains(".")) {
                    inputEmail.setError(getString(R.string.error_invalid_email));
                    focusView = inputEmail;
                    cancel = true;
                }


                if (TextUtils.isEmpty(mFirstName)) {
                    inputFirstName.setError(getString(R.string.error_field_required));
                    focusView = inputFirstName;
                    cancel = true;
                }

                if (TextUtils.isEmpty(mLastName)) {
                    inputLastName.setError(getString(R.string.error_field_required));
                    focusView = inputLastName;
                    cancel = true;
                }

                if (TextUtils.isEmpty(mPhoneNumber)) {
                    inputMobile.setError(getString(R.string.error_field_required));
                    focusView = inputMobile;
                    cancel = true;
                }
                else if(mPhoneNumber.length() != 10) {
                    inputMobile.setError("Invalid PhoneNumber");
                    focusView = inputMobile;
                    cancel = true;
                }


                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    NetAsync(view);

                }

            }


        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        super.onResume();

    }


    /**
     * Async Task to check whether internet connection is working
     **/

    private class NetCheck extends AsyncTask<String,String,Boolean>
    {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(SignUpActivity.this);
            nDialog.setMessage("Loading..");
            nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args){


/**
 * Gets current device state and checks for working internet connection by trying Google.
 **/
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;

        }
        @Override
        protected void onPostExecute(Boolean th){

            if(th == true){
                nDialog.dismiss();
                new ProcessRegister().execute();
            }
            else{
                nDialog.dismiss();
                showAlertDialog(SignUpActivity.this, "No Internet Connection",
                        "You don't have internet connection.", false);
            }
        }
    }


    @SuppressWarnings("deprecation")
    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    private class ProcessRegister extends AsyncTask<String, String, JSONObject> {

        /**
         * Defining Process dialog
         **/
        private ProgressDialog pDialog;

        String email,password,fname,lname,mobile;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inputMobile = (FloatLabeledEditText) findViewById(R.id.umobile);
            inputPassword = (FloatLabeledEditText) findViewById(R.id.pword);
            fname = inputFirstName.getText().toString();
            lname = inputLastName.getText().toString();
            email = inputEmail.getText().toString();
            mobile = inputMobile.getText().toString();
            password = inputPassword.getText().toString();
            pDialog = new ProgressDialog(SignUpActivity.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Registering ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {


            UserFunctions userFunction = new UserFunctions();
            Log.d("upjson", fname+lname+email+mobile+password);
            registered_through  ="s";
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
            if(latitude != "" && longitude != "") {

                // To get city name
                try {
                    Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = gcd.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);
                    if (addresses.size() > 0) {
                        registered_at = addresses.get(0).getLocality();
                    }
                } catch (Exception e) {
                    Log.d("haha", "City Name Call Failed");
                }
            }
            ip_address = bu.get_defaults("ip_address");
            gcm_token = bu.get_defaults("gcm_token");
            image_url = "";
            JSONObject json = userFunction.registerUser(fname, lname, email, mobile, password, registered_at, registered_through, imei, ip_address, image_url, longitude, latitude, gcm_token);
            Log.d("json", json.toString());
            return json;


        }
        @Override
        protected void onPostExecute(JSONObject json) {
            /**
             * Checks for success message.
             **/
            try {
                if (json.getString(KEY_SUCCESS) != null) {
                    Log.d("jsonhereinsignup", json.toString());
                    String res = json.getString(KEY_SUCCESS);

                    String red = json.getString(KEY_ERROR);

                    if(Integer.parseInt(res) == 1){
                        pDialog.setTitle("Getting Data");
                        pDialog.setMessage("Loading Info");



                        basic_utils bf = new basic_utils(getApplicationContext());

                        bf.set_defaults("email", email);
                        bf.set_defaults("password", password);
                        bf.set_defaults("fname", fname);
                        bf.set_defaults("lname", lname);
                        bf.set_defaults("mobile", mobile);


                        Intent registered = new Intent(getApplicationContext(), MainActivity.class);

                        /**
                         * Close all views before launching Registered screen
                         **/
                        registered.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        pDialog.dismiss();
                        startActivity(registered);


                        finish();
                    }

                    else if (Integer.parseInt(red) ==2){
                        pDialog.dismiss();
                        showAlertDialog(SignUpActivity.this, "Error",
                                "User already exists.", false);
                        }
                    else if (Integer.parseInt(red) ==3){
                        pDialog.dismiss();
                        showAlertDialog(SignUpActivity.this, "Error",
                                "Invalid Email id.", false);

                    }

                    else {
                        pDialog.dismiss();
                        showAlertDialog(SignUpActivity.this, "Error",
                                "Error occured in registration.", false);

                    }
                }


                else{
                    pDialog.dismiss();
                    showAlertDialog(SignUpActivity.this, "Error",
                            "Error occured in registration.", false);

                }

            } catch (JSONException e) {
                e.printStackTrace();


            }
        }}
    public void NetAsync(View view){
        new NetCheck().execute();
    }

    private void clearErrors(){
       inputEmail.setError(null);
       inputMobile.setError(null);
       inputFirstName.setError(null);
       inputLastName.setError(null);
       inputPassword.setError(null);

    }

}
