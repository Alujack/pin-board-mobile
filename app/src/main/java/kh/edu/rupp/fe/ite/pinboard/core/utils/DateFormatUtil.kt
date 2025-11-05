package kh.edu.rupp.fe.ite.pinboard.core.utils

import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.abs

object DateFormatUtil {

    // Common date formats
    object Formats {
        const val ISO_DATE = "yyyy-MM-dd"
        const val ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss"
        const val READABLE = "MMM dd, yyyy"
        const val READABLE_WITH_TIME = "MMM dd, yyyy 'at' hh:mm a"
        const val FULL_DATE = "EEEE, MMMM dd, yyyy"
        const val SHORT_DATE = "MM/dd/yyyy"
        const val TIME_ONLY = "hh:mm a"
        const val TIME_24H = "HH:mm"
        const val MONTH_YEAR = "MMMM yyyy"
        const val DAY_MONTH = "dd MMM"
        const val ARTICLE_DATE = "MMM d, yyyy"
    }

    private val locale = Locale.getDefault()

    // Extensions for Date
    fun Date.format(pattern: String): String {
        return SimpleDateFormat(pattern, locale).format(this)
    }

    fun Date.toReadable(): String = format(Formats.READABLE)
    fun Date.toReadableWithTime(): String = format(Formats.READABLE_WITH_TIME)
    fun Date.toFullDate(): String = format(Formats.FULL_DATE)
    fun Date.toTimeOnly(): String = format(Formats.TIME_ONLY)
    fun Date.toShortDate(): String = format(Formats.SHORT_DATE)
    fun Date.toArticleDate(): String = format(Formats.ARTICLE_DATE)

    // Extensions for LocalDateTime
    fun LocalDateTime.format(pattern: String): String {
        return DateTimeFormatter.ofPattern(pattern, locale).format(this)
    }

    fun LocalDateTime.toReadable(): String = format(Formats.READABLE)
    fun LocalDateTime.toReadableWithTime(): String = format(Formats.READABLE_WITH_TIME)
    fun LocalDateTime.toFullDate(): String = format(Formats.FULL_DATE)
    fun LocalDateTime.toTimeOnly(): String = format(Formats.TIME_ONLY)
    fun LocalDateTime.toShortDate(): String = format(Formats.SHORT_DATE)
    fun LocalDateTime.toArticleDate(): String = format(Formats.ARTICLE_DATE)

    // Extensions for LocalDate
    fun LocalDate.format(pattern: String): String {
        return DateTimeFormatter.ofPattern(pattern, locale).format(this)
    }

    fun LocalDate.toReadable(): String = format(Formats.READABLE)
    fun LocalDate.toFullDate(): String = format(Formats.FULL_DATE)
    fun LocalDate.toShortDate(): String = format(Formats.SHORT_DATE)
    fun LocalDate.toArticleDate(): String = format(Formats.ARTICLE_DATE)
    fun LocalDate.toDayMonth(): String = format(Formats.DAY_MONTH)

    // Relative time formatting (e.g., "2 hours ago", "Yesterday")
    fun Date.toRelativeTime(): String {
        val now = Date()
        val diffInMillis = now.time - this.time

        return when {
            diffInMillis < 0 -> "In the future"
            diffInMillis < 60_000 -> "Just now"
            diffInMillis < 3_600_000 -> "${diffInMillis / 60_000} minutes ago"
            diffInMillis < 86_400_000 -> "${diffInMillis / 3_600_000} hours ago"
            diffInMillis < 2 * 86_400_000 -> "Yesterday"
            diffInMillis < 7 * 86_400_000 -> "${diffInMillis / 86_400_000} days ago"
            diffInMillis < 30 * 86_400_000 -> "${diffInMillis / (7 * 86_400_000)} weeks ago"
            diffInMillis < 365 * 86_400_000 -> "${diffInMillis / (30 * 86_400_000)} months ago"
            else -> "${diffInMillis / (365 * 86_400_000)} years ago"
        }
    }

    fun LocalDateTime.toRelativeTime(): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(this, now)

