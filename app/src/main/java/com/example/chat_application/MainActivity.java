package com.example.chat_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ViewPager viewPager;
    private TabLayout tableLayout;


    TabAccessorAdapter tabAccessorAdapter;

    FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    private DatabaseReference RootRef;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat Application");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();



        viewPager = findViewById(R.id.main_tabs_pages);
        tabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAccessorAdapter);

        tableLayout = findViewById(R.id.main_tabe);
        tableLayout.setupWithViewPager(viewPager);
    }



    public void logouts()
    {
        updateUserStatus("offline");
        firebaseAuth.signOut();
        firebaseUser = null;
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseUser==null)
        {
            gotoStartActivity();
        }
        else
        {
            updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(firebaseUser!=null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(firebaseUser!=null)
        {
            updateUserStatus("offline");
        }

    }

    private void VerifyUserExistance() {
        String UuserId = firebaseAuth.getCurrentUser().getUid().toString();

        RootRef.child("Users").child(UuserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {

                }
                else
                {
                    Toast.makeText(MainActivity.this, "Please Set User Name", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void gotoStartActivity() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option)
        {

            logouts();
        }
        if(item.getItemId()==R.id.main_setting_options)
        {
            Intent intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.main_find_friends_option)
        {
                Intent intent = new Intent(MainActivity.this,FindFriendActivity.class);
                startActivity(intent);
        }
        if(item.getItemId()==R.id.main_create_group_option)
        {
            requestNewGroup();
        }
        return true;
    }

    private void requestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogue);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Coding Cafe");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if(groupName.isEmpty())
                {
                    groupNameField.setError("Please Enter group name");
                    return;
                }
                else
                {
                        createGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
            }
        });

        builder.show();

    }

    private void createGroup(final String groupName) {

        RootRef.child("Group").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, groupName+" group has been created", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUserStatus(String state)
    {

        if(firebaseUser!=null) {
            String saveCurrentTime, saveCurrentDate;

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat curentDate = new SimpleDateFormat("MM dd, yyyy");
            saveCurrentDate = curentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(calendar.getTime());


            HashMap<String, Object> onlineState = new HashMap<>();

            onlineState.put("time", saveCurrentTime);
            onlineState.put("date", saveCurrentDate);
            onlineState.put("state", state);


            currentUserId = firebaseAuth.getCurrentUser().getUid().toString();

            RootRef.child("Users").child(currentUserId).child("userState")
                    .updateChildren(onlineState);
        }

    }

}
