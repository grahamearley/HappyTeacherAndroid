package org.jnanaprabodhini.happyteacherapp.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.widget.EditText
import org.jnanaprabodhini.happyteacherapp.R

/**
 * A dialog builder with a TextEdit view.
 *  The string from the TextEdit is returned
 *  in the positive button onClick callback.
 */
class InputTextDialogBuilder(context: Context) {


    private val dialogBuilder = AlertDialog.Builder(context)
    private val editText = EditText(context)

    init {
        val padding = context.resources.getDimensionPixelSize(R.dimen.padding_dialog)
        editText.setPadding(padding, padding, padding, padding)
    }

    fun setTitle(@StringRes title: Int) {
        dialogBuilder.setTitle(title)
    }

    fun setTitle(title: String) {
        dialogBuilder.setTitle(title)
    }

    fun setMessage(@StringRes message: Int) {
        dialogBuilder.setMessage(message)
    }

    fun setMessage(message: String) {
        dialogBuilder.setMessage(message)
    }

    fun setInputText(text: String) {
        editText.setText(text)
    }

    fun setInputHint(hint: String) {
        editText.hint = hint
    }

    fun setNegativeButton(text: String, listener: DialogInterface.OnClickListener) {
        dialogBuilder.setNegativeButton(text, listener)
    }

    fun setNegativeButton(@StringRes text: Int, listener: DialogInterface.OnClickListener) {
        dialogBuilder.setNegativeButton(text, listener)
    }

    fun setPositiveButton(text: String, onClick: (DialogInterface, String) -> Unit) {
        val dialogOnClickListener = DialogInterface.OnClickListener { dialogInterface, _ ->
            onClick(dialogInterface, editText.text.toString())
        }
        dialogBuilder.setPositiveButton(text, dialogOnClickListener)
    }

    fun setPositiveButton(@StringRes text: Int, onClick: (DialogInterface, String) -> Unit) {
        val dialogOnClickListener = DialogInterface.OnClickListener { dialogInterface, _ ->
            onClick(dialogInterface, editText.text.toString())
        }
        dialogBuilder.setPositiveButton(text, dialogOnClickListener)
    }

    fun show() {
        dialogBuilder.setView(editText)
        dialogBuilder.show()
    }

}