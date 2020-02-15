package com.example.memoappexam

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)


    }

}