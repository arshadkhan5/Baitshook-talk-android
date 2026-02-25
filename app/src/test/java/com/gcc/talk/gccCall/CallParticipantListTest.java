/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccCall;

import com.gcc.talk.gccSignaling.GccSignalingMessageReceiver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CallParticipantListTest {

    private GccSignalingMessageReceiver mockedSignalingMessageReceiver;

    private GccCallParticipantList callParticipantList;
    private GccSignalingMessageReceiver.ParticipantListMessageListener participantListMessageListener;

    @Before
    public void setUp() {
        mockedSignalingMessageReceiver = mock(GccSignalingMessageReceiver.class);

        callParticipantList = new GccCallParticipantList(mockedSignalingMessageReceiver);

        // Get internal ParticipantListMessageListener from callParticipantList set in the
        // mockedSignalingMessageReceiver.
        ArgumentCaptor<GccSignalingMessageReceiver.ParticipantListMessageListener> participantListMessageListenerArgumentCaptor =
            ArgumentCaptor.forClass(GccSignalingMessageReceiver.ParticipantListMessageListener.class);

        verify(mockedSignalingMessageReceiver).addListener(participantListMessageListenerArgumentCaptor.capture());

        participantListMessageListener = participantListMessageListenerArgumentCaptor.getValue();
    }

    @Test
    public void testDestroy() {
        callParticipantList.destroy();

        verify(mockedSignalingMessageReceiver).removeListener(participantListMessageListener);
        verifyNoMoreInteractions(mockedSignalingMessageReceiver);
    }
}
