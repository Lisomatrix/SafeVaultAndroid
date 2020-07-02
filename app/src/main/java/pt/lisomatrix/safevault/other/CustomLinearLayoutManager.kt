package pt.lisomatrix.safevault.other

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

class CustomLinearLayoutManager : LinearLayoutManager {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    // Something is happening here
    // Disabling this prevents some crashes accordingly to a google error ticket
    // Found a workaround for now in order to not lose the delete animations
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}