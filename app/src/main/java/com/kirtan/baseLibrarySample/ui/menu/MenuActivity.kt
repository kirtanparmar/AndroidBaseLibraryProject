package com.kirtan.baseLibrarySample.ui.menu

import android.content.Intent
import android.os.Bundle
import com.kirtan.baseLibrary.base.activity.BaseActivity
import com.kirtan.baseLibrarySample.R
import com.kirtan.baseLibrarySample.databinding.ActivityMenuBinding
import com.kirtan.baseLibrarySample.ui.userList.UserListActivity
import com.kirtan.baseLibrarySample.ui.userList.UserListPagingActivity

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