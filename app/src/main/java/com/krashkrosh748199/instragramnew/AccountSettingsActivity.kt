package com.krashkrosh748199.instragramnew

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.krashkrosh748199.instragramnew.Model.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import java.util.Locale

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl=""
    private var imageUri: Uri?=null
    private var storageProfilePicRef : StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef=FirebaseStorage.getInstance().reference.child("Profile Pictures")

        findViewById<Button>(R.id.logout_btn).setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity,SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.change_image_text_btn).setOnClickListener{
            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        findViewById<ImageView>(R.id.save_info_profile_btn).setOnClickListener {
            if(checker=="clicked"){

                uploadImageAndUpdateInfo()
            }
            else{
                updateUserInfoOnly()
            }
        }
        userInfo()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==Activity.RESULT_OK && data!=null ){
            val result=CropImage.getActivityResult(data)
            imageUri=result.uri
            findViewById<ImageView>(R.id.profile_image_view_profile_frag).setImageURI(imageUri)
        }
    }


    private fun updateUserInfoOnly() {

        when {
            TextUtils.isEmpty(findViewById<EditText>(R.id.full_name_profile_frag).text.toString()) -> {
                Toast.makeText(this,"Please write full name first",Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.username_profile_frag).text.toString()=="" -> {
                Toast.makeText(this,"Please write user name first",Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.bio_profile_frag).text.toString()=="" -> {
                Toast.makeText(this,"Please write your bio first",Toast.LENGTH_LONG).show()
            }
            else -> {
                val userRef= FirebaseDatabase.getInstance().reference.child("Users")
                val userMap=HashMap<String,Any>()
                userMap["fullname"] = findViewById<EditText>(R.id.full_name_profile_frag).text.toString().toLowerCase()
                userMap["username"] = findViewById<EditText>(R.id.username_profile_frag).text.toString().toLowerCase()
                userMap["bio"] = findViewById<EditText>(R.id.bio_profile_frag).text.toString().toLowerCase()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this,"Account Information has been updated successfully.",Toast.LENGTH_LONG).show()

                val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    private fun userInfo(){
        val userRef= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(pO: DataSnapshot) {

                if(pO.exists()){
                    val user=pO.getValue<User>(User::class.java)
                    val imageFrag=findViewById<ImageView>(R.id.profile_image_view_profile_frag)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageFrag)
                    findViewById<TextView>(R.id.username_profile_frag).setText(user!!.getUsername())
                    findViewById<TextView>(R.id.full_name_profile_frag).setText(user!!.getFullname())
                    findViewById<TextView>(R.id.bio_profile_frag).setText(user!!.getBio())

                }
            }

            override fun onCancelled(pO: DatabaseError) {

            }
        })
    }

    private fun uploadImageAndUpdateInfo() {

        when{
            imageUri==null->Toast.makeText(this,"please select image first",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(findViewById<EditText>(R.id.full_name_profile_frag).text.toString()) -> {
                Toast.makeText(this,"",Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.username_profile_frag).text.toString()=="" -> {
                Toast.makeText(this,"",Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.bio_profile_frag).text.toString()=="" -> {
                Toast.makeText(this,"",Toast.LENGTH_LONG).show()
            }

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait,we are updating your profile...")
                progressDialog.show()

                val fileRef=storageProfilePicRef!!.child(firebaseUser!!.uid + "jpg")
                var uploadTask:StorageTask<*>
                uploadTask=fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{ task ->
                    if (!task.isSuccessful)

                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri> { task ->

                    if(task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap=HashMap<String,Any>()
                        userMap["fullname"] = findViewById<EditText>(R.id.full_name_profile_frag).text.toString().toLowerCase()
                        userMap["username"] = findViewById<EditText>(R.id.username_profile_frag).text.toString().toLowerCase()
                        userMap["bio"] = findViewById<EditText>(R.id.bio_profile_frag).text.toString().toLowerCase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this,"Account Information has been updated successfully.",Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else{
                        progressDialog.dismiss()
                    }
                })
            }
        }

    }

}