        return when {
            duration.isNegative -> "In the future"
            duration.seconds < 60 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            duration.toDays() == 1L -> "Yesterday"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            duration.toDays() < 30 -> "${duration.toDays() / 7} weeks ago"
            duration.toDays() < 365 -> "${duration.toDays() / 30} months ago"
            else -> "${duration.toDays() / 365} years ago"
        }
    }

    // Smart date formatting (shows relative for recent, absolute for older)
    fun Date.toSmartFormat(): String {
        val now = Date()
        val diffInDays = (now.time - this.time) / (24 * 60 * 60 * 1000)

        return when {
            diffInDays == 0L -> "Today ${this.toTimeOnly()}"
            diffInDays == 1L -> "Yesterday ${this.toTimeOnly()}"
            diffInDays < 7 -> "${diffInDays} days ago"
            diffInDays < 365 -> this.toReadable()
            else -> this.format("MMM yyyy")
        }
    }

    fun LocalDateTime.toSmartFormat(): String {
        val now = LocalDateTime.now()
        val days = Duration.between(this.toLocalDate().atStartOfDay(), now.toLocalDate().atStartOfDay()).toDays()

        return when {
            days == 0L -> "Today ${this.toTimeOnly()}"
            days == 1L -> "Yesterday ${this.toTimeOnly()}"
            days < 7 -> "${days} days ago"
            days < 365 -> this.toReadable()
            else -> this.format("MMM yyyy")
        }
    }

    // Article/Blog style formatting
    fun Date.toArticleStyle(): String {
        val cal = Calendar.getInstance().apply { time = this@toArticleStyle }
        val now = Calendar.getInstance()

        return when {
            isSameDay(cal, now) -> "Today"
            isYesterday(cal, now) -> "Yesterday"
            isSameYear(cal, now) -> this.format("MMM d")
            else -> this.format("MMM d, yyyy")
        }
    }

    fun LocalDateTime.toArticleStyle(): String {
        val today = LocalDate.now()
        val dateOnly = this.toLocalDate()

        return when {
            dateOnly == today -> "Today"
            dateOnly == today.minusDays(1) -> "Yesterday"
            dateOnly.year == today.year -> this.format("MMM d")
            else -> this.format("MMM d, yyyy")
        }
    }

    // Chat/Message style formatting
    fun Date.toChatStyle(): String {
        val cal = Calendar.getInstance().apply { time = this@toChatStyle }
        val now = Calendar.getInstance()

        return when {
            isSameDay(cal, now) -> this.toTimeOnly()
            isYesterday(cal, now) -> "Yesterday"
            isSameWeek(cal, now) -> this.format("EEEE")
            isSameYear(cal, now) -> this.format("MMM d")
            else -> this.format("MM/dd/yy")
        }
    }

    fun LocalDateTime.toChatStyle(): String {
        val today = LocalDate.now()
        val dateOnly = this.toLocalDate()

        return when {
            dateOnly == today -> this.toTimeOnly()
            dateOnly == today.minusDays(1) -> "Yesterday"
            dateOnly.isAfter(today.minusDays(7)) -> dateOnly.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            dateOnly.year == today.year -> this.format("MMM d")
            else -> this.format("MM/dd/yy")
        }
    }

    // Duration formatting
    fun formatDuration(startDate: Date, endDate: Date): String {
        val diffInMillis = endDate.time - startDate.time
        val days = diffInMillis / (24 * 60 * 60 * 1000)
        val hours = (diffInMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (diffInMillis % (60 * 60 * 1000)) / (60 * 1000)

        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    fun formatDuration(start: LocalDateTime, end: LocalDateTime): String {
        val duration = Duration.between(start, end)
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60

        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    // Utility functions for Date comparisons
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(cal: Calendar, now: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(cal, yesterday)
    }

    private fun isSameWeek(cal: Calendar, now: Calendar): Boolean {
        return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameYear(cal: Calendar, now: Calendar): Boolean {
        return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

    // Parse common date strings
    fun parseDate(dateString: String, pattern: String): Date? {
        return try {
            SimpleDateFormat(pattern, locale).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun parseLocalDateTime(dateString: String, pattern: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern, locale))
        } catch (e: Exception) {
            null
        }
    }

    // ISO string parsing
    fun parseISOString(isoString: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(isoString.replace("Z", "+00:00").take(19))
        } catch (e: Exception) {
            null
        }
    }

    // Get readable time difference
    fun getTimeDifference(from: Date, to: Date): String {
        val diffInMillis = abs(to.time - from.time)
        val days = diffInMillis / (24 * 60 * 60 * 1000)
        val hours = (diffInMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (diffInMillis % (60 * 60 * 1000)) / (60 * 1000)

        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "Less than a minute"
        }
    }
}

// Usage Examples:
/*

// Basic formatting
val date = Date()
println(date.toReadable()) // "Mar 15, 2024"
println(date.toReadableWithTime()) // "Mar 15, 2024 at 02:30 PM"
println(date.toFullDate()) // "Friday, March 15, 2024"

// Relative time
println(date.toRelativeTime()) // "2 hours ago"
println(date.toSmartFormat()) // "Today 2:30 PM"

// Article/Blog style
println(date.toArticleStyle()) // "Today" or "Mar 15" or "Mar 15, 2023"

// Chat style
println(date.toChatStyle()) // "2:30 PM" or "Yesterday" or "Friday"

// LocalDateTime
val localDateTime = LocalDateTime.now()
println(localDateTime.toReadable()) // "Mar 15, 2024"
println(localDateTime.toRelativeTime()) // "Just now"

// Custom formatting
println(date.format("EEEE, dd MMMM yyyy")) // "Friday, 15 March 2024"

// Duration
val start = LocalDateTime.now().minusHours(2)
val end = LocalDateTime.now()
println(DateFormatUtil.formatDuration(start, end)) // "2h 0m"

// Parsing
val parsed = DateFormatUtil.parseDate("2024-03-15", DateFormatUtil.Formats.ISO_DATE)
val parsedISO = DateFormatUtil.parseISOString("2024-03-15T14:30:00Z")

*/