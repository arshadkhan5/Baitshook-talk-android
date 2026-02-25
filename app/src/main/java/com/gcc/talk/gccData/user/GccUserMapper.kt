/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.user

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccData.user.model.GccUserEntity

object GccUserMapper {
    fun toModel(entities: List<GccUserEntity?>?): List<GccUser> =
        entities?.map { user: GccUserEntity? ->
            toModel(user)!!
        } ?: emptyList()

    fun toModel(entity: GccUserEntity?): GccUser? =
        entity?.let {
            GccUser(
                entity.id,
                entity.userId,
                entity.username,
                entity.baseUrl!!,
                entity.token,
                entity.displayName,
                entity.pushConfigurationState,
                entity.capabilities,
                entity.serverVersion,
                entity.clientCertificate,
                entity.externalSignalingServer,
                entity.current,
                entity.scheduledForDeletion
            )
        }

    fun toEntity(model: GccUser): GccUserEntity {
        val userEntity = when (val id = model.id) {
            null -> GccUserEntity(userId = model.userId, username = model.username, baseUrl = model.baseUrl!!)
            else -> GccUserEntity(id, model.userId, model.username, model.baseUrl!!)
        }
        userEntity.apply {
            token = model.token
            displayName = model.displayName
            pushConfigurationState = model.pushConfigurationState
            capabilities = model.capabilities
            serverVersion = model.serverVersion
            clientCertificate = model.clientCertificate
            externalSignalingServer = model.externalSignalingServer
            current = model.current
            scheduledForDeletion = model.scheduledForDeletion
        }
        return userEntity
    }
}
