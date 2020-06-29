package pt.lisomatrix.safevault.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_table")
data class Account (
    @PrimaryKey
    var accountID: String,
    var password: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (accountID != other.accountID) return false
        if (!password.contentEquals(other.password)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accountID.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}
