package com.leo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * <p>Date:2020-03-29.21:57</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public abstract class BaseFragment extends Fragment {

    protected abstract String getLogTag();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(getLogTag(),"onAttach");
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.e(getLogTag(),"onAttachFragment");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(getLogTag(),"onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(getLogTag(),"onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.e(getLogTag(),"onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.e(getLogTag(),"onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.e(getLogTag(),"onResume");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.e(getLogTag(),"onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Log.e(getLogTag(),"onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(getLogTag(),"onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.e(getLogTag(),"onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.e(getLogTag(),"onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.e(getLogTag(),"onDetach");
        super.onDetach();
    }
}
