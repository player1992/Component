package com.leo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * <p>Date:2020-03-29.22:00</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class FirstFragment extends BaseFragment {
    @Override
    protected String getLogTag() {
        return getClass().getSimpleName();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_text, container,false);
        EditText text= inflate.findViewById(R.id.hint);
        text.setText("Fragment 111111 ");
        Button show= inflate.findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.mainView,new SecondFragment());
                transaction.addToBackStack("First");
                transaction.commit();
            }
        });
        return inflate;
    }
}
