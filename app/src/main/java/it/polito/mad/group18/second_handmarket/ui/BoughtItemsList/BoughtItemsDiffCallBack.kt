package it.polito.mad.group18.second_handmarket.ui.BoughtItemsList

import androidx.recyclerview.widget.DiffUtil
import it.polito.mad.group18.second_handmarket.model.Item

class BoughtItemsDiffCallBack(private val oldList : MutableList<Item>, private val newList : MutableList<Item>): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }


}