/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2021 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccApi;

import com.gcc.talk.gccModels.json.capabilities.CapabilitiesOverall;
import com.gcc.talk.gccModels.json.capabilities.RoomCapabilitiesOverall;
import com.gcc.talk.gccModels.json.chat.ChatOverall;
import com.gcc.talk.gccModels.json.chat.ChatOverallSingleMessage;
import com.gcc.talk.gccModels.json.chat.ChatShareOverall;
import com.gcc.talk.gccModels.json.chat.ChatShareOverviewOverall;
import com.gcc.talk.gccModels.json.conversations.RoomOverall;
import com.gcc.talk.gccModels.json.conversations.RoomsOverall;
import com.gcc.talk.gccModels.json.generic.GenericOverall;
import com.gcc.talk.gccModels.json.generic.Status;
import com.gcc.talk.gccModels.json.hovercard.HoverCardOverall;
import com.gcc.talk.gccModels.json.invitation.InvitationOverall;
import com.gcc.talk.gccModels.json.mention.MentionOverall;
import com.gcc.talk.gccModels.json.notifications.NotificationOverall;
import com.gcc.talk.gccModels.json.opengraph.OpenGraphOverall;
import com.gcc.talk.gccModels.json.participants.AddParticipantOverall;
import com.gcc.talk.gccModels.json.participants.ParticipantsOverall;
import com.gcc.talk.gccModels.json.push.PushRegistrationOverall;
import com.gcc.talk.gccModels.json.reactions.ReactionsOverall;
import com.gcc.talk.gccModels.json.reminder.ReminderOverall;
import com.gcc.talk.gccModels.json.search.ContactsByNumberOverall;
import com.gcc.talk.gccModels.json.signaling.SignalingOverall;
import com.gcc.talk.gccModels.json.signaling.settings.SignalingSettingsOverall;
import com.gcc.talk.gccModels.json.status.StatusOverall;
import com.gcc.talk.gccModels.json.unifiedsearch.UnifiedSearchOverall;
import com.gcc.talk.gccModels.json.userprofile.UserProfileFieldsOverall;
import com.gcc.talk.gccModels.json.userprofile.UserProfileOverall;
import com.gcc.talk.gccPolls.repositories.model.GccPollOverall;
import com.gcc.talk.gccTranslate.repositories.model.GccLanguagesOverall;
import com.gcc.talk.gccTranslate.repositories.model.GccTranslationsOverall;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import kotlin.Unit;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface GccNcApi {

    /*
        QueryMap items are as follows:
            - "format" : "json"
            - "search" : ""
            - "perPage" : "200"
            - "itemType" : "call"

        Server URL is: baseUrl + ocsApiVersion + /apps/files_sharing/api/v1/sharees

        or if we're on 14 and up:

        baseUrl + ocsApiVersion + "/core/autocomplete/get");

     */
    @GET
    Observable<ResponseBody> getContactsWithSearchParam(@Header("Authorization") String authorization,
                                                        @Url String url,
                                                        @Nullable @Query("shareTypes[]") List<String> listOfShareTypes,
                                                        @QueryMap Map<String, Object> options);


    /*
        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /room
     */
    @GET
    Observable<RoomsOverall> getRooms(@Header("Authorization") String authorization,
                                      @Url String url,
                                      @Nullable @Query("includeStatus") Boolean includeStatus);

    /*
        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /room/roomToken
    */
    @GET
    Observable<RoomOverall> getRoom(@Header("Authorization") String authorization, @Url String url);

    /*
        QueryMap items are as follows:
            - "roomType" : ""
            - "invite" : ""

        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /room
     */

    @POST
    Observable<RoomOverall> createRoom(@Header("Authorization") String authorization,
                                       @Url String url,
                                       @QueryMap Map<String, String> options);

    /*
        QueryMap items are as follows:
            - "newParticipant" : "user"

        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /room/roomToken/participants
    */
    @POST
    Observable<AddParticipantOverall> addParticipant(@Header("Authorization") String authorization,
                                                     @Url String url,
                                                     @QueryMap Map<String,
                                                         String> options);

    @POST
    Observable<GenericOverall> resendParticipantInvitations(@Header("Authorization") String authorization,
                                                            @Url String url);

    // also used for removing a guest from a conversation
    @Deprecated
    @DELETE
    Observable<GenericOverall> removeParticipantFromConversation(@Header("Authorization") String authorization,
                                                                 @Url String url,
                                                                 @Query("participant") String participantId);

    @DELETE
    Observable<GenericOverall> removeAttendeeFromConversation(@Header("Authorization") String authorization,
                                                              @Url String url,
                                                              @Query("attendeeId") Long attendeeId);

    @Deprecated
    @POST
    Observable<GenericOverall> promoteUserToModerator(@Header("Authorization") String authorization,
                                                      @Url String url,
                                                      @Query("participant") String participantId);

    @Deprecated
    @DELETE
    Observable<GenericOverall> demoteModeratorToUser(@Header("Authorization") String authorization,
                                                     @Url String url,
                                                     @Query("participant") String participantId);

    @POST
    Observable<GenericOverall> promoteAttendeeToModerator(@Header("Authorization") String authorization,
                                                          @Url String url,
                                                          @Query("attendeeId") Long attendeeId);

    @DELETE
    Observable<GenericOverall> demoteAttendeeFromModerator(@Header("Authorization") String authorization,
                                                           @Url String url,
                                                           @Query("attendeeId") Long attendeeId);

    /*
        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /room/roomToken/participants/self
     */

    @DELETE
    Observable<GenericOverall> removeSelfFromRoom(@Header("Authorization") String authorization, @Url String url);

    @DELETE
    Observable<GenericOverall> deleteRoom(@Header("Authorization") String authorization, @Url String url);

    /*
        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /call/callToken
    */
    @GET
    Observable<ParticipantsOverall> getPeersForCall(@Header("Authorization") String authorization, @Url String url);

    @GET
    Observable<ParticipantsOverall> getPeersForCall(@Header("Authorization") String authorization,
                                                    @Url String url,
                                                    @QueryMap Map<String, Boolean> fields);

    @FormUrlEncoded
    @POST
    Observable<RoomOverall> joinRoom(@Nullable @Header("Authorization") String authorization,
                                     @Url String url,
                                     @Nullable @Field("password") String password);

    @DELETE
    Observable<GenericOverall> leaveRoom(@Nullable @Header("Authorization") String authorization, @Url String url);

    /*
        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /call/callToken
    */

    @FormUrlEncoded
    @POST
    Observable<GenericOverall> joinCall(@Nullable @Header("Authorization") String authorization,
                                        @Url String url,
                                        @Field("flags") Integer inCall,
                                        @Field("silent") Boolean callWithoutNotification,
                                        @Nullable @Field("recordingConsent") Boolean recordingConsent);

    /*
    Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /call/callToken
    */
    @DELETE
    Observable<GenericOverall> leaveCall(@Nullable @Header("Authorization") String authorization, @Url String url,
                                         @Nullable @Query("all") Boolean all);

    @GET
    Observable<SignalingSettingsOverall> getSignalingSettings(@Nullable @Header("Authorization") String authorization,
                                                              @Url String url);

    /*
        QueryMap items are as follows:
            - "messages" : "message"

        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /signaling
    */
    @FormUrlEncoded
    @POST
    Observable<SignalingOverall> sendSignalingMessages(@Nullable @Header("Authorization") String authorization,
                                                       @Url String url,
                                                       @Field("messages") String messages);

    /*
        Server URL is: baseUrl + ocsApiVersion + spreedApiVersion + /signaling
    */
    @GET
    Observable<SignalingOverall> pullSignalingMessages(@Nullable @Header("Authorization") String authorization,
                                                       @Url String url);

     /*
        QueryMap items are as follows:
            - "format" : "json"

        Server URL is: baseUrl + ocsApiVersion + "/cloud/user"
    */

    @GET
    Observable<UserProfileOverall> getUserProfile(@Header("Authorization") String authorization, @Url String url);


    @GET
    Observable<UserProfileOverall> getUserData(@Header("Authorization") String authorization, @Url String url);

    @DELETE
    Observable<GenericOverall> revertStatus(@Header("Authentication") String authorization, @Url String url);

    @FormUrlEncoded
    @PUT
    Observable<GenericOverall> setUserData(@Header("Authorization") String authorization,
                                           @Url String url,
                                           @Field("key") String key,
                                           @Field("value") String value);


    /*
        Server URL is: baseUrl + /status.php
     */
    @GET
    Observable<Status> getServerStatus(@Url String url);


    /*
        QueryMap items are as follows:
            - "format" : "json"
            - "pushTokenHash" : ""
            - "devicePublicKey" : ""
            - "proxyServer" : ""

        Server URL is: baseUrl + ocsApiVersion + "/apps/notifications/api/v2/push
     */

    @FormUrlEncoded
    @POST
    Observable<PushRegistrationOverall> registerDeviceForNotificationsWithNextcloud(
        @Header("Authorization") String authorization,
        @Url String url,
        @FieldMap Map<String, String> options);

    @DELETE
    Observable<GenericOverall> unregisterDeviceForNotificationsWithNextcloud(
        @Header("Authorization") String authorization,
        @Url String url);

    @FormUrlEncoded
    @POST
    Observable<Unit> registerDeviceForNotificationsWithPushProxy(@Url String url,
                                                                 @FieldMap Map<String, String> fields);


    /*
        QueryMap items are as follows:
          - "deviceIdentifier": "{{deviceIdentifier}}",
          - "deviceIdentifierSignature": "{{signature}}",
          - "userPublicKey": "{{userPublicKey}}"
    */
    @DELETE
    Observable<Void> unregisterDeviceForNotificationsWithProxy(@Url String url,
                                                               @QueryMap Map<String, String> fields);

    @FormUrlEncoded
    @PUT
    Observable<Response<GenericOverall>> setPassword2(@Header("Authorization") String authorization,
                                                      @Url String url,
                                                      @Field("password") String password);

    @GET
    Observable<CapabilitiesOverall> getCapabilities(@Header("Authorization") String authorization, @Url String url);

    @GET
    Observable<CapabilitiesOverall> getCapabilities(@Url String url);

    @GET
    Observable<RoomCapabilitiesOverall> getRoomCapabilities(@Header("Authorization") String authorization,
                                                            @Url String url);

    /*
       QueryMap items are as follows:
         - "lookIntoFuture": int (0 or 1),
         - "limit" : int, range 100-200,
         - "timeout": used with look into future, 30 default, 60 at most
         - "lastKnownMessageId", int, use one from X-Chat-Last-Given
   */
    @GET
    Observable<Response<ChatOverall>> pullChatMessages(@Header("Authorization") String authorization,
                                                       @Url String url,
                                                       @QueryMap Map<String, Integer> fields);

    /*
        Fieldmap items are as follows:
          - "message": ,
          - "actorDisplayName"
    */

    @FormUrlEncoded
    @POST
    Observable<ChatOverallSingleMessage> sendChatMessage(@Header("Authorization") String authorization,
                                               @Url String url,
                                               @Field("message") CharSequence message,
                                               @Field("actorDisplayName") String actorDisplayName,
                                               @Field("replyTo") Integer replyTo,
                                               @Field("silent") Boolean sendWithoutNotification,
                                               @Field("referenceId") String referenceId
                                               );

    @GET
    Observable<Response<ChatShareOverall>> getSharedItems(
        @Header("Authorization") String authorization,
        @Url String url,
        @Query("objectType") String objectType,
        @Nullable @Query("lastKnownMessageId") Integer lastKnownMessageId,
        @Nullable @Query("limit") Integer limit);

    @GET
    Observable<Response<ChatShareOverviewOverall>> getSharedItemsOverview(@Header("Authorization") String authorization,
                                                                          @Url String url,
                                                                          @Nullable @Query("limit") Integer limit);


    @GET
    Observable<MentionOverall> getMentionAutocompleteSuggestions(@Header("Authorization") String authorization,
                                                                 @Url String url,
                                                                 @Query("search") String query,
                                                                 @Nullable @Query("limit") Integer limit,
                                                                 @QueryMap Map<String, String> fields);

    @GET
    Observable<NotificationOverall> getNcNotification(@Header("Authorization") String authorization,
                                                      @Url String url);

    @FormUrlEncoded
    @POST
    Observable<GenericOverall> setNotificationLevel(@Header("Authorization") String authorization,
                                                    @Url String url,
                                                    @Field("level") int level);

    @FormUrlEncoded
    @PUT
    Observable<GenericOverall> setConversationReadOnly(@Header("Authorization") String authorization,
                                                @Url String url,
                                                @Field("state") int state);

    @FormUrlEncoded
    @POST
    Observable<GenericOverall> createRemoteShare(@Nullable @Header("Authorization") String authorization,
                                                 @Url String url,
                                                 @Field("path") String remotePath,
                                                 @Field("shareWith") String roomToken,
                                                 @Field("shareType") String shareType,
                                                 @Field("talkMetaData") String talkMetaData);

    @FormUrlEncoded
    @PUT
    Observable<GenericOverall> setLobbyForConversation(@Header("Authorization") String authorization,
                                                       @Url String url,
                                                       @Field("state") Integer state,
                                                       @Field("timer") Long timer);

    @POST
    Observable<ContactsByNumberOverall> searchContactsByPhoneNumber(@Header("Authorization") String authorization,
                                                                    @Url String url,
                                                                    @Body RequestBody search);

    @PUT
    Observable<Response<GenericOverall>> uploadFile(@Header("Authorization") String authorization,
                                                    @Url String url,
                                                    @Body RequestBody body);

    @HEAD
    Observable<Response<Void>> checkIfFileExists(@Header("Authorization") String authorization,
                                                 @Url String url);

    @GET
    Call<ResponseBody> downloadFile(@Header("Authorization") String authorization,
                                    @Url String url);

    @DELETE
    Observable<ChatOverallSingleMessage> deleteChatMessage(@Header("Authorization") String authorization,
                                                           @Url String url);

    @DELETE
    Observable<GenericOverall> deleteAvatar(@Header("Authorization") String authorization, @Url String url);

    @DELETE
    Observable<RoomOverall> deleteConversationAvatar(@Header("Authorization") String authorization, @Url String url);


    @Multipart
    @POST
    Observable<GenericOverall> uploadAvatar(@Header("Authorization") String authorization,
                                            @Url String url,
                                            @Part MultipartBody.Part attachment);

    @Multipart
    @POST
    Observable<RoomOverall> uploadConversationAvatar(@Header("Authorization") String authorization,
                                                     @Url String url,
                                                     @Part MultipartBody.Part attachment);

    @GET
    Observable<UserProfileFieldsOverall> getEditableUserProfileFields(@Header("Authorization") String authorization,
                                                                      @Url String url);

    @GET
    Call<ResponseBody> downloadResizedImage(@Header("Authorization") String authorization,
                                            @Url String url);

    @FormUrlEncoded
    @POST
    Observable<GenericOverall> sendLocation(@Header("Authorization") String authorization,
                                            @Url String url,
                                            @Field("objectType") String objectType,
                                            @Field("objectId") String objectId,
                                            @Field("metaData") String metaData);

    @FormUrlEncoded
    @POST
    Observable<GenericOverall> notificationCalls(@Header("Authorization") String authorization,
                                                 @Url String url,
                                                 @Field("level") Integer level);

    @DELETE
    Observable<GenericOverall> clearChatHistory(@Header("Authorization") String authorization, @Url String url);

    @GET
    Observable<HoverCardOverall> hoverCard(@Header("Authorization") String authorization, @Url String url);

    // Url is: /api/{apiVersion}/chat/{token}/read
    @FormUrlEncoded
    @POST
    Observable<GenericOverall> setChatReadMarker(@Header("Authorization") String authorization,
                                                 @Url String url,
                                                 @Nullable @Field("lastReadMessage") Integer lastReadMessage);

    // Url is: /api/{apiVersion}/chat/{token}/read
    @DELETE
    Observable<GenericOverall> markRoomAsUnread(@Header("Authorization") String authorization, @Url String url);

    @GET
    Observable<StatusOverall> status(@Header("Authorization") String authorization, @Url String url);

    @GET
    Observable<StatusOverall> backupStatus(@Header("Authorization") String authorization, @Url String url);

    @GET
    Observable<ResponseBody> getPredefinedStatuses(@Header("Authorization") String authorization, @Url String url);

    @DELETE
    Observable<GenericOverall> statusDeleteMessage(@Header("Authorization") String authorization, @Url String url);


    @FormUrlEncoded
    @PUT
    Observable<GenericOverall> setPredefinedStatusMessage(@Header("Authorization") String authorization,
                                                          @Url String url,
                                                          @Field("messageId") String selectedPredefinedMessageId,
                                                          @Field("clearAt") Long clearAt);

    @FormUrlEncoded
    @PUT
    Observable<GenericOverall> setCustomStatusMessage(@Header("Authorization") String authorization,
                                                      @Url String url,
                                                      @Field("statusIcon") String statusIcon,
                                                      @Field("message") String message,
                                                      @Field("clearAt") Long clearAt);

    @FormUrlEncoded
    @PUT
    Observable<GenericOverall> setStatusType(@Header("Authorization") String authorization,
                                             @Url String url,
                                             @Field("statusType") String statusType);


    @POST
    Observable<GenericOverall> sendReaction(@Header("Authorization") String authorization,
                                            @Url String url,
                                            @Query("reaction") String reaction);

    @DELETE
    Observable<GenericOverall> deleteReaction(@Header("Authorization") String authorization,
                                              @Url String url,
                                              @Query("reaction") String reaction);

    @GET
    Observable<ReactionsOverall> getReactions(@Header("Authorization") String authorization,
                                              @Url String url,
                                              @Query("reaction") String reaction);

    @GET
    Observable<UnifiedSearchOverall> performUnifiedSearch(@Header("Authorization") String authorization,
                                                          @Url String url,
                                                          @Query("term") String term,
                                                          @Query("from") String fromUrl,
                                                          @Query("limit") Integer limit,
                                                          @Query("cursor") Integer cursor);

    @GET
    Observable<GccPollOverall> getPoll(@Header("Authorization") String authorization,
                                       @Url String url);

    @FormUrlEncoded
    @POST
    Observable<GccPollOverall> createPoll(@Header("Authorization") String authorization,
                                          @Url String url,
                                          @Query("question") String question,
                                          @Field("options[]") List<String> options,
                                          @Query("resultMode") Integer resultMode,
                                          @Query("maxVotes") Integer maxVotes);

    @FormUrlEncoded
    @POST
    Observable<GccPollOverall> votePoll(@Header("Authorization") String authorization,
                                        @Url String url,
                                        @Field("optionIds[]") List<Integer> optionIds);

    @DELETE
    Observable<GccPollOverall> closePoll(@Header("Authorization") String authorization,
                                         @Url String url);

    @GET
    Observable<OpenGraphOverall> getOpenGraph(@Header("Authorization") String authorization,
                                              @Url String url,
                                              @Query("reference") String urlToFindPreviewFor);

    @FormUrlEncoded
    @POST
    Observable<GenericOverall> startRecording(@Header("Authorization") String authorization,
                                              @Url String url,
                                              @Field("status") Integer status);

    @DELETE
    Observable<GenericOverall> stopRecording(@Header("Authorization") String authorization,
                                             @Url String url);

    @POST
    Observable<GenericOverall> requestAssistance(@Header("Authorization") String authorization,
                                                 @Url String url);

    @DELETE
    Observable<GenericOverall> withdrawRequestAssistance(@Header("Authorization") String authorization,
                                                         @Url String url);

    @POST
    Observable<GenericOverall> sendCommonPostRequest(@Header("Authorization") String authorization, @Url String url);

    @DELETE
    Observable<GenericOverall> sendCommonDeleteRequest(@Header("Authorization") String authorization, @Url String url);


    @POST
    Observable<GccTranslationsOverall> translateMessage(@Header("Authorization") String authorization,
                                                        @Url String url,
                                                        @Query("text") String text,
                                                        @Query("toLanguage") String toLanguage,
                                                        @Nullable @Query("fromLanguage") String fromLanguage);

    @GET
    Observable<GccLanguagesOverall> getLanguages(@Header("Authorization") String authorization,
                                                 @Url String url);

    @GET
    Observable<ReminderOverall> getReminder(@Header("Authorization") String authorization,
                                            @Url String url);

    @DELETE
    Observable<GenericOverall> deleteReminder(@Header("Authorization") String authorization,
                                              @Url String url);

    @FormUrlEncoded
    @POST
    Observable<ReminderOverall> setReminder(@Header("Authorization") String authorization,
                                            @Url String url,
                                            @Field("timestamp") int timestamp);

    @FormUrlEncoded
    @PUT
    Observable<GenericOverall> setRecordingConsent(@Header("Authorization") String authorization,
                                                   @Url String url,
                                                   @Field("recordingConsent") int recordingConsent);

    @GET
    Observable<InvitationOverall> getInvitations(@Header("Authorization") String authorization,
                                                 @Url String url);

    @POST
    Observable<GenericOverall> acceptInvitation(@Header("Authorization") String authorization,
                                                @Url String url);

    @DELETE
    Observable<GenericOverall> rejectInvitation(@Header("Authorization") String authorization,
                                                @Url String url);
}