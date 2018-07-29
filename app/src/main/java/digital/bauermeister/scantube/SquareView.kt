package digital.bauermeister.scantube

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlin.math.min

class SquareView : LinearLayout {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attr: AttributeSet) : super(context, attr) {}

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(size, size)
    }
}