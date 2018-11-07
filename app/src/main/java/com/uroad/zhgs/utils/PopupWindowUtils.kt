package com.uroad.zhgs.utils

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.PopupWindow

/**
 * @author MFB
 * @create 2018/10/18
 * @describe PopupWindow 终极解决方案(7.0, 7.1, 8.0)
 */
class PopupWindowUtils {
    companion object {
        /**
         *
         * @param popupWindow  popupWindow
         * @param anchor v
         * @param xoff   x轴偏移
         * @param yoff   y轴偏移
         */
        fun showAsDropDown(popupWindow: PopupWindow, anchor: View, xoff: Int, yoff: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val visibleFrame = Rect()
                anchor.getGlobalVisibleRect(visibleFrame)
                val height = anchor.resources.displayMetrics.heightPixels - visibleFrame.bottom
                popupWindow.height = height
                popupWindow.showAsDropDown(anchor, xoff, yoff)
            } else {
                popupWindow.showAsDropDown(anchor, xoff, yoff)
            }
        }
    }
}