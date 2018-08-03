package hu.ponte.mobile.authentication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import hu.ponte.mobile.twoaf.auth.GoogleAuthenticator
import hu.ponte.mobile.twoaf.auth.GoogleAuthenticatorConfig
import hu.ponte.mobile.twoaf.auth.HmacHashFunction
import hu.ponte.mobile.twoaf.auth.ReseedingSecureRandom
import hu.ponte.mobile.twoaf.handlers.TimeCorrectionHandler
import hu.ponte.mobile.twoaf.utils.BaseUtils
import hu.ponte.mobile.twoaf.utils.Connection
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val timecorrection = TimeCorrectionHandler()

    private var authenticator: GoogleAuthenticator? = null
    private val decodeUtil = BaseUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        generateToken()
        text.text = getGoogleAuthPassword("ASD6588SERTTR")?.toString()
        timecorrection.syncTime(this)
    }

    private fun generateToken() {
        val config = GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setHmacHashFunction(HmacHashFunction.HmacSHA256)
                .setCodeDigits(6)
                .setWindowSize(3)
                .setTimeOffset()
                .build()

        authenticator = GoogleAuthenticator(config)
        authenticator?.setSecureRandom(ReseedingSecureRandom(authenticator?.getRandomNumberAlgorithm()))
    }

    fun getGoogleAuthPassword(value: String): Int? { // TOTP generation
        val baseText = decodeUtil.encodeToString32(value)
        return authenticator?.getTotpPassword(baseText)
    }
}
