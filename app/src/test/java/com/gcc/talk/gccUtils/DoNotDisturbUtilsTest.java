/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

public class DoNotDisturbUtilsTest {

    @Mock
    private Context context;

    @Mock
    private NotificationManager notificationManager;

    @Mock
    private AudioManager audioManager;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager);
        when(context.getSystemService(Context.AUDIO_SERVICE)).thenReturn(audioManager);
    }

    @Test
    public void shouldPlaySound_givenAndroidMAndInterruptionFilterNone_assertReturnsFalse() {
        GccDoNotDisturbUtils.INSTANCE.setTestingBuildVersion(Build.VERSION_CODES.M);

        when(notificationManager.getCurrentInterruptionFilter()).thenReturn(NotificationManager.INTERRUPTION_FILTER_NONE);
        when(audioManager.getRingerMode()).thenReturn(AudioManager.RINGER_MODE_NORMAL);

        assertFalse("shouldPlaySound incorrectly returned true",
                    GccDoNotDisturbUtils.INSTANCE.shouldPlaySound(context));
    }

    @Test
    public void shouldPlaySound_givenRingerModeNotNormal_assertReturnsFalse() throws Exception {
        GccDoNotDisturbUtils.INSTANCE.setTestingBuildVersion(Build.VERSION_CODES.LOLLIPOP);
        when(audioManager.getRingerMode()).thenReturn(AudioManager.RINGER_MODE_VIBRATE);

        assertFalse("shouldPlaySound incorrectly returned true",
                    GccDoNotDisturbUtils.INSTANCE.shouldPlaySound(context));
    }
}
