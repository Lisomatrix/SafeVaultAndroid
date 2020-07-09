package pt.lisomatrix.safevault.crypto

import android.app.KeyguardManager
import android.security.KeyChain
import android.security.keystore.KeyInfo
import android.util.Log
import androidx.biometric.BiometricPrompt
import pt.lisomatrix.safevault.model.VaultFile
import java.security.KeyFactory
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.collections.HashMap

class CryptoProvider {

    companion object {
        private val cryptoObjects = HashMap<Long, BiometricPrompt.CryptoObject>()

        fun getCryptoObject(id: Long, remove: Boolean = false): BiometricPrompt.CryptoObject? {
            if (cryptoObjects.containsKey(id)) {
                val obj = cryptoObjects[id]

                if (remove)
                    cryptoObjects.remove(id)

                return obj
            }

            return null
        }

        fun createCryptoObject(vaultFile: VaultFile, encrypt: Boolean) {
            val key = getKey(vaultFile.alias!!)!!

            var cipher: Cipher

            if (encrypt)
                cipher = getCipherEncrypt(vaultFile.iv!!, key)
            else
                cipher = getCipherDecrypt(vaultFile.iv!!, key)

            val cryptoObj = BiometricPrompt.CryptoObject(cipher)

            cryptoObjects[vaultFile.id!!] = cryptoObj
        }

        private fun getKey(alias: String): SecretKey? {
            val store = KeyStore.getInstance("AndroidKeyStore")
            store.load(null)

            return store.getKey(alias, null) as SecretKey
        }

        private fun getCipherEncrypt(ivBytes: ByteArray, secretKey: SecretKey): Cipher {
            // Initialize cipher
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            return cipher
        }

        private fun getCipherDecrypt(ivBytes: ByteArray, secretKey: SecretKey): Cipher {
            // Initialize cipher
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            // Set the IV used to encrypt file
            //val spec = IvParameterSpec(ivBytes) this is for CBC
            val spec = GCMParameterSpec(128, ivBytes) // this is for GCM
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            return cipher
        }
    }
}