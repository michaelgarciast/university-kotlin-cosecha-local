package com.garcia.cosechalocal.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.garcia.cosechalocal.ui.main.MainActivity

// Sin espera artificial: el sistema ya muestra el icono mientras arranca y
// MainActivity restaura la sesión en cuanto está lista.
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
