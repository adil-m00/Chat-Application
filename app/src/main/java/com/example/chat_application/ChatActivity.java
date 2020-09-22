package com.example.chat_application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID,messageReceiverName,messageRecieverImage,messageSenderID;

    private TextView userName,userLastSeen;
    private CircleImageView userImage;


    private Toolbar toolbar;

    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;

    private FirebaseAuth mAuth;

    private DatabaseReference RootRef;
    private List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime,saveCurrentDate;

    private String chacker ="",myUri="";

    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid().toString();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID  = getIntent().getStringExtra("visit_user_id");
        messageReceiverName  = getIntent().getStringExtra("visit_user_name");
        messageRecieverImage = getIntent().getStringExtra("visit_user_image");

        InitializeControlers();



        userName.setText(messageReceiverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profile).into(userImage);


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        publicLastSeen();



        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                          "Images",
                          "PDF Files",
                          "MS Word Files"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0)
                        {
                            chacker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                        }
                        if(which==1)
                        {
                            chacker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF file"),438);
                        }
                        if(which==2)
                        {
                            chacker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select Md Word File"),438);
                        }
                    }
                });
                builder.show();

            }
        });



        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot,String s) {


                String from = dataSnapshot.child("from").getValue().toString();
                String message = dataSnapshot.child("message").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();
                String to = dataSnapshot.child("to").getValue().toString();
                String messageID = dataSnapshot.child("messageID").getValue().toString();
                String time = dataSnapshot.child("time").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                String name = "";

                Messages messages = new Messages(from,message,type,to,messageID,time,date,name);


                messageList.add(messages);

                messagesAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    private void InitializeControlers() {



        toolbar = findViewById(R.id.chat_toolber);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =  layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);



        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        sendMessageButton = findViewById(R.id.send_messagE_btn);
        messageInputText = findViewById(R.id.input_message);
        sendFilesButton = findViewById(R.id.send_files_btn);

        messagesAdapter = new MessagesAdapter(messageList);
        userMessagesList = findViewById(R.id.private_message_list_of_users);
        userMessagesList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messagesAdapter);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat curentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = curentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please Wait, we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

                fileUri = data.getData();
                if(!chacker.equals("image"))
                {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");


                    final String messageSenderRef = "Messages/"+messageSenderID+"/"+messageReceiverID;
                    final String messageReceiverRef = "Messages/"+messageReceiverID+"/"+messageSenderID;

                    DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                            .child(messageSenderID).child(messageReceiverID).push();

                    final String messagePushId = userMessageKeyRef.getKey();

                    final StorageReference filePath = storageReference.child(messagePushId+"."+chacker);

                    filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            task.addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    String uril = o.toString();


                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message",uril);
                                    messageTextBody.put("name",fileUri.getLastPathSegment());
                                    messageTextBody.put("type",chacker);
                                    messageTextBody.put("from",messageSenderID);
                                    messageTextBody.put("to",messageReceiverID);
                                    messageTextBody.put("messageID",messagePushId);
                                    messageTextBody.put("time",saveCurrentTime);
                                    messageTextBody.put("date",saveCurrentDate);

                                    Map messageBodyDetail = new HashMap();
                                    messageBodyDetail.put(messageSenderRef+"/"+messagePushId,messageTextBody);

                                    messageBodyDetail.put(messageReceiverRef+"/"+messagePushId,messageTextBody);


                                    RootRef.updateChildren(messageBodyDetail);
                                    loadingBar.dismiss();


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }
                else if(chacker.equals("image"))
                {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");


                    final String messageSenderRef = "Messages/"+messageSenderID+"/"+messageReceiverID;
                    final String messageReceiverRef = "Messages/"+messageReceiverID+"/"+messageSenderID;

                    DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                            .child(messageSenderID).child(messageReceiverID).push();

                    final String messagePushId = userMessageKeyRef.getKey();

                    final StorageReference filePath = storageReference.child(messagePushId+"."+"jpg");



                    StorageReference filePaths = storageReference.child(messagePushId + ".jpg");
                    filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            task.addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    String uril = o.toString();


                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message",uril);
                                    messageTextBody.put("name",fileUri.getLastPathSegment());
                                    messageTextBody.put("type",chacker);
                                    messageTextBody.put("from",messageSenderID);
                                    messageTextBody.put("to",messageReceiverID);
                                    messageTextBody.put("messageID",messagePushId);
                                    messageTextBody.put("time",saveCurrentTime);
                                    messageTextBody.put("date",saveCurrentDate);

                                    Map messageBodyDetail = new HashMap();
                                    messageBodyDetail.put(messageSenderRef+"/"+messagePushId,messageTextBody);

                                    messageBodyDetail.put(messageReceiverRef+"/"+messagePushId,messageTextBody);



                                    RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful())
                                            {
                                                loadingBar.dismiss();
                                             }
                                            else
                                            {
                                                loadingBar.dismiss();
                                                Toast.makeText(ChatActivity.this, "message not sent", Toast.LENGTH_SHORT).show();
                                            }
                                            messageInputText.setText("");
                                        }
                                    });

                                }
                            });
                        }
                    });


                }
                else
                {
                    loadingBar.dismiss();
                    Toast.makeText(this, "nothing selected, Error", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void sendMessage()
    {
        String messageText = messageInputText.getText().toString();

        if(messageText.isEmpty())
        {
            messageInputText.setError("Please First write your message");
            return;
        }
        else
        {
            String messageSenderRef = "Messages/"+messageSenderID+"/"+messageReceiverID;
            String messageReceiverRef = "Messages/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(messageSenderRef+"/"+messagePushId,messageTextBody);

            messageBodyDetail.put(messageReceiverRef+"/"+messagePushId,messageTextBody);



            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "message not sent", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });
        }
    }

    private void publicLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if(state.equals("online"))
                            {
                                userLastSeen.setText("Online");

                            }
                            else if(state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: "+"\n" + date +" " + time);


                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}


