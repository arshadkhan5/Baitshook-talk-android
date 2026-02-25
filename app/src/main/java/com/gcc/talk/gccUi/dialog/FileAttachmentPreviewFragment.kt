/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Julius Linus <julius.linus@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUi.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import autodagger.AutoInjector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.databinding.DialogFileAttachmentPreviewBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.permissions.GccPlatformPermissionUtil
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class FileAttachmentPreviewFragment : DialogFragment() {
    private lateinit var files: String
    private lateinit var filesList: ArrayList<String>
    private var uploadFiles: (files: MutableList<String>, caption: String) -> Unit = { _, _ -> }
    lateinit var binding: DialogFileAttachmentPreviewBinding

    @Inject
    lateinit var permissionUtil: GccPlatformPermissionUtil

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    fun setListener(uploadFiles: (files: MutableList<String>, caption: String) -> Unit) {
        this.uploadFiles = uploadFiles
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            files = it.getString(FILE_NAMES_ARG, "")
            filesList = it.getStringArrayList(FILES_TO_UPLOAD_ARG)!!
        }

        binding = DialogFileAttachmentPreviewBinding.inflate(layoutInflater)
        return MaterialAlertDialogBuilder(requireContext()).setView(binding.root).create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
        setUpViews()
        setUpListeners()
        return inflater.inflate(R.layout.dialog_file_attachment_preview, container, false)
    }

    private fun setUpViews() {
        binding.dialogFileAttachmentPreviewFilenames.text = files
        viewThemeUtils.material.colorMaterialButtonPrimaryBorderless(binding.buttonClose)
        viewThemeUtils.material.colorMaterialButtonPrimaryBorderless(binding.buttonSend)
        viewThemeUtils.platform.colorViewBackground(binding.root)
        viewThemeUtils.material.colorTextInputLayout(binding.dialogFileAttachmentPreviewLayout)
    }

    private fun setUpListeners() {
        binding.buttonClose.setOnClickListener {
            dismiss()
        }

        binding.buttonSend.setOnClickListener {
            val caption: String = binding.dialogFileAttachmentPreviewCaption.text.toString()
            uploadFiles(filesList, caption)
            dismiss()
        }
    }

    companion object {

        private const val FILE_NAMES_ARG = "FILE_NAMES_ARG"
        private const val FILES_TO_UPLOAD_ARG = "FILES_TO_UPLOAD_ARG"

        @JvmStatic
        fun newInstance(filenames: String, filesToUpload: MutableList<String>): FileAttachmentPreviewFragment {
            val fileAttachmentFragment = FileAttachmentPreviewFragment()
            val args = Bundle()
            args.putString(FILE_NAMES_ARG, filenames)
            args.putStringArrayList(FILES_TO_UPLOAD_ARG, ArrayList(filesToUpload))
            fileAttachmentFragment.arguments = args
            return fileAttachmentFragment
        }

        val TAG: String = FilterConversationFragment::class.java.simpleName
    }
}
