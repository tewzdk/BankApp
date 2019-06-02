package com.example.bankapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.example.bankapp.Fragments.AccountsFragment;
import com.example.bankapp.Fragments.ManageFragment;
import com.example.bankapp.Fragments.RulesFragment;
import com.example.bankapp.Fragments.UserFragment;
import com.example.bankapp.Model.Bill;
import com.example.bankapp.R;
import com.example.bankapp.Repository.Repository;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "AccountsFragment";
    public static final String MyPREFERENCES = "MyPrefs" ;
    private TextView nav_user;
    private String userEmail;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mAuth;
    private Repository repo;
    public SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        sharedPref = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        repo = new Repository(db,sharedPref,userEmail);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AccountsFragment()).commit();

        if (savedInstanceState == null) {
            setTitle("BankApp Accounts");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AccountsFragment()).commit();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        nav_user = findViewById(R.id.nav_user);
        nav_user.setText(userEmail);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Log.d(TAG,"Going to UserFragment");
            setTitle("BankApp Profile");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new UserFragment()).commit();
            // Handle the camera action
        } else if (id == R.id.nav_accounts) {
            Log.d(TAG,"Going to AccountsFragment");
            setTitle("BankApp Accounts");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AccountsFragment()).commit();
        } else if (id == R.id.nav_add_account) {
            Log.d(TAG,"Going to ManageFragment");
            setTitle("BankApp Manage Accounts");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new ManageFragment()).commit();

        } else if (id == R.id.nav_rules){
            Log.d(TAG,"Going to RulesFragment");
            setTitle("BankApp Change Rules");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new RulesFragment()).commit();

        } else if (id == R.id.nav_signout) {
            signOut();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void signOut() {
        Intent i = new Intent(this, AuthActivity.class);
        FirebaseAuth.getInstance().signOut();
        startActivity(i);
    }




}
