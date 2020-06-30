package pt.lisomatrix.safevault.ui.home.options.myfiles.viewholder

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.other.ViewHolderClickListener
import pt.lisomatrix.safevault.model.VaultFile

/**
 * Private [VaultFile] data holder
 */
class MyFileViewHolder(view: View, private val clickListener: ViewHolderClickListener, isSelectMode: LiveData<Boolean>)
    : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

    /**
     * [VaultFile] ID
     */
    var id: Long = 0

    /**
     * [VaultFile] File Name
     */
    val fileNameText: TextView = view.findViewById(R.id.fileNameTxt)

    /**
     * [VaultFile] File Size
     */
    val fileSizeText: TextView = view.findViewById(R.id.fileSizeTxt)

    /**
     * Whether [VaultFile] is selected or not
     */
    private val selectCB: CheckBox = view.findViewById(R.id.isSelectedCb)

    init {
        // Listen of clicks
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)

        // Listen for select mode flag change
        isSelectMode.observeForever{ isSelectMode ->
            setSelected(false)
            updateSelectMode(isSelectMode)
        }

    }

    /**
     * Set checkbox to checked
     */
    fun setSelected(isSelected: Boolean) {
        selectCB.isChecked = isSelected
    }

    /**
     * Show/Hide Checkbox
     * Show when select flag is true
     */
    private fun updateSelectMode(isSelectMode: Boolean) {
        var params = selectCB.layoutParams

        if (isSelectMode) {
            params.width = 150
            selectCB.layoutParams = params
        } else {
            params.width = 0
            selectCB.layoutParams = params
        }
    }

    /**
     * Check/Uncheck Checkbox and notify adapter of the click
     */
    override fun onClick(p0: View?) {
        clickListener.onTap(adapterPosition)
        selectCB.isChecked = !selectCB.isChecked
    }

    /**
     * Check Checkbox and notify adapter of the long click
     *
     * This will always return true
     */
    override fun onLongClick(p0: View?): Boolean {
        clickListener.onLongTap(adapterPosition)
        selectCB.isChecked = true
        return true
    }
}