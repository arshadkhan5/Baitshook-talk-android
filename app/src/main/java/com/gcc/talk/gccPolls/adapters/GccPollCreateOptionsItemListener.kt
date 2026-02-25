/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters

import android.widget.EditText

interface GccPollCreateOptionsItemListener {

    fun onRemoveOptionsItemClick(pollCreateOptionItem: GccPollCreateOptionItem, position: Int)

    fun onOptionsItemTextChanged(pollCreateOptionItem: GccPollCreateOptionItem)

    fun requestFocus(textField: EditText)
}
