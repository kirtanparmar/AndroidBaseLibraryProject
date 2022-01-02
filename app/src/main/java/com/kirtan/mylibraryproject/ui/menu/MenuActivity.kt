package com.kirtan.mylibraryproject.ui.menu

import android.content.Intent
import android.os.Bundle
import com.kirtan.baseLibrary.base.activity.BaseActivity
import com.kirtan.mylibraryproject.R
import com.kirtan.mylibraryproject.databinding.ActivityMenuBinding
import com.kirtan.mylibraryproject.ui.userList.UserListActivity
import com.kirtan.mylibraryproject.ui.userList.UserListPagingActivity

class MenuActivity : BaseActivity<ActivityMenuBinding>() {
    override val getLayout: Int
        get() = R.layout.activity_menu

    override fun storeBundleValueIfNeeded(bundle: Bundle): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen.openApiList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
        screen.openApiPagingList.setOnClickListener {
            startActivity(Intent(this, UserListPagingActivity::class.java))
        }
    }
}