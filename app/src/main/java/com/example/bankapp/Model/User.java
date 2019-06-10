package com.example.bankapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.util.Date;

public class User implements Parcelable {

    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private int phoneNumber;
    private String affiliate;

    public User() {
    }

    public User(String email, String firstName, String lastName, LocalDate dateOfBirth) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public User(String affiliate, LocalDate dateOfBirth, String firstName, String lastName, int phoneNumber, String email) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.affiliate = affiliate;

    }

    protected User(Parcel in) {
        email = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        phoneNumber = in.readInt();
        affiliate = in.readString();
    }


    public String getAffiliate() {
        return affiliate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setAffiliate(String affiliate) {
        this.affiliate = affiliate;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeValue(dateOfBirth);
        dest.writeInt(phoneNumber);
        dest.writeString(affiliate);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    //I did not use Parceable in this project but instead Bundle specific strings
    //
    //User user = new User("","","","","");
    //Intent intent =new Intent(AuthActivity.this,MainActivity.class);
    //intent.PutExtra("user", user);
    //startActivity(intent);

    //to get the parcelable user
    //Intent intent = getIntent();
    //User user = intent.getParcelableExtra("user");

}
