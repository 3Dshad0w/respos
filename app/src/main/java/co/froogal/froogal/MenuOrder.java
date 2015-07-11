package co.froogal.froogal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import co.froogal.froogal.fragment.MenuFinalOrderViewFragment;
import co.froogal.froogal.fragment.MenuOrderViewFragment;
import co.froogal.froogal.library.UserFunctions;
import co.froogal.froogal.slidingTab.SlidingTabLayout;
import co.froogal.froogal.util.basic_utils;
import co.froogal.froogal.view.ParallaxFragmentPagerAdapter;
import co.froogal.froogal.view.ParallaxViewPagerBaseActivity;

/**
 * Created by akhil on 26/6/15.
 */
public class MenuOrder extends ParallaxViewPagerBaseActivity {

    private ImageView mTopImage;
    private static ImageView proceed;
    private SlidingTabLayout mNavigBar;
    private TextView restaurantName;
    private static RelativeLayout bottom;
    private  static  TextView countTextView;

    public static JSONObject menuJson = null;
    public static String resID = "0";
    public  static  String userID;
    basic_utils bu = new basic_utils(this);
    ImageView closeDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_menu);

        resID = "1";
        Intent i = getIntent();
        resID = i.getStringExtra("res_ID");

        new ProcessMenu().execute();

        bottom = (RelativeLayout) findViewById(R.id.bottom);
        countTextView = (TextView) findViewById(R.id.itemCountTextView);
        countTextView.setText("0");
        proceed = (ImageView) findViewById(R.id.proceed);

        mTopImage = (ImageView) findViewById(R.id.userImage);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mNavigBar = (SlidingTabLayout) findViewById(R.id.navig_tab);
        closeDeal = (ImageView) findViewById(R.id.closeDeal);
        mHeader = findViewById(R.id.header);
        SpannableString s = new SpannableString("Restaurant Name");
        Typeface myfont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        s.setSpan(myfont, 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setTitle(s);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userID = bu.get_defaults("uid");

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ProcessOrder(userID, resID, MenuOrderViewFragment.selectedItems).execute();

            }
        });

        closeDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ProcessCloseOrder(userID, resID).execute();

            }
        });



        if (savedInstanceState != null) {
            mTopImage.setTranslationY(savedInstanceState.getFloat(IMAGE_TRANSLATION_Y));
            mHeader.setTranslationY(savedInstanceState.getFloat(HEADER_TRANSLATION_Y));
        }






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

    @Override
    protected void initValues() {
        int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
        mMinHeaderHeight = getResources().getDimensionPixelSize(R.dimen.min_header_height_order);
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height_order);
        mMinHeaderTranslation = -mMinHeaderHeight + tabHeight ;


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat(IMAGE_TRANSLATION_Y, mTopImage.getTranslationY());
        outState.putFloat(HEADER_TRANSLATION_Y, mHeader.getTranslationY());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {

        super.onStop();

    }

    @Override
    protected void setupAdapter() {
        if (mAdapter == null) {
            mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mNumFragments);
        }

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(mNumFragments);
        mNavigBar.setOnPageChangeListener(getViewPagerChangeListener());
        mNavigBar.setViewPager(mViewPager);

    }

    @Override
    protected void scrollHeader(int scrollY) {
        float translationY = Math.max(-scrollY, mMinHeaderTranslation);
        mHeader.setTranslationY(translationY);
        mTopImage.setTranslationY(-translationY / 3);
    }

    public static void updateBar(int count) {

        String value = countTextView.getText().toString();

        int val = Integer.parseInt(value) + count;
        if(val > 0){
            bottom.setVisibility(View.VISIBLE);
            countTextView.setText(String.valueOf(val));

        }
        else {
            countTextView.setText("0");
            bottom.setVisibility(View.INVISIBLE);
        }




    }


//    private int getActionBarHeight() {
//        if (mActionBarHeight != 0) {
//            return mActionBarHeight;
//        }
//
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
//            getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
//        } else {
//            getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);
//        }
//
//        mActionBarHeight = TypedValue.complexToDimensionPixelSize(
//                mTypedValue.data, getResources().getDisplayMetrics());
//
//        return mActionBarHeight;
//    }

    private static class ViewPagerAdapter extends ParallaxFragmentPagerAdapter {

        int numFragments;
        public ViewPagerAdapter(FragmentManager fm, int numFragments) {
            super(fm, numFragments+1);
            this.numFragments = numFragments;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            if(position ==  numFragments){
                return MenuFinalOrderViewFragment.newInstance(position);
            }
            fragment = MenuOrderViewFragment.newInstance(position);
            return fragment;
        }
        @Override
        public CharSequence getPageTitle(int position) {

            String title = null;
            if(position ==  numFragments) {
                title = "My Orders";
            }
            else {
                try {
                    int val = position-1;
                    title = menuJson.getJSONObject("'" + position + "'").getString("name");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return title;

        }
    }





    private class ProcessMenu extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String email, password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MenuOrder.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Getting Menu ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            Log.d("redmenu", "true");
            JSONObject json = userFunction.getMenu("1");

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {


            pDialog.dismiss();

            try {
                menuJson = json.getJSONObject("menu");
            } catch (JSONException e) {
                e.printStackTrace();
                menuJson = null;
            }


            mNumFragments = menuJson.length();
            initValues();
            setupAdapter();


        }

    }


    private class ProcessOrder extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String userID,resID;
        Map<String, String> products;

        public ProcessOrder(String userId, String resID, Map<String, String> products) {
            this.userID = userId;
            this.resID = resID;
            this.products = products;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            pDialog = new ProgressDialog(MenuOrder.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Processing Review ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json = userFunction.processOrder(userID, resID, products);

            Log.d("keysuccess", json.toString());

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mNumFragments);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setOffscreenPageLimit(mNumFragments+1);
            countTextView.setText("0");
            updateBar(0);
            pDialog.dismiss();


        }
    }


    private class ProcessCloseOrder extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String userID,resID;

        public ProcessCloseOrder(String userId, String resID) {
            this.userID = userId;
            this.resID = resID;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            pDialog = new ProgressDialog(MenuOrder.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Processing Order ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json = userFunction.processCloseOrder(userID, resID);

            Log.d("keysuccess", json.toString());

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mNumFragments);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setOffscreenPageLimit(mNumFragments);
            countTextView.setText("0");
            updateBar(0);

            pDialog.dismiss();


        }
    }


}