package com.kirtan.mylibraryproject.ui.userList

import com.bumptech.glide.Glide
import com.kirtan.mylibrary.base.BaseAdapter
import com.kirtan.mylibraryproject.R
import com.kirtan.mylibraryproject.apis.responseModels.userListResponse.User
import com.kirtan.mylibraryproject.databinding.ItemHomeListBinding

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