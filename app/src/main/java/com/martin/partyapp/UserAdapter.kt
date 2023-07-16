package com.martin.partyapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAdapter(private val context: Context, private val userList: List<String>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    private lateinit var authUser: User
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val authUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val authUserRef = database.getReference("User/$authUserId")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userId = userList[position]
        val userRef = database.getReference("User/$userId")
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)!!
                    holder.bind(user)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("userId", userId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    private fun updateButtonActions(holder: UserViewHolder, user: User){
        when (context) {
            is ContactListActivity -> {
                when (context.userType) {
                    "followers" -> {
                        holder.buttonAction.setImageResource(R.drawable.unfollow_icon)
                        holder.buttonAction.setOnClickListener {
                            holder.removeFollowerAction(user)
                        }
                        holder.buttonAction2.visibility = View.GONE
                    }
                    "following" -> {
                        holder.buttonAction.setImageResource(R.drawable.unfollow_icon)
                        holder.buttonAction.setOnClickListener {
                            holder.removeFollowingAction(user)
                        }
                        holder.buttonAction2.visibility = View.GONE
                    }
                    "requests" -> {
                        //request
                        holder.buttonAction.setImageResource(R.drawable.following_icon)
                        holder.buttonAction.setOnClickListener {
                            holder.acceptFollowingRequestAction(user)
                        }
                        holder.buttonAction2.visibility = View.VISIBLE
                        holder.buttonAction2.setImageResource(R.drawable.unfollow_icon)
                        holder.buttonAction2.setOnClickListener {
                            holder.declineFollowingRequestAction(user)
                        }
                    }
                }
            }

            is UserListActivity -> {
                holder.buttonAction.setImageResource(R.drawable.following_icon)
                holder.buttonAction.setOnClickListener {
                    holder.addFollowingRequestAction(user)
                }
                holder.buttonAction2.visibility = View.GONE
            }

            is EventDescriptionActivity -> {
                //we have the authenticated User information's
                val authUserFollowingIds = authUser.following.map { it.userId }
                if (authUserFollowingIds.contains(user.userId)){
                    // the Auth User is following the user
                    holder.buttonAction.setImageResource(R.drawable.following_icon)
                    holder.buttonAction.setOnClickListener {
                        holder.removeFollowingAction(user)
                    }
                } else {
                    holder.buttonAction.setImageResource(R.drawable.follow_icon)
                    holder.buttonAction.setOnClickListener {
                        holder.addFollowingRequestAction(user)
                    }
                }
                holder.buttonAction2.visibility = View.GONE
            }
        }
    }


    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_username)
        private val profilePictureImage: ImageView = itemView.findViewById(R.id.image_profile_picture)

        val buttonAction: ImageButton = itemView.findViewById(R.id.button_user_action)
        val buttonAction2: ImageButton = itemView.findViewById(R.id.button_user_action_2)


        fun bind(user: User) {
            nameTextView.text = user.username
            Glide.with(itemView)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)

            authUserRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        authUser = snapshot.getValue(User::class.java)!!
                        updateButtonActions(this@UserViewHolder, user)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        }

        fun addFollowingRequestAction(user: User){
            val ref = database.getReference("User/${user.userId}")
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)
                        currentUser!!.addFollowingRequest(authUser)
                        ref.setValue(user)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        fun removeFollowerAction(user: User){
            authUser.removeFollower(user)
            authUserRef.setValue(authUser)

            val userRef = database.getReference("User/${user.userId}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)
                        currentUser!!.removeFollowing(authUser)
                        userRef.setValue(user)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        fun removeFollowingAction(user: User){
            authUser.removeFollowing(user)
            authUserRef.setValue(authUser)

            val userRef = database.getReference("User/${user.userId}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)
                        currentUser!!.removeFollower(authUser)
                        userRef.setValue(user)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        fun acceptFollowingRequestAction(user: User){
            authUser.acceptFollowingRequest(user)
            authUserRef.setValue(authUser)

            val userRef = database.getReference("User/${user.userId}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)
                        currentUser!!.addFollowing(authUser)
                        userRef.setValue(user)

                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        fun declineFollowingRequestAction(user: User){
            authUser.declineFollowingRequest(user)
            authUserRef.setValue(authUser)
        }
    }
}