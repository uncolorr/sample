package ru.icames.store.presentation.code_reader

import android.view.View
import kotlinx.android.synthetic.main.item_founded_data.view.*
import ru.icames.store.domain.model.Code
import ru.icames.store.presentation.base.BaseViewHolder
/**
 * Holder for show founded scan data
 * */

class FoundedDataViewHolder(itemView: View) : BaseViewHolder(itemView) {
    override fun <MODEL> bind(model: MODEL) {
        if(model is Code){
            itemView.textViewType.text = model.typeDescription
            itemView.textViewData1.text = model.data
        }
    }
}