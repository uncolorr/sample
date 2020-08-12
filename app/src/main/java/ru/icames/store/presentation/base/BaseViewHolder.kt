package ru.icames.store.presentation.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * Base class for all ViewHolders in project
 */

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    //var deleteItemListener: UniversalAdapter.DeleteItemListener? = null

    /**
     * Method for bind item
     * @param model template object that contains bind data
     * */

    abstract fun<MODEL> bind(model: MODEL)
}