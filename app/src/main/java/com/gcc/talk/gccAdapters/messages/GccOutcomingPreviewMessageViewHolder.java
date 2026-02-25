/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2021 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages;

import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.gcc.talk.R;
import com.gcc.talk.databinding.ItemCustomOutcomingPreviewMessageBinding;
import com.gcc.talk.databinding.ItemThreadTitleBinding;
import com.gcc.talk.databinding.ReactionsInsideMessageBinding;
import com.gcc.talk.gccChat.data.model.GccChatMessage;
import com.gcc.talk.gccModels.json.chat.ReadStatus;
import com.gcc.talk.gccUtils.GccTextMatchers;

import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.emoji2.widget.EmojiTextView;

public class GccOutcomingPreviewMessageViewHolder extends GccPreviewMessageViewHolder {

    private final ItemCustomOutcomingPreviewMessageBinding binding;

    public GccOutcomingPreviewMessageViewHolder(View itemView) {
        super(itemView, null);
        binding = ItemCustomOutcomingPreviewMessageBinding.bind(itemView);
    }

    @Override
    public void onBind(@NonNull GccChatMessage message) {
        super.onBind(message);
        if(!message.isVoiceMessage()
            && !Objects.equals(message.getMessage(), "{file}")
        ) {
            Spanned processedMessageText = null;
            binding.bubble.setBackgroundResource(R.drawable.shape_grouped_outcoming_message);
            if (viewThemeUtils != null) {
                processedMessageText = messageUtils.enrichChatMessageText(
                    binding.messageCaption.getContext(),
                    message,
                    false,
                    viewThemeUtils);
                viewThemeUtils.talk.themeOutgoingMessageBubble(binding.bubble, true, false,
                                                               false);
            }

            if (processedMessageText != null) {
                processedMessageText = messageUtils.processMessageParameters(
                    binding.messageCaption.getContext(),
                    viewThemeUtils,
                    processedMessageText,
                    message,
                    binding.bubble);
            }
            binding.bubble.setOnClickListener(null);

            float textSize = 0;
            if (context != null) {
                textSize = context.getResources().getDimension(R.dimen.chat_text_size);
            }
            HashMap<String, HashMap<String, String>> messageParameters = message.getMessageParameters();
            if (
                (messageParameters == null || messageParameters.size() <= 0) &&
                    GccTextMatchers.isMessageWithSingleEmoticonOnly(message.getText())
            ) {
                textSize = (float)(textSize * GccIncomingTextMessageViewHolder.TEXT_SIZE_MULTIPLIER);
                itemView.setSelected(true);
            }
            binding.messageCaption.setVisibility(View.VISIBLE);
            binding.messageCaption.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            binding.messageCaption.setText(processedMessageText);
        } else {
            binding.bubble.setBackground(null);
            binding.messageCaption.setVisibility(View.GONE);
        }

        binding.messageText.setTextColor(ContextCompat.getColor(binding.messageText.getContext(),
                                                                R.color.no_emphasis_text));
        binding.messageTime.setTextColor(ContextCompat.getColor(binding.messageText.getContext(),
                                                                R.color.no_emphasis_text));

        binding.messageEditIndicator.setTextColor(ContextCompat.getColor(binding.messageText.getContext(),
                                                                         R.color.no_emphasis_text));

        binding.checkMark.setVisibility(View.GONE);
        Integer readStatusDrawableInt = null;
        String readStatusContentDescriptionString = null;
        if (message.getReadStatus() == ReadStatus.READ) {
            readStatusDrawableInt = R.drawable.ic_check_all;
            readStatusContentDescriptionString =
                binding.checkMark.getContext().getString(R.string.nc_message_read);
        } else if (message.getReadStatus() == ReadStatus.SENT) {
            readStatusDrawableInt = R.drawable.ic_check;
            readStatusContentDescriptionString =
                binding.checkMark.getContext().getString(R.string.nc_message_sent);
        }

        if (readStatusDrawableInt != null) {
            binding.checkMark.setVisibility(View.VISIBLE);
            binding.checkMark.setImageDrawable(ContextCompat.getDrawable(binding.checkMark.getContext(),
                                                                         readStatusDrawableInt));
            if (viewThemeUtils != null) {
                viewThemeUtils.talk.themeMessageCheckMark(binding.checkMark);
            }
        }
        binding.checkMark.setContentDescription(readStatusContentDescriptionString);


        if(!message.isThread()) {
            binding.threadTitleWrapperContainer.setVisibility(View.GONE);
        } else {
            binding.threadTitleWrapperContainer.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public EmojiTextView getMessageText() {
        return binding.messageText;
    }

    @Override
    public ProgressBar getProgressBar() {
        return binding.progressBar;
    }

    @NonNull
    @Override
    public View getPreviewContainer() {
        return binding.previewContainer;
    }

    @NonNull
    @Override
    public MaterialCardView getPreviewContactContainer() {
        return binding.contactContainer;
    }

    @NonNull
    @Override
    public ImageView getPreviewContactPhoto() {
        return binding.contactPhoto;
    }

    @NonNull
    @Override
    public EmojiTextView getPreviewContactName() {
        return binding.contactName;
    }

    @Override
    public ProgressBar getPreviewContactProgressBar() {
        return binding.contactProgressBar;
    }

    @Override
    public ReactionsInsideMessageBinding getReactionsBinding() { return binding.reactions; }

    @Override
    public ItemThreadTitleBinding getThreadsBinding(){ return binding.threadTitleWrapper; }

    @NonNull
    @Override
    public EmojiTextView getMessageCaption() { return binding.messageCaption; }

    @NonNull
    @Override
    public TextView getMessageEditIndicator() {
        return binding.messageEditIndicator;
    }
}
