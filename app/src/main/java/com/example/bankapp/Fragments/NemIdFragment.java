package com.example.bankapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bankapp.R;

import java.util.Random;

public class NemIdFragment extends DialogFragment {

    private static final String TAG = "MyCustomDialog";


    //public OnInputListener onInputListener;



    private EditText input;
    private TextView actionOk, actionCancel, header;
    Communicator communicator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            communicator = (Communicator) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_nem_id,container,false);


        actionCancel = v.findViewById(R.id.action_cancel);
        actionOk = v.findViewById(R.id.action_ok);
        input = v.findViewById(R.id.input);
        header = v.findViewById(R.id.heading);
        Random random = new Random();
        int randomInt = random.nextInt(9999);
        header.setText("Indtast din nøgle: #"+randomInt);

        actionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                //onInputListener.sendInput(false);
                getDialog().dismiss();
            }
        });


        actionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capturing input");

                if (input.length() > 5 &&!input.getText().toString().equalsIgnoreCase("")) {
                    communicator.sendInput(true);
                    getDialog().dismiss();
                } else {
                    input.setError("Input your NemId Key");
                }
            }
        });

        return v;
    }




    interface Communicator
    {
        public void sendInput(boolean input);
    }


}
