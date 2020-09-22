package com.example.chat_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID,current_stats,currentUserId;

    private CircleImageView userProfileIage;
    private TextView userName,userStatus;
    private Button sendMessageRequestButton,declineMessageRequestButton;

    private DatabaseReference UserRef,chatRequestRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        recieverUserID = getIntent().getStringExtra("visit_user_id");

        UserRef = FirebaseDatabase.getInstance().getReference("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference("Contacts");

        notificationRef = FirebaseDatabase.getInstance().getReference("Notifications");

        userProfileIage = findViewById(R.id.visit_profile_image);
        userName = findViewById(R.id.visit_user_name);
        userStatus = findViewById(R.id.visit_user_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);

        current_stats  = "new";

        currentUserId = mAuth.getCurrentUser().getUid().toString();
        retrieveUserInfo();

    }

    private void retrieveUserInfo() {

        UserRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("image").exists())
                {
                    String image = dataSnapshot.child("image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();


                    userName.setText(name);
                    userStatus.setText(status);
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(userProfileIage);

                    manageRequest();

                }
                else if(dataSnapshot.child("name").exists())
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();

                    userName.setText(name);
                    userStatus.setText(status);

                    manageRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageRequest() {

        chatRequestRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(recieverUserID))
                {
                    String request_type = dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();

                    if(request_type.equals("sent"))
                    {
                        current_stats = "request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }
                    else if(request_type.equals("received"))
                    {
                        current_stats = "request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");
                        declineMessageRequestButton.setVisibility(View.VISIBLE);

                        declineMessageRequestButton.setEnabled(true);

                        declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });

                    }


                }
                else
                {
                    contactsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(recieverUserID))
                        {
                            current_stats = "friends";
                            sendMessageRequestButton.setText("Remove Contact");

                        }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!currentUserId.equals(recieverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if(current_stats.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_stats.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }

                    if(current_stats.equals("request_received"))
                    {
                        acceptChatRequest();
                    }
                    if(current_stats.equals("friends"))
                    {
                        removeSpecificContact();
                    }
                }
            });
        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }


    }


    private void removeSpecificContact() {
        contactsRef.child(currentUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    contactsRef.child(recieverUserID).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendMessageRequestButton.setEnabled(true);
                                current_stats = "new";
                                sendMessageRequestButton.setText("Send Message");
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest() {
        contactsRef.child(currentUserId).child(recieverUserID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    contactsRef.child(recieverUserID).child(currentUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                chatRequestRef.child(currentUserId).child(recieverUserID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    chatRequestRef.child(recieverUserID).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            sendMessageRequestButton.setEnabled(true);
                                                            current_stats = "friends";
                                                            sendMessageRequestButton.setText("Remove Contact");
                                                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                            declineMessageRequestButton.setEnabled(false);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelChatRequest() {

        chatRequestRef.child(currentUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    chatRequestRef.child(recieverUserID).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            sendMessageRequestButton.setEnabled(true);
                            current_stats = "new";
                            sendMessageRequestButton.setText("Send Message");
                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                            declineMessageRequestButton.setEnabled(false);
                        }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        chatRequestRef.child(currentUserId).child(recieverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    chatRequestRef.child(recieverUserID).child(currentUserId).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful())
                               {

                                   HashMap<String,String> chatNotificationMap = new HashMap<>();
                                   chatNotificationMap.put("from",currentUserId);
                                   chatNotificationMap.put("type","request");

                                   notificationRef.child(recieverUserID).push()
                                           .setValue(chatNotificationMap)
                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if(task.isSuccessful())
                                                   {
                                                       sendMessageRequestButton.setEnabled(true);
                                                       current_stats = "request_sent";
                                                       sendMessageRequestButton.setText("Cancel Chat Request");

                                                   }
                                               }
                                           });


                                   }
                                }
                            });
                }
            }
        });


    }
}
