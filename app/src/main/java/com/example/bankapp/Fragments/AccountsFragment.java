package com.example.bankapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import com.example.bankapp.Model.Account;
import com.example.bankapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import static android.support.constraint.Constraints.TAG;

public class AccountsFragment extends Fragment implements View.OnClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private String userEmail, retrievedDate;
    private int age;
    private SharedPreferences sharedPref;

    private Bundle bundleContext;

    TextView defaultView, savingsView, budgetView, pensionView, businessView, pensionAlert;
    TextView defaultBalance, savingsBalance, budgetBalance, pensionBalance, businessBalance;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_accounts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        userEmail = user.getEmail();
        sharedPref = this.getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        retrievedDate = sharedPref.getString("date","01/01/2000");
        age = checkAge();

        pensionAlert = view.findViewById(R.id.pensionAlert);
        bundleContext = new Bundle();




        defaultView = view.findViewById(R.id.defaultView);
        defaultView.setOnClickListener(this);
        savingsView = view.findViewById(R.id.savingsView);
        savingsView.setOnClickListener(this);
        budgetView = view.findViewById(R.id.budgetView);
        budgetView.setOnClickListener(this);
        pensionView = view.findViewById(R.id.pensionView);
        if (age > 76){
            pensionView.setOnClickListener(this);
        }
        businessView = view.findViewById(R.id.businessView);
        businessView.setOnClickListener(this);

        defaultBalance = view.findViewById(R.id.defaultBalance);
        savingsBalance = view.findViewById(R.id.savingsBalance);
        budgetBalance = view.findViewById(R.id.budgetBalance);
        pensionBalance = view.findViewById(R.id.pensionBalance);
        businessBalance = view.findViewById(R.id.businessBalance);

        //getString();
        Log.d(TAG,"Age is: "+age);
        getAccounts();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        TransactionFragment fragment = new TransactionFragment();
        Bundle bundle = new Bundle();

        if (i == R.id.savingsView) {
            bundle.putString("accountName", getFirstWord(savingsView.getText().toString()));
            bundle.putString("accountBalance", getFirstWord(savingsBalance.getText().toString()));

        } else if (i == R.id.budgetView) {
            bundle.putString("accountName", getFirstWord(budgetView.getText().toString()));
            bundle.putString("accountBalance", getFirstWord(budgetBalance.getText().toString()));

        } else if (i == R.id.pensionView) {
            bundle.putString("accountName", getFirstWord(pensionView.getText().toString()));
            bundle.putString("accountBalance", getFirstWord(pensionBalance.getText().toString()));

        }else if (i == R.id.defaultView) {
            bundle.putString("accountName", getFirstWord(defaultView.getText().toString()));
            bundle.putString("accountBalance", getFirstWord(defaultBalance.getText().toString()));

        }else if (i == R.id.businessView) {
            bundle.putString("accountName", getFirstWord(businessView.getText().toString()));
            bundle.putString("accountBalance", getFirstWord(businessBalance.getText().toString()));

        }



        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

    }

    private void getString() {
        db.collection("users").document(userEmail).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                retrievedDate = documentSnapshot.getString("dateOfBirth");
                Log.d(TAG,retrievedDate);
                if (retrievedDate == null) {
                    retrievedDate = "01/01/2000";
                }
                age = checkAge();
            }
        });
    }


    private int checkAge(){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate birthDate = LocalDate.parse(retrievedDate,formatter);
        Log.d(TAG,birthDate.toString());
        LocalDate currentDate = LocalDate.now();
        Log.d(TAG,currentDate.toString());
        if ((birthDate != null) && (currentDate != null)) {
            return Period.between(birthDate, currentDate).getYears();
        } else {
            return 0;
        }

    }

    private void getAccounts() {

        db.collection("users").document(userEmail).collection("accounts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG,"I got this far! 1");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG,"I got this far! 2");

                                //Account account = new Account(bundleContext.getBoolean("accountActive"),bundleContext.getString("accountType"),bundleContext.getString("balance"));
                                Account account;
                                try {
                                    String accountType = document.getString("accountType");
                                    String balance = document.getString("balance");
                                    Boolean accountActive = document.getBoolean("accountActive");
                                    account = new Account(accountActive,accountType, balance);
                                    bundleContext.putString("accountType",accountType);
                                    bundleContext.putString("balance",balance);
                                    bundleContext.putBoolean("accountActive",accountActive);
                                } catch (NullPointerException nPEX){
                                    Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                    return;
                                }

                                //Account account = document.toObject(Account.class);

                                Log.d(TAG,"I got this far! 3");
                                if (account.getAccountType().equalsIgnoreCase("Budget") && account.isAccountActive()) {
                                    budgetView.setText(getString(R.string.account_display, account.getAccountType()));
                                    budgetBalance.setText(account.getBalance().toString());

                                    budgetBalance.setVisibility(View.VISIBLE);
                                    budgetView.setVisibility(View.VISIBLE);

                                } else if (account.getAccountType().equalsIgnoreCase("Business") && account.isAccountActive()) {
                                    businessView.setText(getString(R.string.account_display, account.getAccountType()));
                                    businessBalance.setText(account.getBalance().toString());

                                    businessBalance.setVisibility(View.VISIBLE);
                                    businessView.setVisibility(View.VISIBLE);

                                } else if (account.getAccountType().equalsIgnoreCase("Default") && account.isAccountActive()) {
                                    defaultView.setText(getString(R.string.account_display, account.getAccountType()));
                                    defaultBalance.setText(account.getBalance().toString());

                                    defaultView.setVisibility(View.VISIBLE);
                                    defaultBalance.setVisibility(View.VISIBLE);

                                } else if (account.getAccountType().equalsIgnoreCase("Pension") && account.isAccountActive()) {

                                    pensionView.setText(getString(R.string.account_display, account.getAccountType()));
                                    pensionBalance.setText(account.getBalance().toString());


                                    pensionBalance.setVisibility(View.VISIBLE);
                                    pensionView.setVisibility(View.VISIBLE);
                                    if (age < 77) {
                                        pensionAlert.setVisibility(View.VISIBLE);

                                    }

                                } else if (account.getAccountType().equalsIgnoreCase("Savings") && account.isAccountActive()) {

                                    savingsView.setText(getString(R.string.account_display, account.getAccountType()));
                                    savingsBalance.setText(account.getBalance().toString());
                                    savingsView.setVisibility(View.VISIBLE);
                                    savingsBalance.setVisibility(View.VISIBLE);

                                }

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }

                });



    }

    public void reload() {
        FragmentTransaction reload = getFragmentManager().beginTransaction();

        if (Build.VERSION.SDK_INT >= 26) {
            reload.setReorderingAllowed(false);
        }
        reload.detach(this).attach(this).commit();
    }

    private String getFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) { // Check if there is more than one word.
            return text.substring(0, index); // Extract first word.
        } else {
            return text; // Text is the first word itself.
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //savedInstanceState.putString("da",this.getContext());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }


}
