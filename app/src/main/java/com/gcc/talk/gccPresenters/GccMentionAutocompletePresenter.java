/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPresenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.gcc.talk.gccAdapters.items.GccMentionAutocompleteItem;
import com.gcc.talk.gccApi.GccNcApi;
import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccModels.json.mention.Mention;
import com.gcc.talk.gccModels.json.mention.MentionOverall;
import com.gcc.talk.gccUi.theme.ViewThemeUtils;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccUtils.GccApiUtils;
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld;
import com.otaliastudios.autocomplete.RecyclerViewPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import autodagger.AutoInjector;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@AutoInjector(GccTalkApplication.class)
public class GccMentionAutocompletePresenter extends RecyclerViewPresenter<Mention> implements FlexibleAdapter.OnItemClickListener {
    private static final String TAG = "GccMentionAutocompletePresenter";

    @Inject
    GccNcApi ncApi;

    @Inject
    GccUserManager userManager;

    @Inject
    GccCurrentUserProviderOld currentUserProvider;

    @Inject
    ViewThemeUtils viewThemeUtils;

    private GccUser currentUser;
    private FlexibleAdapter<AbstractFlexibleItem> adapter;
    private Context context;

    private String roomToken;
    private int chatApiVersion;

    private List<AbstractFlexibleItem> abstractFlexibleItemList = new ArrayList<>();

    public GccMentionAutocompletePresenter(Context context) {
        super(context);
        this.context = context;
        GccTalkApplication.Companion.getSharedApplication().getComponentApplication().inject(this);
        currentUser = currentUserProvider.getCurrentUser().blockingGet();
    }

    public GccMentionAutocompletePresenter(Context context, String roomToken, int chatApiVersion) {
        super(context);
        this.roomToken = roomToken;
        this.context = context;
        this.chatApiVersion = chatApiVersion;
        GccTalkApplication.Companion.getSharedApplication().getComponentApplication().inject(this);
        currentUser = currentUserProvider.getCurrentUser().blockingGet();
    }

    @Override
    protected RecyclerView.Adapter instantiateAdapter() {
        adapter = new FlexibleAdapter<>(abstractFlexibleItemList, context, false);
        adapter.addListener(this);
        return adapter;
    }

    @Override
    protected PopupDimensions getPopupDimensions() {
        PopupDimensions popupDimensions = new PopupDimensions();
        popupDimensions.width = ViewGroup.LayoutParams.MATCH_PARENT;
        popupDimensions.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        return popupDimensions;
    }

    @Override
    protected void onQuery(@Nullable CharSequence query) {

        String queryString;
        if (query != null && query.length() > 1) {
            queryString = String.valueOf(query.subSequence(1, query.length()));
        } else {
            queryString = "";
        }

        adapter.setFilter(queryString);

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("includeStatus", "true");

        ncApi.getMentionAutocompleteSuggestions(
                        GccApiUtils.getCredentials(currentUser.getUsername(), currentUser.getToken()),
                        GccApiUtils.getUrlForMentionSuggestions(chatApiVersion, currentUser.getBaseUrl(), roomToken),
                        queryString, 5, queryMap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .retry(3)
            .subscribe(new Observer<MentionOverall>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    // no actions atm
                }

                @Override
                public void onNext(@NonNull MentionOverall mentionOverall) {
                    if (mentionOverall.getOcs() != null) {
                        List<Mention> mentionsList = mentionOverall.getOcs().getData();

                        if (mentionsList != null) {

                            if (mentionsList.isEmpty()) {
                                adapter.clear();
                            } else {
                                List<AbstractFlexibleItem> internalAbstractFlexibleItemList =
                                    new ArrayList<>(mentionsList.size());
                                for (Mention mention : mentionsList) {
                                    internalAbstractFlexibleItemList.add(
                                        new GccMentionAutocompleteItem(
                                            mention,
                                            currentUser,
                                            context,
                                            roomToken,
                                            viewThemeUtils));
                                }

                                if (adapter.getItemCount() != 0) {
                                    adapter.clear();
                                }

                                adapter.updateDataSet(internalAbstractFlexibleItemList);
                            }
                        }
                    }
                }

                @SuppressLint("LongLogTag")
                @Override
                public void onError(@NonNull Throwable e) {
                    adapter.clear();
                    Log.e(TAG, "failed to get MentionAutocompleteSuggestions", e);
                }

                @Override
                public void onComplete() {
                    // no actions atm
                }
            });
    }

    @Override
    public boolean onItemClick(View view, int position) {
        Mention mention = new Mention();
        GccMentionAutocompleteItem mentionAutocompleteItem = (GccMentionAutocompleteItem) adapter.getItem(position);
        if (mentionAutocompleteItem != null) {
            String mentionId = mentionAutocompleteItem.mentionId;
            if (mentionId != null) {
                mention.setMentionId(mentionId);
            }
            mention.setId(mentionAutocompleteItem.objectId);
            mention.setLabel(mentionAutocompleteItem.displayName);
            mention.setSource(mentionAutocompleteItem.source);
            mention.setRoomToken(mentionAutocompleteItem.roomToken);
            dispatchClick(mention);
        }
        return true;
    }
}
