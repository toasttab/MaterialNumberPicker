package com.github.stephenvinouze.materialnumberpickercore

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker

/**
 * Created by stephenvinouze on 25/09/2017.
 */
class MaterialNumberPicker : NumberPicker {

    companion object {
        private const val DEFAULT_SEPARATOR_COLOR = Color.TRANSPARENT
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_TEXT_SIZE = 40
        private const val DEFAULT_VALUE = 1
        private const val MAX_VALUE = 10
        private const val DEFAULT_EDITABLE = false
        private const val DEFAULT_WRAPPED = false
    }

    var separatorColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            val pickerFields = NumberPicker::class.java.declaredFields
            for (pf in pickerFields) {
                if (pf.name == "mSelectionDivider") {
                    pf.isAccessible = true
                    try {
                        pf.set(this, ColorDrawable(separatorColor))
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }
                    break
                }
            }
        }

    var textColor: Int = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            updateTextAttributes()
        }

    var textSize: Int = DEFAULT_TEXT_SIZE
        set(value) {
            field = value
            updateTextAttributes()
        }

    var editable: Boolean = DEFAULT_EDITABLE
        set(value) {
            field = value
            descendantFocusability = if (value) ViewGroup.FOCUS_AFTER_DESCENDANTS else ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }

    constructor(context: Context,
                separatorColor: Int = DEFAULT_SEPARATOR_COLOR,
                textColor: Int = DEFAULT_TEXT_COLOR,
                textSize: Int = DEFAULT_TEXT_SIZE,
                editable: Boolean = DEFAULT_EDITABLE,
                wrapped: Boolean = DEFAULT_EDITABLE,
                defaultValue: Int = DEFAULT_VALUE,
                minValue: Int = DEFAULT_VALUE,
                maxValue: Int = MAX_VALUE,
                formatter: Formatter? = null
    ) : super(context) {
        this.separatorColor = separatorColor
        this.textColor = textColor
        this.textSize = textSize
        this.editable = editable
        this.wrapSelectorWheel = wrapped
        this.value = defaultValue
        this.minValue = minValue
        this.maxValue = maxValue
        setFormatter(formatter)

        disableFocusability()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MaterialNumberPicker, 0, 0)

        separatorColor = a.getColor(R.styleable.MaterialNumberPicker_mnpSeparatorColor, DEFAULT_SEPARATOR_COLOR)
        textColor = a.getColor(R.styleable.MaterialNumberPicker_mnpTextColor, DEFAULT_TEXT_COLOR)
        textSize = a.getDimensionPixelSize(R.styleable.MaterialNumberPicker_mnpTextSize, DEFAULT_TEXT_SIZE)
        editable = a.getBoolean(R.styleable.MaterialNumberPicker_mnpEditable, DEFAULT_EDITABLE)
        wrapSelectorWheel = a.getBoolean(R.styleable.MaterialNumberPicker_mnpWrapped, DEFAULT_WRAPPED)

        value = a.getInteger(R.styleable.MaterialNumberPicker_mnpDefaultValue, DEFAULT_VALUE)
        minValue = a.getInteger(R.styleable.MaterialNumberPicker_mnpMinValue, DEFAULT_VALUE)
        maxValue = a.getInteger(R.styleable.MaterialNumberPicker_mnpMaxValue, MAX_VALUE)

        a.recycle()

        disableFocusability()
    }

    /**
     * Disable focusability of edit text embedded inside the number picker
     * We also override the edit text filter private attribute by using reflection as the formatter is still buggy while attempting to display the default value
     * This is still an open Google @see <a href="https://code.google.com/p/android/issues/detail?id=35482#c9">issue</a> from 2012
     */
    private fun disableFocusability() {
        try {
            val f = NumberPicker::class.java.getDeclaredField("mInputText")
            f.isAccessible = true
            val inputText = f.get(this) as EditText
            inputText.filters = arrayOfNulls(0)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    /**
     * Uses reflection to access text size private attribute for both wheel and edit text inside the number picker.
     */
    private fun updateTextAttributes() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is EditText) {
                try {
                    val selectorWheelPaintField = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                    selectorWheelPaintField.isAccessible = true

                    val wheelPaint = selectorWheelPaintField.get(this) as Paint
                    wheelPaint.color = textColor
                    wheelPaint.textSize = textSize.toFloat()

                    child.setTextColor(textColor)
                    child.setTextSize(TypedValue.COMPLEX_UNIT_SP, pixelsToSp(context, textSize.toFloat()))

                    invalidate()
                    break
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun pixelsToSp(context: Context, px: Float): Float =
            px / context.resources.displayMetrics.scaledDensity

}