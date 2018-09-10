package com.uroad.zhgs.rv

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Vibrator
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.ListView
import java.util.*

class RecyclerTouchListener private constructor() : RecyclerView.OnItemTouchListener, OnActivityTouchListener {
    internal val handler = Handler()
    private lateinit var activity: Activity
    private lateinit var rView: RecyclerView
    private lateinit var unSwipeableRows: List<Int>
    /*
     * independentViews are views on the foreground layer which when clicked, act "independent" from the foreground
     * ie, they are treated separately from the "row click" action
     */
    private lateinit var independentViews: MutableList<Int>
    private lateinit var unClickableRows: MutableList<Int>
    private lateinit var optionViews: MutableList<Int>
    private lateinit var ignoredViewTypes: MutableSet<Int>
    // Cached ViewConfiguration and system-wide constant values
    private var touchSlop: Int = 0
    private var minFlingVel: Int = 0
    private var maxFlingVel: Int = 0

    companion object {
        private const val ANIMATION_STANDARD: Long = 300
        private const val ANIMATION_CLOSE: Long = 150
        private const val LONG_CLICK_DELAY = 800
    }

    private var bgWidth = 1 // 1 and not 0 to prevent dividing by zero
    // Transient properties
    // private List<PendingDismissData> mPendingDismisses = new ArrayList<>();
    private var mDismissAnimationRefCount = 0
    private var touchedX: Float = 0.toFloat()
    private var touchedY: Float = 0.toFloat()
    private var isFgSwiping: Boolean = false
    private var mSwipingSlop: Int = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var touchedPosition: Int = 0
    private var touchedView: View? = null
    private var mPaused: Boolean = false
    private var bgVisible: Boolean = false
    private var fgPartialViewClicked: Boolean = false
    private var bgVisiblePosition: Int = 0
    private var bgVisibleView: View? = null
    private var isRViewScrolling: Boolean = false
    private var heightOutsideRView: Int = 0
    private var screenHeight: Int = 0
    private var mLongClickPerformed: Boolean = false
    // Foreground view (to be swiped), Background view (to show)
    private var fgView: View? = null
    private var bgView: View? = null
    //view ID
    private var fgViewID: Int = 0
    private var bgViewID: Int = 0
    private var fadeViews: MutableList<Int>? = null
    private var mRowClickListener: OnRowClickListener? = null
    private var mRowLongClickListener: OnRowLongClickListener? = null
    private var mBgClickListener: OnSwipeOptionsClickListener? = null
    // user choices
    private var clickable = false
    private var longClickable = false
    private var swipeable = false
    private var longClickVibrate: Boolean = false
    private var mLongPressed: Runnable = Runnable {
        if (!longClickable)
            return@Runnable
        mLongClickPerformed = true
        if (!bgVisible && touchedPosition >= 0 && !unClickableRows.contains(touchedPosition) && !isRViewScrolling) {
            if (longClickVibrate) {
                val vibe = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibe.vibrate(200)
            }
            mRowLongClickListener?.onRowLongClicked(touchedPosition)
        }
    }

