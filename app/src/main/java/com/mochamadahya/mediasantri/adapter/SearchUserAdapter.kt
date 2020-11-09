package com.mochamadahya.mediasantri.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mochamadahya.mediasantri.R
import com.mochamadahya.mediasantri.fragment.ProfileFragment
import com.mochamadahya.mediasantri.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.list_item_search.view.*

class SearchUserAdapter (private var mContext: Context, private val mUser: List<User>,
                         private var isFragment : Boolean = false) :
    RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder>() {


    private var firebaseUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.list_item_search, parent, false)
        return SearchUserAdapter.SearchUserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val user = mUser [position]

        holder.userNameTxtView.text = user.userName
        holder.userFullnameTxtView.text = user.fullname
        Picasso.get()
            .load(user.image)
            .placeholder(R.drawable.profile)
            .into(holder.userProfile)


        user.uid?.let { cekFollowingStatus(it, holder.follow_btn) }


        holder.itemView.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.uid)
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, ProfileFragment()).commit()
        }

        holder.follow_btn.setOnClickListener {
            if (holder.follow_btn.text.toString() == "Follow"){

                firebaseUser?.uid.let { it ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.uid.toString())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                firebaseUser?.uid.let { it ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.uid.toString())
                                        .child("Followers").child(it.toString())
                                        .setValue(true).addOnCompleteListener {task ->
                                            if (task.isSuccessful){

                                            }
                                        }
                                }
                            }
                        }
                }
            } else {
                firebaseUser?.uid.let { it ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.uid.toString())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                firebaseUser?.uid.let { it ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.uid.toString())
                                        .child("Followers").child(it.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful){

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }


    }
    class SearchUserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView){

        val userNameTxtView: TextView = itemView.findViewById(R.id.user_name_search)
        val userFullnameTxtView: TextView = itemView.findViewById(R.id.user_fullname_search)
        val userProfile: CircleImageView = itemView.findViewById(R.id.image_search)
        val follow_btn: Button = itemView.findViewById(R.id.btn_search_follow)

    }


    private fun cekFollowingStatus(uid: String, followBtn: Button) {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.child(uid).exists()){
                    followBtn.text = "Following"
                } else {
                    followBtn.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}

