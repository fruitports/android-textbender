package sh.eliza.textbender

import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.math.min

/** List of children sorted by reverse drawing order. */
val AccessibilityNodeInfo.children: List<AccessibilityNodeInfo>
  get() {
    val childCount = childCount
    val list = ArrayList<AccessibilityNodeInfo>(childCount)
    for (i in 0 until childCount) {
      getChild(i, AccessibilityNodeInfo.FLAG_PREFETCH_SIBLINGS)?.let { list.add(it) }
    }
    list.sortBy { -it.drawingOrder }
    return list
  }

val AccessibilityNodeInfo.boundsInScreen: ImmutableRect
  get() = ImmutableRect(Rect().apply { getBoundsInScreen(this) })

val AccessibilityNodeInfo.textSizeInPx: Float?
  get() {
    refreshWithExtraData(AccessibilityNodeInfo.EXTRA_DATA_RENDERING_INFO_KEY, Bundle())
    return extraRenderingInfo?.textSizeInPx
  }

val AccessibilityNodeInfo.textBounds: ImmutableRect?
  get() {
    refreshWithExtraData(
      AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY,
      Bundle().apply {
        putInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, 0)
        putInt(
          AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH,
          min(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_MAX_LENGTH, text.length)
        )
      }
    )
    val array =
      extras.getParcelableArray(
        AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY,
        RectF::class.java
      )
    if (array === null || array.all { it === null }) {
      return null
    }
    val result = RectF()
    for (rect in array) {
      if (rect !== null) {
        result.union(rect)
      }
    }
    return ImmutableRect(result)
  }

val View.boundsInScreen: ImmutableRect
  get() {
    val location = intArrayOf(0, 0).apply { getLocationOnScreen(this) }
    return ImmutableRect(location[0], location[1], location[0] + width, location[1] + height)
  }
