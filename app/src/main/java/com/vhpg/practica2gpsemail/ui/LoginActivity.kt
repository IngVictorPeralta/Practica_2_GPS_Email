package com.vhpg.practica2gpsemail.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.vhpg.practica2gpsemail.R
import com.vhpg.practica2gpsemail.databinding.ActivityLoginBinding
import com.vhpg.practica2gpsemail.util.Constants
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.concurrent.Executor


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //Para el lector biométrico
    private var banderaLectorHuella = true //el disp. cuenta con lector
    private var ingresoConHuella = false
    private var textoErrorLectorHuella = "" //para los mensajes de error
    private lateinit var biometricManager: androidx.biometric.BiometricManager
    private lateinit var executor: Executor //Lo requiere el prompt biométrico

    //Para Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    //SharedPreferences Encriptadas:
    private lateinit var encryptedSharedPreferences: EncryptedSharedPreferences
    private lateinit var encryptedSharedPrefsEditor: SharedPreferences.Editor

    //para la autenticacion con huella activa
    private var usuarioSp: String? = ""
    private var contraseniaSp: String? = ""

    //cajas de texto
    private var email  = ""
    private var contrasenia = ""

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)


        biometricManager = BiometricManager.from(this)
        executor = ContextCompat.getMainExecutor(this)

        firebaseAuth = FirebaseAuth.getInstance()

        try {
            //Creando la llave para encriptar
            val masterKeyAlias = MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedSharedPreferences = EncryptedSharedPreferences
                .create(
                    this,
                    Constants.account,
                    masterKeyAlias,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ) as EncryptedSharedPreferences
        }catch(e: GeneralSecurityException){
            e.printStackTrace()
            Log.d(Constants.LOGTAG, "${Constants.error} ${e.message}")
        }catch (e: IOException){
            e.printStackTrace()
            Log.d(Constants.LOGTAG, "${Constants.error} ${e.message}")
        }

        encryptedSharedPrefsEditor = encryptedSharedPreferences.edit()

        usuarioSp = encryptedSharedPreferences.getString("${Constants.usuarioSp}","0")
        contraseniaSp = encryptedSharedPreferences.getString("${Constants.contraseniaSp}","0")


        when(biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)){
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(Constants.LOGTAG, getString(R.string.biometric_active))
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                banderaLectorHuella = false
                textoErrorLectorHuella = "${R.string.not_sensor}"
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                banderaLectorHuella = false
                textoErrorLectorHuella = "${R.string.not_sensor_now}"
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                banderaLectorHuella = false
                textoErrorLectorHuella = getString(R.string.fingerprint_before)
            }
        }

        binding.ibtnHuella.setOnClickListener{
            if(usuarioSp == "0") {
                Toast.makeText(
                    this,
                    getString(R.string.not_user_fingerprint),
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                showBiometricPrompt()
            }
        }

        binding.btnLogin.setOnClickListener{
            if(!validaCampos()) return@setOnClickListener

            binding.progressBar.visibility = View.VISIBLE

            //Autenticando al usuario

            autenticaUsuario(email,contrasenia)
        }

        binding.btnRegistrarse.setOnClickListener{
            if(!validaCampos()) return@setOnClickListener

            binding.progressBar.visibility = View.VISIBLE

            //Registrando al usuario
            firebaseAuth.createUserWithEmailAndPassword(email, contrasenia).addOnCompleteListener { authResult->
                if(authResult.isSuccessful){
                    //Enviar correo para verificación de email
                    var user_fb = firebaseAuth.currentUser
                    user_fb?.sendEmailVerification()?.addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.sended_email), Toast.LENGTH_SHORT).show()
                    }?.addOnFailureListener {
                        Toast.makeText(this, getString(R.string.not_send_email), Toast.LENGTH_SHORT).show()
                    }

                    Toast.makeText(this, getString(R.string.created_user), Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(Constants.psw, contrasenia)
                    startActivity(intent)
                    finish()


                }else{
                    binding.progressBar.visibility = View.GONE
                    manejaErrores(authResult)
                }
            }
        }

        binding.tvRestablecerPassword.setOnClickListener{
            val resetMail = EditText(it.context)
            resetMail.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            val passwordResetDialog = AlertDialog.Builder(it.context)
                .setTitle(getString(R.string.restore_pass))
                .setMessage(getString(R.string.insert_email))
                .setView(resetMail)
                .setPositiveButton(getString(R.string.send)) { _, _ ->
                    val mail = resetMail.text.toString()
                    if (mail.isNotEmpty()) {
                        firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener {
                            Toast.makeText(
                                this,
                                getString(R.string.link_sended_email),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                "${R.string.cant_send_email} ${it.message}",
                                Toast.LENGTH_SHORT
                            )
                                .show() //it tiene la excepción
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.pls_email),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }.setNegativeButton("${R.string.cancel}") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun showBiometricPrompt(){
        if(biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)!= BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE){
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth))
                .setSubtitle(getString(R.string.log_fingerprint))
                .setNegativeButtonText(getString(R.string.cancel))
                .build()

            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    if(banderaLectorHuella){
                        Toast.makeText(applicationContext, getString(R.string.cant_auth), Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(applicationContext, textoErrorLectorHuella, Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val authenticatedCryptoObject = result.cryptoObject
                    //Autenticación exitosa
                    binding.progressBar.visibility = View.VISIBLE
                    ingresoConHuella = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    //Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

            //Desplegando el biometric prompt
            biometricPrompt.authenticate(promptInfo)
        }else{
            Toast.makeText(applicationContext, textoErrorLectorHuella, Toast.LENGTH_SHORT).show()
        }
    }
    private fun validaCampos(): Boolean{
        email = binding.tietEmail.text.toString().trim() //para que quite espacios en blanco
        contrasenia = binding.tietContrasenia.text.toString().trim()

        if(email.isEmpty()){
            binding.tietEmail.error = "${R.string.empty_user}"
            binding.tietEmail.requestFocus()
            return false
        }

        if(contrasenia.isEmpty() || contrasenia.length < 6){
            binding.tietContrasenia.error = getString(R.string.error_pass)
            binding.tietContrasenia.requestFocus()
            return false
        }

        return true
    }
    private fun manejaErrores(task: Task<AuthResult>){
        var errorCode = ""

        try{
            errorCode = (task.exception as FirebaseAuthException).errorCode
        }catch(e: Exception){
            e.printStackTrace()
        }

        when(errorCode){
            "ERROR_INVALID_EMAIL" -> {
                Toast.makeText(this, getString(R.string.toast_error_email), Toast.LENGTH_SHORT).show()
                binding.tietEmail.error = getString(R.string.toast_error_email)
                binding.tietEmail.requestFocus()
            }
            "ERROR_WRONG_PASSWORD" -> {
                Toast.makeText(this, getString(R.string.not_valid_pass), Toast.LENGTH_SHORT).show()
                binding.tietContrasenia.error = getString(R.string.not_valid_pass)
                binding.tietContrasenia.requestFocus()
                binding.tietContrasenia.setText("")

            }
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                //An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.
                Toast.makeText(this, getString(R.string.not_same_pass), Toast.LENGTH_SHORT).show()
            }
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                Toast.makeText(this, getString(R.string.user_exists), Toast.LENGTH_LONG).show()
                binding.tietEmail.error = (getString(R.string.user_exists))
                binding.tietEmail.requestFocus()
            }
            "ERROR_USER_TOKEN_EXPIRED" -> {
                Toast.makeText(this, getString(R.string.expired_sesion), Toast.LENGTH_LONG).show()
            }
            "ERROR_USER_NOT_FOUND" -> {
                Toast.makeText(this, getString(R.string.uset_not_exists), Toast.LENGTH_LONG).show()
            }
            "ERROR_WEAK_PASSWORD" -> {
                Toast.makeText(this, getString(R.string.invalid_pass), Toast.LENGTH_LONG).show()
                binding.tietContrasenia.error = getString(R.string.at_least_6_pass)
                binding.tietContrasenia.requestFocus()
            }
            "NO_NETWORK" -> {
                Toast.makeText(this, getString(R.string.not_network), Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, getString(R.string.cant_autenticate), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun autenticaUsuario(usr: String, psw: String){

            firebaseAuth.signInWithEmailAndPassword(usr,psw).addOnCompleteListener{authResult ->
                if(authResult.isSuccessful){
                    Toast.makeText(this,getString(R.string.successful_auth),Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("${Constants.psw}",psw)

                    startActivity(intent)
                    finish()

                }else{
                    binding.progressBar.visibility = View.GONE
                    manejaErrores(authResult)
                }

            }

    }
}