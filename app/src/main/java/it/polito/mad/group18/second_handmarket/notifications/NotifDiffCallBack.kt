package it.polito.mad.group18.second_handmarket.notifications

import androidx.recyclerview.widget.DiffUtil
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Message


class NotifDiffCallBack (private val oldList : MutableList<Message>, private val newList : MutableList<Message>): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }


}