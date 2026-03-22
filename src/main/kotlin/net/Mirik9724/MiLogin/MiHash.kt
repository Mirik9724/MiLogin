package net.Mirik9724.MiLogin

import java.security.SecureRandom
import net.Mirik9724.MiLogin.MiLogin.Companion.salt
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import com.nulabinc.zxcvbn.Strength
import com.nulabinc.zxcvbn.Zxcvbn

fun Hash(password: String): String {
    val iterations = 10000
    val keyLength = 256

    val spec = PBEKeySpec(
        password.toCharArray(),
        salt,
        iterations,
        keyLength
    )

    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val hashBytes = factory.generateSecret(spec).encoded

    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun generateSalt(length: Int = 32): ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(length)
    random.nextBytes(salt)
    return salt
}

object PasswordGuard {
    private val zxcvbn by lazy { Zxcvbn() }

    fun isSimple(password: String, username: String): Boolean {
        if (password.length < 6) return true

        val strength: Strength = zxcvbn.measure(password, listOf(username))

        return strength.score < 2
    }
}
