package com.mochamadahya.mediasantri

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mochamadahya.mediasantri.fragment.HomeFragment
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUrl: Uri? = null
    private var storagePostPictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPictureRef = FirebaseStorage.getInstance().reference.child("Post Picture")

        CropImage.activity()
            .setAspectRatio(4,3) //ukuran yang mau di crop
            .start(this@AddPostActivity)

        ok_add.setOnClickListener {
            uploadImagePost()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
            && data!= null){
            val result = CropImage.getActivityResult(data)
            imageUrl = result.uri
            image_post.setImageURI(imageUrl)
        }
    }
    private fun uploadImagePost() {
        when{
            imageUrl == null -> Toast.makeText(this,"Pilih....",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(deskripsi_post.text.toString()) -> Toast.makeText(this,"hdhavdh",Toast.LENGTH_SHORT).show()

            else -> {
                val progressDialog = ProgressDialog(this@AddPostActivity)
                progressDialog.setTitle("Post")
                progressDialog.setMessage("sabar")
                progressDialog.show()

                val fileRef = storagePostPictureRef!!.child(System.currentTimeMillis().toString()+ ".jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUrl!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>> { task ->
                    if (!task.isSuccessful){
                        task.exception.let {
                            throw it!!
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri>{  task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = postRef.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postid"]       = postId!!
                        postMap["description"]  = deskripsi_post.text.toString()
                        postMap["publisher"]    = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"]    = myUrl

                        postRef.child(postId).updateChildren(postMap)

                        Toast.makeText(this,"Post berhasil",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

}