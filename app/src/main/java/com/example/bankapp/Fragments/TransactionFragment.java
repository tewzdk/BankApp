package com.example.bankapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.bankapp.Model.Account;
import com.example.bankapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class TransactionFragment extends Fragment implements View.OnClickListener, NemIdFragment.Communicator{

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private NemIdFragment fragment;

    final static String TAG = "TransactionFragment";
    public static final int DIALOG_FRAGMENT = 1;


    private String userEmail;
    private String currentAccountName;
    private Boolean otherAccountBoolen;

    TextView accountName, accountBalance, amountText,reciverText;
    EditText transactionAmount, customRecipient;
    Spinner accountsSpinner, otherAccountSpinner;
    Button makeTransaction, otherAccount, ownAccount;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragment = new NemIdFragment();
        userEmail = user.getEmail();
        init(view);
        otherAccountBoolen = false;

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.transaction_otheraccount) {
            otherAccountSpinner.setVisibility(View.VISIBLE);
            accountsSpinner.setVisibility(View.GONE);
            //test();

        } else if (id == R.id.transaction_ownaccount) {
            accountsSpinner.setVisibility(View.VISIBLE);
            otherAccountSpinner.setVisibility(View.GONE);

        } else if (id == R.id.transaction_button) {
            String recipient;

            if (!transactionAmount.getText().toString().equalsIgnoreCase("")) {
                    Log.d(TAG, "Here START");
                    int value = Integer.parseInt(transactionAmount.getText().toString());

                    try {
                        String checkIfPension = accountsSpinner.getSelectedItem().toString();
                        Log.d(TAG,"SPINNER CONTAINS: "+checkIfPension);
                        BigDecimal amount = new BigDecimal(transactionAmount.getText().toString());
                    if (accountsSpinner.getVisibility() == View.VISIBLE && !checkIfPension.equalsIgnoreCase("Pension")) {

                        //THIS WILL TRANSFER WITHIN OWN ACCOUNTS
                        recipient = accountsSpinner.getSelectedItem().toString();
                        //transactionAmount.setText("OWN ACCOUNT");
                        Log.d(TAG, "Here OTHER");
                        transferOtherAccount(recipient, amount);

                    } else if (otherAccountSpinner.getVisibility() == View.VISIBLE || checkIfPension.equalsIgnoreCase("Pension")) {

                        if (!otherAccountBoolen) {
                            NemIdFragment dialog = new NemIdFragment();
                            //dialog.onAttach(this.getContext());
                            dialog.setTargetFragment(this, DIALOG_FRAGMENT);
                            dialog.show(getFragmentManager(), "NemIdFragment");
                        } else if (otherAccountBoolen) {

                                //THIS WILL TRANSFER TO OTHER USERS DEFAULT ACCOUNT
                                Log.d(TAG, "Here THIS ACCOUNT");
                                recipient = otherAccountSpinner.getSelectedItem().toString();
                                //transactionAmount.setText("OTHER ACCOUNT");
                                transferOtherUser(recipient, amount);

                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "something went wrong");
                }

            } else {
                transactionAmount.setError("Must input");
            }
        }

    }


    private void transferOtherAccount(String recipient, final BigDecimal amount) {
        Log.d(TAG, "transferOtherAccount");

        final DocumentReference fromAccount = db.collection("users").document(userEmail).collection("accounts")
                .document(currentAccountName);


        final DocumentReference toAccount = db.collection("users").document(userEmail).collection("accounts")
                .document(recipient);

        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot saveFromAccount = transaction.get(fromAccount);
                DocumentSnapshot saveToAccount = transaction.get(toAccount);

                BigDecimal balanceFromAccount = new BigDecimal(saveFromAccount.getString("balance"));
                BigDecimal balanceToAccount = new BigDecimal(saveToAccount.getString("balance"));

                transaction.update(fromAccount, "balance", balanceFromAccount.subtract(amount).toString());
                transaction.update(toAccount, "balance", balanceToAccount.add(amount).toString());


                Log.d(TAG, "Success: " + balanceFromAccount + " " + balanceToAccount + " " + amount);

                return null;
            }
        });
    }

    private void transferOtherUser(String recipient, final BigDecimal amount) {

        final DocumentReference fromAccount = db.collection("users").document(userEmail).collection("accounts")
                .document(currentAccountName);

        final DocumentReference toAccount = db.collection("users").document(recipient).collection("accounts")
                .document("Default");

        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot saveFromAccount = transaction.get(fromAccount);
                DocumentSnapshot saveToAccount = transaction.get(toAccount);

                BigDecimal balanceFromAccount = new BigDecimal(saveFromAccount.getString("balance"));
                BigDecimal balanceToAccount = new BigDecimal(saveToAccount.getString("balance"));

                transaction.update(fromAccount, "balance", balanceFromAccount.subtract(amount).toString());
                transaction.update(toAccount, "balance", balanceToAccount.add(amount).toString());


                return null;
            }
        });
    }

    public void test() {

        final DocumentReference fromAccount = db.collection("users").document(userEmail).collection("accounts")
                .document("Default");

        final DocumentReference toBudget = db.collection("users").document(userEmail).collection("accounts")
                .document("Budget");

        final DocumentReference toSavings = db.collection("users").document(userEmail).collection("accounts")
                .document("Savings");

        final DocumentReference getRules = db.collection("users").document(userEmail).collection("rules")
                .document("monthly");


        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot saveFromAccount = transaction.get(fromAccount);
                BigDecimal balanceFromDefault = new BigDecimal(saveFromAccount.getString("balance"));

                DocumentSnapshot saveToBudget = transaction.get(toBudget);
                DocumentSnapshot saveToSavings = transaction.get(toSavings);

                BigDecimal balanceFromBudget = new BigDecimal(saveToBudget.getString("balance"));
                BigDecimal balanceFromSavings = new BigDecimal(saveToSavings.getString("balance"));

                BigDecimal hundred = new BigDecimal("100");

                /*
                DocumentSnapshot saveRules = transaction.get(getRules);
                BigDecimal monthlyBudget = new BigDecimal(saveRules.getString("monthlyBudget"));
                BigDecimal monthlySavings = new BigDecimal(saveRules.getString("monthlyBudget"));
                BigDecimal addedValues = monthlyBudget.add(monthlySavings);
                */

                transaction.update(fromAccount, "balance", balanceFromDefault.subtract(hundred).toString());
                transaction.update(toBudget, "balance", balanceFromBudget.add(hundred).toString());
                transaction.update(toSavings, "balance", balanceFromSavings.add(hundred).toString());
                return null;
            }
        });
    }

    public void getAccount() {
        Log.d(TAG, "getAccount: Has been called");
        DocumentReference docRef = db.collection("users").document(user.getEmail())
                .collection("accounts").document(getFirstWord(accountName.getText().toString()));
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Account account = documentSnapshot.toObject(Account.class);

                accountBalance.setText(getString(R.string.balance_display, account.getBalance()));

            }
        });
    }

    private void spinners() {
        db.collection("users").document(userEmail).collection("accounts").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        List<String> accountList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Account account;

                                try {
                                    String accountType = document.getString("accountType");
                                    String balance = document.getString("balance");
                                    Boolean accountActive = document.getBoolean("accountActive");
                                    account = new Account(accountActive, accountType, balance);
                                } catch (NullPointerException nPEX) {
                                    Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                    return;
                                }

                                if (account.isAccountActive() && !account.getAccountType().equalsIgnoreCase(currentAccountName)) {
                                    accountList.add(account.getAccountType());
                                }
                            }

                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, accountList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            accountsSpinner.setAdapter(dataAdapter);

                        } else {
                            Log.d(TAG, "Failed to retrieve document");
                        }

                    }
                });

        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        List<String> userAccounts = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            try {
                                String userName = document.getId();
                                userAccounts.add(userName);
                            } catch (NullPointerException nPEX) {
                                Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                return;
                            }

                        }

                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, userAccounts);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        otherAccountSpinner.setAdapter(dataAdapter);

                    }
                });
    }


    private String getFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) { // Check if there is more than one word.
            return text.substring(0, index); // Extract first word.
        } else {
            return text; // Text is the first word itself.
        }
    }

    public void init(View v) {

        currentAccountName = getArguments().getString("accountName");

        accountName = v.findViewById(R.id.transaction_account_name);
        accountBalance = v.findViewById(R.id.transaction_account_balance);
        transactionAmount = v.findViewById(R.id.transaction_amount);
        accountsSpinner = v.findViewById(R.id.transaction_spinner);
        otherAccountSpinner = v.findViewById(R.id.transaction_spinner_other);
        amountText = v.findViewById(R.id.transaction_amount_text);
        reciverText = v.findViewById(R.id.transaction_reciever);

        //Buttons
        otherAccount = v.findViewById(R.id.transaction_otheraccount);
        ownAccount = v.findViewById(R.id.transaction_ownaccount);
        makeTransaction = v.findViewById(R.id.transaction_button);

        accountName.setText(getString(R.string.account_display, currentAccountName));
        accountBalance.setText(getArguments().getString("accountBalance"));

        otherAccount.setOnClickListener(this);
        ownAccount.setOnClickListener(this);
        makeTransaction.setOnClickListener(this);


        spinners();
    }

    @Override
    public void sendInput(boolean input) {
        otherAccountBoolen = input;
    }
}
