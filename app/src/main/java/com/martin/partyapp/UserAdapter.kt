package com.martin.partyapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAdapter(private val context: Context, private val userList: List<String>, private val authUser: User) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val authUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val authUserRef = database.getReference("User/$authUserId")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false)
        view.visibility = View.GONE
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

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_username)
        private val profilePictureImage: ImageView = itemView.findViewById(R.id.image_profile_picture)

        val buttonAction: Button = itemView.findViewById(R.id.button_user_action)
        val buttonAction2: Button = itemView.findViewById(R.id.button_user_action_2)

        fun bind(user: User) {
            nameTextView.text = user.username
            Glide.with(itemView)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)

            updateButtonActions(user)
            itemView.visibility = View.VISIBLE
        }

        private fun updateButtonActions(user: User){
            val authUserFollowingIds: List<String> = authUser.following.map { it.userId!! }

            when (context) {
                is ContactListActivity -> {
                    when (context.userType) {
                        "followers" -> {
                            //holder.buttonAction.setImageResource(R.drawable.unfollow_icon)
                            buttonAction.visibility = View.VISIBLE
                            buttonAction.text = "Remove"
                            buttonAction.setOnClickListener {
                                removeFollowerPopup(user)
                            }
                            if (authUserFollowingIds.contains(user.userId)){
                                buttonAction2.visibility = View.GONE
                            } else {
                                buttonAction2.visibility = View.VISIBLE

                                val userRequestsIds = user.requests.map {it.userId}
                                if (userRequestsIds.contains(authUserId)){
                                    buttonAction2.text = "Request send"
                                } else {
                                    buttonAction2.text = "Follow"
                                    buttonAction2.setOnClickListener {
                                        addFollowingRequestAction(user)
                                        buttonAction2.text = "Request send"
                                    }
                                }
                            }
                        }
                        "following" -> {
                            //holder.buttonAction.setImageResource(R.drawable.unfollow_icon)
                            buttonAction.visibility = View.VISIBLE
                            buttonAction.text = "Followed"
                            buttonAction.setOnClickListener {
                                removeFollowingPopup(user)
                                //holder.removeFollowingAction(user)
                            }
                            buttonAction2.visibility = View.GONE
                        }
                        "requests" -> {
                            //request
                            //holder.buttonAction.setImageResource(R.drawable.following_icon)
                            buttonAction.visibility = View.VISIBLE
                            buttonAction.text = "Accept"
                            buttonAction.setOnClickListener {
                                acceptFollowingRequestAction(user)
                            }
                            buttonAction2.visibility = View.VISIBLE
                            buttonAction2.text = "Decline"
                            //holder.buttonAction2.setImageResource(R.drawable.unfollow_icon)
                            buttonAction2.setOnClickListener {
                                declineFollowingRequestAction(user)
                            }
                        }
                    }
                }

                is UserListActivity -> {
                    //holder.buttonAction.setImageResource(R.drawable.following_icon)
                    buttonAction.visibility = View.VISIBLE
                    val userRequestIds = user.requests.map { it.userId }
                    if (userRequestIds.contains(authUser.userId)){
                        buttonAction.text = "Request send"
                    } else {
                        if (authUserFollowingIds.contains(user.userId)){
                            buttonAction.text = "Followed"
                            buttonAction.setOnClickListener {
                                removeFollowingPopup(user)
                            }
                        } else {
                            buttonAction.text = "Follow"
                            buttonAction.setOnClickListener {
                                buttonAction.text = "Request send"
                                addFollowingRequestAction(user)
                            }
                        }
                    }
                    buttonAction2.visibility = View.GONE
                }

                is EventDescriptionActivity -> {
                    //we have the authenticated User information's
                    buttonAction.visibility = View.VISIBLE



                    if (authUserFollowingIds.contains(user.userId)){
                        // the Auth User is following the user
                        //holder.buttonAction.setImageResource(R.drawable.following_icon)
                        buttonAction.text = "Followed"
                        buttonAction.setOnClickListener {
                            removeFollowingPopup(user)
                        }
                    } else {
                        val userRequestsIds = user.requests.map {it.userId}
                        if (userRequestsIds.contains(authUserId)){
                            buttonAction.text = "Request send"
                        } else {
                            buttonAction.text = "Follow"
                            buttonAction.setOnClickListener {
                                addFollowingRequestAction(user)
                                buttonAction.text = "Request send"
                            }
                        }
                    }
                    buttonAction2.visibility = View.GONE
                }
            }
        }
        fun addFollowingRequestAction(user: User){
            val userRef = database.getReference("User/${user.userId}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)
                        val updateData = HashMap<String, Any>()
                        currentUser!!.addFollowingRequest(authUser)
                        updateData["requests"] = currentUser.requests
                        userRef.updateChildren(updateData)
                            .addOnSuccessListener {
                                notifyDataSetChanged()
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun removeFollowerAction(user: User){
            val updateData = HashMap<String, Any>()
            authUser.removeFollower(user)
            updateData["followers"] = authUser.followers
            authUserRef.updateChildren(updateData)

            val userRef = database.getReference("User/${user.userId}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)!!
                        val updateData = HashMap<String, Any>()
                        currentUser.removeFollowing(authUser)
                        updateData["following"] = currentUser.following
                        userRef.updateChildren(updateData)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun removeFollowingAction(user: User){
            val updateData = HashMap<String, Any>()
            authUser.removeFollowing(user)
            updateData["following"] = authUser.following
            authUserRef.updateChildren(updateData)

            val userRef = database.getReference("User/${user.userId}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)!!
                        val updateData = HashMap<String, Any>()
                        currentUser.removeFollower(authUser)
                        updateData["followers"] = currentUser.followers
                        userRef.updateChildren(updateData)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun acceptFollowingRequestAction(user: User){
            val updateData = HashMap<String, Any>()
            authUser.acceptFollowingRequest(user)
            updateData["followers"] = authUser.followers
            updateData["requests"] = authUser.requests
            authUserRef.updateChildren(updateData)

            val userRef = database.getReference("User/${user.userId}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val currentUser = snapshot.getValue(User::class.java)!!
                        val updateData = HashMap<String, Any>()
                        currentUser.addFollowing(authUser)
                        updateData["following"] = currentUser.following
                        userRef.updateChildren(updateData)
                            .addOnSuccessListener {
                                if (!authUser.following.contains(currentUser)){
                                    buttonAction.visibility = View.GONE
                                    buttonAction2.visibility = View.VISIBLE
                                    buttonAction2.text = "Follow back"
                                    buttonAction2.setOnClickListener {
                                        addFollowingRequestAction(user)
                                        buttonAction2.text = "Request send"
                                    }
                                }
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun declineFollowingRequestAction(user: User){
            val updateData = HashMap<String, Any>()
            authUser.declineFollowingRequest(user)
            updateData["requests"] = authUser.requests
            authUserRef.updateChildren(updateData)
        }

        private fun removeFollowingPopup(user: User){
            val dialogBuilder = AlertDialog.Builder(context)
            val popupView = LayoutInflater.from(context).inflate(R.layout.unfollow_popup, null)

            val userNameText = popupView.findViewById<TextView>(R.id.text_username)
            userNameText.text = user.username

            val profilePictureImage = popupView.findViewById<ImageView>(R.id.image_profile_picture)
            Glide.with(itemView)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)

            dialogBuilder.setView(popupView)
            val alertDialog = dialogBuilder.create()

            val unfollowButton: Button = popupView.findViewById(R.id.button_unfollow)
            unfollowButton.setOnClickListener {
                removeFollowingAction(user)
                alertDialog.dismiss()
            }

            val cancelButton: Button = popupView.findViewById(R.id.button_cancel)
            cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
        }
        private fun removeFollowerPopup(user:User){
            val dialogBuilder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            val popupView = LayoutInflater.from(context).inflate(R.layout.unfollow_popup, null)

            val userNameText = popupView.findViewById<TextView>(R.id.text_username)
            userNameText.text = user.username

            val profilePictureImage = popupView.findViewById<ImageView>(R.id.image_profile_picture)
            Glide.with(itemView)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)

            dialogBuilder.setView(popupView)
            val alertDialog = dialogBuilder.create()

            val unfollowButton: Button = popupView.findViewById(R.id.button_unfollow)
            unfollowButton.setOnClickListener {
                removeFollowerAction(user)
                alertDialog.dismiss()
            }

            val cancelButton: Button = popupView.findViewById(R.id.button_cancel)
            cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
        }
    }
}
/*
private fun showItemPopUp(item: Item){
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
    val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.event_item_popup_layout, null)

    val itemNameText = popupView.findViewById<TextView>(R.id.text_item_name)
    itemNameText.text = item.itemName!!

    val itemInfoAdapter = EventItemInfoAdapter(requireContext(), item.itemUserQuantityList)

    val eventItemInfoRecyclerView = popupView.findViewById<RecyclerView>(R.id.item_info_recycler_view)
    eventItemInfoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    eventItemInfoRecyclerView.adapter = itemInfoAdapter

    bottomSheetDialog.setContentView(popupView)
    bottomSheetDialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundResource(R.drawable.custom_bottom_sheet_dialog_background)
    bottomSheetDialog.show()
}

 */