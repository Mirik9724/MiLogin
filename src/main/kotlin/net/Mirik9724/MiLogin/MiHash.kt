package net.Mirik9724.MiLogin

import java.security.MessageDigest
import java.security.SecureRandom
import net.Mirik9724.MiLogin.MiLogin.Companion.salt

fun Hash(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(salt)
    val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun generateSalt(length: Int = 32): ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(length)
    random.nextBytes(salt)
    return salt
}

fun isSimple(password: String) : Boolean {
    return false
}