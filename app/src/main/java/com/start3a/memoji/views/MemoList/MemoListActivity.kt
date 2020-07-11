package com.start3a.memoji.views.MemoList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.start3a.memoji.CategoryActivity
import com.start3a.memoji.R
import com.start3a.memoji.repository.Repository
import com.start3a.memoji.viewmodel.MemoListViewModel
import com.start3a.memoji.views.EditMemo.EditMemoActivity
import com.start3a.memoji.views.LoadingProgressBar
import kotlinx.android.synthetic.main.app_bar_memo_list.*

class MemoListActivity : AppCompatActivity() {

    private var viewModel: MemoListViewModel? = null
    private lateinit var mDrawerLayout: DrawerLayout

    private val REQUEST_AUTH_SIGN_IN = 0
    private val REQUEST_CATEGORY = 1

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_list)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(MemoListViewModel::class.java).apply {
                    context = applicationContext
                    val nav: NavigationView = findViewById(R.id.navViewMemoList)
                    navMenu = nav.menu
                }
        }

        // 툴바
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }

        // 프래그먼트
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.memoListLayout,
            MemoListFragment()
        )
        fragmentTransaction.commit()

        // 새 메모 버튼
        fabAdd.setOnClickListener {
            val intent = Intent(applicationContext, EditMemoActivity::class.java)
            startActivity(intent)
        }

        viewModel!!.let { VM ->
            mDrawerLayout = findViewById(R.id.drawerCategory)

            VM.curTab.observe(this, Observer {
                // 프래그먼트 갱신
                when (it) {
                    getString(R.string.add_cat) -> {
                        val intent = Intent(applicationContext, CategoryActivity::class.java)
                        startActivityForResult(intent, REQUEST_CATEGORY)
                    }

                    getString(R.string.none) -> {}

                    // 모든 메모 카테고리명
                    else -> {
                        supportActionBar?.title = it
                        VM.setNewListQuery()
                    }
                }
            })

            // 내비게이션 메뉴
            val mNavigationView: NavigationView = findViewById(R.id.navViewMemoList)
            mNavigationView.setNavigationItemSelectedListener {
                mDrawerLayout.closeDrawers()
                val title = it.title.toString()
                val curTitle = VM.curTab.value!!

                if (curTitle != title)
                    VM.curTab.value = title

                return@setNavigationItemSelectedListener true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (shouldStartSignIn())
            startSignIn()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        viewModel!!.let {
            it.mActionMenu = menu
            menuInflater.inflate(R.menu.menu_memo_list, it.mActionMenu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        viewModel!!.let { VM ->
            when (item.itemId) {

                android.R.id.home -> {
                    mDrawerLayout.openDrawer(GravityCompat.START)
                }

                R.id.action_layout_configuration -> {
                    // 레이아웃 구성 변경
                    // 메뉴 아이콘 변경
                    if (VM.layoutMemoList.value == VM.LAYOUT_LINEAR) {
                        VM.setLayoutMemoList(VM.LAYOUT_GRID)
                        VM.mActionMenu?.findItem(R.id.action_layout_configuration)
                            ?.setIcon(R.drawable.ic_view_linear_list_black_24dp)
                    } else if (VM.layoutMemoList.value == VM.LAYOUT_GRID) {
                        VM.setLayoutMemoList(VM.LAYOUT_LINEAR)
                        VM.mActionMenu?.findItem(R.id.action_layout_configuration)
                            ?.setIcon(R.drawable.ic_view_grid_black_24dp)
                    }
                }

                R.id.action_sign_out -> {
                    signOutUser()
                }
            }
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_AUTH_SIGN_IN -> {
                if (resultCode != Activity.RESULT_OK && shouldStartSignIn()) {
                    startSignIn()
                } else {
                    successSignIn()
                }
            }

            REQUEST_CATEGORY -> {
                viewModel!!.setCurTab(getString(R.string.none))
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.getBooleanExtra("isChangedCat", false)) {
                        updateDrawerCategory()
                        viewModel!!.listNotifyListener?.let { it() }
                    }
                }
            }
        }
    }

    private fun shouldStartSignIn(): Boolean {
        return (!viewModel!!.isSingingIn && FirebaseAuth.getInstance().currentUser == null)
    }

    private fun startSignIn() {
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.AppTheme_NoActionBar)
            .build()

        startActivityForResult(intent, REQUEST_AUTH_SIGN_IN)
    }

    private fun successSignIn() {
        // 사용자 데이터 불러오기
        LoadingProgressBar.Progress_ProcessingData(this@MemoListActivity)
        viewModel!!.isSingingIn = true
        Repository.userID = FirebaseAuth.getInstance().currentUser?.email
        viewModel!!.getUserData()
    }

    private fun signOutUser() {
        viewModel!!.isSingingIn = false
        Repository.userID = null
        AuthUI.getInstance().signOut(this)
        viewModel!!.signOutUser()
        startSignIn()
    }

    private fun updateDrawerCategory() {
        val menu = viewModel!!.navMenu
        menu.clear()
        menu.add(R.id.groupCat, R.id.nav_all_memo, Menu.NONE, R.string.menu_all_memo)
        menu.add(R.id.groupEdit, R.id.nav_add_cat, Menu.NONE, R.string.add_cat)
        viewModel!!.categoryLiveData.value?.forEach {
            menu.add(R.id.groupCat, it.id.toInt(), Menu.NONE, it.nameCat)
        }
    }
}