    constructor(activity: Activity, recyclerView: RecyclerView) : this() {
        this.activity = activity
        this.rView = recyclerView
        val vc = ViewConfiguration.get(recyclerView.context)
        touchSlop = vc.scaledTouchSlop
        minFlingVel = vc.scaledMinimumFlingVelocity * 16
        maxFlingVel = vc.scaledMaximumFlingVelocity
        bgVisible = false
        bgVisiblePosition = -1
        bgVisibleView = null
        fgPartialViewClicked = false
        unSwipeableRows = ArrayList()
        unClickableRows = ArrayList()
        ignoredViewTypes = HashSet()
        independentViews = ArrayList()
        optionViews = ArrayList()
        fadeViews = ArrayList()
        isRViewScrolling = false
        rView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING)
                isRViewScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            }
        })
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    fun setEnabled(enabled: Boolean) {
        mPaused = !enabled
    }

    /******************Clickable**************************/
    fun setClickable(listener: OnRowClickListener): RecyclerTouchListener {
        this.clickable = true
        this.mRowClickListener = listener
        return this
    }

    fun setClickable(clickable: Boolean): RecyclerTouchListener {
        this.clickable = clickable
        return this
    }

    fun setLongClickable(vibrate: Boolean, listener: OnRowLongClickListener): RecyclerTouchListener {
        this.longClickable = true
        this.mRowLongClickListener = listener
        this.longClickVibrate = vibrate
        return this
    }

    fun setLongClickable(longClickable: Boolean): RecyclerTouchListener {
        this.longClickable = longClickable
        return this
    }

    fun setIndependentViews(vararg viewIds: Int): RecyclerTouchListener {
        this.independentViews = viewIds.toMutableList()
        return this
    }

    fun setUnClickableRows(vararg rows: Int): RecyclerTouchListener {
        this.unClickableRows = rows.toMutableList()
        return this
    }

    fun setIgnoredViewTypes(vararg viewTypes: Int): RecyclerTouchListener {
        ignoredViewTypes.clear()
        ignoredViewTypes.addAll(viewTypes.toMutableList())
        return this
    }

    /************************Swipeable*******************/
    fun setSwipeable(foregroundID: Int, backgroundID: Int, listener: OnSwipeOptionsClickListener): RecyclerTouchListener {
        this.swipeable = true
        if (fgViewID != 0 && foregroundID != fgViewID)
            throw IllegalArgumentException("foregroundID does not match previously set ID")
        fgViewID = foregroundID
        bgViewID = backgroundID
        this.mBgClickListener = listener
        if (activity is RecyclerTouchListenerHelper)
            (activity as RecyclerTouchListenerHelper).setOnActivityTouchListener(this)
        val displaymetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displaymetrics)
        screenHeight = displaymetrics.heightPixels
        return this
    }

    fun setSwipeable(value: Boolean): RecyclerTouchListener {
        this.swipeable = value
        if (!value) invalidateSwipeOptions()
        return this
    }

    private fun invalidateSwipeOptions() {
        bgWidth = 1
    }

    fun setSwipeOptionViews(vararg viewIds: Int): RecyclerTouchListener {
        this.optionViews = viewIds.toMutableList()
        return this
    }

    fun setUnSwipeableRows(vararg rows: Int): RecyclerTouchListener {
        this.unSwipeableRows = rows.toMutableList()
        return this
    }

    /************************Fade Views*******************/
    // Set views which are faded out as fg is opened
    fun setViewsToFade(vararg viewIds: Int): RecyclerTouchListener {
        this.fadeViews = viewIds.toMutableList()
        return this
    }

    // the entire foreground is faded out as it is opened
    fun setFgFade(): RecyclerTouchListener {
        fadeViews?.let {
            if (!it.contains(fgViewID)) it.add(fgViewID)
        }
        return this
    }
    //-------------- Checkers for preventing ---------------//

    private fun isIndependentViewClicked(motionEvent: MotionEvent): Boolean {
        for (i in independentViews.indices) {
            touchedView?.let {
                val rect = Rect()
                val x = motionEvent.rawX.toInt()
                val y = motionEvent.rawY.toInt()
                it.findViewById<View>(independentViews[i])?.getGlobalVisibleRect(rect)
                if (rect.contains(x, y)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getOptionViewID(motionEvent: MotionEvent): Int {
        for (i in optionViews.indices) {
            touchedView?.let {
                val rect = Rect()
                val x = motionEvent.rawX.toInt()
                val y = motionEvent.rawY.toInt()
                it.findViewById<View>(optionViews[i]).getGlobalVisibleRect(rect)
                if (rect.contains(x, y)) {
                    return optionViews[i]
                }
            }
        }
        return -1
    }

    private fun getIndependentViewID(motionEvent: MotionEvent): Int {
        for (i in independentViews.indices) {
            touchedView?.let {
                val rect = Rect()
                val x = motionEvent.rawX.toInt()
                val y = motionEvent.rawY.toInt()
                it.findViewById<View>(independentViews[i]).getGlobalVisibleRect(rect)
                if (rect.contains(x, y)) {
                    return independentViews[i]
                }
            }
        }
        return -1
    }

    fun openSwipeOptions(position: Int) {
        if (!swipeable || rView.getChildAt(position) == null || unSwipeableRows.contains(position) || shouldIgnoreAction(position))
            return
        if (bgWidth < 2) {
            if (activity.findViewById<View>(bgViewID) != null)
                bgWidth = activity.findViewById<View>(bgViewID).width
            heightOutsideRView = screenHeight - rView.height
        }
        touchedPosition = position
        touchedView = rView.getChildAt(position)
        touchedView?.let {
            fgView = it.findViewById(fgViewID)
            bgView = it.findViewById(bgViewID)
            bgView?.let { view -> fgView?.let { view.minimumHeight = it.height } }
            closeVisibleBG(null)
            animateFG(it, Animation.OPEN, ANIMATION_STANDARD)
            bgVisible = true
            bgVisibleView = fgView
            bgVisiblePosition = touchedPosition
        }
    }

    private fun closeVisibleBG(mSwipeCloseListener: OnSwipeListener?) {
        bgVisibleView?.let {
            val translateAnimator = ObjectAnimator.ofFloat(bgVisibleView, View.TRANSLATION_X, 0f)
            translateAnimator.duration = ANIMATION_CLOSE
            translateAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    mSwipeCloseListener?.onSwipeOptionsClosed()
                    translateAnimator.removeAllListeners()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            translateAnimator.start()
            animateFadeViews(it, 1f, ANIMATION_CLOSE)
            bgVisible = false
            bgVisibleView = null
            bgVisiblePosition = -1
        }
    }

    private fun animateFadeViews(downView: View, alpha: Float, duration: Long) {
        fadeViews?.let {
            for (viewID in it) {
                downView.findViewById<View>(viewID).animate().alpha(alpha).duration = duration
            }
        }
    }

    private fun animateFG(downView: View, animateType: Animation, duration: Long) {
        fgView?.let {
            if (animateType == Animation.OPEN) {
                val translateAnimator = ObjectAnimator.ofFloat<View>(it, View.TRANSLATION_X, -bgWidth.toFloat())
                translateAnimator.duration = duration
                translateAnimator.interpolator = DecelerateInterpolator(1.5f)
                translateAnimator.start()
                animateFadeViews(downView, 0f, duration)
            } else if (animateType == Animation.CLOSE) {
                val translateAnimator = ObjectAnimator.ofFloat(it, View.TRANSLATION_X, 0f)
                translateAnimator.duration = duration
                translateAnimator.interpolator = DecelerateInterpolator(1.5f)
                translateAnimator.start()
                animateFadeViews(downView, 1f, duration)
            }
        }
    }

    private fun animateFG(downView: View, animateType: Animation, duration: Long, mSwipeCloseListener: OnSwipeListener?) {
        fgView?.let { it ->
            val translateAnimator: ObjectAnimator
            if (animateType == Animation.OPEN) {
                translateAnimator = ObjectAnimator.ofFloat<View>(it, View.TRANSLATION_X, -bgWidth.toFloat())
                translateAnimator.duration = duration
                translateAnimator.interpolator = DecelerateInterpolator(1.5f)
                translateAnimator.start()
                animateFadeViews(downView, 0f, duration)
            } else
            /*if (animateType == Animation.CLOSE)*/ {
                translateAnimator = ObjectAnimator.ofFloat(it, View.TRANSLATION_X, 0f)
                translateAnimator.duration = duration
                translateAnimator.interpolator = DecelerateInterpolator(1.5f)
                translateAnimator.start()
                animateFadeViews(downView, 1f, duration)
            }
            translateAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    mSwipeCloseListener?.let {
                        if (animateType == Animation.OPEN) it.onSwipeOptionsOpened()
                        else if (animateType == Animation.CLOSE) it.onSwipeOptionsClosed()
                    }
                    translateAnimator.removeAllListeners()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }

    override fun onTouchEvent(rv: RecyclerView, motionEvent: MotionEvent) {
        handleTouchEvent(motionEvent)
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, motionEvent: MotionEvent): Boolean {
        return handleTouchEvent(motionEvent)
    }

    private fun handleTouchEvent(motionEvent: MotionEvent): Boolean {
        if (swipeable && bgWidth < 2) {
            //            bgWidth = rView.getWidth();
            activity.findViewById<View>(bgViewID)?.let { bgWidth = it.width }
            heightOutsideRView = screenHeight - rView.height
        }
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (mPaused) {
                    return false
                }
                // Find the child view that was touched (perform a hit test)
                val rect = Rect()
                val childCount = rView.childCount
                val listViewCoords = IntArray(2)
                rView.getLocationOnScreen(listViewCoords)
                // x and y values respective to the recycler view
                var x = motionEvent.rawX.toInt() - listViewCoords[0]
                var y = motionEvent.rawY.toInt() - listViewCoords[1]
                var child: View
                /*
                 * check for every child (row) in the recycler view whether the touched co-ordinates belong to that
                 * respective child and if it does, register that child as the touched view (touchedView)
                 */
                for (i in 0 until childCount) {
                    child = rView.getChildAt(i)
                    child.getHitRect(rect)
                    if (rect.contains(x, y)) {
                        touchedView = child
                        break
                    }
                }
                touchedView?.let { view ->
                    touchedX = motionEvent.rawX
                    touchedY = motionEvent.rawY
                    touchedPosition = rView.getChildAdapterPosition(view)
                    if (shouldIgnoreAction(touchedPosition)) {
                        touchedPosition = ListView.INVALID_POSITION
                        return false   // <-- guard here allows for ignoring events, allowing more than one view type and preventing NPE
                    }
                    if (longClickable) {
                        mLongClickPerformed = false
                        handler.postDelayed(mLongPressed, LONG_CLICK_DELAY.toLong())
                    }
                    if (swipeable) {
                        mVelocityTracker = VelocityTracker.obtain().apply { addMovement(motionEvent) }
                        fgView = view.findViewById(fgViewID)
                        bgView = view.findViewById(bgViewID)
                        //                        bgView.getLayoutParams().height = fgView.getHeight();
                        bgView?.let { _ -> fgView?.let { view.minimumHeight = it.height } }
                        /*
                        * bgVisible is true when the options menu is opened
                        * This block is to register fgPartialViewClicked status - Partial view is the view that is still
                        * shown on the screen if the options width is < device width
                        */
                        if (bgVisible) {
                            fgView?.let {
                                handler.removeCallbacks(mLongPressed)
                                x = motionEvent.rawX.toInt()
                                y = motionEvent.rawY.toInt()
                                it.getGlobalVisibleRect(rect)
                                fgPartialViewClicked = rect.contains(x, y)
                            }
                        } else {
                            fgPartialViewClicked = false
                        }
                    }
                }
                /*
                 * If options menu is shown and the touched position is not the same as the row for which the
                 * options is displayed - close the options menu for the row which is displaying it
                 * (bgVisibleView and bgVisiblePosition is used for this purpose which registers which view and
                 * which position has it's options menu opened)
                 */
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()
                rView.getHitRect(rect)
                if (swipeable && bgVisible && touchedPosition != bgVisiblePosition) {
                    handler.removeCallbacks(mLongPressed)
                    closeVisibleBG(null)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(mLongPressed)
                if (mLongClickPerformed) return false
                if (mVelocityTracker == null) return false
                if (swipeable) {
                    if (isFgSwiping) {
                        touchedView?.let { animateFG(it, Animation.CLOSE, ANIMATION_STANDARD) }
                    }
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                    isFgSwiping = false
                    bgView = null
                }
                touchedX = 0f
                touchedY = 0f
                touchedView = null
                touchedPosition = ListView.INVALID_POSITION
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(mLongPressed)
                if (mLongClickPerformed) return false
                if (mVelocityTracker == null && swipeable) return false
                if (touchedPosition < 0) return false
                // swipedLeft and swipedRight are true if the user swipes in the respective direction (no conditions)
                var swipedLeft = false
                var swipedRight = false
                /*
                 * swipedLeftProper and swipedRightProper are true if user swipes in the respective direction
                 * and if certain conditions are satisfied (given some few lines below)
                 */
                var swipedLeftProper = false
                var swipedRightProper = false

                val mFinalDelta = motionEvent.rawX - touchedX

//                mVelocityTracker.addMovement(motionEvent);
//                mVelocityTracker.computeCurrentVelocity(1000);
//                float velocityX = mVelocityTracker.getXVelocity();
//                float absVelocityX = Math.abs(velocityX);
//                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());

                // if swiped in a direction, make that respective variable true
                if (isFgSwiping) {
                    swipedLeft = mFinalDelta < 0
                    swipedRight = mFinalDelta > 0
                }
/*
                 * If the user has swiped more than half of the width of the options menu, or if the
                 * velocity of swiping is between min and max fling values
                 * "proper" variable are set true
                 */
                if (Math.abs(mFinalDelta) > bgWidth / 2 && isFgSwiping) {
                    swipedLeftProper = mFinalDelta < 0
                    swipedRightProper = mFinalDelta > 0
                } else if (swipeable) {
                    mVelocityTracker?.let {
                        it.addMovement(motionEvent)
                        it.computeCurrentVelocity(1000)
                        val velocityX = it.xVelocity
                        val absVelocityX = Math.abs(velocityX)
                        val absVelocityY = Math.abs(it.yVelocity)
                        if (minFlingVel <= absVelocityX && absVelocityX <= maxFlingVel
                                && absVelocityY < absVelocityX && isFgSwiping) {
                            // dismiss only if flinging in the same direction as dragging
                            swipedLeftProper = velocityX < 0 == mFinalDelta < 0
                            swipedRightProper = velocityX > 0 == mFinalDelta > 0
                        }
                    }
                }
                if (swipeable && !swipedRight && swipedLeftProper && touchedPosition != RecyclerView.NO_POSITION
                        && !unSwipeableRows.contains(touchedPosition) && !bgVisible) run {
                    val downPosition = touchedPosition
                    ++mDismissAnimationRefCount
                    touchedView?.let { animateFG(it, Animation.OPEN, ANIMATION_STANDARD) }
                    bgVisible = true
                    bgVisibleView = fgView
                    bgVisiblePosition = downPosition
                } else if (swipeable && !swipedLeft && swipedRightProper && touchedPosition != RecyclerView.NO_POSITION
                        && !unSwipeableRows.contains(touchedPosition) && bgVisible) {
                    // dismiss
                    ++mDismissAnimationRefCount
                    touchedView?.let { animateFG(it, Animation.CLOSE, ANIMATION_STANDARD) }
                    bgVisible = false
                    bgVisibleView = null
                    bgVisiblePosition = -1
                } else if (swipeable && swipedLeft && !bgVisible) {
                    // cancel
                    val tempBgView = bgView
                    touchedView?.let {
                        animateFG(it, Animation.CLOSE, ANIMATION_STANDARD, object : OnSwipeListener {
                            override fun onSwipeOptionsClosed() {
                                if (tempBgView != null)
                                    tempBgView.visibility = View.VISIBLE
                            }

                            override fun onSwipeOptionsOpened() {

                            }
                        })
                    }
                    bgVisible = false
                    bgVisibleView = null
                    bgVisiblePosition = -1
                } else if (swipeable && swipedRight && bgVisible) {
                    // cancel
                    touchedView?.let { animateFG(it, Animation.OPEN, ANIMATION_STANDARD) }
                    bgVisible = true
                    bgVisibleView = fgView
                    bgVisiblePosition = touchedPosition
                } else if (swipeable && swipedRight && !bgVisible) run {
                    // cancel
                    touchedView?.let { animateFG(it, Animation.CLOSE, ANIMATION_STANDARD) }
                    bgVisible = false
                    bgVisibleView = null
                    bgVisiblePosition = -1
                } else if (swipeable && swipedLeft && bgVisible) run {
                    // cancel
                    touchedView?.let { animateFG(it, Animation.OPEN, ANIMATION_STANDARD) }
                    bgVisible = true
                    bgVisibleView = fgView
                    bgVisiblePosition = touchedPosition
                } else if (!swipedRight && !swipedLeft) {
                    // if partial foreground view is clicked (see ACTION_DOWN) bring foreground back to original position
                    // bgVisible is true automatically since it's already checked in ACTION_DOWN block
                    if (swipeable && fgPartialViewClicked) {
                        touchedView?.let { animateFG(it, Animation.CLOSE, ANIMATION_STANDARD) }
                        bgVisible = false
                        bgVisibleView = null
                        bgVisiblePosition = -1
                    } else if (clickable && !bgVisible && touchedPosition >= 0 && !unClickableRows.contains(touchedPosition)
                            && isIndependentViewClicked(motionEvent) && !isRViewScrolling) {
                        mRowClickListener?.onRowClicked(touchedPosition)
                    } else if (clickable && !bgVisible && touchedPosition >= 0 && !unClickableRows.contains(touchedPosition)
                            && !isIndependentViewClicked(motionEvent) && !isRViewScrolling) {
                        val independentViewID = getIndependentViewID(motionEvent)
                        if (independentViewID >= 0)
                            mRowClickListener?.onIndependentViewClicked(independentViewID, touchedPosition)
                    } else if (swipeable && bgVisible && !fgPartialViewClicked) {
                        val optionID = getOptionViewID(motionEvent)
                        if (optionID >= 0 && touchedPosition >= 0) {
                            val downPosition = touchedPosition
                            closeVisibleBG(object : OnSwipeListener {
                                override fun onSwipeOptionsClosed() {
                                    mBgClickListener?.onSwipeOptionClicked(optionID, downPosition)
                                }

                                override fun onSwipeOptionsOpened() {

                                }
                            })
                        }
                    }
                }
                if (swipeable) {
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                }
                touchedX = 0f
                touchedY = 0f
                touchedView = null
                touchedPosition = ListView.INVALID_POSITION
                isFgSwiping = false
                bgView = null
            }
            MotionEvent.ACTION_MOVE -> {
                if (mLongClickPerformed) return false
                if (mVelocityTracker == null || mPaused || !swipeable) return false
                mVelocityTracker?.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - touchedX
                val deltaY = motionEvent.rawY - touchedY
                /*
                 * isFgSwiping variable which is set to true here is used to alter the swipedLeft, swipedRightProper
                 * variables in "ACTION_UP" block by checking if user is actually swiping at present or not
                 */
                if (!isFgSwiping && Math.abs(deltaX) > touchSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    handler.removeCallbacks(mLongPressed)
                    isFgSwiping = true
                    mSwipingSlop = if (deltaX > 0) touchSlop else -touchSlop
                }
                if (swipeable && isFgSwiping && !unSwipeableRows.contains(touchedPosition)) {
                    if (bgView == null) {
                        bgView = touchedView?.findViewById(bgViewID)
                        bgView?.visibility = View.VISIBLE
                    }
                    // if fg is being swiped left
                    if (deltaX < touchSlop && !bgVisible) {
                        val translateAmount = deltaX - mSwipingSlop
                        fgView?.let {
                            it.translationX = if (Math.abs(translateAmount) > bgWidth) -bgWidth.toFloat() else translateAmount
                            if (it.translationX > 0) it.translationX = 0f
                        }
                        //                        }
                        // fades all the fadeViews gradually to 0 alpha as dragged
                        fadeViews?.let { list ->
                            for (viewID in list) {
                                touchedView?.let { it.findViewById<View>(viewID).alpha = 1 - Math.abs(translateAmount) / bgWidth }
                            }
                        }
                    } else if (deltaX > 0 && bgVisible) {
                        // for closing rightOptions
                        if (bgVisible) {
                            val translateAmount = deltaX - mSwipingSlop - bgWidth
                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            fgView?.let { it.translationX = if (translateAmount > 0) 0f else translateAmount }
                            // fades all the fadeViews gradually to 0 alpha as dragged
                            fadeViews?.let { list ->
                                for (viewID in list) {
                                    touchedView?.let { it.findViewById<View>(viewID).alpha = 1 - Math.abs(translateAmount) / bgWidth }
                                }
                            }
                        } else {
                            val translateAmount = deltaX - mSwipingSlop - bgWidth
                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            fgView?.let { it.translationX = if (translateAmount > 0) 0f else translateAmount }
                            // fades all the fadeViews gradually to 0 alpha as dragged
                            fadeViews?.let { list ->
                                for (viewID in list) {
                                    touchedView?.let { it.findViewById<View>(viewID).alpha = 1 - Math.abs(translateAmount) / bgWidth }
                                }
                            }
                        }// for opening leftOptions
                    }// if fg is being swiped right
                    return true
                } else if (swipeable && isFgSwiping && unSwipeableRows.contains(touchedPosition)) {
                    if (deltaX < touchSlop && !bgVisible) {
                        val translateAmount = deltaX - mSwipingSlop
                        if (bgView == null)
                            bgView = touchedView?.findViewById(bgViewID)
                        bgView?.let { it.visibility = View.GONE }
                        // swipe fg till width of bg. If swiped further, nothing happens (stalls at width of bg)
                        fgView?.let {
                            it.translationX = translateAmount / 5
                            if (it.translationX > 0) it.translationX = 0f
                        }

                    }
                    return true
                }
            }
        }
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }


    override fun getTouchCoordinates(ev: MotionEvent) {
        val y = ev.rawY.toInt()
        if (swipeable && bgVisible && ev.actionMasked == MotionEvent.ACTION_DOWN && y < heightOutsideRView)
            closeVisibleBG(null)
    }


    private fun shouldIgnoreAction(touchedPosition: Int): Boolean {
        return ignoredViewTypes.contains(rView.adapter?.getItemViewType(touchedPosition))
    }

    private enum class Animation {
        OPEN, CLOSE
    }

    interface OnRowClickListener {
        fun onRowClicked(position: Int)

        fun onIndependentViewClicked(independentViewID: Int, position: Int)
    }

    interface OnRowLongClickListener {
        fun onRowLongClicked(position: Int)
    }

    interface OnSwipeOptionsClickListener {
        fun onSwipeOptionClicked(viewID: Int, position: Int)
    }

    interface RecyclerTouchListenerHelper {
        fun setOnActivityTouchListener(listener: OnActivityTouchListener)
    }

    interface OnSwipeListener {
        fun onSwipeOptionsClosed()

        fun onSwipeOptionsOpened()
    }
}
