package pt.lisomatrix.safevault.ui.home.options.myfiles.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.other.MainClickListener
import pt.lisomatrix.safevault.other.ViewHolderClickListener
import pt.lisomatrix.safevault.ui.home.options.myfiles.viewholder.MyFileViewHolder

/**
 * Adapter to handle private [VaultFile]s
 */
class MyFilesAdapter(private val myFiles: MutableList<VaultFile>,
                     private val mainInterface: MainClickListener
                    )
    : RecyclerView.Adapter<MyFileViewHolder>(), ViewHolderClickListener {

    // Selected mode flag
    private var _isSelectMode = MutableLiveData<Boolean>(false)
    val isSelectMode: LiveData<Boolean>
        get() = _isSelectMode

    init {
        setHasStableIds(true)
    }

    /**
     * List of selected [VaultFile]s
     */
    var selectedIds: MutableList<Long> = ArrayList()

    /**
     * Attempt to recover selected state
     *
     * @param [isSelectMode] whether it was in select mode
     * @param [ids] list of ids of the selected items
     */
    fun recoverSelectState(isSelectMode: Boolean, ids: MutableList<Long>) {
        this._isSelectMode.value = isSelectMode
        this.selectedIds = ids
    }

    /**
     * Cancel selected flag
     */
    fun cancelSelectMode() {
        _isSelectMode.value = false
        selectedIds.clear()
        mainInterface.mainInterface(selectedIds.size)
    }

    /**
     * Notify that a item was added
     */
    fun itemAdded() {
        notifyItemInserted(myFiles.size - 1)
    }

    fun itemRemoved(id: Long) {

    }

    /**
     * Create view of a item list
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_files_list_item, parent, false)

        return MyFileViewHolder(view, this, _isSelectMode)
    }

    /**
     * The name speaks for itself
     */
    override fun getItemCount(): Int {
        return myFiles.size
    }

    /**
     * Bind [VaultFile] data to the [MyFileViewHolder]
     */
    override fun onBindViewHolder(holder: MyFileViewHolder, position: Int) {
        val file = myFiles[position]

        holder.fileNameText.text = file.name
        holder.fileSizeText.text = "50 KBs"
        holder.id = file.id!!

        holder.setSelected(selectedIds.contains(file.id!!))
    }

    override fun getItemId(position: Int): Long = position.toLong()

    /**
     * Called when a view is long tapped
     * we use this in order to enabled
     * select mode and add it to the
     * [selectedIds] list
     *
     * @param [index] of the selected [VaultFile]
     */
    override fun onLongTap(index: Int) {
        if (!isSelectMode.value!!) {
            _isSelectMode.value = true
            addOrRemoveIDIntoSelectedIds(index)
        }
    }

    /**
     * Called when a view is tapped
     * we use this in order to selected
     * or deselect the [VaultFile] of
     * the given id
     *
     * @param [index] of the selected [VaultFile]
     */
    override fun onTap(index: Int) {
        if (_isSelectMode.value!!) {
            addOrRemoveIDIntoSelectedIds(index)
        }
    }

    /**
     * As the name implies add id into the
     * selected IDs. However if the ID is
     * already there then remove it
     *
     * Probably should change it later
     * or give it a better name
     *
     * @param [index] of the selected [VaultFile]
     */
    private fun addOrRemoveIDIntoSelectedIds(index: Int) {
        val id = myFiles[index].id

        if (selectedIds.contains(id))
            selectedIds.remove(id)
        else
            selectedIds.add(id!!)

        if (selectedIds.size < 1) {
            selectedIds.clear()
            _isSelectMode.value = false
        }

        // Notify parent that the number of selected elements changed
        mainInterface.mainInterface(selectedIds.size)
    }

    fun setList(newList: ArrayList<VaultFile>) {
        // TODO: CHECK IF BOTH ARRAY ARE THE SAME
        val result = DiffUtil.calculateDiff(MyFilesListDiffUtilCallback(this.myFiles, newList))
        result.dispatchUpdatesTo(this)
        this.myFiles.clear()
        this.myFiles.addAll(newList)
    }

    class MyFilesListDiffUtilCallback(private val oldList: List<VaultFile>, private val newList: List<VaultFile>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }


    }
}
