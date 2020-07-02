package pt.lisomatrix.safevault.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nullable

@Entity(tableName = "vault_file_table")
data class VaultFile(

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var name: String = "",
    var extension: String = "",
    var path: String = "",
    var size: Long = 0L,
    var key: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultFile

        if (id != other.id) return false
        if (name != other.name) return false
        if (extension != other.extension) return false
        if (path != other.path) return false
        if (size != other.size) return false
        if (key != null) {
            if (other.key == null) return false
            if (!key!!.contentEquals(other.key!!)) return false
        } else if (other.key != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (key?.contentHashCode() ?: 0)
        return result
    }
}