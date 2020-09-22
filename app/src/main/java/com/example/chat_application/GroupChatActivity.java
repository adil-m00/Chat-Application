package com.example.chat_application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView  mScrollView;
    private TextView displayTextMessage;

    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;
    private FirebaseAuth firebaseAuth;

    private DatabaseReference userRef,groupNameRef,groupMessageKeyRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getStringExtra("groupName");

        firebaseAuth = FirebaseAuth.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid().toString();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference("Group").child(currentGroupName);



        initialize();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void saveMessageInfoToDatabase() {

        String Message = userMessageInput.getText().toString();
        String messageKey  =groupNameRef.push().getKey();

        if(Message.isEmpty())
        {
            userMessageInput.setError("Please Enter Message");
        }
        else
        {
            Calendar ccalfordata= Calendar.getInstance();
            SimpleDateFormat currentDateFormatio = new SimpleDateFormat("MM dd, yyyy");

            currentDate = currentDateFormatio.format(ccalfordata.getTime());


            Calendar calForTime= Calendar.getInstance();
            SimpleDateFormat currentTimeFormate = new SimpleDateFormat("hh:mm:ss a");

            currentTime = currentTimeFormate.format(calForTime.getTime());


            HashMap<String,Object> groupMessageKey= new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);
            groupMessageKeyRef = groupNameRef.child(messageKey);
            HashMap<String,Object> messageInfo = new HashMap<>();
            messageInfo.put("name",currentUserName);
            messageInfo.put("message",Message);
            messageInfo.put("date",currentDate);
            messageInfo.put("time",currentTime);
            groupMessageKeyRef.updateChildren(messageInfo);
        }
    }

    private void getUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initialize() {

        toolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displayTextMessage = findViewById(R.id.groupd_chat_text_display);
        mScrollView = findViewById(R.id.my_scrill_view);



    }



    private void DisplayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext())
        {
            String charDate = ((DataSnapshot)iterator.next()).getValue().toString();

            String message = ((DataSnapshot)iterator.next()).getValue().toString();
            String name = ((DataSnapshot)iterator.next()).getValue().toString();
            String time = ((DataSnapshot)iterator.next()).getValue().toString();

            displayTextMessage.append(name+" :\n"+ message + "\n"+time +"  "+charDate+"\n");

//            scrolldown

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}
