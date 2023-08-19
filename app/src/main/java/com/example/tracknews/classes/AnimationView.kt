package com.example.tracknews.classes

import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup

class AnimationView {
    //набор анимаций для изменения элементов View из кода
    //duration - длительность анимации

    private fun unHide(view: View){
        //анимация: показать/проявить
        view.alpha = 0f

        view.animate().alpha(1F).withEndAction {
            view.visibility = View.VISIBLE
        }
    }
    private fun hide(view: View, visible: Int = View.INVISIBLE) {
        //анимация: скрыть

        view.alpha = 1f
        view.animate().alpha(0F).withEndAction {
            view.visibility = visible
        }
    }

    fun lateHide(view: View, startDelay: Long = 500) {
        //анимация: скрыть
        view.alpha = 1f

        view.animate().alpha(0f).startDelay = startDelay
    }

    private fun rotation(view: View, duration: Long = 400, angle: Float = 180F) {
        view.rotation = angle
        view.animate().rotation(0F).duration = duration
    }

    fun scaleWidth(view: View, size: Int){
        //анимация: изменение ширины
        view.layoutParams = view.layoutParams
        (view as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        view.layoutParams.width = size
    }

    fun swapButton(hideView: View,
                   unHideView: View,
                   durationHide: Long = 200,
                   durationUnHide: Long = 300,
                   rotation: Boolean = false,
                   angle: Float = 360F,
                   visibilityGone: Boolean = false){

        val visible = if (visibilityGone) View.GONE else View.INVISIBLE
        hide(hideView, visible)
        unHide(unHideView)
        if (rotation) {
            rotation(hideView, durationHide, angle)
            rotation(unHideView, durationUnHide, angle)
        }

    }
}