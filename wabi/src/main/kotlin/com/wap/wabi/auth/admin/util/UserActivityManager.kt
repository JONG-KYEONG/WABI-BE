package com.wap.wabi.auth.admin.util

import com.wap.wabi.auth.admin.entity.Admin
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Metrics
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class UserActivityManager {
    private var dauCounter = 0
    private var mauCounter = 0

    private val activeUsersToday = HashSet<Admin>()

    private val logger = LoggerFactory.getLogger(UserActivityManager::class.java)

    fun updateUserActivity(admin: Admin) {
        if (activeUsersToday.add(admin)) {
            dauCounter++
            mauCounter++
        }

        Gauge.builder("user_activity_dau") { dauCounter }.register(Metrics.globalRegistry)
        Gauge.builder("user_activity_mau") { mauCounter }.register(Metrics.globalRegistry)
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun resetDailyActiveUser() {
        dauCounter = 0
        activeUsersToday.clear()
        logger.info("Reset daily active user")
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    fun resetMonthlyActiveUser() {
        mauCounter = 0
        logger.info("Reset monthly active user")
    }
}
