package io.aniruddh.delego.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;

import io.aniruddh.delego.R;


/**
 * Created by Aniruddh on 18-10-2017.
 */

public class SlideOne extends Fragment {
    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;

    public SlideOne() {
    }

    public static SlideOne newInstance(int layoutResId) {
        SlideOne sampleSlide = new SlideOne();

        Bundle bundleArgs = new Bundle();
        bundleArgs.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(bundleArgs);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide_one, container, false);
        LottieAnimationView animationView = (LottieAnimationView) view.findViewById(R.id.slideOneAnimation);
        animationView.setAnimation("animations/checked_loading.json");
        animationView.loop(true);
        animationView.playAnimation();
        return view;
    }

}