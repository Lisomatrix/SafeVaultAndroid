package pt.lisomatrix.safevault.ui.account

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import pt.lisomatrix.safevault.database.dao.AccountDao
import pt.lisomatrix.safevault.model.Account

class AccountViewModel @ViewModelInject constructor(accountDao: AccountDao): ViewModel()  {

    val account: LiveData<Account> = accountDao.getAccount()
}