package co.circe.respos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import co.circe.respos.fragment.AboutScrollViewFragment;
import co.circe.respos.fragment.MenuListViewFragment;
import co.circe.respos.fragment.OffersFragment;
import co.circe.respos.library.UserFunctions;
import co.circe.respos.slidingTab.SlidingTabLayout;
import co.circe.respos.util.basic_utils;
import co.circe.respos.view.ParallaxFragmentPagerAdapter;
import co.circe.respos.view.ParallaxViewPagerBaseActivity;
import co.circe.respos.fragment.ReviewFragment;


public class ResDetailsActivity extends ParallaxViewPagerBaseActivity {

    private ImageView mTopImage;
    private SlidingTabLayout mNavigBar;

    private Drawable mActionBarBackgroundDrawable;
    private ImageView favourite;
    private ImageView call;
    private ImageView directions;
    private ProgressDialog normal_dialog;
    public static String resID = "0";
    public static String isFav = "0";
    public static String res_latitude;
    private String status="open";
    private UserFunctions uf;
    private basic_utils bu;
    public static String res_longitude;
    private JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resdetails);
        mActionBarBackgroundDrawable = getResources().getDrawable(R.color.main_color_500);
        initValues();

        // Getting extras
        Intent intent = getIntent();
        resID = intent.getStringExtra("res_ID");
        res_latitude = intent.getStringExtra("res_latitude");
        res_longitude = intent.getStringExtra("res_longitude");
        isFav = intent.getStringExtra("isFav");
        Log.d("isFav", isFav);
        uf = new UserFunctions();
        bu = new basic_utils(this);
        Button checkin = (Button) findViewById(R.id.checkInButton);
        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(ResDetailsActivity.this, scanner_activity.class);
                i.putExtra("res_latitude", res_latitude);
                i.putExtra("res_longitude", res_longitude);
                i.putExtra("res_ID", resID);
                startActivity(i);
                finish();

            }
        });
        bu = new basic_utils(this);
        mTopImage = (ImageView) findViewById(R.id.userImage);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mNavigBar = (SlidingTabLayout) findViewById(R.id.navig_tab);
        mHeader = findViewById(R.id.header);
        favourite = (ImageView) findViewById(R.id.favourite);
        call = (ImageView) findViewById(R.id.call);
        directions = (ImageView) findViewById(R.id.directions);
        if (isFav.equals("1")) {
            favourite.setBackground(getResources().getDrawable(R.drawable.round_border_main_selected));
            favourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_24dp));
        }
        else{
            favourite.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));
            favourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        }
        SpannableString s = new SpannableString("Restaurant Name");
        Typeface myfont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        s.setSpan(myfont, 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setTitle(s);
        mActionBarBackgroundDrawable.setAlpha(0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mActionBarBackgroundDrawable.setCallback(mDrawableCallback);
        } else {


            getSupportActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState != null) {
            mTopImage.setTranslationY(savedInstanceState.getFloat(IMAGE_TRANSLATION_Y));
            mHeader.setTranslationY(savedInstanceState.getFloat(HEADER_TRANSLATION_Y));
        }

        setupAdapter();

    }

    private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getSupportActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
        }
    };

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
        mMinHeaderHeight = getResources().getDimensionPixelSize(R.dimen.min_header_height);
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mMinHeaderTranslation = -mMinHeaderHeight + tabHeight + tabHeight + tabHeight;

        mNumFragments = 4;
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
        final int headerHeight = mHeader.getHeight() - getSupportActionBar().getHeight();
        final float ratio = (float) Math.min(Math.max(scrollY, 0), headerHeight) / headerHeight;
        final int newAlpha = (int) (ratio * 255);
        mActionBarBackgroundDrawable.setAlpha(newAlpha);
    }

    public void favourite(View view) {
        new ProcessFav(isFav, bu.get_defaults("uid"), resID).execute();

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

        public ViewPagerAdapter(FragmentManager fm, int numFragments) {
            super(fm, numFragments);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = OffersFragment.newInstance(0);
                    break;

                case 1:
                    fragment = AboutScrollViewFragment.newInstance(1);
                    break;

                case 2:
                    fragment = ReviewFragment.newInstance(2);
                    break;

                case 3:
                    fragment = MenuListViewFragment.newInstance(3);
                    break;

                default:
                    throw new IllegalArgumentException("Wrong page given " + position);
            }
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Offers";

                case 1:
                    return "About";

                case 2:
                    return "Reviews";

                case 3:
                    return "Menu";

                default:
                    throw new IllegalArgumentException("wrong position for the fragment in vehicle page");
            }
        }
    }

    private class ProcessMenu extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String email, password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ResDetailsActivity.this);
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

            Intent intent = new Intent(getApplicationContext(), MenuOrder.class);
            try {
                intent.putExtra("menuJson", json.getJSONObject("menu").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(intent);
            finish();


        }

    }

    private class ProcessFav extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String isFav, uid, resID;

        public ProcessFav(String isFav, String uid, String resID) {
            this.isFav = isFav;
            this.uid = uid;
            this.resID = resID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ResDetailsActivity.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Processing Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            Log.d("redmenu", "true");
            JSONObject json = userFunction.processFav(ResDetailsActivity.isFav, uid, resID);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            try {
                if (json.getString("success") != null) {
                    Log.d("isFav", json.toString());
                    String res = json.getString("success");

                    if (Integer.parseInt(res) == 1) {
                        if (ResDetailsActivity.isFav.equals("1")) {
                            ResDetailsActivity.isFav = "0";
                        } else {
                            ResDetailsActivity.isFav = "1";
                        }

                        if (ResDetailsActivity.isFav.equals("0")) {
                            favourite.setBackground(getResources().getDrawable(R.drawable.round_border_main_unselected));
                            favourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
                        } else {
                            favourite.setBackground(getResources().getDrawable(R.drawable.round_border_main_selected));
                            favourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_24dp));
                        }
                    }
                }


                pDialog.dismiss();


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

    }
}