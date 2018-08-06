package hu.ponte.mobile.authentication

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import hu.ponte.mobile.twoaf.auth.GoogleAuthenticator
import hu.ponte.mobile.twoaf.auth.GoogleAuthenticatorConfig
import hu.ponte.mobile.twoaf.auth.HmacHashFunction
import hu.ponte.mobile.twoaf.auth.ReseedingSecureRandom
import hu.ponte.mobile.twoaf.handlers.TimeCorrectionHandler
import hu.ponte.mobile.twoaf.interfaces.Twoaf
import hu.ponte.mobile.twoaf.utils.BaseUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val timecorrection = TimeCorrectionHandler()

    private var authenticator: GoogleAuthenticator? = null
    private val decodeUtil = BaseUtils()
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        generateBtn.setOnClickListener { getRepeatedlyGoogleAuthPassword(edit.text.toString()).toString() }

        generateEndBtn.setOnClickListener { authenticator?.stopTotpPasswordGeneration() }

        syncBtn.setOnClickListener {
            timecorrection.syncTime(this) {
                handler.post { Toast.makeText(this, "Time synchronized", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun init() {
        val config = GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setHmacHashFunction(HmacHashFunction.HmacSHA256)
                .setCodeDigits(6)
                .setWindowSize(3)
                .build()

        authenticator = GoogleAuthenticator(config)
        authenticator?.setSecureRandom(ReseedingSecureRandom(authenticator?.getRandomNumberAlgorithm()))
    }

    fun getRepeatedlyGoogleAuthPassword(value: String) {
        authenticator?.startTotpPasswordGeneration(this, twoaf, value)
    }

    fun getSingleGoogleAuthPassword(value: String) {
        authenticator?.getTotpPassword(this, twoaf, value)
    }

    private val twoaf = object : Twoaf {
        override fun onTokenChangedListener(token: String?, remainingTimeInSeconds: Long) {
        }

        override fun onTokenChangedUIListener(token: String?, remainingTimeInSeconds: Long) {
            text.text = "$token, time: ${remainingTimeInSeconds}"
        }

    }
}
