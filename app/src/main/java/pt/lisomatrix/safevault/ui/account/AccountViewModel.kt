package pt.lisomatrix.safevault.ui.account

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

import pt.lisomatrix.safevault.database.dao.AccountDao
import pt.lisomatrix.safevault.model.Account

class AccountViewModel @ViewModelInject constructor(accountDao: AccountDao): ViewModel()  {


    val account: Single<Account> = RxJavaBridge
        .toV3Single(accountDao.getAccount())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
}