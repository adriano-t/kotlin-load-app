package com.udacity


sealed class ButtonState {
    object Waiting : ButtonState()
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}