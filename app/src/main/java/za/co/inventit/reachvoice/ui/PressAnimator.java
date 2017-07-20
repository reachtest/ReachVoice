package za.co.inventit.reachvoice.ui;

/**
 * A class handling animations on press of a view
 *
 * <p/>
 * Created by Laurie on 2017/07/20.
 */

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import za.co.inventit.reachvoice.R;

/**
 * A class handling animations on press of a view
 * <p/>
 * Created by Laurie on 11/23/2016.
 */
public class PressAnimator {

    public final static int ANIMATION_DURATION = 200; // millis

    private long upPressStartTime;
    private long downPressStartTime;

    private View viewToAnimate;

    public PressAnimator(final View viewToPress, final View viewToAnimate) {

        this.viewToAnimate = viewToAnimate;

        final Animation shrinkInitialAnim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.shrink_small);
        final Animation growFromShrinkSmallAnim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.grow_from_shrink_small);;
        final Animation growMedFromShrinkAnim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.grow_from_shrink_medium);
        final Animation shrinkFromGrownMedAnim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.shrink_from_grow_medium);

        viewToPress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                // Start shrinking on down press
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    downPressStartTime = System.currentTimeMillis();

                    viewToAnimate.startAnimation(shrinkInitialAnim);
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    upPressStartTime = System.currentTimeMillis();

                    long elapsedTime = upPressStartTime - downPressStartTime;

                    // If up was pressed before shrink animation has finished, delay the grow-shrink-back animation until it is done (plus some leeway)
                    if (elapsedTime < ANIMATION_DURATION) {

                        long timeForShrinkAnimToFinish = ANIMATION_DURATION - elapsedTime + 50;

                        viewToAnimate.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                viewToAnimate.startAnimation(growMedFromShrinkAnim);

                                growMedFromShrinkAnim.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        // Nothing needed here
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        // Start shrinking once the growing is done
                                        if (upPressStartTime > 0) {
                                            viewToAnimate.startAnimation(shrinkFromGrownMedAnim);
                                        }
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                        // Nothing needed here
                                    }
                                });
                            }
                        }, timeForShrinkAnimToFinish);
                    } else {
                        // Otherwise. the initial shrink animation is done, now just grow back
                        viewToAnimate.startAnimation(growFromShrinkSmallAnim);
                    }
                }

                return false;
            }
        });
    }

    public void animateOnce() {

        final Animation shrinkInitialAnim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.shrink_small);
        final Animation growMedFromShrinkAnim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.grow_from_shrink_medium);
        final Animation shrinkFromGrownMedAnim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.shrink_from_grow_medium);

        shrinkInitialAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                growMedFromShrinkAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // Do nothing
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        viewToAnimate.startAnimation(shrinkFromGrownMedAnim);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // Do nothing
                    }
                });

                viewToAnimate.startAnimation(growMedFromShrinkAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        });

        viewToAnimate.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewToAnimate.startAnimation(shrinkInitialAnim);
            }
        }, 800);
    }
}

