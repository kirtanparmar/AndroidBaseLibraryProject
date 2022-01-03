package com.kirtan.baseLibrarySample.ui.userList

import com.bumptech.glide.Glide
import com.kirtan.baseLibrary.base.BaseAdapter
import com.kirtan.baseLibrarySample.R
import com.kirtan.baseLibrarySample.apis.responseModels.userListResponse.User
import com.kirtan.baseLibrarySample.databinding.ItemHomeListBinding

class UserListAdapter(private val callback: (model: User) -> Unit) :
    BaseAdapter<ItemHomeListBinding, User, UserListAdapter.HomeListViewHolder>(
        User.diffUtil
    ) {
    inner class HomeListViewHolder(view: ItemHomeListBinding) : BaseViewHolder(view) {
        override fun setData(model: User) {
            view.root.setOnClickListener { callback.invoke(model) }
            Glide.with(view.root.context).load(model.picture).circleCrop().into(view.userImage)
            view.model = model
        }
    }

    override val resource: Int
        get() = R.layout.item_home_list

    override fun createViewHolder(view: ItemHomeListBinding): HomeListViewHolder =
        HomeListViewHolder(view)
}