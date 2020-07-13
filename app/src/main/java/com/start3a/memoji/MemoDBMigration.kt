package com.start3a.memoji

import android.util.Log
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
    var imageFileLinks: RealmList<MemoImageFilePath> = RealmList(),
    var alarmTimeList: RealmList<Date> = RealmList()
) : RealmObject()
         */
        if (oldVersion == 0L) {
            if (!schema.contains("MemoData")) {
                schema.create("MemoData")
                    .addField("id", String::class.javaPrimitiveType, FieldAttribute.PRIMARY_KEY)
                    .addField("title", String::class.java)
                    .addField("content", String::class.java)
                    .addField("summary", String::class.java)
                    .addField("date", Date::class.java)
                    .addRealmListField("imageFileLinks", schema.get("MemoImageFilePath"))
                    .addRealmListField("alarmTimeList", Date::class.java)
            }
            ++oldVersion
        }

        /*
    Migrate to version 2: Add 1 field.
    open class MemoData(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var summary: String = "",
    var date: Date = Date(),
    var imageFileLinks: RealmList<MemoImageFilePath> = RealmList(),
    var alarmTimeList: RealmList<Date> = RealmList(),
    var category: String
) : RealmObject()

    open class Category(
        @PrimaryKey
        var id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
        var nameCat: String = ""
    ) : RealmObject()
         */
        if (oldVersion == 1L) {
            schema.get("MemoData")!!
                .addField("category", String::class.java, FieldAttribute.REQUIRED)

            if (!schema.contains("Category")) {
                schema.create("Category")
                    .addField("idCat", Long::class.javaPrimitiveType, FieldAttribute.PRIMARY_KEY)
                    .addField("nameCat", String::class.java, FieldAttribute.REQUIRED)
                ++oldVersion
            }
        }
    }
}