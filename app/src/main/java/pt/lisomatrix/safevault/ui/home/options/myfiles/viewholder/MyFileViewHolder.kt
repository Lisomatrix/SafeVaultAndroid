package pt.lisomatrix.safevault.ui.home.options.myfiles.viewholder

import android.animation.AnimatorSet
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.other.ViewHolderClickListener


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

    /**
     * Used in order to animate the checkbox appearance
     */
    private var previousSelectState: Boolean = false

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
        // If states are equal then cancel
        if (isSelectMode == previousSelectState) return

        // Determine which values to change
        var before: Int = 0
        var new: Int = 150

        if (previousSelectState) {
            before = 150
            new = 0
        }

        // Update previous state
        previousSelectState = isSelectMode

        // Create animation
        val slideAnimator = ValueAnimator
            .ofInt(before, new)
            .setDuration(300)

        // Gradually change values (animate)
        slideAnimator.addUpdateListener { animation1: ValueAnimator ->
            val value = animation1.animatedValue as Int
            selectCB.layoutParams.width = value
            selectCB.requestLayout()
        }

        // Play animation
        val animationSet = AnimatorSet()

        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.play(slideAnimator)
        animationSet.start()
    }

    /**
     * Check/Uncheck Checkbox and notify adapter of the click
     */
    override fun onClick(p0: View?) {
        clickListener.onTap(adapterPosition, id)
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

