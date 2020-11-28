package re.returnapp

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat.OPAQUE
import android.graphics.drawable.Drawable
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import re.returnapp.model.Type
import kotlin.math.min

fun Int.toIdStr(): String {
    return "~@$this@"
}
val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Type.toDrawable(): Int {
    return when (this) {
        Type.PEOPLE -> R.drawable.ic_people
        Type.PERIOD -> R.drawable.ic_period
        Type.PLACE -> R.drawable.ic_place
        Type.EVENT -> R.drawable.ic_event
        Type.SESSION -> R.drawable.ic_session
        Type.NOTE -> R.drawable.ic_note
        Type.PRACTICE -> R.drawable.ic_practice
        Type.DIARY -> R.drawable.ic_diary
        Type.TAG -> R.drawable.ic_tag
    }
}

class SessionTime(
    var sessionCount: Int = 0,
    var totalTime: Int = 0
)
