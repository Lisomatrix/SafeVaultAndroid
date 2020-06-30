package pt.lisomatrix.safevault.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nullable

@Entity(tableName = "vault_file_table")
data class VaultFile(

    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var name: String,
    var extension: String,
    var path: String
)