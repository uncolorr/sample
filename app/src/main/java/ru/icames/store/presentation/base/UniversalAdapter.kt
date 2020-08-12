package ru.icames.store.presentation.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.icames.store.presentation.code_reader.FoundedDataViewHolderSwiped
import ru.icames.store.R
import ru.icames.store.presentation.code_reader.FoundedDataViewHolder
import ru.icames.store.util.SingleLiveEvent

/***
 * Universal adapter for all cases in project
 *
 * @param layoutItem main item for bind view holders.
 * In addition, the adapter can bind loading item
 */


class UniversalAdapter<MODEL: Any> constructor(private val layoutItem: Int) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val VIEW_TYPE_ITEM = 0

        private const val VIEW_TYPE_LOADING = 1
    }

    private var loading: Boolean = false

    var loadMoreListener: LoadMoreListener? = null

    //var deleteItemListener: DeleteItemListener<MODEL?>? = null

    private var items: ArrayList<MODEL?> = arrayListOf()

    /**
     * Live Event to notify label that item count changes
    * */
    var itemsCountChanged = SingleLiveEvent<Void>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(layoutItem, parent, false)
                return ViewHolderBuilder.getViewHolderByLayoutId(layoutItem, view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate( R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is BaseViewHolder) {
            //holder.deleteItemListener = deleteItemListener
            holder.bind(items[position]!!)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(items[position] == null) {
            return VIEW_TYPE_LOADING
        }
        return VIEW_TYPE_ITEM
    }


    fun update(items: ArrayList<MODEL?>){
        this.items = items
        notifyDataSetChanged()
        itemsCountChanged.call()
    }
    fun addFirst(model: MODEL){
        items.add(0, model)
        notifyItemInserted(0)
        itemsCountChanged.call()
    }

    fun add(model: MODEL){
        items.add(model)
        notifyItemInserted(items.lastIndex)
        itemsCountChanged.call()
    }

    fun getItems(): List<MODEL?>{
        return items.filterNotNull()
    }

    fun isEmpty(): Boolean{
        return items.isEmpty()
    }

    /**
     * Return scroll listener for RecyclerView for load more items
     * */

    fun getScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                    @NonNull recyclerView: RecyclerView, dx: Int,
                    dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleThreshold = 1
                val lastVisibleItem: Int
                val totalItemCount: Int
                val linearLayoutManager =
                        recyclerView.layoutManager as LinearLayoutManager?
                                ?: return
                if (dy > 0) {
                    totalItemCount = linearLayoutManager.itemCount
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                        if (loadMoreListener != null) {
                            loadMoreListener?.onLoadMore()
                        }
                        loading = true
                    }
                }
            }
        }
    }

    /**
     * Set adapter in state when it can load more items again
     * */

    fun setLoaded() {
        loading = false
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
        itemsCountChanged.call()
    }


    private object ViewHolderBuilder {

        /**
         * Generate ViewHolder by layout id
         * @param layoutId for generate needed ViewHolder
         * @param view inflating view from layoutId
         * @return base interface for ViewHolder
         * @see ru.icames.terminal.presentation.base.BaseViewHolder
         * */

        fun getViewHolderByLayoutId(layoutId: Int, view: View): BaseViewHolder {
            when(layoutId){
                R.layout.item_founded_data_swiped -> {
                    return FoundedDataViewHolderSwiped(view)
                }

                R.layout.item_founded_data -> {
                    return FoundedDataViewHolder(view)
                }
            }
            throw RuntimeException("Wrong layout id")
        }
    }

    interface LoadMoreListener {
        fun onLoadMore()
    }

   /* interface DeleteItemListener<T> {
        fun onDeleteItem(position: Int, data: T)
    } */

 }