package com.pdinc.bingo.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.pdinc.bingo.Modals.User
import com.pdinc.bingo.R
import com.pdinc.bingo.databinding.ActivityMainBinding
import java.util.concurrent.Executor

const val UID="uid"
const val Name="name"
const val Image="image"
class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private val mCurrentId by lazy {
        FirebaseAuth.getInstance().uid
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.hostRmBtn.setOnClickListener {
            startActivity(Intent(this, hostActivity::class.java))
            finish()
        }
        binding.joinRmBtn.setOnClickListener {
            inputRoomid()
        }
       // binding.nameTv.text=
    }

    private fun inputRoomid(): Dialog {
        return this.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = MainActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_joinroom, null))
                // Add action buttons
                .setPositiveButton("JOIN") { _, _ ->
                    startActivity(Intent(this, GameActivity::class.java))
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    }