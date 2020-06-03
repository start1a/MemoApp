package com.start3a.memoji

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.start3a.memoji.Model.MemoDao
import com.start3a.memoji.views.EditMemo.EditMemoActivity
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*

class MemoAlarmTool : BroadcastReceiver() {

    companion object {
        private const val ACTION_RUN_MEMO_ALARM = "RUN_MEMO_ALARM"

        private fun createAlarmIntent(context: Context, id: String, date: Date): PendingIntent {
            val intent = Intent(context, MemoAlarmTool::class.java).apply {
                val dateString = SimpleDateFormat("yyyyMMdd_HHmmss").format(date)
                // id : + 메모id + 알람 시간
                data = Uri.parse("id:" + id + dateString)
                putExtra("MEMO_ID", id)
                // 해당 메모의 알람 데이터 삭제 탐색용
                putExtra("ALARM_TIME", date.time)
                action = ACTION_RUN_MEMO_ALARM
            }
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        fun addAlarm(context: Context, id: String, alarmTime: Date) {
            val alarmIntent = createAlarmIntent(context, id, alarmTime)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.time, alarmIntent)
        }

        fun deleteAlarm(context: Context, id: String, date: Date) {
            val alarmIntent = createAlarmIntent(context, id, date)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(alarmIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            ACTION_RUN_MEMO_ALARM -> {
                val memoId = intent.getStringExtra("MEMO_ID")
                val time = intent.getLongExtra("ALARM_TIME", 0)
                val realm = Realm.getDefaultInstance()
                val memoDao = MemoDao(realm)
                val memoData = memoDao.selectMemo(memoId)

                // 해당 알람 DB 제거
                memoDao.deleteAlarmMemo(memoId, Date(time))

                val notificationIntent = Intent(context, EditMemoActivity::class.java)
                notificationIntent.putExtra("MEMO_ID", memoId)

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val builder = NotificationCompat.Builder(context, "memoAlarm")
                    .setContentTitle(memoData.title)
                    .setContentText(memoData.content)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setSmallIcon(R.drawable.icon_memo)
                    val channel = NotificationChannel(
                        "memoAlarm",
                        "메모 알람 메시지",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                } else {
                    builder.setSmallIcon(R.mipmap.ic_launcher)
                }

                notificationManager.notify(1, builder.build())
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // 설정된 모든 알람 재등록
                val realm = Realm.getDefaultInstance()
                val memos = MemoDao(realm).getAllMemos()

                for (memo in memos)
                    for (time in memo.alarmTimeList)
                        if (time.after(Date()))
                            addAlarm(context, memo.id, time)
            }
        }

    }

}