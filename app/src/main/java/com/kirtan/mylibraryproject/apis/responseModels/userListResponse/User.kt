package com.kirtan.mylibraryproject.apis.responseModels.userListResponse

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.annotations.SerializedName
import com.kirtan.mylibrary.utils.PagingListModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("id")
    var id: String = "",
    @SerializedName("title")
    var title: String = "",
    @SerializedName("firstName")
    var firstName: String = "",
    @SerializedName("lastName")
    var lastName: String = "",
    @SerializedName("picture")
    var picture: String = "",
) : PagingListModel(), Parcelable {
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem == newItem
        }
    }
}
