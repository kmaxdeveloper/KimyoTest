package uz.kmax.kimyotest.data.tools.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import uz.kmax.kimyotest.data.tools.manager.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Remote xabar kelganda NotificationHelper orqali ko'rsatamiz
        remoteMessage.notification?.let {
            val title = it.title ?: "Kimyo Test"
            val message = it.body ?: ""
            NotificationHelper(applicationContext).showNotification(title, message)
        } ?: run {
            // Agar faqat data payload bo'lsa
            if (remoteMessage.data.isNotEmpty()) {
                val title = remoteMessage.data["title"] ?: "Kimyo Test"
                val message = remoteMessage.data["message"] ?: ""
                NotificationHelper(applicationContext).showNotification(title, message)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Tokenni serverga yuborish logikasi kerak bo'lsa shu yerda yoziladi
    }
}