package ru.icames.store.presentation.code_reader

import android.view.View
import kotlinx.android.synthetic.main.item_founded_data.view.*
import kotlinx.android.synthetic.main.item_layout_swipe_delete.view.*
import ru.icames.store.domain.model.Code
import ru.icames.store.presentation.base.BaseViewHolder

/**
 * Holder for show founded scan data with the ability to delete
 * */

class FoundedDataViewHolderSwiped(itemView: View) : BaseViewHolder(itemView) {
    override fun <MODEL> bind(model: MODEL) {
        if(model is Code){
           //val items = model.split(",")
            itemView.textViewType.text = model.typeDescription
            itemView.textViewData1.text = model.data

           /* itemView.imageButtonRemove.setOnClickListener {
                if(deleteItemListener != null) {
                   // deleteItemListener.onDeleteItem(adapterPosition, model)
                }
            } */
        }
    }

}