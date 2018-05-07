/*
 * Copyright 2018 xiemingtian.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mingtian.mtpulltorefresh

import android.annotation.TargetApi
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import com.handmark.pulltorefresh.library.R

/**
 * PullToRefreshRecyclerView.java
 * Created by xiemingtian on 2017/10/19.
 */

class PullToRefreshRecyclerView : PullToRefreshBase<RecyclerView> {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, mode: Mode) : super(context, mode)

    constructor(context: Context, mode: Mode, style: AnimationStyle) : super(context, mode, style) {}

    override fun getPullToRefreshScrollDirection(): Orientation =
            Orientation.VERTICAL

    override fun createRefreshableView(context: Context, attrs: AttributeSet): RecyclerView {
        val recyclerView: RecyclerView
        recyclerView = InternalScrollViewSDK9(context, attrs)
        recyclerView.setId(R.id.recyclerview)
        return recyclerView
    }

    override fun isReadyForPullStart(): Boolean {
        val layoutManager = mRefreshableView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            return if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                0 == layoutManager.findFirstCompletelyVisibleItemPosition()
            } else {
                false
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            return if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                val ints = layoutManager.findFirstCompletelyVisibleItemPositions(null)
                if (mRefreshableView.childCount > 0) {
                    val firstView = mRefreshableView.getChildAt(0)
                    (firstView.y == 0f).and(ints[0] == 0)
                } else {
                    true
                }
            } else {
                false
            }
        }
        return mRefreshableView.scrollY == 0
    }

    override fun isReadyForPullEnd(): Boolean {
        val scrollViewChild = mRefreshableView.getChildAt(0)
        return if (null != scrollViewChild) {
            mRefreshableView.scrollY >= scrollViewChild.height - height
        } else false
    }

    @TargetApi(9)
    internal inner class InternalScrollViewSDK9(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

        /**
         * Taken from the AOSP ScrollView source
         */
        private val scrollRange: Int
            get() {
                var scrollRange = 0
                if (childCount > 0) {
                    val child = getChildAt(0)
                    scrollRange = Math.max(0, child.height - (height - paddingBottom - paddingTop))
                }
                return scrollRange
            }

        override fun overScrollBy(deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeX: Int,
                                  scrollRangeY: Int, maxOverScrollX: Int, maxOverScrollY: Int, isTouchEvent: Boolean): Boolean {

            val returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                    scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent)

            // Does all of the hard work...
            OverscrollHelper.overScrollBy(this@PullToRefreshRecyclerView, deltaX, scrollX, deltaY, scrollY,
                    scrollRange, isTouchEvent)

            return returnValue
        }
    }
}
