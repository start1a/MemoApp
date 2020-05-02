package com.start3a.memoji

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .schemaVersion(0)
            .migration(MemoDBMigration())
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
    }

}