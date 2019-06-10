package com.example.bankapp.Repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.bankapp.Model.Account;
import com.example.bankapp.Model.Bill;
import com.example.bankapp.R;
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


    public Repository(FirebaseFirestore db, SharedPreferences sharedPref, String userEmail) {
        this.db = db;
        this.sharedPref = sharedPref;
        this.userEmail = userEmail;
    }


}
