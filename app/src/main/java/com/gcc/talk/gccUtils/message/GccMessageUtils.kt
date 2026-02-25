/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.message

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.gcc.talk.R
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccDisplayUtils
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListDrawable
import io.noties.markwon.ext.tasklist.TaskListPlugin

class GccMessageUtils(val context: Context) {
    fun enrichChatReplyMessageText(
        context: Context,
        message: GccChatMessage,
        incoming: Boolean,
        viewThemeUtils: ViewThemeUtils
    ): Spanned? =
        if (message.message == null) {
            null
        } else if (message.renderMarkdown == false) {
            SpannableString(GccDisplayUtils.ellipsize(message.text, MAX_REPLY_LENGTH))
        } else {
            enrichChatMessageText(
                context,
                GccDisplayUtils.ellipsize(message.text, MAX_REPLY_LENGTH),
                incoming,
                viewThemeUtils
            )
        }

    fun enrichChatMessageText(
        context: Context,
        message: GccChatMessage,
        incoming: Boolean,
        viewThemeUtils: ViewThemeUtils
    ): Spanned? =
        if (message.message == null) {
            null
        } else if (message.renderMarkdown == false) {
            SpannableString(message.message)
        } else {
            val newMessage = message.message!!.replace("\n", "  \n", false)
            enrichChatMessageText(context, newMessage, incoming, viewThemeUtils)
        }

    fun enrichChatMessageText(
        context: Context,
        message: String,
        incoming: Boolean,
        viewThemeUtils: ViewThemeUtils
    ): Spanned = viewThemeUtils.talk.themeMarkdown(context, message, incoming)

    fun processMessageParameters(
        themingContext: Context,
        viewThemeUtils: ViewThemeUtils,
        spannedText: Spanned,
        message: GccChatMessage,
        itemView: View?
    ): Spanned {
        var processedMessageText = spannedText
        val messageParameters = message.messageParameters
        if (messageParameters != null && messageParameters.size > 0) {
            processedMessageText = processMessageParameters(
                themingContext,
                viewThemeUtils,
                messageParameters,
                message,
                processedMessageText,
                itemView
            )
        }
        return processedMessageText
    }

    @Suppress("NestedBlockDepth", "LongParameterList")
    private fun processMessageParameters(
        themingContext: Context,
        viewThemeUtils: ViewThemeUtils,
        messageParameters: HashMap<String?, HashMap<String?, String?>>,
        message: GccChatMessage,
        messageString: Spanned,
        itemView: View?
    ): Spanned {
        var messageStringInternal = messageString
        for (key in messageParameters.keys) {
            val individualHashMap = message.messageParameters?.get(key)
            if (individualHashMap != null) {
                when (individualHashMap["type"]) {
                    "user", "guest", "call", "user-group", "email", "circle" -> {
                        val chip = if (individualHashMap["id"]?.equals(message.activeUser?.userId) == true) {
                            R.xml.chip_you
                        } else {
                            R.xml.chip_others
                        }
                        val id = if (individualHashMap["server"] != null) {
                            individualHashMap["id"] + "@" + individualHashMap["server"]
                        } else {
                            individualHashMap["id"]
                        }

                        val name = individualHashMap["name"]
                        val type = individualHashMap["type"]
                        val user = message.activeUser
                        if (user == null || key == null) break
                        if (id == null || name == null || type == null) break

                        messageStringInternal = GccDisplayUtils.searchAndReplaceWithMentionSpan(
                            key,
                            themingContext,
                            messageStringInternal,
                            id,
                            message.token,
                            name,
                            type,
                            user,
                            chip,
                            viewThemeUtils,
                            individualHashMap["server"] != null
                        )
                    }

                    "file" -> {
                        itemView?.setOnClickListener { v ->
                            val browserIntent = Intent(Intent.ACTION_VIEW, individualHashMap["link"]?.toUri())
                            context.startActivity(browserIntent)
                        }
                    }
                    else -> {
                        messageStringInternal = defaultMessageParameters(messageStringInternal, individualHashMap, key)
                    }
                }
            }
        }
        return messageStringInternal
    }

    fun processEditMessageParameters(
        messageParameters: HashMap<String?, HashMap<String?, String?>>,
        message: GccChatMessage?,
        inputEditText: String
    ): Spanned {
        var result = inputEditText
        for (key in messageParameters.keys) {
            val individualHashMap = message?.messageParameters?.get(key)
            if (individualHashMap != null) {
                val mentionId = individualHashMap["mention-id"]
                val type = individualHashMap["type"]
                val name = individualHashMap["name"]
                val placeholder = "@$name"
                result = when (type) {
                    "user", "guest", "email" -> result.replace(placeholder, "@$mentionId", ignoreCase = false)
                    "user-group", "circle" -> result.replace(placeholder, "@\"$mentionId\"", ignoreCase = false)
                    "call" -> result.replace(placeholder, "@all", ignoreCase = false)
                    else -> result
                }
            }
        }
        return SpannableString(result)
    }

    private fun defaultMessageParameters(
        messageString: Spanned,
        individualHashMap: HashMap<String?, String?>,
        key: String?
    ): Spanned {
        val spannable = SpannableStringBuilder(messageString)
        val placeholder = "{$key}"
        val replacementText = individualHashMap["name"]
        var start = spannable.indexOf(placeholder)
        while (start != -1) {
            val end = start + placeholder.length
            spannable.replace(start, end, replacementText)
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                start + replacementText!!.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = spannable.indexOf(placeholder, start + replacementText.length)
        }
        return spannable
    }

    fun getRenderedMarkdownText(context: Context, markdown: String, textColor: Int): Spanned {
        val drawable = TaskListDrawable(textColor, textColor, context.getColor(R.color.bg_default))
        val markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder.isLinkUnderlined(true).headingBreakHeight(0)
            }

            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                builder.linkResolver { view: View?, link: String? ->
                    Log.i(TAG, "Link action not implemented $view / $link")
                }
            }
        })
            .usePlugin(TaskListPlugin.create(drawable))
            .usePlugin(TablePlugin.create { _ -> })
            .usePlugin(StrikethroughPlugin.create()).build()
        return markwon.toMarkdown(markdown)
    }

    companion object {
        private const val TAG = "GccMessageUtils"
        const val MAX_REPLY_LENGTH = 250
    }
}
