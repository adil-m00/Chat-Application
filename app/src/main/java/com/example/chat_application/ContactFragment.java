package com.example.chat_application;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactFragment extends Fragment {


    View contactsView;

    private RecyclerView myContactList;

    private DatabaseReference contactsReference,usersRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    public ContactFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView = inflater.inflate(R.layout.fragment_contact, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid().toString();


        usersRef  = FirebaseDatabase.getInstance().getReference("Users");
        contactsReference = FirebaseDatabase.getInstance().getReference("Contacts").child(currentUserId);
        myContactList = contactsView.findViewById(R.id.contacts_list);
        myContactList.setHasFixedSize(true);
        myContactList.setLayoutManager(new LinearLayoutManager(contactsView.getContext()));

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsReference,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter
                 = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts demo) {
                    String userIDS = getRef(i).getKey().toString();
                    usersRef.child(userIDS).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if(dataSnapshot.exists())
                           {

                               if(dataSnapshot.child("userState").hasChild("state"))
                               {
                                   String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                   String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                   String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                   if(state.equals("online"))
                                   {
                                       holder.onlineIcon.setVisibility(View.VISIBLE);

                                   }
                                   else if(state.equals("offline"))
                                   {
                                       holder.onlineIcon.setVisibility(View.INVISIBLE);
                                   }
                               }
                               else
                               {
                                   holder.onlineIcon.setVisibility(View.INVISIBLE);

                               }


                               if(dataSnapshot.hasChild("image"))
                               {
                                   String ssImage = dataSnapshot.child("image").getValue().toString();
                                   String username = dataSnapshot.child("name").getValue().toString();
                                   String userstatus = dataSnapshot.child("status").getValue().toString();

                                   holder.name.setText(username);
                                   holder.status.setText(userstatus);
                                   Picasso.get().load(ssImage).placeholder(R.drawable.profile).into(holder.profileImage);
                               }
                               else
                               {
                                   String username = dataSnapshot.child("name").getValue().toString();
                                   String userstatus = dataSnapshot.child("status").getValue().toString();

                                   holder.name.setText(username);
                                   holder.status.setText(userstatus);

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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);

                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        private TextView name,status;
        private CircleImageView profileImage;
        private ImageView onlineIcon;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.user_profile_name);
            status = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
