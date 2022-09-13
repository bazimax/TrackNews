package com.example.tracknews.classes

import android.animation.LayoutTransition
import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlin.time.toDuration

class AnimationView {
    //набор анимаций для изменения элементов View из кода
    //duration - длительность анимации

    //view.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)
    //view.setTransitionVisibility(LayoutTransition.CHANGING)

    fun unHide(view: View, duration: Long = 400){
        //анимация: показатать/проявить
        view.alpha = 0f

        view.animate().alpha(1F).withEndAction(Runnable {
            view.visibility = View.VISIBLE
        })
        //view.animate().alpha(1f).duration = duration
        //view.visibility = View.VISIBLE
    }
    fun hide(view: View, duration: Long = 400, visible: Int = View.INVISIBLE) {
        //анимация: скрыть

        view.alpha = 1f
        view.animate().alpha(0F).withEndAction(Runnable {
            view.visibility = visible
        })
        //view.animate().alpha(0f).duration = duration
        //view.visibility = visible
    }

    fun lateHide(view: View, startDelay: Long = 500) {
        //анимация: скрыть
        view.alpha = 1f
        //view.animate().alpha(0f).duration = duration
        //Log.d("TAG1", "hide start")
        view.animate().alpha(0f).startDelay = startDelay
        //Log.d("TAG1", "hide delay")
        //view.visibility = View.INVISIBLE
        //Log.d("TAG1", "hide end")
    }

    fun testUnHide(view: View, duration: Long = 200) {
        //анимация: скрыть

        view.alpha = 0f
        //Log.d("TAG1", "un hide start")
        //view.animate().alpha(1f).duration = duration
        view.animate().alpha(1f).setStartDelay(200).duration = duration
        //Log.d("TAG1", "un hide delay")
        //view.visibility = View.VISIBLE
        //Log.d("TAG1", "un hide end")
    }

    fun rotation(view: View, duration: Long = 400, angle: Float = 180F) {
        view.rotation = angle
        view.animate().rotation(0F).duration = duration
    }

    fun scaleWidth(view: View, size: Int){
        //анимация: изменение ширины
        //item.animate().scaleXBy(1F).duration = 300
        view.layoutParams = view.layoutParams
        (view as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        view.layoutParams.width = size
        //(view).layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
    }
    fun scaleHeight(view: View, size: Int){
        //анимация: изменение ширины
        //item.animate().scaleXBy(1F).duration = 300
        view.layoutParams = view.layoutParams
        (view as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        view.layoutParams.height = size
    }

    fun swapButton(hideView: View,
                   unHideView: View,
                   durationHide: Long = 200,
                   durationUnHide: Long = 300,
                   rotation: Boolean = false,
                   angle: Float = 360F,
                   visibilityGone: Boolean = false){

        val visible = if (visibilityGone) View.GONE else View.INVISIBLE
        hide(hideView, durationHide, visible)
        unHide(unHideView, durationUnHide)
        if (rotation) {
            rotation(hideView, durationHide, angle)
            rotation(unHideView, durationUnHide, angle)
        }

    }
}