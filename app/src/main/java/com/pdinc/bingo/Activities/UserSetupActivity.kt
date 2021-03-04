package com.pdinc.bingo.Activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.pdinc.bingo.Modals.User
import com.pdinc.bingo.databinding.ActivityUserSetupBinding

class UserSetupActivity : AppCompatActivity() {
    private val storage by lazy {
        Firebase.storage
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var downloadUri: String
    private lateinit var binding: ActivityUserSetupBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityUserSetupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.profileimage.setOnClickListener {
            checkPermissionForImage()
        }
        binding.saveBtn.setOnClickListener {
            val name = binding.nameedt.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this,"NickName not provided!", Toast.LENGTH_SHORT).show()
            } else {
                binding.saveBtn.isEnabled = true
                val user = User(name, downloadUri, auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
    override fun onBackPressed() {

    }
    private fun checkPermissionForImage() {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED &&
            (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))== PackageManager.PERMISSION_DENIED){
            val permission= arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionwrite= arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(
                permission,
                1001
            )
            requestPermissions(
                permissionwrite,1002
            )
        }
        else{
            pickImageFromGallery()
        }
    }
    private fun pickImageFromGallery() {
        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        startActivityForResult(
            intent,1000
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode==1000){
            data?.data.let {
                binding.profileimage.setImageURI(it)
                if (it != null) {
                    uploadImage(it)
                }
            }
        }
    }
    //uploading image to firebase
    private fun uploadImage(it: Uri) {
        binding.saveBtn.isEnabled=false
        val ref=storage.reference.child("uploads/"+auth.uid.toString())
        val uploadtask=ref.putFile(it)
        uploadtask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if(task.isSuccessful){
                task.exception.let {
                    throw it!!
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener{ task ->
            binding.saveBtn.isEnabled=true
            if(task.isSuccessful){
                downloadUri=task.result.toString()
                Log.i("URL","downloadurl: $downloadUri")
            }
        }
    }
}