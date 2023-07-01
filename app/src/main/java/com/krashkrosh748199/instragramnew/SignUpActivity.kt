package com.krashkrosh748199.instragramnew

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        findViewById<Button>(R.id.singin_link_btn).setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
        }
        
        findViewById<Button>(R.id.signup_btn).setOnClickListener { 
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        
        val fullName = findViewById<EditText>(R.id.fullname_signup).text.toString()
        val userName = findViewById<EditText>(R.id.username_signup).text.toString()
        val email = findViewById<EditText>(R.id.email_signup).text.toString()
        val password = findViewById<EditText>(R.id.password_signup).text.toString()
        
        when{
            TextUtils.isEmpty(fullName)-> Toast.makeText(this,"full name is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName)-> Toast.makeText(this,"user name is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email)-> Toast.makeText(this,"email is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password)-> Toast.makeText(this,"password name is required",Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog=ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("signUp")
                progressDialog.setMessage("please wait,this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                
                val mAuth:FirebaseAuth=FirebaseAuth.getInstance()
                
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{task ->
                        if(task.isSuccessful){
                            saveUserInfo(fullName,userName,email,progressDialog)
                        }
                        else{
                            val message= task.exception!!.toString()
                            Toast.makeText(this,"Error:$message",Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
            
        }
        

    }

    private fun saveUserInfo(fullName: String, userName: String, email: String,progressDialog:ProgressDialog) {
           
        val currentUserID= FirebaseAuth.getInstance().currentUser!!.uid
        val userRef:DatabaseReference=FirebaseDatabase.getInstance().reference.child("Users")
        val userMap=HashMap<String,Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "hey i am Native Android Developer."
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-new-bdcf2.appspot.com/o/Default%20images%2Fprofile.png?alt=media&token=8f8714d2-28b1-4b21-89ad-a0a104462d3d"
        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener {
                task ->

                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this,"Account has been created successfully.",Toast.LENGTH_LONG).show()

                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(currentUserID)
                            .child("Following").child(currentUserID)
                            .setValue(true)

                    val intent = Intent(this@SignUpActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message= task.exception!!.toString()
                    Toast.makeText(this,"Error:$message",Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()

                }
            }



    }
}