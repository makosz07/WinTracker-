package com.macoshapps.wintracker.contests

/**
 * Draft model for the next WinTracker release.
 * This file is not yet included in the Android source set.
 */
enum class ContestType { FREE, PURCHASE_REQUIRED, TASK, LOTTERY, OTHER }
enum class ContestStatus { SAVED, PARTICIPATING, WAITING_FOR_RESULT, WON, NOT_WON, EXPIRED }
enum class PrizeStatus { WAITING, SHIPPED, RECEIVED, NOT_RECEIVED }

data class Contest(
    val id: String,
    val title: String,
    val organizer: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val resultDate: String = "",
    val type: ContestType = ContestType.FREE,
    val status: ContestStatus = ContestStatus.SAVED,
    val prizeDescriptions: List<String> = emptyList(),
    val prizeTags: List<String> = emptyList(),
    val posterLocalPath: String? = null,
    val rulesUrl: String = "",
    val participationUrl: String = "",
    val notes: String = "",
    val createdAt: Long = 0L
)

data class PrizePhoto(val id: String, val localPath: String)

data class WonPrizeDetails(
    val entryId: String,
    val photos: List<PrizePhoto> = emptyList(),
    val status: PrizeStatus = PrizeStatus.RECEIVED
)

/** Contest status WON never creates a won-prize entry automatically. */
