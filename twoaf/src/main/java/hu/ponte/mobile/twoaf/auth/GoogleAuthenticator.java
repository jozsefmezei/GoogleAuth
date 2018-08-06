/*
 * Copyright (c) 2014-2018 Enrico M. Crisostomo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the name of the author nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package hu.ponte.mobile.twoaf.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.ponte.mobile.twoaf.exception.GoogleAuthenticatorException;
import hu.ponte.mobile.twoaf.exception.TwoafException;
import hu.ponte.mobile.twoaf.interfaces.IGoogleAuthenticator;
import hu.ponte.mobile.twoaf.interfaces.Twoaf;
import hu.ponte.mobile.twoaf.utils.BaseUtils;

/**
 * This class implements the functionality described in RFC 6238 (TOTP: Time
 * based one-time password algorithm) and has been tested again Google's
 * implementation of such algorithm in its Google Authenticator application.
 * <p/>
 * This class lets users create a new 16-bit base32-encoded secret key with
 * the validation code calculated at {@code time = 0} (the UNIX epoch) and the
 * URL of a Google-provided QR barcode to let an user load the generated
 * information into Google Authenticator.
 * <p/>
 * The random number generator used by this class uses the default algorithm and
 * provider.  Users can override them by setting the following system properties
 * to the algorithm and provider name of their choice:
 * <ul>
 * <li>{@link #RNG_ALGORITHM}.</li>
 * <li>{@link #RNG_ALGORITHM_PROVIDER}.</li>
 * </ul>
 * <p/>
 * This class does not store in any way either the generated keys nor the keys
 * passed during the authorization process.
 * <p/>
 * Java Server side class for Google Authenticator's TOTP generator was inspired
 * by an author's blog post.
 *
 * @author Enrico M. Crisostomo
 * @author Warren Strange
 * @version 1.1.4
 * @see <a href="http://thegreyblog.blogspot.com/2011/12/google-authenticator-using-it-in-your.html" />
 * @see <a href="http://code.google.com/p/google-authenticator" />
 * @see <a href="http://tools.ietf.org/id/draft-mraihi-totp-timebased-06.txt" />
 * @since 0.3.0
 */
public final class GoogleAuthenticator implements IGoogleAuthenticator {

    /**
     * The system property to specify the random number generator algorithm to use.
     *
     * @since 0.5.0
     */
    public static final String RNG_ALGORITHM = "com.warrenstrange.googleauth.rng.algorithm";

    /**
     * The system property to specify the random number generator provider to use.
     *
     * @since 0.5.0
     */
    public static final String RNG_ALGORITHM_PROVIDER = "com.warrenstrange.googleauth.rng.algorithmProvider";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(GoogleAuthenticator.class.getName());

    /**
     * The number of bits of a secret key in binary form. Since the Base32
     * encoding with 8 bit characters introduces an 160% overhead, we just need
     * 80 bits (10 bytes) to generate a 16 bytes Base32-encoded secret key.
     */
    private static final int SECRET_BITS = 80;

    /**
     * Number of digits of a scratch code represented as a decimal integer.
     */
    private static final int SCRATCH_CODE_LENGTH = 8;

    /**
     * Modulus used to truncate the scratch code.
     */
    public static final int SCRATCH_CODE_MODULUS = (int) Math.pow(10, SCRATCH_CODE_LENGTH);

    /**
     * Magic number representing an invalid scratch code.
     */
    private static final int SCRATCH_CODE_INVALID = -1;

    /**
     * Length in bytes of each scratch code. We're using Google's default of
     * using 4 bytes per scratch code.
     */
    private static final int BYTES_PER_SCRATCH_CODE = 4;

    /**
     * The default SecureRandom algorithm to use if none is specified.
     *
     * @see java.security.SecureRandom#getInstance(String)
     * @since 0.5.0
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String DEFAULT_RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";

    /**
     * The default random number algorithm provider to use if none is specified.
     *
     * @see java.security.SecureRandom#getInstance(String)
     * @since 0.5.0
     */
    private static final String DEFAULT_RANDOM_NUMBER_ALGORITHM_PROVIDER = "SUN";

