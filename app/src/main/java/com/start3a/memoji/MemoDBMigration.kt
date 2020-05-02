package com.start3a.memoji

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import java.util.*

class MemoDBMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion
        val schema = realm.schema

        /*
    Migrate to version 1: Add MemoData class.
    open class MemoData(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var summary: String = "",
    var date: Date = Date(),
    var imageFileLinks: RealmList<MemoImageFilePath> = RealmList()
) : RealmObject()
         */
        if (oldVersion == 0L) {
            schema.create("MemoData")
                .addField("id", String::class.javaPrimitiveType, FieldAttribute.PRIMARY_KEY)
                .addField("title", String::class.java)
                .addField("content", String::class.java)
                .addField("summary", String::class.java)
                .addField("date", Date::class.java)
                .addRealmListField("imageFileLinks", schema.get("MemoImageFilePath"))
            ++oldVersion
        }
    }

}