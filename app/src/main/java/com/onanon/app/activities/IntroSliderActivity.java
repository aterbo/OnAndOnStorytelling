package com.onanon.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onanon.app.PrefManager;
import com.onanon.app.R;

public class IntroSliderActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] background_color_references, graphic_references, title_references, description_references;
    private Button btnSkip, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_intro_slider);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layout_dots);
        btnSkip = (Button) findViewById(R.id.skip_button);
        btnNext = (Button) findViewById(R.id.next_button);


        // layouts of all welcome sliders
        // add few more layouts if you want
        createLayouts();

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
    }

    private void createLayouts(){

        graphic_references = new int[] {
                R.drawable.alberticon,
                R.mipmap.ic_launcher,
                R.drawable.alberticon,
                R.mipmap.ic_launcher};

        title_references = new int[] {
                R.string.slide_1_title,
                R.string.slide_2_title,
                R.string.slide_3_title,
                R.string.slide_4_title};

        description_references = new int[] {
                R.string.slide_1_desc,
                R.string.slide_2_desc,
                R.string.slide_3_desc,
                R.string.slide_4_desc};

        background_color_references = new int[]{
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark,
                R.color.colorAccent};
    }

    public void nextButtonClicked(View view) {

        // checking for last page
        // if last page home screen will be launched
        int current = getItem(+1);
        if (current < background_color_references.length) {
            // move to next screen
            viewPager.setCurrentItem(current);
        } else {
            closeIntroSlides();
        }
    }

    public void skipButtonClicked(View view) {
        closeIntroSlides();
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[background_color_references.length];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.blackTint));
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(getResources().getColor(R.color.whiteTint));
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void closeIntroSlides() {
        PrefManager prefManager = new PrefManager(this);
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(IntroSliderActivity.this, SplashScreenActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == background_color_references.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.welcome_slide_template, container, false);
            view.findViewById(R.id.intro_slide_background).setBackgroundResource(background_color_references[position]);
            ((ImageView) view.findViewById(R.id.intro_slide_graphic)).setImageResource(graphic_references[position]);
            ((TextView) view.findViewById(R.id.intro_slide_title)).setText(title_references[position]);
            ((TextView) view.findViewById(R.id.intro_slide_description)).setText(description_references[position]);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return background_color_references.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}