package pt.lisomatrix.safevault.ui.home.options.myfiles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyFilesViewModel : ViewModel() {

    var isMultiSelectOn: Boolean = false
    var selectedIds: MutableList<Long> = ArrayList()

}