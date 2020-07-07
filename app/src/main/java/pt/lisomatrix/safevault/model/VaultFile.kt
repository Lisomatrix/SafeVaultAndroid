package pt.lisomatrix.safevault.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_file_table")
data class VaultFile(

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var name: String = "",
    var extension: String = "",
    var path: String = "",
    var size: Long = 0L,
    var iv: ByteArray? = null,
    var alias: String? = null
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
        if (alias != other.alias) return false
        if (iv != null) {
            if (other.iv == null) return false
            if (!iv!!.contentEquals(other.iv!!)) return false
        } else if (other.iv != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + alias.hashCode()
        result = 31 * result + (iv?.contentHashCode() ?: 0)
        return result
    }
}