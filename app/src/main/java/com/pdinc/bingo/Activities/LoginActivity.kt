package com.pdinc.bingo.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pdinc.bingo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var countryCode:String
    private lateinit var phoneNumber:String
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.phoneNoEt.addTextChangedListener {
            if(it!=null){
                binding.nextBtn.isEnabled=it.toString().length==10
            }
        }
        binding.nextBtn.setOnClickListener {
         checkNumber()
        }

    }

    private fun checkNumber() {
        countryCode=binding.ccp.selectedCountryCodeWithPlus
        phoneNumber=countryCode + binding.phoneNoEt.text.toString()
        confirmPhoneNumber()
    }

    private fun confirmPhoneNumber() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("Please confirm your Phone number $phoneNumber\n"
                    +"Press OK to proceed or Edit to edit the number")
            setPositiveButton("OK"){_,_ ->
                showOtpActivity()
            }
            setNegativeButton("EDIT"){ dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this,OTPActivity::class.java).putExtra(phoneno,phoneNumber))
        finish()
    }
}