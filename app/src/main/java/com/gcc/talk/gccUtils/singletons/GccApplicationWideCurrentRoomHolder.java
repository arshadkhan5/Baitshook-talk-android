/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.singletons;

import android.util.Log;

import com.gcc.talk.gccData.user.model.GccUser;

public class GccApplicationWideCurrentRoomHolder {

    public static final String TAG = "GccApplicationWideCurrentRoomHolder";
    private static final GccApplicationWideCurrentRoomHolder holder = new GccApplicationWideCurrentRoomHolder();
//    private String currentRoomId = "";
    private String currentRoomToken = "";
    private GccUser userInRoom = new GccUser();
    private boolean inCall = false;
    private boolean isDialing = false;
    private String session = "";

    private Long callStartTime = null;

    public static GccApplicationWideCurrentRoomHolder getInstance() {
        return holder;
    }

    public void clear() {
        Log.d(TAG, "GccApplicationWideCurrentRoomHolder was cleared");
//        currentRoomId = "";
        userInRoom = new GccUser();
        inCall = false;
        isDialing = false;
        currentRoomToken = "";
        session = "";
    }

    public String getCurrentRoomToken() {
        return currentRoomToken;
    }

    public void setCurrentRoomToken(String currentRoomToken) {
        this.currentRoomToken = currentRoomToken;
    }

//    public String getCurrentRoomId() {
//        return currentRoomId;
//    }
//
//    public void setCurrentRoomId(String currentRoomId) {
//        this.currentRoomId = currentRoomId;
//    }

    public GccUser getUserInRoom() {
        return userInRoom;
    }

    public void setUserInRoom(GccUser userInRoom) {
        this.userInRoom = userInRoom;
    }

    public boolean isInCall() {
        return inCall;
    }

    public void setInCall(boolean inCall) {
        this.inCall = inCall;
    }

    public boolean isDialing() {
        return isDialing;
    }

    public void setDialing(boolean dialing) {
        isDialing = dialing;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Long getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(Long callStartTime) {
        this.callStartTime = callStartTime;
    }
}
