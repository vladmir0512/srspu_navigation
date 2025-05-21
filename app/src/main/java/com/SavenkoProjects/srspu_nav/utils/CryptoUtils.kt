package com.SavenkoProjects.srspu_nav.utils

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY = "SRSPU_NAV_KEY_16" // 16 символов для AES-128
    private const val TAG = "CryptoUtils"
    private val IV = ByteArray(16) // Инициализационный вектор

    // Тестовая функция для шифрования чисел
    fun testEncryption() {
        for (i in 0..7) {
            val encrypted = encrypt(i.toString())
            Log.d(TAG, "Число $i зашифровано как: $encrypted")
        }
    }

    fun encrypt(data: String): String {
        try {
            Log.d(TAG, "Шифрование данных: '$data'")
            val key = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, javax.crypto.spec.IvParameterSpec(IV))
            val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            val result = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            Log.d(TAG, "Результат шифрования: '$result'")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при шифровании: ${e.message}")
            throw e
        }
    }

    fun decrypt(encryptedData: String): String {
        try {
            Log.d(TAG, "Расшифровка данных: '$encryptedData'")
            Log.d(TAG, "Длина зашифрованных данных: ${encryptedData.length}")
            
            val key = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, javax.crypto.spec.IvParameterSpec(IV))
            
            val decodedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
            Log.d(TAG, "Длина декодированных байтов: ${decodedBytes.size}")
            
            val decryptedBytes = cipher.doFinal(decodedBytes)
            val result = String(decryptedBytes, StandardCharsets.UTF_8)
            Log.d(TAG, "Результат расшифровки: '$result'")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при расшифровке: ${e.message}")
            Log.e(TAG, "Тип ошибки: ${e.javaClass.simpleName}")
            throw e
        }
    }
} 