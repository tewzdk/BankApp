package com.example.bankapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.bankapp.Adapters.NothingSelectedSpinnerAdapter;
import com.example.bankapp.Model.User;
import com.example.bankapp.R;
import com.example.bankapp.Repository.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;


public class UserFragment extends Fragment implements View.OnClickListener {


    EditText firstname, lastname ,phone;
    Button submit;
    private Spinner affiliate;
    private DatePicker datePicker;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private String userEmail;
    private SharedPreferences sharedPref;
    private Repository repo;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_user_details,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        affiliate = view.findViewById(R.id.affiliate_spinner);
        firstname = view.findViewById(R.id.input_firstname);
        lastname = view.findViewById(R.id.input_lastname);
        phone = view.findViewById(R.id.input_phone);
        submit = view.findViewById(R.id.submit_user_details);
        datePicker = view.findViewById(R.id.datePicker);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        sharedPref = this.getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        repo = new Repository(db,sharedPref,userEmail);


        submit.setOnClickListener(this);
        getUserDetails();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.affiliates, android.R.layout.simple_spinner_item);

        affiliate.setAdapter(
                new NothingSelectedSpinnerAdapter(adapter,
                        R.layout.spinner_nothing_selected,
                        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                        getContext()));


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



    }


    private void getUserDetails() {
        db.collection("users").document(userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG,"I got this far! 1");
                            DocumentSnapshot document = task.getResult();

                                Log.d(TAG,"I got this far! 2");
                                User user;
                                String[] words;

                            try {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                    String firstname = document.getString("firstName");
                                    String lastName = document.getString("lastName");
                                    String date = document.getString("dateOfBirth");
                                    LocalDate dateOfBirth = LocalDate.parse(date,formatter);
                                    String affiliate = document.getString("affiliate");
                                    Log.d(TAG, String.valueOf(dateOfBirth));
                                    words = date.split("/");
                                    int phoneNumber = Integer.parseInt(document.getString("phoneNumber"));
                                    user = new User(affiliate, dateOfBirth,firstname,lastName,phoneNumber, userEmail);
                                } catch (NullPointerException nPEX){
                                    Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                    return;
                                }

                                Log.d(TAG,"I got this far! 3");

                                //TODO: Dette er ikke godt, men skal finde en løsning som gør at jeg kan få data fra server og smide i spinner
                            if (user.getAffiliate() != null) {
                                if (user.getAffiliate().equalsIgnoreCase("Odense")) {
                                    affiliate.setSelection(1);
                                } else {
                                    affiliate.setSelection(2);
                                }
                            }
                                firstname.setText(user.getFirstName());
                                lastname.setText(user.getLastName());
                                phone.setText(String.valueOf(user.getPhoneNumber()));
                                int day = Integer.parseInt(words[0]);
                                int month = Integer.parseInt(words[1]);
                                month = month-1;
                                int year = Integer.parseInt(words[2]);


                                datePicker.updateDate(year,month,day);


                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                    }
                });

    }
    private void userDetails(String email) {
        //int month = datePicker.getMonth()+1;
        String month = returnString(datePicker.getMonth()+1);
        String day = returnString(datePicker.getDayOfMonth());


        String dateTime = day + "/" + month + "/" + datePicker.getYear();
        Log.d(TAG,"DatePicker: day, "+ datePicker.getDayOfMonth());
        Log.d(TAG,"DatePicker: month, "+ datePicker.getMonth());
        Log.d(TAG,"DatePicker: year, "+datePicker.getYear());
        Log.d(TAG,"DatePicker: "+dateTime);

        //SAVES IN SHAREDPREFERENCES SO I CAN BE USED IN ACCOUNTFRAGMENT
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("date",dateTime);
        editor.commit();

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("firstName", firstname.getText().toString());
        userDetails.put("lastName", lastname.getText().toString());
        userDetails.put("phoneNumber", phone.getText().toString());
        userDetails.put("dateOfBirth", dateTime);
        if (affiliate.getSelectedItem() != null){
        userDetails.put("affiliate",affiliate.getSelectedItem().toString());
        }
        db.collection("users").document(email)
                .set(userDetails)
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

    @Override
    public void onClick(View v) {
        Log.d(TAG,"onClick is called");
        int id = v.getId();

        if (id == R.id.submit_user_details) {
            Log.d(TAG,firstname.getText().toString()+" " + lastname.getText().toString());
            boolean detailsBool = false;

            if (phone.length() < 8) {
                detailsBool = false;
                phone.setError("Phone number must be 8 digits");
            } if (TextUtils.isEmpty(firstname.getText().toString())) {
                detailsBool = false;
                firstname.setError("You should enter your firstname");
            } if (TextUtils.isEmpty(lastname.getText().toString())) {
                detailsBool = false;
                lastname.setError("You should enter your lastname");
            }
            if (phone.length() == 8 && firstname.length() > 0 && lastname.length() > 0) {
                userDetails(user.getEmail());
                Toast toast = Toast.makeText(getContext(), "User details has been saved", Toast.LENGTH_SHORT);
                toast.show();
            }

        }

    }

    private String returnString(int number) {
        String monthString = "";

        if (number <= 9) {
            monthString = "0"+number;
        } else {
            monthString = String.valueOf(number);
        }

        return monthString;

    }


}
