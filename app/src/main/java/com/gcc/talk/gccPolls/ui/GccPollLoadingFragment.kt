/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import autodagger.AutoInjector
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.databinding.DialogPollLoadingBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccPollLoadingFragment : Fragment() {

    private lateinit var binding: DialogPollLoadingBinding

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogPollLoadingBinding.inflate(inflater, container, false)
        binding.root.layoutParams.height = HEIGHT
        viewThemeUtils.platform.colorCircularProgressBar(binding.pollLoadingProgressbar, ColorRole.PRIMARY)
        return binding.root
    }

    companion object {
        private const val HEIGHT = 300

        @JvmStatic
        fun newInstance(): GccPollLoadingFragment = GccPollLoadingFragment()
    }
}
