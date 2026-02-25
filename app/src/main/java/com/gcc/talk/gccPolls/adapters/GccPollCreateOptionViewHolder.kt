/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.R
import com.gcc.talk.databinding.PollCreateOptionsItemBinding
import com.gcc.talk.gccUi.theme .ViewThemeUtils
import com.gcc.talk.gccUtils.GccEmojiTextInputEditText

class GccPollCreateOptionViewHolder(
    private val binding: PollCreateOptionsItemBinding,
    private val viewThemeUtils: ViewThemeUtils
) : RecyclerView.ViewHolder(binding.root) {

    lateinit var optionText: GccEmojiTextInputEditText
    private var textListener: TextWatcher? = null

    @SuppressLint("SetTextI18n")
    fun bind(
        pollCreateOptionItem: GccPollCreateOptionItem,
        itemsListener: GccPollCreateOptionsItemListener,
        position: Int,
        focus: Boolean
    ) {
        textListener?.let {
            binding.pollOptionTextEdit.removeTextChangedListener(it)
        }

        binding.pollOptionTextEdit.setText(pollCreateOptionItem.pollOption)
        viewThemeUtils.material.colorTextInputLayout(binding.pollOptionTextInputLayout)

        if (focus) {
            itemsListener.requestFocus(binding.pollOptionTextEdit)
        }

        binding.pollOptionDelete.setOnClickListener {
            itemsListener.onRemoveOptionsItemClick(pollCreateOptionItem, position)
        }

        textListener = getTextWatcher(pollCreateOptionItem, itemsListener)
        binding.pollOptionTextEdit.addTextChangedListener(textListener)
        binding.pollOptionTextInputLayout.hint = String.format(
            binding.pollOptionTextInputLayout.resources.getString(R.string.polls_option_hint),
            position + 1
        )

        binding.pollOptionDelete.contentDescription = String.format(
            binding.pollOptionTextInputLayout.resources.getString(R.string.polls_option_delete),
            position + 1
        )
    }

    private fun getTextWatcher(
        pollCreateOptionItem: GccPollCreateOptionItem,
        itemsListener: GccPollCreateOptionsItemListener
    ) = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            // unused atm
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // unused atm
        }

        override fun onTextChanged(option: CharSequence, start: Int, before: Int, count: Int) {
            pollCreateOptionItem.pollOption = option.toString()

            itemsListener.onOptionsItemTextChanged(pollCreateOptionItem)
        }
    }
}
