package com.mcmlr.system.products.announcements

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.EnvironmentScope
import com.mcmlr.system.products.support.TextModel
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@EnvironmentScope
class AnnouncementsRepository @Inject constructor(
    resources: Resources,
): Repository<AnnouncementListModel>(resources.dataFolder()) {

    init {
        loadModel("Feed", "announcements", AnnouncementListModel())
    }

    fun initAnnouncements(announcements: List<AnnouncementModel>) = save {
        model.announcements.clear()
        announcements.forEach {
            model.announcements.add(it)
        }
    }

    fun addAnnouncement(announcement: AnnouncementModel) = save {
        model.announcements.add(announcement)
    }

    fun removeAnnouncement(announcement: AnnouncementModel) = save {
        var index = -1
        model.announcements.forEachIndexed { i, model ->
            if (announcement.id == model.id) index = i
        }

        if (index > -1) model.announcements.removeAt(index)
    }

    fun updateAnnouncement(announcement: AnnouncementModel) = save {
        var index = -1
        model.announcements.forEachIndexed { i, model ->
            if (announcement.id == model.id) index = i
        }

        if (index > -1) model.announcements[index] = announcement
    }

    fun announcements() = model.announcements.reversed()

    fun getAnnouncements(announcementId: UUID) = model.announcements.find { it.id == announcementId }
}

data class AnnouncementListModel(
    val announcements: MutableList<AnnouncementModel> = mutableListOf(
        AnnouncementModel(
            id = UUID.fromString("89d618b7-6fb0-46d0-8523-196126035068"),
            title = "§lGetting Started",
            message = "Thank you for downloading §6§lApps§r! §rWe have created a simple setup guide to help you get started and configure §6§lApps§r to better fit your server needs. §rClick the Setup button to begin! §r",
            authorId = "b6aa893c-0c59-4f87-a137-9ca5fca36bcd",
            timestamp = Date().time,
            ctaText = "§6Setup ➡",
            cta = ". setup://",
            ctaType = AnnouncementCTAType.PLAYER,
        )
    )
): ConfigModel()

data class AnnouncementModel(
    val id: UUID,
    val title: String,
    val message: String,
    val authorId: String,
    val timestamp: Long,
    val ctaText: String? = null,
    val cta: String? = null,
    val ctaType: AnnouncementCTAType = AnnouncementCTAType.SERVER,
) {

    fun toBuilder(): Builder {
        val builder = Builder()
        builder.id = id
        builder.title = title
        builder.message = TextModel(mutableListOf(message))
        builder.authorId = authorId
        builder.timestamp = timestamp
        builder.ctaText = ctaText
        builder.cta = cta
        builder.ctaType = ctaType

        return builder
    }

    class Builder {
        var id: UUID? = null
        var title: String? = null
        var message: TextModel? = null
        var authorId: String? = null
        var timestamp: Long? = null
        var ctaText: String? = null
        var cta: String? = null
        var ctaType: AnnouncementCTAType = AnnouncementCTAType.SERVER

        fun build(): AnnouncementModel? {
            title?.let { title ->
                message?.let { message ->
                    authorId?.let {  authorId ->
                        return AnnouncementModel(
                            id ?: UUID.randomUUID(),
                            title,
                            message.toMCFormattedText(),
                            authorId,
                            timestamp ?: Date().time,
                            ctaText,
                            cta,
                            ctaType,
                        )
                    }
                }
            }

            return null
        }
    }

    override fun toString(): String = "Id=$id Title=$title Message=$message Author=$authorId Timestamp=$timestamp CTA Text=$ctaText CTA Action=$cta CTAType=$ctaType"
}

enum class AnnouncementCTAType {
    PLAYER,
    SERVER,
}