    /**
     * The configuration used by the current instance.
     */
    private final GoogleAuthenticatorConfig config;

    /**
     * The internal SecureRandom instance used by this class.  Since Java 7
     * {@link Random} instances are required to be thread-safe, no synchronisation is
     * required in the methods of this class using this instance.  Thread-safety
     * of this class was a de-facto standard in previous versions of Java so
     * that it is expected to work correctly in previous versions of the Java
     * platform as well.
     */
    private ReseedingSecureRandom secureRandom;

    private boolean credentialRepositorySearched;

    private Timer tokenTimer;
    private Handler handler = new Handler(Looper.myLooper());

    public GoogleAuthenticator() {
        config = new GoogleAuthenticatorConfig();
    }

    public GoogleAuthenticator(GoogleAuthenticatorConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }

        this.config = config;
    }

    private void initSecureRandom() {
        if (secureRandom == null) {
            secureRandom = new ReseedingSecureRandom(getRandomNumberAlgorithm(), getRandomNumberAlgorithmProvider());
        }
    }

    public void getTotpPassword(Context context, Twoaf twoaf, String secret) {
        getTotpPassword(twoaf, secret, new Date().getTime() - config.getTimeOffstet(context));
    }

    public void getTotpPassword(Twoaf twoaf, String secret, long time) {
        if (secret == null || secret.isEmpty()) throw new TwoafException(null, TwoafException.TwoafReason.EMPTY_SECRET);
        String encodedSecret = encodeSecret(secret);
        long timeWindow = getTimeWindowFromTime(time);
        int token = calculateCode(decodeSecret(encodedSecret), timeWindow);
        long remainingTime = calculateRemainingTime(time, timeWindow);

        String tokenString = String.format(Locale.getDefault(), "%06d", token);
        twoaf.onTokenChangedListener(tokenString, remainingTime);
        handler.post(() -> {
            twoaf.onTokenChangedUIListener(tokenString, remainingTime);
        });
    }

        public void startTotpPasswordGeneration(Context context, Twoaf twoaf, String secret) {
        stopTotpPasswordGeneration();
        startTotpPasswordGeneration(context, twoaf, secret, new Date().getTime() - config.getTimeOffstet(context));
    }

    public void startTotpPasswordGeneration(Context context, Twoaf twoaf, String secret, long time) {
        long timeWindow = getTimeWindowFromTime(time);
        long remainingTime = calculateRemainingTime(time, timeWindow);
        getTotpPassword(twoaf, secret, time);
        tokenTimer = new Timer();
        tokenTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopTotpPasswordGeneration();
                startTotpPasswordGeneration(context, twoaf, secret);
            }
        }, remainingTime, remainingTime);
    }

    public void stopTotpPasswordGeneration() {
        if(tokenTimer != null) {
            tokenTimer.cancel();
            tokenTimer.purge();
            tokenTimer = null;
        }
    }

    /**
     * Calculate the remaining time of token validity in millis
     */

    private long calculateRemainingTime(long time, long timeWindow) {
        return config.getTimeStepSizeInMillis() - (time - (timeWindow * config.getTimeStepSizeInMillis()));
    }

    private long getTimeWindowFromTime(long time) {
        return time / this.config.getTimeStepSizeInMillis();
    }

    private byte[] decodeSecret(String secret) {
        // Decoding the secret key to get its raw byte representation.
        switch (config.getKeyRepresentation()) {
            case BASE32:
                // See: https://issues.apache.org/jira/browse/CODEC-234
                // Commons Codec Base32::decode does not support lowercase letters.
                return new BaseUtils().decode32(secret.toUpperCase());
            case BASE64:
                return new BaseUtils().decode64(secret);
            default:
                throw new IllegalArgumentException("Unknown key representation type.");
        }
    }

    private String encodeSecret(String secret){
        if (config.getEncodeType() == null || BaseUtils.BaseType.BASE_32 == config.getEncodeType())
            return new BaseUtils().encodeToString32(secret);
        return new BaseUtils().encodeToString64(secret);
    }

    /**
     * Calculates the verification code of the provided key at the specified
     * instant of time using the algorithm specified in RFC 6238.
     *
     * @param key the secret key in binary format.
     * @param tm  the instant of time.
     * @return the validation code for the provided key at the specified instant
     * of time.
     */
    private int calculateCode(byte[] key, long tm) {
        // Allocating an array of bytes to represent the specified instant
        // of time.
        byte[] data = new byte[8];
        long value = tm;

        // Converting the instant of time from the long representation to a
        // big-endian array of bytes (RFC4226, 5.2. Description).
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }

        // Building the secret key specification for the HmacSHA1 algorithm.
        SecretKeySpec signKey = new SecretKeySpec(key, config.getHmacHashFunction().toString());

        try {
            // Getting an HmacSHA1/HmacSHA256 algorithm implementation from the JCE.
            Mac mac = Mac.getInstance(config.getHmacHashFunction().toString());

            // Initializing the MAC algorithm.
            mac.init(signKey);

            // Processing the instant of time and getting the encrypted data.
            byte[] hash = mac.doFinal(data);

            // Building the validation code performing dynamic truncation
            // (RFC4226, 5.3. Generating an HOTP value)
            int offset = hash[hash.length - 1] & 0xF;

            // We are using a long because Java hasn't got an unsigned integer type
            // and we need 32 unsigned bits).
            long truncatedHash = 0;

            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;

                // Java bytes are signed but we need an unsigned integer:
                // cleaning off all but the LSB.
                truncatedHash |= (hash[offset + i] & 0xFF);
            }

            // Clean bits higher than the 32nd (inclusive) and calculate the
            // module with the maximum validation code value.
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= config.getKeyModulus();

            // Returning the validation code to the caller.
            return (int) truncatedHash;
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            // Logging the exception.
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);

            // We're not disclosing internal error details to our clients.
            throw new GoogleAuthenticatorException("The operation cannot be performed now.");
        }
    }

    /**
     * This method implements the algorithm specified in RFC 6238 to check if a
     * validation code is valid in a given instant of time for the given secret
     * key.
     *
     * @param secret    the Base32 encoded secret key.
     * @param code      the code to validate.
     * @param timestamp the instant of time to use during the validation process.
     * @param window    the window size to use during the validation process.
     * @return <code>true</code> if the validation code is valid,
     * <code>false</code> otherwise.
     */
    private boolean checkCode(String secret, long code, long timestamp, int window) {
        byte[] decodedKey = decodeSecret(secret);

        // convert unix time into a 30 second "window" as specified by the
        // TOTP specification. Using Google's default interval of 30 seconds.
        final long timeWindow = getTimeWindowFromTime(timestamp);

        // Calculating the verification code of the given key in each of the
        // time intervals and returning true if the provided code is equal to
        // one of them.
        for (int i = -((window - 1) / 2); i <= window / 2; ++i) {
            // Calculating the verification code for the current time interval.
            long hash = calculateCode(decodedKey, timeWindow + i);

            // Checking if the provided code is equal to the calculated one.
            if (hash == code) {
                // The verification code is valid.
                return true;
            }
        }

        // The verification code is invalid.
        return false;
    }

    /**
     * @return the random number generator algorithm.
     * @since 0.5.0
     */
    public String getRandomNumberAlgorithm() {
        return System.getProperty(RNG_ALGORITHM, DEFAULT_RANDOM_NUMBER_ALGORITHM);
    }

    /**
     * @return the random number generator algorithm provider.
     * @since 0.5.0
     */
    public String getRandomNumberAlgorithmProvider() {
        return System.getProperty(RNG_ALGORITHM_PROVIDER, DEFAULT_RANDOM_NUMBER_ALGORITHM_PROVIDER);
    }


    public void setSecureRandom(ReseedingSecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }
}