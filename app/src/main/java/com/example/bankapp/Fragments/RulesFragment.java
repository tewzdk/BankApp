package com.example.bankapp.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Constraints;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bankapp.Activities.MainActivity;
import com.example.bankapp.Model.Account;
import com.example.bankapp.Model.Bill;
import com.example.bankapp.R;
import com.example.bankapp.Repository.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class RulesFragment extends Fragment implements View.OnClickListener, NemIdFragment.Communicator{

    public static final int DIALOG_FRAGMENT = 1;
    private String TAG = "TransactionFragment";

    private EditText setBudget, setSavings, setBillName, setBillAmount;
    private Button buttonSave, buttonAdd, buttonRemove;
    private Spinner billSpinner;
    private Boolean nemId;
    private Repository repo;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userEmail;
    private List<Bill> listOfBills = new ArrayList<>();
    private List<String> listOfBills2 = new ArrayList<>();
    private SharedPreferences sharedPref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_rules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPref = this.getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        userEmail = user.getEmail();
        init(view);
        nemId = false;
        getBills();
        getMonthly();


    }



    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.rules_btn_bs) {
            updateRules(setBudget.getText().toString(),setSavings.getText().toString());
        } else if (id == R.id.rules_btn_add_bill) {

            addBill(setBillName.getText().toString(),setBillAmount.getText().toString());

        } else if (id == R.id.rules_btn_remove_bill) {
            removeBill(billSpinner.getSelectedItem().toString());
        }

    }

    private void updateRules(String monthlyBudget, String monthlySavings) {

        Map<String, Object> rules = new HashMap<>();
        rules.put("monthlyBudget", monthlyBudget);
        rules.put("monthlySavings", monthlySavings);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("monthlyBudget",monthlyBudget);
        editor.putString("monthlySavings",monthlySavings);
        editor.commit();



        db.collection("users").document(userEmail).collection("rules").document("monthly")
                .set(rules).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
                Toast toast = Toast.makeText(getContext(),"Monthly budget and savings transactions updated", Toast.LENGTH_SHORT);
                toast.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "DocumentSnapshot was not saved to firebase");
            }
        });
    }

    private void removeBill(final String billName) {
        if (!nemId) {
            NemIdFragment dialog = new NemIdFragment();
            //dialog.onAttach(this.getContext());
            dialog.setTargetFragment(this, DIALOG_FRAGMENT);
            dialog.show(getFragmentManager(), "NemIdFragment");

        } else if (nemId) {
            db.collection("users").document(userEmail).collection("bills")
                    .document(billName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    Toast toast = Toast.makeText(getContext(), billName+" has been removed from bills", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

    }

    private void addBill(final String billName, String billAmount) {

        if (!nemId) {
            NemIdFragment dialog = new NemIdFragment();
            //dialog.onAttach(this.getContext());
            dialog.setTargetFragment(this, DIALOG_FRAGMENT);
            dialog.show(getFragmentManager(), "NemIdFragment");

        } else if (nemId) {
            Map<String, Object> newBill = new HashMap<>();
            newBill.put("billName", billName);
            newBill.put("billAmount", billAmount);
            newBill.put("accountNumber", "3124547649");

            db.collection("users").document(userEmail).collection("bills")
                    .document(billName).set(newBill)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            Toast toast = Toast.makeText(getContext(), billName+" has been added to bills", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "DocumentSnapshot was not saved to firebase");
                }
            });
        }
    }


    private void getMonthly() {

        db.collection("users").document(userEmail).collection("rules").document("monthly")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String monthlyBudget = documentSnapshot.getString("monthlyBudget");
                String monthlySavings = documentSnapshot.getString("monthlySavings");

                setBudget.setText(monthlyBudget);
                setSavings.setText(monthlySavings);
            }
        });
    }

    private void getBills() {

        db.collection("users").document(userEmail).collection("bills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for ( QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG,"I got this far! 2");
                                Bill bill;
                                Log.d(TAG,document.toString());
                                try {
                                    String billName = document.getString("billName");
                                    String billAmount = document.getString("billAmount");
                                    String accountNumber = document.getString("accountNumber");
                                    bill = new Bill(accountNumber,billAmount,billName);
                                    //bill = document.toObject(Bill.class);
                                    listOfBills2.add(bill.getName());


                                } catch (NullPointerException nPEX){
                                    Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                    return;
                                }
                            }
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listOfBills2);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            billSpinner.setAdapter(dataAdapter);
                        }
                    }
                });
    }

    private void init(View v) {
        setBudget = v.findViewById(R.id.rules_set_budget);
        setSavings = v.findViewById(R.id.rules_set_savings);
        setBillName = v.findViewById(R.id.rules_bill_name);
        setBillAmount = v.findViewById(R.id.rules_bill_input);

        buttonSave = v.findViewById(R.id.rules_btn_bs);
        buttonAdd = v.findViewById(R.id.rules_btn_add_bill);
        buttonRemove = v.findViewById(R.id.rules_btn_remove_bill);

        billSpinner = v.findViewById(R.id.rules_spinner);

        buttonSave.setOnClickListener(this);
        buttonAdd.setOnClickListener(this);
        buttonRemove.setOnClickListener(this);

    }

    @Override
    public void sendInput(boolean input) {
        nemId = input;
    }
}
