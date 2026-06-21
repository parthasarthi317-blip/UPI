package com.example.upionemoretime.voice.biometrics

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

class BiometricVault(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "biometric_vault",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isEnrolled(): Boolean {
        return sharedPreferences.contains(KEY_MASTER_EMBEDDING)
    }

    fun storeMasterEmbedding(embedding: FloatArray) {
        val stringBuilder = StringBuilder()
        embedding.forEach { stringBuilder.append(it).append(",") }
        sharedPreferences.edit()
            .putString(KEY_MASTER_EMBEDDING, stringBuilder.toString())
            .apply()
        Log.d("BIOMETRIC_VAULT", "Master embedding stored securely.")
    }

    fun getMasterEmbedding(): FloatArray? {
        val data = sharedPreferences.getString(KEY_MASTER_EMBEDDING, null) ?: return null
        return try {
            data.split(",")
                .filter { it.isNotBlank() }
                .map { it.toFloat() }
                .toFloatArray()
        } catch (e: Exception) {
            Log.e("BIOMETRIC_VAULT", "Error parsing embedding: ${e.message}")
            null
        }
    }

    fun clear() {
        sharedPreferences.edit().remove(KEY_MASTER_EMBEDDING).apply()
    }

    companion object {
        private const val KEY_MASTER_EMBEDDING = "master_voice_embedding"
    }
}
