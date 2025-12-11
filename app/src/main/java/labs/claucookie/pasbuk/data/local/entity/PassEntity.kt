package labs.claucookie.pasbuk.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "passes",
    indices = [
        Index(value = ["serialNumber"], unique = true),
        Index(value = ["relevantDate"])
    ]
)
data class PassEntity(
    @PrimaryKey val id: String,
    val serialNumber: String,
    val passTypeIdentifier: String,
    val organizationName: String,
    val description: String,
    val teamIdentifier: String,
    val relevantDate: Long?,
    val expirationDate: Long?,
    val locationsJson: String?,
    val barcodeJson: String?,
    val fieldsJson: String?,
    val logoText: String?,
    val backgroundColor: String?,
    val foregroundColor: String?,
    val labelColor: String?,
    val logoImagePath: String?,
    val iconImagePath: String?,
    val thumbnailImagePath: String?,
    val stripImagePath: String?,
    val backgroundImagePath: String?,
    val originalPkpassPath: String,
    val passType: String,
    val createdAt: Long,
    val modifiedAt: Long
)
