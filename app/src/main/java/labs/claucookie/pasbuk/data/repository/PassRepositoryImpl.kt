package labs.claucookie.pasbuk.data.repository

import android.net.Uri
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import labs.claucookie.pasbuk.data.local.dao.PassDao
import labs.claucookie.pasbuk.data.mapper.toDomain
import labs.claucookie.pasbuk.data.mapper.toEntity
import labs.claucookie.pasbuk.data.parser.PkpassParser
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.DuplicatePassException
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassRepositoryImpl @Inject constructor(
    private val passDao: PassDao,
    private val pkpassParser: PkpassParser,
    private val moshi: Moshi
) : PassRepository {

    override suspend fun importPass(uri: Uri): Pass = withContext(Dispatchers.IO) {
        // Parse the .pkpass file
        val pass = pkpassParser.parse(uri)

        // Check for duplicates by serial number
        val existingPass = passDao.getBySerialNumber(pass.serialNumber)
        if (existingPass != null) {
            // Clean up the newly parsed files since we won't use them
            pkpassParser.deletePassFiles(pass.id)
            throw DuplicatePassException(
                "A pass with serial number '${pass.serialNumber}' already exists"
            )
        }

        // Save to database
        passDao.insert(pass.toEntity(moshi))

        pass
    }

    override suspend fun getPassById(id: String): Pass? = withContext(Dispatchers.IO) {
        passDao.getById(id)?.toDomain(moshi)
    }

    override fun getAllPassesSortedByDate(): Flow<List<Pass>> {
        return passDao.getAllSortedByDate().map { entities ->
            entities.map { it.toDomain(moshi) }
        }
    }

    override suspend fun deletePass(id: String): Unit = withContext(Dispatchers.IO) {
        val passEntity = passDao.getById(id) ?: return@withContext

        // Delete database record
        passDao.delete(passEntity)

        // Delete associated files
        pkpassParser.deletePassFiles(id)
    }
}
