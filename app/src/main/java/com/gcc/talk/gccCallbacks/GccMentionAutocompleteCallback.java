/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccCallbacks;

import android.content.Context;
import android.text.Editable;
import android.text.Spanned;
import android.widget.EditText;

import com.gcc.talk.R;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccModels.json.mention.Mention;
import com.gcc.talk.gccUi.theme.ViewThemeUtils;
import com.gcc.talk.gccUtils.GccDisplayUtils;
import com.gcc.talk.gccUtils.GccCharPolicy;
import com.gcc.talk.gccUtils.text.GccSpans;
import com.otaliastudios.autocomplete.AutocompleteCallback;
import com.vanniktech.emoji.EmojiRange;
import com.vanniktech.emoji.Emojis;

import java.util.Objects;

import kotlin.OptIn;
import third.parties.fresco.BetterImageSpan;

public class GccMentionAutocompleteCallback implements AutocompleteCallback<Mention> {
    private final ViewThemeUtils viewThemeUtils;
    private Context context;
    private GccUser conversationUser;
    private EditText editText;

    public GccMentionAutocompleteCallback(Context context,
                                          GccUser conversationUser,
                                          EditText editText,
                                          ViewThemeUtils viewThemeUtils) {
        this.context = context;
        this.conversationUser = conversationUser;
        this.editText = editText;
        this.viewThemeUtils = viewThemeUtils;
    }

    @OptIn(markerClass = kotlin.ExperimentalStdlibApi.class)
    @Override
    public boolean onPopupItemClicked(Editable editable, Mention item) {
        GccCharPolicy.TextSpan range = GccCharPolicy.getQueryRange(editable);
        if (range == null) {
            return false;
        }
        String replacement = item.getLabel();

        StringBuilder replacementStringBuilder = new StringBuilder(Objects.requireNonNull(item.getLabel()));
        for (EmojiRange emojiRange : Emojis.emojis(replacement)) {
            replacementStringBuilder.delete(emojiRange.range.getStart(), emojiRange.range.getEndInclusive());
        }

        String charSequence = " ";
        editable.replace(range.getStart(), range.getEnd(), charSequence + replacementStringBuilder + " ");
        String id;
        if (item.getMentionId() != null) id = item.getMentionId(); else id = item.getId();
        GccSpans.MentionChipSpan mentionChipSpan =
            new GccSpans.MentionChipSpan(GccDisplayUtils.getDrawableForMentionChipSpan(context,
                                                                                       item.getId(),
                                                                                       item.getRoomToken(),
                                                                                       item.getLabel(),
                                                                                       conversationUser,
                                                                                       item.getSource(),
                                                                                       R.xml.chip_you,
                                                                                       editText,
                                                                                       viewThemeUtils,
                                                                                       "federated_users".equals(item.getSource())),
                                         BetterImageSpan.ALIGN_CENTER,
                                         id, item.getLabel());
        editable.setSpan(mentionChipSpan,
                         range.getStart() + charSequence.length(),
                         range.getStart() + replacementStringBuilder.length() + charSequence.length(),
                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        return true;
    }

    @Override
    public void onPopupVisibilityChanged(boolean shown) {

    }
}
