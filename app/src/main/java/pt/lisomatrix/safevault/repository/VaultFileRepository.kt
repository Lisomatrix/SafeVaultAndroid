package pt.lisomatrix.safevault.repository

import pt.lisomatrix.safevault.database.dao.VaultFileDao

// TODO: THIS CLASS IS USED TO IMPLEMENT THE LOGIC OF WHETHER TO GET DATA FROM NETWORK OR DATABASE
// AND INSERT LOCALLY AND NETWORK BASED
// Not in use since I won't be using a server for now
class VaultFileRepository(private val vaultFileDao: VaultFileDao)