/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters

import com.gcc.talk.gccModels.json.reactions.ReactionVoter

data class GccReactionItem(val reactionVoter: ReactionVoter, val reaction: String?)
