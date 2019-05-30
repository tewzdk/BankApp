package com.example.bankapp.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bankapp.Model.Account;
import com.example.bankapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

public class ManageFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private String userEmail, assigned;
    private List<Account> accounts;
    private TextView defaultView, savingsView, budgetView, pensionView, businessView;
    private Button defaultButton, savingsButton, budgetButton, pensionButton, businessButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_accounts,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        accounts = new ArrayList<>();
        assigned = "";
        init(view);


        checkIfAssigned();


    }

    private void checkIfAssigned() {
        db.collection("users").document(userEmail).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                assigned = documentSnapshot.getString("affiliate");
                if (assigned == null) {
                    assigned= "";
                }
                getAccounts();
            }
        });
    }

    public void getAccounts() {
        db.collection("users").document(user.getEmail()).collection("accounts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG,"I got this far! 1");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG,"I got this far! 2");
                                Account account;
                                try {
                                    String accountType = document.getString("accountType");
                                    String balance = document.getString("balance");
                                    Boolean accountActive = document.getBoolean("accountActive");
                                    account = new Account(accountActive,accountType, balance);
                                } catch (NullPointerException nPEX){
                                    Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                    return;
                                }

                                accounts.add(account);

                                //Account account = document.toObject(Account.class);

                                Log.d(TAG,"I got this far! 3");
                                if (account.getAccountType().equalsIgnoreCase("Budget")) {
                                    budgetView.setText(getString(R.string.account_display, account.getAccountType()));

                                    if (account.isAccountActive()) {
                                        budgetButton.setBackgroundColor(getResources().getColor(R.color.active));
                                        budgetButton.setText(R.string.deactivate);
                                    }

                                    budgetView.setVisibility(View.VISIBLE);
                                    budgetButton.setVisibility(View.VISIBLE);

                                } else if (account.getAccountType().equalsIgnoreCase("Business")) {
                                    businessView.setText(getString(R.string.account_display, account.getAccountType()));

                                    if (account.isAccountActive()) {
                                        businessButton.setBackgroundColor(getResources().getColor(R.color.active));
                                        businessButton.setText(R.string.deactivate);
                                    }
                                    if (assigned.equalsIgnoreCase("")) {
                                        businessButton.setBackgroundColor(getResources().getColor(R.color.locked));
                                        businessButton.setText(R.string.locked);
                                    }

                                    businessView.setVisibility(View.VISIBLE);
                                    businessButton.setVisibility(View.VISIBLE);


                                } else if (account.getAccountType().equalsIgnoreCase("Default")) {
                                    defaultView.setText(getString(R.string.account_display, account.getAccountType()));

                                    if (account.isAccountActive()) {
                                        defaultButton.setBackgroundColor(getResources().getColor(R.color.active));
                                        defaultButton.setText(R.string.deactivate);
                                    }

                                    defaultView.setVisibility(View.VISIBLE);
                                    defaultButton.setVisibility(View.VISIBLE);


                                } else if (account.getAccountType().equalsIgnoreCase("Pension")) {
                                    pensionView.setText(getString(R.string.account_display, account.getAccountType()));

                                    if (account.isAccountActive()) {
                                        pensionButton.setBackgroundColor(getResources().getColor(R.color.active));
                                        pensionButton.setText(R.string.deactivate);
                                    }

                                    pensionView.setVisibility(View.VISIBLE);
                                    pensionButton.setVisibility(View.VISIBLE);



                                } else if (account.getAccountType().equalsIgnoreCase("Savings")) {

                                    savingsView.setText(account.getAccountType());

                                    if (account.isAccountActive()) {
                                        savingsButton.setBackgroundColor(getResources().getColor(R.color.active));
                                        savingsButton.setText(R.string.deactivate);
                                    }
                                    savingsView.setVisibility(View.VISIBLE);
                                    savingsButton.setVisibility(View.VISIBLE);

                                }

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.defaultButton) {

            String name = "Default";
            Boolean activate = returnActive(name);
            manageAccount(name,activate,userEmail);
            reload();

        } else if (id == R.id.savingsButton) {
            String name = "Savings";
            Boolean activate = returnActive(name);
            manageAccount(name,activate,userEmail);
            reload();

        } else if (id == R.id.budgetButton) {
            String name = "Budget";
            Boolean activate = returnActive(name);
            manageAccount(name,activate,userEmail);
            reload();

        } else if (id == R.id.businessButton) {
            String name = "Business";
            if (!assigned.equalsIgnoreCase("")) {
                Boolean activate = returnActive(name);
                manageAccount(name, activate, userEmail);
                reload();
            }

        } else if( id == R.id.pensionButton) {
            String name = "Pension";
            Boolean activate = returnActive(name);
            manageAccount(name,activate,userEmail);
            reload();

        }

    }

    public void reload() {
        FragmentTransaction reload = getFragmentManager().beginTransaction();

        if (Build.VERSION.SDK_INT >= 26) {
            reload.setReorderingAllowed(false);
        }
        reload.detach(this).attach(this).commit();
    }
    public Boolean returnActive(String name) {

        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAccountType().equalsIgnoreCase(name)) {
                Boolean activate = !accounts.get(i).isAccountActive();
                return activate;
            }
        }
        return false;
    }

    public void manageAccount(String accountType, boolean accountActive, String email) {
        Map<String, Object> account = new HashMap<>();
        account.put("accountActive", accountActive);

        db.collection("users").document(email).collection("accounts").document(accountType)
                .update(account)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public void init(View view) {
        defaultView = view.findViewById(R.id.defaultView2);
        savingsView = view.findViewById(R.id.savingsView2);
        budgetView = view.findViewById(R.id.budgetView2);
        pensionView = view.findViewById(R.id.pensionView2);
        businessView = view.findViewById(R.id.businessView2);

        defaultButton = view.findViewById(R.id.defaultButton);
        savingsButton = view.findViewById(R.id.savingsButton);
        budgetButton = view.findViewById(R.id.budgetButton);
        pensionButton = view.findViewById(R.id.pensionButton);
        businessButton = view.findViewById(R.id.businessButton);

        defaultButton.setOnClickListener(this);
        savingsButton.setOnClickListener(this);
        budgetButton.setOnClickListener(this);
        pensionButton.setOnClickListener(this);
        businessButton.setOnClickListener(this);
    }
}
