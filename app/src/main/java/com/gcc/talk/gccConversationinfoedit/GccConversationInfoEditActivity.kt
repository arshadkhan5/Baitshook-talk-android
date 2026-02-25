/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2023 Ezhil Shanmugham <ezhil56x.contact@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccConversationinfoedit

import android.app.Activity
import android.os.Bundle
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toFile
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import autodagger.AutoInjector
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.gcc.talk.R
import com.gcc.talk.gccActivities.GccBaseActivity
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccConversationinfoedit.viewmodel.GccConversationInfoEditViewModel
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ActivityConversationInfoEditBinding
import com.gcc.talk.gccExtensions.loadConversationAvatar
import com.gcc.talk.gccExtensions.loadSystemAvatar
import com.gcc.talk.gccExtensions.loadUserAvatar
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.capabilities.SpreedCapability
import com.gcc.talk.gccModels.json.conversations.ConversationEnums
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.CapabilitiesUtil
import com.gcc.talk.gccUtils.GccPickImage
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import java.io.File
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccConversationInfoEditActivity : GccBaseActivity() {

    private lateinit var binding: ActivityConversationInfoEditBinding

    @Inject
    lateinit var ncApi: GccNcApi

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var conversationInfoEditViewModel: GccConversationInfoEditViewModel

    private lateinit var roomToken: String
    private lateinit var conversationUser: GccUser
    private lateinit var credentials: String

    private var conversation: GccConversationModel? = null

    private lateinit var pickImage: GccPickImage

    private lateinit var spreedCapabilities: SpreedCapability

    private val startImagePickerForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleResult(it) { result ->
                pickImage.onImagePickerResult(result.data) { uri ->
                    uploadAvatar(uri.toFile())
                }
            }
        }

    private val startSelectRemoteFilesIntentForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        handleResult(it) { result ->
            pickImage.onSelectRemoteFilesResult(startImagePickerForResult, result.data)
        }
    }

    private val startTakePictureIntentForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        handleResult(it) { result ->
            pickImage.onTakePictureResult(startImagePickerForResult, result.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        binding = ActivityConversationInfoEditBinding.inflate(layoutInflater)
        setupActionBar()
        setContentView(binding.root)
        initSystemBars()

        val extras: Bundle? = intent.extras

        conversationUser = currentUserProviderOld.currentUser.blockingGet()

        roomToken = extras?.getString(GccBundleKeys.KEY_ROOM_TOKEN)!!

        conversationInfoEditViewModel =
            ViewModelProvider(this, viewModelFactory)[GccConversationInfoEditViewModel::class.java]

        conversationInfoEditViewModel.getRoom(conversationUser, roomToken)

        viewThemeUtils.material.colorTextInputLayout(binding.conversationNameInputLayout)
        viewThemeUtils.material.colorTextInputLayout(binding.conversationDescriptionInputLayout)

        credentials = GccApiUtils.getCredentials(conversationUser.username, conversationUser.token)!!

        pickImage = GccPickImage(this, conversationUser)

        val max = CapabilitiesUtil.conversationDescriptionLength(conversationUser.capabilities?.spreedCapability!!)
        binding.conversationDescription.filters = arrayOf(
            InputFilter.LengthFilter(max)
        )
        binding.conversationDescriptionInputLayout.counterMaxLength = max

        initObservers()
    }

    private fun initObservers() {
        initViewStateObserver()
        conversationInfoEditViewModel.renameRoomUiState.observe(this) { uiState ->
            when (uiState) {
                is GccConversationInfoEditViewModel.RenameRoomUiState.None -> {
                }
                is GccConversationInfoEditViewModel.RenameRoomUiState.Success -> {
                    if (CapabilitiesUtil.isConversationDescriptionEndpointAvailable(spreedCapabilities)) {
                        saveConversationDescription()
                    } else {
                        finish()
                    }
                }
                is GccConversationInfoEditViewModel.RenameRoomUiState.Error -> {
                    Snackbar
                        .make(binding.root, context.getString(R.string.default_error_msg), Snackbar.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "Error while saving conversation name", uiState.exception)
                }
            }
        }

        conversationInfoEditViewModel.setConversationDescriptionUiState.observe(this) { uiState ->
            when (uiState) {
                is GccConversationInfoEditViewModel.SetConversationDescriptionUiState.None -> {
                }
                is GccConversationInfoEditViewModel.SetConversationDescriptionUiState.Success -> {
                    finish()
                }
                is GccConversationInfoEditViewModel.SetConversationDescriptionUiState.Error -> {
                    Snackbar
                        .make(binding.root, context.getString(R.string.default_error_msg), Snackbar.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "Error while saving conversation description", uiState.exception)
                }
            }
        }
    }

    private fun initViewStateObserver() {
        conversationInfoEditViewModel.viewState.observe(this) { state ->
            when (state) {
                is GccConversationInfoEditViewModel.GetRoomSuccessState -> {
                    conversation = state.conversationModel

                    spreedCapabilities = conversationUser.capabilities!!.spreedCapability!!

                    binding.conversationName.setText(conversation!!.displayName)

                    if (conversation!!.description.isNotEmpty()) {
                        binding.conversationDescription.setText(conversation!!.description)
                    }

                    if (!CapabilitiesUtil.isConversationDescriptionEndpointAvailable(spreedCapabilities)) {
                        binding.conversationDescription.isEnabled = false
                    }

                    if (conversation?.objectType == ConversationEnums.ObjectType.EVENT) {
                        binding.conversationName.isEnabled = false
                        binding.conversationDescription.isEnabled = false
                    }

                    loadConversationAvatar()
                }

                is GccConversationInfoEditViewModel.GetRoomErrorState -> {
                    Snackbar.make(binding.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                }

                is GccConversationInfoEditViewModel.UploadAvatarSuccessState -> {
                    conversation = state.conversationModel
                    loadConversationAvatar()
                }

                is GccConversationInfoEditViewModel.UploadAvatarErrorState -> {
                    Snackbar.make(binding.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                }

                is GccConversationInfoEditViewModel.DeleteAvatarSuccessState -> {
                    conversation = state.conversationModel
                    loadConversationAvatar()
                }

                is GccConversationInfoEditViewModel.DeleteAvatarErrorState -> {
                    Snackbar.make(binding.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun setupAvatarOptions() {
        binding.avatarUpload.setOnClickListener {
            pickImage.selectLocal(startImagePickerForResult = startImagePickerForResult)
        }

        binding.avatarChoose.setOnClickListener {
            pickImage.selectRemote(startSelectRemoteFilesIntentForResult = startSelectRemoteFilesIntentForResult)
        }

        binding.avatarCamera.setOnClickListener {
            pickImage.takePicture(startTakePictureIntentForResult = startTakePictureIntentForResult)
        }

        if (conversation?.hasCustomAvatar == true) {
            binding.avatarDelete.visibility = View.VISIBLE
            binding.avatarDelete.setOnClickListener { deleteAvatar() }
        } else {
            binding.avatarDelete.visibility = View.GONE
        }

        binding.avatarImage.let { ViewCompat.setTransitionName(it, "userAvatar.transitionTag") }

        binding.let {
            viewThemeUtils.material.themeFAB(it.avatarUpload)
            viewThemeUtils.material.themeFAB(it.avatarChoose)
            viewThemeUtils.material.themeFAB(it.avatarCamera)
            viewThemeUtils.material.themeFAB(it.avatarDelete)
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.conversationInfoEditToolbar)
        binding.conversationInfoEditToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(resources!!.getColor(android.R.color.transparent, null).toDrawable())
        supportActionBar?.title = resources!!.getString(R.string.nc_conversation_menu_conversation_info)

        viewThemeUtils.material.themeToolbar(binding.conversationInfoEditToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_conversation_info_edit, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            if (conversation?.objectType != ConversationEnums.ObjectType.EVENT) {
                saveConversationNameAndDescription()
            }
        }
        return true
    }

    private fun saveConversationNameAndDescription() {
        val newRoomName = binding.conversationName.text.toString()
        conversationInfoEditViewModel.renameRoom(
            conversation!!.token,
            newRoomName
        )
    }

    private fun saveConversationDescription() {
        val conversationDescription = binding.conversationDescription.text.toString()
        conversationInfoEditViewModel.setConversationDescription(conversation!!.token, conversationDescription)
    }

    private fun handleResult(result: ActivityResult, onResult: (result: ActivityResult) -> Unit) {
        when (result.resultCode) {
            Activity.RESULT_OK -> onResult(result)

            ImagePicker.RESULT_ERROR -> {
                Snackbar.make(binding.root, ImagePicker.getError(result.data), Snackbar.LENGTH_SHORT).show()
            }

            else -> {
                Log.i(TAG, "Task Cancelled")
            }
        }
    }

    private fun uploadAvatar(file: File) {
        conversationInfoEditViewModel.uploadConversationAvatar(conversationUser, file, roomToken)
    }

    private fun deleteAvatar() {
        conversationInfoEditViewModel.deleteConversationAvatar(conversationUser, roomToken)
    }

    private fun loadConversationAvatar() {
        setupAvatarOptions()

        when (conversation!!.type) {
            ConversationEnums.ConversationType.ROOM_TYPE_ONE_TO_ONE_CALL -> if (!TextUtils.isEmpty(
                    conversation!!.name
                )
            ) {
                conversation!!.name.let { binding.avatarImage.loadUserAvatar(conversationUser, it, true, false) }
            }

            ConversationEnums.ConversationType.ROOM_GROUP_CALL, ConversationEnums.ConversationType.ROOM_PUBLIC_CALL -> {
                binding.avatarImage.loadConversationAvatar(conversationUser, conversation!!, false, viewThemeUtils)
            }

            ConversationEnums.ConversationType.ROOM_SYSTEM -> {
                binding.avatarImage.loadSystemAvatar()
            }

            else -> {
                // unused atm
            }
        }
    }

    companion object {
        private val TAG = GccConversationInfoEditActivity::class.simpleName
    }
}
