package labs.claucookie.pasbuk.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Pass

interface PassRepository {
    suspend fun importPass(uri: Uri): Pass
    suspend fun getPassById(id: String): Pass?
    fun getAllPassesSortedByDate(): Flow<List<Pass>>
    suspend fun deletePass(id: String)
}
