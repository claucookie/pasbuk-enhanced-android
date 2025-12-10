package labs.claucookie.pasbuk.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Pass

/**
 * Repository interface for managing Pass entities.
 * Provides methods for importing, retrieving, and deleting passes.
 */
interface PassRepository {
    /**
     * Import a .pkpass file from the given URI.
     *
     * @param uri URI of the .pkpass file to import
     * @return The imported Pass domain model
     * @throws InvalidPassException if the file is corrupted or invalid
     * @throws DuplicatePassException if a pass with the same serial number already exists
     */
    suspend fun importPass(uri: Uri): Pass

    /**
     * Get a pass by its unique identifier.
     *
     * @param id The pass identifier
     * @return The Pass if found, null otherwise
     */
    suspend fun getPassById(id: String): Pass?

    /**
     * Get all passes sorted by relevant date (most recent first).
     *
     * @return Flow emitting the list of passes sorted by date
     */
    fun getAllPassesSortedByDate(): Flow<List<Pass>>

    /**
     * Delete a pass by its unique identifier.
     * This will also remove the pass from all journeys and delete associated files.
     *
     * @param id The pass identifier to delete
     */
    suspend fun deletePass(id: String)
}
