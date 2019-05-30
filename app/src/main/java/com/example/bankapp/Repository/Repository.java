package com.example.bankapp.Repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.bankapp.Model.Bill;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.support.constraint.Constraints.TAG;

public class Repository {
    private final String TAG = "Repository";

    private FirebaseFirestore db;
    private SharedPreferences sharedPref;

    private String userEmail, returnMonthly;
    private List<Bill> listofBills = new ArrayList<>();

    public Repository(FirebaseFirestore db, SharedPreferences sharedPref, String userEmail) {
        this.db = db;
        this.sharedPref = sharedPref;
        this.userEmail = userEmail;
    }

    public String getMonthly(final String fetch) {

        db.collection("users").document(userEmail).collection("rules").document("monthly")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (fetch.equalsIgnoreCase("monthlyBudget")) {
                    returnMonthly = documentSnapshot.getString("monthlyBudget");
                } else if (fetch.equalsIgnoreCase("monthlySavings")) {
                    returnMonthly = documentSnapshot.getString("monthlySavings");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        return returnMonthly;
    }

    public List<Bill> getBills() {

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
                                    //listofBills = document.toObject(Bill.class);
                                    listofBills.add(bill);

                                } catch (NullPointerException nPEX){
                                    Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                    return;
                                }
                            }
                        }
                    }
                });
        return listofBills;
    }

    public void getPref() {
        String test = sharedPref.getString("monthlyBudget","0");
        Log.d(TAG, test);
    }
    public void checkRules() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2019,12,31);
        Calendar lastLoggedIn = Calendar.getInstance();
        lastLoggedIn.setTimeInMillis(sharedPref.getLong("lastLoggedIn",0));

        //Log.d(TAG,"SHAREDPREF"+lastLoggedIn.toString());
        //Log.d(TAG,"NY BOIIII"+calendar.toString());
        Log.d(TAG, "SIDNEY 1");

        //SharedPreferences.Editor editor = sharedPref.edit();
        //long millis = calendar.getTimeInMillis();
        //editor.putLong("lastLoggedIn", millis);
        //editor.commit();

        // int lastDay = calendar.getActualMaximum(Calendar.DATE);
        //checkIt(calendar);

        if (checkIt(calendar)) {

            final DocumentReference fromAccount = db.collection("users").document(userEmail).collection("accounts")
                    .document("Default");

            final DocumentReference toBudget = db.collection("users").document(userEmail).collection("accounts")
                    .document("Budget");

            final DocumentReference toSavings = db.collection("users").document(userEmail).collection("accounts")
                    .document("Savings");


            final BigDecimal monthlyBudget = new BigDecimal(sharedPref.getString("monthlyBudget", "0"));
            final BigDecimal monthlySavings = new BigDecimal(sharedPref.getString("monthlySavings", "0"));
            final BigDecimal addedValues = monthlyBudget.add(monthlySavings);

            Log.d(TAG, "SIDNEY 2 " + addedValues);

            db.runTransaction(new Transaction.Function<Void>() {
                @android.support.annotation.Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    Log.d(TAG, "SIDNEY 3 ");

                    DocumentSnapshot saveFromAccount = transaction.get(fromAccount);
                    DocumentSnapshot saveToBudget = transaction.get(toBudget);
                    DocumentSnapshot saveToSavings = transaction.get(toSavings);

                    Log.d(TAG, "SIDNEY 4");


                    BigDecimal balanceFromDefault = new BigDecimal(saveFromAccount.getString("balance"));
                    BigDecimal balanceFromBudget = new BigDecimal(saveToBudget.getString("balance"));
                    BigDecimal balanceFromSavings = new BigDecimal(saveToSavings.getString("balance"));

                    Log.d(TAG, "SIDNEY 5" + balanceFromDefault + " " + balanceFromBudget + " " + balanceFromSavings);

                    transaction.update(fromAccount, "balance", balanceFromDefault.subtract(addedValues).toString());
                    transaction.update(toBudget, "balance", balanceFromBudget.add(monthlyBudget).toString());
                    transaction.update(toSavings, "balance", balanceFromSavings.add(monthlySavings).toString());

                    Log.d(TAG, "SIDNEY 6");

                    return null;
                }
            });
        }

    }

    private boolean checkIt(Calendar calendar) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMM");


        String monthStr = monthFormat.format(calendar.getTime());
        Log.d(TAG,"Maximum for " + monthStr +" is "
                + calendar.getActualMaximum(Calendar.DATE));

        int month = calendar.MONTH;
        int day = calendar.DAY_OF_MONTH;
        Log.d(TAG,String.valueOf(month)+" "+ (day));



        boolean isLastDay = calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE);
        Log.d(TAG,"The calendar date " +
                (isLastDay ? "is " : "is not ") +
                "the last day of the month.");

        return isLastDay;
    }

    public void getString() {
        db.collection("users").document(userEmail).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String retrievedDate = documentSnapshot.getString("dateOfBirth");
                Log.d(TAG,retrievedDate);
                if (retrievedDate == null) {
                    retrievedDate = "01/01/2000";
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("date",retrievedDate);
                editor.commit();
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
}
