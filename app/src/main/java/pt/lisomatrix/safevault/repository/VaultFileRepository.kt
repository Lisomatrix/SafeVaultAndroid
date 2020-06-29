package pt.lisomatrix.safevault.repository

import pt.lisomatrix.safevault.database.dao.VaultFileDao

// TODO: THIS CLASS IS USED TO IMPLEMENT THE LOGIC OF WHETER TO GET DATA FROM NETWORK OR DATABASE
// AND INSERT LOCALLY AND NETWORK BASED
class VaultFileRepository(private val vaultFileDao: VaultFileDao)