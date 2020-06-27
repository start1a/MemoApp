package com.start3a.memoji.views.MemoList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.start3a.memoji.R
import com.start3a.memoji.repository.Repository
import com.start3a.memoji.viewmodel.MemoListViewModel
import com.start3a.memoji.views.EditMemo.EditMemoActivity
import com.start3a.memoji.views.LoadingProgressBar
import kotlinx.android.synthetic.main.activity_memo_list.*

class MemoListActivity : AppCompatActivity() {

    private var viewModel: MemoListViewModel? = null

    private val AUTH_SIGN_IN = 9001

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(MemoListViewModel::class.java).also { VM ->
                    VM.context = applicationContext
                }
        }

        // 새 메모 버튼
        fab.setOnClickListener {
            val intent = Intent(applicationContext, EditMemoActivity::class.java)
            startActivity(intent)
        }

        // 프래그먼트
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.memoListLayout,
            MemoListFragment()
        )
        fragmentTransaction.commit()
    }

    override fun onStart() {
        super.onStart()

        if (shouldStartSignIn())
            startSignIn()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        viewModel!!.let {
            it.mMenu = menu
            menuInflater.inflate(R.menu.menu_memo_list, it.mMenu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        viewModel!!.let { VM ->

            when (item.itemId) {

                R.id.action_layout_configuration -> {
                    // 레이아웃 구성 변경
                    // 메뉴 아이콘 변경
                    if (VM.layout_memoList.value == VM.LAYOUT_LINEAR) {
                        VM.setLayoutMemoList(VM.LAYOUT_GRID)
                        VM.mMenu?.findItem(R.id.action_layout_configuration)
                            ?.setIcon(R.drawable.ic_view_linear_list_black_24dp)
                    } else if (VM.layout_memoList.value == VM.LAYOUT_GRID) {
                        VM.setLayoutMemoList(VM.LAYOUT_LINEAR)
                        VM.mMenu?.findItem(R.id.action_layout_configuration)
                            ?.setIcon(R.drawable.ic_view_grid_black_24dp)
                    }
                }

                R.id.action_sign_out -> {
                    signOutUser()
                }
            }
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTH_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK && shouldStartSignIn()) {
                startSignIn()
            } else {
                successSignIn()
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

        startActivityForResult(intent, AUTH_SIGN_IN)
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
}