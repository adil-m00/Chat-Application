package com.example.chat_application;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    private View RequestFramgentView;
    private RecyclerView myRequestList;
    private DatabaseReference ChatRequestRef,UsersRef,contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFramgentView =  inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid().toString();

        contactsRef = FirebaseDatabase.getInstance().getReference("Contacts");
        UsersRef = FirebaseDatabase.getInstance().getReference("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference("Chat Requests");
        myRequestList = RequestFramgentView.findViewById(R.id.recyclerView);
        myRequestList.setHasFixedSize(true);
        myRequestList.setLayoutManager(new LinearLayoutManager(RequestFramgentView.getContext()));



        return RequestFramgentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int i, @NonNull Contacts model) {
                        holder.acceptRequest.setVisibility(View.VISIBLE);
                        holder.cancelRequest.setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(i).getKey().toString();

                        DatabaseReference getTypRef = getRef(i).child("request_type").getRef();


                        getTypRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();
                                    if(type.equals("received"))
                                    {
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                     String image = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.profileImage);
                                                }
                                                final String username = dataSnapshot.child("name").getValue().toString();
                                                String userstatus = dataSnapshot.child("status").getValue().toString();
                                                holder.name.setText(username);
                                                holder.status.setText("Wants To Connect With You");


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(RequestFramgentView.getContext());
                                                        builder.setTitle(username + " Chat request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if(which==0)
                                                                {
                                                                    contactsRef.child(currentUserId).child(list_user_id).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                contactsRef.child(list_user_id).child(currentUserId).child("Contacts").setValue("Saved")
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    ChatRequestRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                ChatRequestRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        Toast.makeText(RequestFramgentView.getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                                if(which==1)
                                                                {

                                                                                        ChatRequestRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    ChatRequestRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            Toast.makeText(RequestFramgentView.getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });



                                                                }
                                                            }
                                                        });

                                                        builder.show();

                                                    }


                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }



                                    else if(type.equals("sent"))
                                    {
                                        Button request_sent_button = holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_sent_button.setText("Request Sent");
                                        holder.cancelRequest.setVisibility(View.INVISIBLE);


                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                    String image = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.profileImage);
                                                }
                                                final String username = dataSnapshot.child("name").getValue().toString();
                                                String userstatus = dataSnapshot.child("status").getValue().toString();
                                                holder.name.setText(username);
                                                holder.status.setText("You have sent a request to "+username);


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(RequestFramgentView.getContext());
                                                        builder.setTitle("Already Sent Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {



                                                                    ChatRequestRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                ChatRequestRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        Toast.makeText(RequestFramgentView.getContext(), "You have cancel friend Request ", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });




                                                            }
                                                        });

                                                        builder.show();

                                                    }


                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });




                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        RequestViewHolder requestViewHolder = new RequestViewHolder(view);
                        return requestViewHolder;


                    }
                };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class  RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView name,status;
        private CircleImageView profileImage;
        private Button acceptRequest,cancelRequest;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.user_profile_name);
            status = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptRequest = itemView.findViewById(R.id.request_accept_btn);
            cancelRequest = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
