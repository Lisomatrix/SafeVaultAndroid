package pt.lisomatrix.safevault.other

import androidx.lifecycle.LiveData

interface SelectedListener {
    fun onCancel()
    fun onShare()
    fun onDelete()
    fun selectedItemsSize(): LiveData<Int>
}