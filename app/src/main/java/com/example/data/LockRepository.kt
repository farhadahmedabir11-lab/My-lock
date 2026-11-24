package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

private val Context.lockDataStore: DataStore<Preferences> by preferencesDataStore(name = "my_lock_prefs")

data class VaultItem(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val localPath: String,
    val isImage: Boolean,
    val addedAtMillis: Long
) {
    val sizeKb: Long
        get() = kotlin.math.round(sizeBytes / 1024.0).toLong()
}

data class LockCredentialsState(
    val isSetupComplete: Boolean = false,
    val lastUnlockedAtMillis: Long? = null,
    val pinUpdatedAtMillis: Long? = null,
    val vaultItems: List<VaultItem> = emptyList()
)

interface LockRepository {
    val stateFlow: Flow<LockCredentialsState>
    suspend fun isSetupDone(): Boolean
    suspend fun saveSetup(pin: String, backupCode: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun verifyBackupCode(backupCode: String): Boolean
    suspend fun resetPinWithBackup(backupCode: String, newPin: String): Boolean
    suspend fun recordUnlockTimestamp()
    suspend fun addVaultItems(uriStrings: List<String>, forceImage: Boolean = false): Int
    suspend fun removeVaultItem(itemId: String)
    suspend fun clearAll()
}

class DataStoreLockRepository(
    private val context: Context
) : LockRepository {

    private object Keys {
        val SALT = stringPreferencesKey("vault_salt")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val BACKUP_HASH = stringPreferencesKey("backup_hash")
        val LAST_UNLOCKED_AT = longPreferencesKey("last_unlocked_at")
        val PIN_UPDATED_AT = longPreferencesKey("pin_updated_at")
        val VAULT_ITEMS_JSON = stringPreferencesKey("vault_items_json")
    }

    override val stateFlow: Flow<LockCredentialsState> = context.lockDataStore.data.map { prefs ->
        LockCredentialsState(
            isSetupComplete = prefs[Keys.PIN_HASH] != null && prefs[Keys.BACKUP_HASH] != null,
            lastUnlockedAtMillis = prefs[Keys.LAST_UNLOCKED_AT],
            pinUpdatedAtMillis = prefs[Keys.PIN_UPDATED_AT],
            vaultItems = decodeVaultItems(prefs[Keys.VAULT_ITEMS_JSON])
        )
    }

    override suspend fun isSetupDone(): Boolean {
        val prefs = context.lockDataStore.data.first()
        return prefs[Keys.PIN_HASH] != null && prefs[Keys.BACKUP_HASH] != null
    }

    override suspend fun saveSetup(pin: String, backupCode: String) {
        val salt = generateSalt()
        val pinHash = hashSecret(salt, pin)
        val backupHash = hashSecret(salt, backupCode.trim())
        val now = System.currentTimeMillis()
        context.lockDataStore.edit { prefs ->
            prefs[Keys.SALT] = salt
            prefs[Keys.PIN_HASH] = pinHash
            prefs[Keys.BACKUP_HASH] = backupHash
            prefs[Keys.PIN_UPDATED_AT] = now
            prefs.remove(Keys.LAST_UNLOCKED_AT)
        }
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.lockDataStore.data.first()
        val salt = prefs[Keys.SALT] ?: return false
        val expectedHash = prefs[Keys.PIN_HASH] ?: return false
        return MessageDigest.isEqual(
            hashSecret(salt, pin).toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }

    override suspend fun verifyBackupCode(backupCode: String): Boolean {
        val prefs = context.lockDataStore.data.first()
        val salt = prefs[Keys.SALT] ?: return false
        val expectedBackupHash = prefs[Keys.BACKUP_HASH] ?: return false
        return MessageDigest.isEqual(
            hashSecret(salt, backupCode.trim()).toByteArray(Charsets.UTF_8),
            expectedBackupHash.toByteArray(Charsets.UTF_8)
        )
    }

    override suspend fun resetPinWithBackup(backupCode: String, newPin: String): Boolean {
        val prefs = context.lockDataStore.data.first()
        val salt = prefs[Keys.SALT] ?: return false
        val expectedBackupHash = prefs[Keys.BACKUP_HASH] ?: return false
        val matches = MessageDigest.isEqual(
            hashSecret(salt, backupCode.trim()).toByteArray(Charsets.UTF_8),
            expectedBackupHash.toByteArray(Charsets.UTF_8)
        )
        if (!matches) return false

        val newPinHash = hashSecret(salt, newPin)
        val now = System.currentTimeMillis()
        context.lockDataStore.edit { mutablePrefs ->
            mutablePrefs[Keys.PIN_HASH] = newPinHash
            mutablePrefs[Keys.PIN_UPDATED_AT] = now
        }
        return true
    }

    override suspend fun recordUnlockTimestamp() {
        val now = System.currentTimeMillis()
        context.lockDataStore.edit { prefs ->
            prefs[Keys.LAST_UNLOCKED_AT] = now
        }
    }

    override suspend fun addVaultItems(uriStrings: List<String>, forceImage: Boolean): Int {
        if (uriStrings.isEmpty()) return 0

        val imported = withContext(Dispatchers.IO) {
            val vaultDir = File(context.filesDir, "vault_items").apply { mkdirs() }
            val result = mutableListOf<VaultItem>()

            for (uriString in uriStrings) {
                val uri = Uri.parse(uriString)
                val item = copyUriToVault(uri = uri, vaultDir = vaultDir, forceImage = forceImage)
                if (item != null) {
                    result.add(item)
                }
            }
            result
        }

        if (imported.isEmpty()) return 0

        context.lockDataStore.edit { prefs ->
            val existing = decodeVaultItems(prefs[Keys.VAULT_ITEMS_JSON])
            prefs[Keys.VAULT_ITEMS_JSON] = encodeVaultItems(existing + imported)
        }
        return imported.size
    }

    override suspend fun removeVaultItem(itemId: String) {
        var removedPath: String? = null
        context.lockDataStore.edit { prefs ->
            val existing = decodeVaultItems(prefs[Keys.VAULT_ITEMS_JSON])
            val target = existing.firstOrNull { it.id == itemId }
            removedPath = target?.localPath
            val updated = existing.filterNot { it.id == itemId }
            prefs[Keys.VAULT_ITEMS_JSON] = encodeVaultItems(updated)
        }
        removedPath?.let { path ->
            withContext(Dispatchers.IO) {
                runCatching { File(path).delete() }
            }
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            runCatching {
                File(context.filesDir, "vault_items").deleteRecursively()
            }
        }
        context.lockDataStore.edit { it.clear() }
    }

    private fun copyUriToVault(
        uri: Uri,
        vaultDir: File,
        forceImage: Boolean
    ): VaultItem? = runCatching {
        val resolver = context.contentResolver
        var displayName: String? = null
        var reportedSize = -1L

        resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) displayName = cursor.getString(nameIdx)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) reportedSize = cursor.getLong(sizeIdx)
            }
        }

        val resolvedName = displayName?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            ?: "vault_item_${System.currentTimeMillis()}"

        val mimeType = resolver.getType(uri).orEmpty()
        val isImage = forceImage ||
            mimeType.startsWith("image/", ignoreCase = true) ||
            hasImageExtension(resolvedName)

        val id = UUID.randomUUID().toString()
        val safeSuffix = resolvedName.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(48)
        val destFile = File(vaultDir, "${id}_$safeSuffix")

        var copiedBytes = 0L
        resolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                copiedBytes = input.copyTo(output)
            }
        } ?: return null

        val finalSize = if (reportedSize > 0L) reportedSize else copiedBytes
        VaultItem(
            id = id,
            name = resolvedName,
            sizeBytes = finalSize,
            mimeType = mimeType.ifBlank { if (isImage) "image/*" else "application/octet-stream" },
            localPath = destFile.absolutePath,
            isImage = isImage,
            addedAtMillis = System.currentTimeMillis()
        )
    }.getOrNull()

    private fun hasImageExtension(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".png") ||
            lower.endsWith(".webp") ||
            lower.endsWith(".gif") ||
            lower.endsWith(".heic") ||
            lower.endsWith(".heif")
    }

    private fun encodeVaultItems(items: List<VaultItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("sizeBytes", item.sizeBytes)
                    put("mimeType", item.mimeType)
                    put("localPath", item.localPath)
                    put("isImage", item.isImage)
                    put("addedAtMillis", item.addedAtMillis)
                }
            )
        }
        return array.toString()
    }

    private fun decodeVaultItems(rawJson: String?): List<VaultItem> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(rawJson)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        VaultItem(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            sizeBytes = obj.optLong("sizeBytes", 0L),
                            mimeType = obj.optString("mimeType", "application/octet-stream"),
                            localPath = obj.getString("localPath"),
                            isImage = obj.optBoolean("isImage", false),
                            addedAtMillis = obj.optLong("addedAtMillis", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashSecret(salt: String, secret: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$secret".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(input)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}
