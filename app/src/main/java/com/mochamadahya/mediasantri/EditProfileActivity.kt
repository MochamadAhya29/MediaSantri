package com.mochamadahya.mediasantri

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mochamadahya.mediasantri.fragment.ProfileFragment
import com.mochamadahya.mediasantri.model.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_register.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var cekInfoProfile= ""
    private var myUri = ""
    private var imageUri: Uri? = null
    private var storageProfilePictureRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val pergi = Intent(this@EditProfileActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(pergi)
            finish()
        }
        close.setOnClickListener {
            val go = Intent(this@EditProfileActivity, ProfileFragment::class.java)
            startActivity(go)
            finish()
        }
        ganti_foto_profile.setOnClickListener {
            cekInfoProfile = "clicked"

            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@EditProfileActivity)
        }
        btn_save.setOnClickListener {
            if (cekInfoProfile == "clicked"){
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }
        userInfo()

    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(set_fullname_profile.text.toString()) -> {
                Toast.makeText(this, "Silahkan Isi terlebih dahulu", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(set_username_profile.text.toString()) -> {
                Toast.makeText(this, "Silahkan Isi terlebih dahulu", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(set_bio_profile.text.toString()) -> {
                Toast.makeText(this, "Silahkan Isi terlebih dahulu", Toast.LENGTH_LONG).show()
            }

            else -> {
                val usersRef  = FirebaseDatabase.getInstance().reference
                    .child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = set_fullname_profile.text.toString().toLowerCase()
                userMap["userName"] = set_username_profile.text.toString().toLowerCase()
                userMap["bio"] = set_bio_profile.text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)
                Toast.makeText(this, "Info Profil sudah diperbaharui", Toast.LENGTH_LONG).show()

                val intent = Intent (this@EditProfileActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user  = p0.getValue<User>(User::class.java)

                    Glide.with(this@EditProfileActivity)
                        .load(user!!.image)
                        .into(foto_profile)
                    set_username_profile.setText(user.userName)
                    set_fullname_profile.setText(user.fullname)
                    set_bio_profile.setText(user.bio)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
            && data != null){
            val result= CropImage.getActivityResult(data)
            imageUri = result.uri
            foto_profile.setImageURI(imageUri)
        } else {

        }
    }

    private fun uploadImageAndUpdateInfo() {

        when {
            imageUri == null -> Toast.makeText(this, "Silahkan pilih gambar", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(set_fullname_profile.text.toString()) -> {
//                Toast.makeText(this, "Silahkan Isi terlebih dahulu", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(set_username_profile.text.toString()) -> {
//                Toast.makeText(this, "Silahkan Isi terlebih dahulu", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty (set_bio_profile.text.toString()) -> {
//                Toast.makeText(this, "Silahkan Isi terlebih dahulu", Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Pengaturan Akun")
                progressDialog.setMessage("Silahkan Tunggu, Sedang memperbaharui profil")
                progressDialog.show()

                val fileRef = storageProfilePictureRef!!.child(firebaseUser!!.uid + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful){
                        task.exception.let {it ->
//                            throw it!!
//                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->

                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUri = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = set_fullname_profile.text.toString()
                        userMap["username"] = set_username_profile.text.toString()
                        userMap["bio"] = set_bio_profile.text.toString()
                        userMap["image"] = myUri

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this, "Info Profil sudah diperbaharui" , Toast.LENGTH_LONG).show()
                        val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
//                        progressDialog.dismiss()
//                    } else {
//                        progressDialog.dismiss()
                    }
                })
            }
        }
    }
}