package com.example.bankapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.bankapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    private static final String TAG = "AuthActivity";

    EditText inputEmail, inputPassword;
    Button register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    public void onClick(View v) {

        Log.d(TAG,"onClick is called");
        int id = v.getId();

        if (id == R.id.buttonLogin) {
            loginAccount(inputEmail.getText().toString(),inputPassword.getText().toString());
        }
        else if (id == R.id.buttonRegister){

            createUser(inputEmail.getText().toString(),inputPassword.getText().toString());
        }



    }

    public void createUser(String email, String password){

        Log.d(TAG, "createAccount");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();



                            createAccount("Savings", BigDecimal.valueOf(0), false, user.getEmail());
                            createAccount("Budget", BigDecimal.valueOf(0), true, user.getEmail());
                            createAccount("Pension", BigDecimal.valueOf(0), false, user.getEmail());
                            createAccount("Default", BigDecimal.valueOf(0), true, user.getEmail());
                            createAccount("Business", BigDecimal.valueOf(0), false, user.getEmail());

                            updateUI(user);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void createAccount(String accountType, BigDecimal balance, boolean accountActive, String email) {


        Map<String, Object> account = new HashMap<>();
        account.put("accountType", accountType);
        account.put("balance",balance.toString());
        account.put("accountActive", accountActive);

        db.collection("users").document(email).collection("accounts").document(accountType)
                .set(account)
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

    public void loginAccount(String email, String password) {
        Log.d(TAG, "loginAcount is called");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {
        Log.d(TAG, "updateUI is called");

        Intent i = new Intent(this, MainActivity.class);
        if (currentUser != null) {
            startActivity(i);
        }
    }

    public void init() {
        Log.d(TAG, "init called");
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        register = findViewById(R.id.buttonRegister);

    }
}
