package labs.claucookie.pasbuk.domain.repository

import android.net.Uri
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.data.local.entity.PassEntity
import labs.claucookie.pasbuk.domain.model.Pass

/**
 * Repository interface for managing Pass data operations.
 *
 * Provides methods for importing, retrieving, and deleting passes from local storage.
 * All operations are suspended or return Flow for reactive updates.
 */
interface PassRepository {
    /**
     * Imports a .pkpass file from the given URI.
     *
     * Parses the pass file, validates its contents, checks for duplicates,
     * and saves it to local storage. Also checks for sufficient storage space
     * before importing.
     *
     * @param uri URI to the .pkpass file (typically from a file picker)
     * @return The imported Pass object
     * @throws InvalidPassException if the pass file is corrupted or invalid
     * @throws DuplicatePassException if a pass with the same serial number already exists
     * @throws LowStorageException if there is insufficient storage space
     */
    suspend fun importPass(uri: Uri): Pass

    /**
     * Retrieves a pass by its unique identifier.
     *
     * @param id The unique identifier of the pass
     * @return The Pass object if found, null otherwise
     */
    suspend fun getPassById(id: String): Pass?

    /**
     * Returns a Flow of all passes sorted by relevant date in descending order.
     *
     * Passes without a relevant date appear last. The Flow emits a new list
     * whenever the underlying data changes.
     *
     * @return Flow emitting sorted lists of passes
     */
    fun getAllPassesSortedByDate(): Flow<List<Pass>>

    /**
     * Returns a PagingSource for all passes sorted by relevant date.
     *
     * Enables efficient pagination of large pass lists. Passes without a
     * relevant date appear last.
     *
     * @return PagingSource for paginated pass loading
     */
    fun getAllPassesSortedByDatePaged(): PagingSource<Int, PassEntity>

    /**
     * Deletes a pass and its associated files from local storage.
     *
     * Removes both the database record and any cached images/files
     * associated with the pass.
     *
     * @param id The unique identifier of the pass to delete
     */
    suspend fun deletePass(id: String)
}
