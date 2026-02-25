/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccFilebrowser.webdav;

import android.net.Uri;
import android.util.Log;

import com.gcc.talk.gccFilebrowser.models.GccBrowserFile;
import com.gcc.talk.gccFilebrowser.models.GccDavResponse;
import com.gcc.talk.gccDagger.modules.GccRestModule;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccUtils.GccApiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.bitfire.dav4jvm.DavResource;
import at.bitfire.dav4jvm.Response;
import at.bitfire.dav4jvm.exception.DavException;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class GccReadFilesystemOperation {
    private static final String TAG = "GccReadFilesystemOperation";
    private final OkHttpClient okHttpClient;
    private final String url;
    private final int depth;
    private final String basePath;

    public GccReadFilesystemOperation(OkHttpClient okHttpClient, GccUser currentUser, String path, int depth) {
        OkHttpClient.Builder okHttpClientBuilder = okHttpClient.newBuilder();
        okHttpClientBuilder.followRedirects(false);
        okHttpClientBuilder.followSslRedirects(false);
        okHttpClientBuilder.authenticator(
                new GccRestModule.HttpAuthenticator(
                        GccApiUtils.getCredentials(
                                currentUser.getUsername(),
                                currentUser.getToken()
                                                  ),
                        "Authorization")
                                         );
        this.okHttpClient = okHttpClientBuilder.build();
        this.basePath = currentUser.getBaseUrl() + GccDavUtils.DAV_PATH + currentUser.getUserId();
        this.url = basePath + Uri.encode(path, "/");
        this.depth = depth;
    }

    public GccDavResponse readRemotePath() {
        GccDavResponse davResponse = new GccDavResponse();
        final List<Response> memberElements = new ArrayList<>();
        final Response[] rootElement = new Response[1];

        try {
            new DavResource(okHttpClient, HttpUrl.parse(url)).propfind(depth, GccDavUtils.getAllPropSet(),
                    new Function2<Response, Response.HrefRelation, Unit>() {
                        @Override
                        public Unit invoke(Response response, Response.HrefRelation hrefRelation) {
                            davResponse.setResponse(response);
                            switch (hrefRelation) {
                                case MEMBER:
                                    memberElements.add(response);
                                    break;
                                case SELF:
                                    rootElement[0] = response;
                                    break;
                                case OTHER:
                                default:
                            }
                            return Unit.INSTANCE;
                        }
                    });
        } catch (IOException | DavException e) {
            Log.w(TAG, "Error reading remote path");
        }

        final List<GccBrowserFile> remoteFiles = new ArrayList<>(1 + memberElements.size());
        remoteFiles.add(GccBrowserFile.Companion.getModelFromResponse(rootElement[0],
                                                                      rootElement[0].getHref().toString().substring(basePath.length())));
        for (Response memberElement : memberElements) {
            remoteFiles.add(GccBrowserFile.Companion.getModelFromResponse(memberElement,
                                                                          memberElement.getHref().toString().substring(basePath.length())));
        }

        davResponse.setData(remoteFiles);
        return davResponse;
    }
}
