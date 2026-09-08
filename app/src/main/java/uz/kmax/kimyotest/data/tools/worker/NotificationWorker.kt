package uz.kmax.kimyotest.data.tools.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import uz.kmax.kimyotest.data.tools.manager.NotificationHelper
import java.util.Calendar

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Notification faqat 10:00 dan 22:00 gacha chiqadi
        if (hour in 10..21) {
            val helper = NotificationHelper(applicationContext)
            val titles = arrayOf(
                "Bugun aqlliroq bo'lishni xohlaysizmi? 🧠",
                "Kimyo bo'yicha chempionmisiz? 🏆",
                "Atigi 2 daqiqa vaqt ajrating ⏱️",
                "Yangi sirli savollar tayyor! 🤫",
                "Miyangizni mashq qildiring 🧪",
                "Kimyo olamiga qaytamizmi? ✨",
                "Sizni kutmoqdamiz! 😊",
                "Bilimingizni sinash vaqti keldi ⚡",
                "Bugungi natijangiz qanday bo'ladi? 📊",
                "Kimyoni biz bilan oson o'rganing 📚"
            )
            val messages = arrayOf(
                "Kunlik kimyo testini yechib, o'z bilimingizni yangilab oling.",
                "Siz uchun maxsus saralangan savollarimiz bor. Qani boshladik!",
                "Bilimingiz zanglab qolmasin. Bugun 5 ta savolga javob bering.",
                "Kimyo qiyin emas, asosiysi muntazamlik. Birga davom etamiz!",
                "O'z darajangizni oshirish uchun ideal vaqt. Ilovaga kiring.",
                "Bugun qaysi yangi element haqida bilib olasiz? Tekshirib ko'ring.",
                "Sizning natijangiz boshqalardan yuqori bo'lishi mumkin!",
                "Kimyo sirlarini ochishda davom eting. Yangi testlar yuklandi.",
                "Kichik qadamlar katta muvaffaqiyatga olib keladi. Boshlaymiz!",
                "Hozir eng qulay vaqt. Birgalikda test yechamiz!"
            )
            
            val index = (titles.indices).random()
            helper.showNotification(titles[index], messages[index])
        }

        return Result.success()
    }
}