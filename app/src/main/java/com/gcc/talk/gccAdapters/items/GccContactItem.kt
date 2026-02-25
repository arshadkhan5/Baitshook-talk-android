/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <infoi@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.items

import android.text.TextUtils
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.R
import com.gcc.talk.gccAdapters.items.GccContactItem.ContactItemViewHolder
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.RvItemContactBinding
import com.gcc.talk.gccExtensions.loadUserAvatar
import com.gcc.talk.gccModels.json.participants.Participant
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.items.ISectionable
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.Objects
import java.util.regex.Pattern

class GccContactItem (
    /**
     * @return the model object
     */
    val model: Participant,
    private val user: GccUser,
    private var header: GccGenericTextHeaderItem?,
    private val viewThemeUtils: ViewThemeUtils
) : AbstractFlexibleItem<ContactItemViewHolder?>(),
    ISectionable<ContactItemViewHolder?, GccGenericTextHeaderItem?>,
    IFilterable<String?> {
    var isOnline: Boolean = true

    override fun equals(o: Any?): Boolean {
        if (o is GccContactItem) {
            return model.calculatedActorType == o.model.calculatedActorType &&
                model.calculatedActorId == o.model.calculatedActorId
        }
        return false
    }
    override fun hashCode(): Int = model.hashCode()

    override fun filter(constraint: String?): Boolean =
        model.displayName != null &&
            (
                Pattern.compile(constraint!!, Pattern.CASE_INSENSITIVE or Pattern.LITERAL)
                    .matcher(model.displayName!!.trim())
                    .find() ||
                    Pattern.compile(constraint!!, Pattern.CASE_INSENSITIVE or Pattern.LITERAL)
                        .matcher(model.calculatedActorId!!.trim())
                        .find()
                )

    override fun getLayoutRes(): Int = R.layout.rv_item_contact

    override fun createViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ): ContactItemViewHolder = ContactItemViewHolder(view, adapter)

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
        holder: ContactItemViewHolder?,
        position: Int,
        payloads: List<Any>?
    ) {
        if (model.selected) {
            holder?.binding?.checkedImageView?.let { viewThemeUtils.platform.colorImageView(it) }
            holder?.binding?.checkedImageView?.visibility = View.VISIBLE
        } else {
            holder?.binding?.checkedImageView?.visibility = View.GONE
        }

        if (!isOnline) {
            holder?.binding?.nameText?.setTextColor(
                ResourcesCompat.getColor(
                    holder.binding.nameText.context.resources,
                    R.color.medium_emphasis_text,
                    null
                )
            )
            holder?.binding?.avatarView?.alpha = SEMI_TRANSPARENT
        } else {
            holder?.binding?.nameText?.setTextColor(
                ResourcesCompat.getColor(
                    holder.binding.nameText.context.resources,
                    R.color.high_emphasis_text,
                    null
                )
            )
            holder?.binding?.avatarView?.alpha = FULLY_OPAQUE
        }

        holder?.binding?.nameText?.text = model.displayName

        if (adapter != null) {
            if (adapter.hasFilter()) {
                holder?.binding?.let {
                    viewThemeUtils.talk.themeAndHighlightText(
                        it.nameText,
                        model.displayName,
                        adapter.getFilter(String::class.java).toString()
                    )
                }
            }
        }

        if (TextUtils.isEmpty(model.displayName) &&
            (
                model.type == Participant.ParticipantType.GUEST ||
                    model.type == Participant.ParticipantType.USER_FOLLOWING_LINK
                )
        ) {
            holder?.binding?.nameText?.text = sharedApplication!!.getString(R.string.nc_guest)
        }

        setAvatar(holder)
    }

    private fun setAvatar(holder: ContactItemViewHolder?) {
        if (model.calculatedActorType == Participant.ActorType.GROUPS ||
            model.calculatedActorType == Participant.ActorType.CIRCLES
        ) {
            setGenericAvatar(holder!!, R.drawable.ic_avatar_group)
        } else if (model.calculatedActorType == Participant.ActorType.EMAILS) {
            setGenericAvatar(holder!!, R.drawable.ic_avatar_mail)
        } else if (model.calculatedActorType == Participant.ActorType.GUESTS ||
            model.type == Participant.ParticipantType.GUEST ||
            model.type == Participant.ParticipantType.GUEST_MODERATOR
        ) {
            var displayName: String?

            displayName = if (!TextUtils.isEmpty(model.displayName)) {
                model.displayName
            } else {
                Objects.requireNonNull(sharedApplication)!!.resources!!.getString(R.string.nc_guest)
            }

            // absolute fallback to prevent NPE deference
            if (displayName == null) {
                displayName = "Guest"
            }

            holder?.binding?.avatarView?.loadUserAvatar(user, displayName, true, false)
        } else if (model.calculatedActorType == Participant.ActorType.USERS) {
            holder?.binding?.avatarView
                ?.loadUserAvatar(
                    user,
                    model.calculatedActorId!!,
                    true,
                    false
                )
        }
    }

    private fun setGenericAvatar(holder: ContactItemViewHolder, roundPlaceholderDrawable: Int) {
        val avatar =
            viewThemeUtils.talk.themePlaceholderAvatar(
                holder.binding.avatarView,
                roundPlaceholderDrawable
            )

        holder.binding.avatarView.loadUserAvatar(avatar)
    }

    override fun getHeader(): GccGenericTextHeaderItem? = header

    override fun setHeader(p0: GccGenericTextHeaderItem?) {
        this.header = header
    }

    class ContactItemViewHolder(view: View?, adapter: FlexibleAdapter<*>?) : FlexibleViewHolder(view, adapter) {
        var binding: RvItemContactBinding =
            RvItemContactBinding.bind(view!!)
    }

    companion object {
        private const val FULLY_OPAQUE: Float = 1.0f
        private const val SEMI_TRANSPARENT: Float = 0.38f
    }
}
