package com.udacity.project4.locationreminders

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    companion object {
        const val AUTH_REQUEST = 1
        const val EMAIL = "EMAIL"
        const val PACKAGE_NAME = "com.udacity.project4"
    }

    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        pref = getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE)

        // When not logged in
        if(pref.getString(EMAIL, "").isNullOrEmpty()) {
            val authIntent = Intent(this, AuthenticationActivity::class.java)
            startActivityForResult(authIntent, AUTH_REQUEST)
        } else {
            requestPermissions()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
            R.id.logout -> {
                val authIntent = Intent(this, AuthenticationActivity::class.java)
                startActivityForResult(authIntent, AUTH_REQUEST)
                pref.edit().putString(EMAIL, "").apply()
                Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("ReminderActivity", "onActivityResult 호출")
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AUTH_REQUEST && resultCode == Activity.RESULT_OK) {
            val email = data?.getStringExtra(EMAIL)
            pref.edit().putString(EMAIL, email).apply()
            Toast.makeText(this, "$email 로그인 되었습니다", Toast.LENGTH_LONG).show()
            requestPermissions()
        } else {
            finish()
        }
    }

    private fun requestPermissions() {

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK
        )
            if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
    }
}
