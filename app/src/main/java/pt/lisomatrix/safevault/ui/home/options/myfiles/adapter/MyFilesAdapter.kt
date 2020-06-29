package pt.lisomatrix.safevault.ui.home.options.myfiles.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.other.MainClickListener
import pt.lisomatrix.safevault.other.ViewHolderClickListener

class MyFilesAdapter(private val myFiles: List<VaultFile>,
                     private val mainInterface: MainClickListener,
                     private val viewManager: LinearLayoutManager
                    )
    : RecyclerView.Adapter<MyFilesAdapter.MyFileViewHolder>(), ViewHolderClickListener {

    private var _isSelectMode = MutableLiveData<Boolean>(false)
    val isSelectMode: LiveData<Boolean>
        get() = _isSelectMode

    init {
        setHasStableIds(true)
    }

    var selectedIds: MutableList<Long> = ArrayList()

    class MyFileViewHolder(view: View, private val clickListener: ViewHolderClickListener, isSelectMode: LiveData<Boolean>)
        : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

        var id: Long = 0
        val fileNameText: TextView = view.findViewById(R.id.fileNameTxt)
        val fileSizeText: TextView = view.findViewById(R.id.fileSizeTxt)
        private val selectCB: CheckBox = view.findViewById(R.id.isSelectedCb)

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)

            isSelectMode.observeForever{ isSelectMode ->
                updateSelectMode(isSelectMode)
            }

        }

        fun setSelected(isSelected: Boolean) {
            selectCB.isChecked = isSelected
        }

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

        override fun onClick(p0: View?) {
            clickListener.onTap(adapterPosition)
            selectCB.isChecked = !selectCB.isChecked
        }

        override fun onLongClick(p0: View?): Boolean {
            clickListener.onLongTap(adapterPosition)
            selectCB.isChecked = true
            return true
        }
    }

    fun recoverSelectState(isSelectMode: Boolean, ids: MutableList<Long>) {
        this._isSelectMode.value = isSelectMode
        this.selectedIds = ids
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_files_list_item, parent, false)


        return MyFileViewHolder(view, this, _isSelectMode)
    }

    override fun getItemCount(): Int {
        return myFiles.size
    }

    override fun onBindViewHolder(holder: MyFileViewHolder, position: Int) {
        val file = myFiles[position]

        holder.fileNameText.text = file.name
        holder.fileSizeText.text = "50 KBs"
        holder.id = file.id

        holder.setSelected(selectedIds.contains(file.id))
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onLongTap(index: Int) {
        if (!isSelectMode.value!!) {
            _isSelectMode.value = true
            addIDIntoSelectedIds(index)
        }
    }

    override fun onTap(index: Int) {
        if (_isSelectMode.value!!) {
            addIDIntoSelectedIds(index)
        }
    }

    private fun addIDIntoSelectedIds(index: Int) {
        val id = myFiles[index].id

        if (selectedIds.contains(id))
            selectedIds.remove(id)
        else
            selectedIds.add(id)

        if (selectedIds.size < 1) {
            selectedIds.clear()
            _isSelectMode.value = false
        }

        mainInterface.mainInterface(selectedIds.size)
    }
}
