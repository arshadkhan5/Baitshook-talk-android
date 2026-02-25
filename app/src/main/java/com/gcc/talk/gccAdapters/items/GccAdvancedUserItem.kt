/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <infoi@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.items

import android.accounts.Account
import android.text.TextUtils
import android.view.View
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.R
import com.gcc.talk.gccAdapters.items.GccAdvancedUserItem.UserItemViewHolder
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.AccountItemBinding
import com.gcc.talk.gccExtensions.loadUserAvatar
import com.gcc.talk.gccModels.json.participants.Participant
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.regex.Pattern

class GccAdvancedUserItem(
    /**
     * @return the model object
     */
    val model: Participant,
    @JvmField val user: GccUser?,
    val account: Account?,
    private val viewThemeUtils: ViewThemeUtils,
    private val actionRequiredCount: Int
) : AbstractFlexibleItem<UserItemViewHolder>(),
    IFilterable<String?> {

    override fun equals(other: Any?): Boolean =
        if (other is GccAdvancedUserItem) {
            model == other.model
        } else {
            false
        }

    override fun hashCode(): Int = model.hashCode()

    override fun getLayoutRes(): Int = R.layout.account_item

    override fun createViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ): UserItemViewHolder = UserItemViewHolder(view, adapter)

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: UserItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (adapter.hasFilter()) {
            viewThemeUtils.talk.themeAndHighlightText(
                holder.binding.userName,
                model.displayName,
                adapter.getFilter(String::class.java).toString()
            )
        } else {
            holder.binding.userName.text = model.displayName
        }
        if (user != null && !TextUtils.isEmpty(user.baseUrl)) {
            val host = user.baseUrl!!.toUri().host
            if (!TextUtils.isEmpty(host)) {
                holder.binding.account.text = user.baseUrl!!.toUri().host
            } else {
                holder.binding.account.text = user.baseUrl
            }
        }
        if (user?.baseUrl != null &&
            (user.baseUrl!!.startsWith("http://") || user.baseUrl!!.startsWith("https://"))
        ) {
            holder.binding.userIcon.loadUserAvatar(user, model.calculatedActorId!!, true, false)
        }
        if (actionRequiredCount > 0) {
            holder.binding.actionRequired.visibility = View.VISIBLE
        } else {
            holder.binding.actionRequired.visibility = View.GONE
        }
    }

    override fun filter(constraint: String?): Boolean =
        model.displayName != null &&
            Pattern
                .compile(constraint, Pattern.CASE_INSENSITIVE or Pattern.LITERAL)
                .matcher(model.displayName!!.trim())
                .find()

    class UserItemViewHolder(view: View?, adapter: FlexibleAdapter<*>?) : FlexibleViewHolder(view, adapter) {
        var binding: AccountItemBinding

        /**
         * Default constructor.
         */
        init {
            binding = AccountItemBinding.bind(view!!)
        }
    }
}
