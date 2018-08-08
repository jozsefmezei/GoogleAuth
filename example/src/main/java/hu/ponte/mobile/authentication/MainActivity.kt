package hu.ponte.mobile.authentication

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import hu.ponte.mobile.twoaf.auth.GoogleAuthenticator
import hu.ponte.mobile.twoaf.auth.GoogleAuthenticatorConfig
import hu.ponte.mobile.twoaf.auth.HmacHashFunction
import hu.ponte.mobile.twoaf.auth.ReseedingSecureRandom
import hu.ponte.mobile.twoaf.handlers.TimeCorrectionHandler
import hu.ponte.mobile.twoaf.interfaces.Twoaf
import hu.ponte.mobile.twoaf.utils.BaseUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val timecorrection = TimeCorrectionHandler()

    private var authenticator: GoogleAuthenticator? = null
    private val handler = Handler()
    private var animator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        generateBtn.setOnClickListener { view ->
            if (edit.text.toString().isEmpty()) Toast.makeText(this, "Secret can not be empty", Toast.LENGTH_SHORT).show()
            else getRepeatedlyGoogleAuthPassword(edit.text.toString()).toString()
        }

        generateEndBtn.setOnClickListener {
            authenticator?.stopTotpPasswordGeneration()
            animator?.removeAllUpdateListeners()
        }

        syncBtn.setOnClickListener {
            timecorrection.syncTime(this) {
                handler.post { Toast.makeText(this, "Time synchronized", Toast.LENGTH_SHORT).show() }
                val time = timecorrection.getCorrectedTime(MainActivity@ this)
                val cal = Calendar.getInstance()
                cal.timeInMillis = time
                Log.d("twoaf", cal.time.toString())
            }
        }
    }

    // Initialisation ------------------------------------------------------------------------------
    private fun init() {
        val config = GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setHmacHashFunction(HmacHashFunction.HmacSHA256)
                .setCodeDigits(6)
                .setWindowSize(3)
                .setEncodeType(BaseUtils.BaseType.BASE_32)
                .build()

        authenticator = GoogleAuthenticator(config)
        authenticator?.setSecureRandom(ReseedingSecureRandom(authenticator?.getRandomNumberAlgorithm()))
    }

    // Get token -----------------------------------------------------------------------------------
    fun getRepeatedlyGoogleAuthPassword(value: String) {
        authenticator?.startTotpPasswordGeneration(this, twoaf, value)
    }

    fun getSingleGoogleAuthPassword(value: String) {
        authenticator?.getTotpPassword(this, twoaf, value)
    }

    // Listener ------------------------------------------------------------------------------------
    private val twoaf = object : Twoaf {
        override fun onTokenChangedListener(token: String?, remainingTimeInSeconds: Long) {
        }

        override fun onTokenChangedUIListener(token: String, remainingTimeInSeconds: Long) {
            text.text = token
            progressAnimation(remainingTimeInSeconds)
        }
    }

    // UI ------------------------------------------------------------------------------------------
    private fun progressAnimation(fromTime: Long) {
        animator = ValueAnimator.ofFloat(fromTime.toFloat() / 1000, 0f)
        animator?.duration = fromTime
        animator?.interpolator = LinearInterpolator()
        animator?.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            circleView.setValue(30 - animatedValue);
            circleView.setText(String.format(Locale.getDefault(), "%.0f", animatedValue));
        }
        animator?.start()
    }

}
