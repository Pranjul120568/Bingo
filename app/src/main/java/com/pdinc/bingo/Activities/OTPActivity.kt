package com.pdinc.bingo.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pdinc.bingo.R
import com.pdinc.bingo.databinding.ActivityOTPBinding
import java.util.concurrent.TimeUnit

const val phoneno="phoneNumber"
class OTPActivity : AppCompatActivity() {
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var auth: FirebaseAuth
    private var countdown: CountDownTimer? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mverificationId: String? = null
    private lateinit var code: String
    private var phoneNumber:String?=null
    lateinit var binding: ActivityOTPBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOTPBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        auth = Firebase.auth
        startVerify()
    }
    private fun initViews() {
        phoneNumber = intent.getStringExtra(phoneno).toString()
        //To enter the phone number just do one thing that extract string resource
        // of text entered and then in value put %s for getting string resource value
        binding.tvverify.text = getString(R.string.verify_number, phoneNumber)
        setSpannableString()
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                val smsCode = credential.smsCode
                if (!smsCode.isNullOrBlank()) {
                    binding.setcodeet.setText(smsCode)
                }
                signInWithPhoneAuthCredential(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if(e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
                // Show a message and update the UI
                // ...
            }
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Save verification ID and resending token so we can use them later
                mverificationId = verificationId
                resendToken = token
                // ...
            }
        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Login Success", "signInWithCredential:success")

                    val user = task.result?.user
                    startActivity(Intent(this, UserSetupActivity::class.java))
                    // [START_EXCLUDE]
                    // updateUI(STATE_SIGNIN_SUCCESS, user)
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    //  Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        //  binding.fieldVerificationCode.error = "Invalid code."
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    // updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }
    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_for_sms_to_send, phoneNumber))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                //Used to handle the clicks on the span
                startActivity(
                    Intent(this@OTPActivity, LoginActivity::class.java).setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        span.setSpan(clickableSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.messagesenttv.movementMethod = LinkMovementMethod.getInstance()
        binding.messagesenttv.text = span
    }

    private fun startVerify() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        showTimer(60000)
        PhoneAuthProvider.verifyPhoneNumber(options)
        binding.ConfirmOtpbtn.setOnClickListener {
            code = binding.setcodeet.text.toString()
            if (TextUtils.isEmpty(code)) {
                binding.verifyOtp.error = "Cannot be empty"
            } else {
                verifyPhoneNumberWithCode(mverificationId, code)
            }
        }
        binding.ResendOtp.setOnClickListener {
            resendVerificationCode(phoneNumber!!, resendToken!!)
        }
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun showTimer(milliSecInFuture: Long) {
        binding.ResendOtp.isEnabled = false
        countdown = object : CountDownTimer(milliSecInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.secondsremaintv.isVisible = true
                binding.secondsremaintv.text =
                    getString(
                        R.string.seconds_remaining,
                        millisUntilFinished / 1000
                    )
            }
            override fun onFinish() {
                binding.ResendOtp.isEnabled = true
                binding.secondsremaintv.isVisible = false
            }
        }.start()
    }
    override fun onBackPressed() {

    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
    }
}