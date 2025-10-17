/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.core.content.ContextCompat
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.ExpandableListChildItemBinding
import ie.equalit.ceno.databinding.ExpandableListGroupItemBinding


class CachedGroupAdapter(private val context: Context, private val groupList: List<GroupItem>, private val clickListener: GroupClickListener?) :
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return groupList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return groupList[groupPosition].children.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return groupList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return groupList[groupPosition].children[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val binding = ExpandableListGroupItemBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.groupNameTextView.text = groupList[groupPosition].name
        return binding.root
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val binding = ExpandableListChildItemBinding.inflate(LayoutInflater.from(context), parent, false)
        val cacheItem = groupList[groupPosition].children[childPosition]
        binding.childNameTextView.text = cacheItem.url
        binding.root.setOnClickListener {
            clickListener?.onLinkClicked(cacheItem.url)
        }
        setIsPinned(cacheItem.isPinned, binding)
        binding.btnPinToCache.setOnClickListener() { _ ->
            clickListener?.onPinChanged(cacheItem.url, !cacheItem.isPinned)
            setIsPinned(!cacheItem.isPinned, binding)
        }
        return binding.root
    }

    private fun setIsPinned(
        isPinned: Boolean,
        binding: ExpandableListChildItemBinding
    ) {
        if (isPinned) {
            binding.btnPinToCache.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_unpin
                )
            )
        } else {
            binding.btnPinToCache.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_pin_cache
                )
            )
        }
    }

    data class GroupItem(val name: String, val children: List<GroupChildItem>)

    data class GroupChildItem(val url: String, val isPinned:Boolean)

    interface GroupClickListener {
        fun onLinkClicked(url: String)
        fun onPinChanged(url: String, isPinned: Boolean)
    }
}