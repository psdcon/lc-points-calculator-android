package com.psdcon.paul.leavingcertcalculator;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

/**
 * Created by Paul on 03/01/2016.
 */
public class AboutActivity extends AppCompatActivity {

    private View revealableLayout = null;
    private View slidingBtnView = null;
    private Button gotItBtn = null;

    boolean runOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);
        setContentView(R.layout.activity_about);

        // This activity has a transparent background and when it is created,
        // a view inside the transparent layout is circularlyRevealed
        // http://stackoverflow.com/questions/30958834/circular-reveal-transition-for-new-activity

        // Get the layout to circularly reveal
        revealableLayout = findViewById(R.id.revealable_layout);

        // Get the view containing the button to be animated in
        slidingBtnView = findViewById(R.id.sliding_btn_view);
        slidingBtnView.setVisibility(View.GONE);
        // Add click listener to the button that will close the finish the activity with animation
        gotItBtn = (Button) findViewById(R.id.got_it_btn);
        gotItBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circularCollapseActivity();
            }
        });

        //important to put circularRevealActivity() into a OnGlobalLayoutListener, because the view needs to be drawn for the animation.
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= 21) {
            revealableLayout.setVisibility(View.INVISIBLE); // Hide view until ready to start animation

            ViewTreeObserver viewTreeObserver = revealableLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                    }
                });
            }
        }
    }

    // Override the back press to trigger closing animation. finish() is called after the animation has ended
    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= 21)
            circularCollapseActivity();
        else
            super.onBackPressed();
    }

    // Do not move the transparent activity, fade it out
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.do_not_move, R.anim.fade_out);
    }

    @TargetApi(21)
    private void circularRevealActivity() {
        // Make sure animation only runs once
        if (runOnce)
            return;
        else
            runOnce = true;

        // Animate from top right under menu button
        int cx = revealableLayout.getWidth() - 10;
        int cy = 0;

        // Make the radius height that bit bigger
        float finalRadius = Math.max(revealableLayout.getWidth(), revealableLayout.getHeight() + revealableLayout.getWidth());

        // Create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(revealableLayout, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        // Make the view visible and start the animation
        revealableLayout.setVisibility(View.VISIBLE);
        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

//                http://stackoverflow.com/questions/18232372/slide-a-layout-up-from-bottom-of-screen
                Animation bottomUp = AnimationUtils.loadAnimation(AboutActivity.this, R.anim.slide_from_bottom);
                slidingBtnView.startAnimation(bottomUp);
                slidingBtnView.setVisibility(View.VISIBLE);

//                slidingBtnView.animate().setDuration(400).translationY(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        circularReveal.start();
    }

    @TargetApi(21)
    private void circularCollapseActivity() {

        int cx = revealableLayout.getWidth() - 10;
        int cy = 0;

        int startRadius = Math.max(revealableLayout.getWidth(), revealableLayout.getHeight() + revealableLayout.getWidth());
        int finalRadius = 0;

        Animator circularCollapse = ViewAnimationUtils.createCircularReveal(revealableLayout, cx, cy, startRadius, finalRadius);
        circularCollapse.setDuration(800);
        circularCollapse.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Hide view once it's animated out or else it flashes back in
                revealableLayout.setVisibility(View.INVISIBLE);
                // Close the activity
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        circularCollapse.start();
    }
}

