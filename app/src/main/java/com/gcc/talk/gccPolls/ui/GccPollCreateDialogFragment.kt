/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import autodagger.AutoInjector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.databinding.DialogPollCreateBinding
import com.gcc.talk.gccPolls.adapters.GccPollCreateOptionItem
import com.gcc.talk.gccPolls.adapters.GccPollCreateOptionsAdapter
import com.gcc.talk.gccPolls.adapters.GccPollCreateOptionsItemListener
import com.gcc.talk.gccPolls.viewmodels.GccPollCreateViewModel
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccPollCreateDialogFragment :
    DialogFragment(),
    GccPollCreateOptionsItemListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    private lateinit var binding: DialogPollCreateBinding
    private lateinit var viewModel: GccPollCreateViewModel

    private var adapter: GccPollCreateOptionsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[GccPollCreateViewModel::class.java]
        val roomToken = arguments?.getString(KEY_ROOM_TOKEN)!!
        viewModel.setData(roomToken)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPollCreateBinding.inflate(layoutInflater)

        val dialogBuilder = MaterialAlertDialogBuilder(binding.root.context)
            .setView(binding.root)
        viewThemeUtils.dialog.colorMaterialAlertDialogBackground(binding.root.context, dialogBuilder)

        return dialogBuilder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.options.observe(viewLifecycleOwner) { options -> adapter?.updateOptionsList(options) }

        binding.pollCreateOptionsList.layoutManager = LinearLayoutManager(context)

        adapter = GccPollCreateOptionsAdapter(this, viewThemeUtils)
        binding.pollCreateOptionsList.adapter = adapter

        themeDialog()

        setupListeners()
        setupStateObserver()
    }

    private fun themeDialog() {
        viewThemeUtils.platform.colorTextView(binding.pollQuestion)
        viewThemeUtils.platform.colorTextView(binding.pollOptions)
        viewThemeUtils.platform.colorTextView(binding.pollSettings)

        viewThemeUtils.material.colorTextInputLayout(binding.pollCreateQuestionTextInputLayout)

        viewThemeUtils.material.colorMaterialButtonText(binding.pollAddOptionsItem)
        viewThemeUtils.material.colorMaterialButtonText(binding.pollDismiss)
        viewThemeUtils.material.colorMaterialButtonPrimaryFilled(binding.pollCreateButton)

        viewThemeUtils.platform.themeCheckbox(binding.pollPrivatePollCheckbox)
        viewThemeUtils.platform.themeCheckbox(binding.pollMultipleAnswersCheckbox)
    }

    private fun setupListeners() {
        binding.pollAddOptionsItem.setOnClickListener {
            viewModel.addOption()
            adapter?.itemCount?.minus(1)?.let { binding.pollCreateOptionsList.scrollToPosition(it) }
        }

        binding.pollDismiss.setOnClickListener {
            dismiss()
        }

        binding.pollCreateQuestionTextEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // unused atm
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // unused atm
            }

            override fun onTextChanged(question: CharSequence, start: Int, before: Int, count: Int) {
                if (question.toString() != viewModel.question) {
                    viewModel.setQuestion(question.toString())
                }
            }
        })

        binding.pollPrivatePollCheckbox.setOnClickListener {
            viewModel.setPrivatePoll(binding.pollPrivatePollCheckbox.isChecked)
        }

        binding.pollMultipleAnswersCheckbox.setOnClickListener {
            viewModel.setMultipleAnswer(binding.pollMultipleAnswersCheckbox.isChecked)
        }

        binding.pollCreateButton.setOnClickListener {
            viewModel.createPoll()
        }
    }

    private fun setupStateObserver() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GccPollCreateViewModel.PollCreatedState -> dismiss()
                is GccPollCreateViewModel.PollCreationFailedState -> showError()
                is GccPollCreateViewModel.PollCreationState -> updateButtons(state)
            }
        }
    }

    private fun updateButtons(state: GccPollCreateViewModel.PollCreationState) {
        binding.pollAddOptionsItem.isEnabled = state.enableAddOptionButton
        binding.pollCreateButton.isEnabled = state.enableCreatePollButton
    }

    private fun showError() {
        dismiss()
        Log.e(TAG, "Failed to create poll")
        Snackbar.make(binding.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
    }

    override fun onRemoveOptionsItemClick(pollCreateOptionItem: GccPollCreateOptionItem, position: Int) {
        viewModel.removeOption(pollCreateOptionItem)
    }

    override fun onOptionsItemTextChanged(pollCreateOptionItem: GccPollCreateOptionItem) {
        viewModel.optionsItemTextChanged()
    }

    override fun requestFocus(textField: EditText) {
        if (binding.pollCreateQuestionTextEdit.text?.isBlank() == true) {
            binding.pollCreateQuestionTextEdit.requestFocus()
        } else {
            textField.requestFocus()
        }
    }

    /**
     * Fragment creator
     */
    companion object {
        private val TAG = GccPollCreateDialogFragment::class.java.simpleName
        private const val KEY_ROOM_TOKEN = "keyRoomToken"

        @JvmStatic
        fun newInstance(roomTokenParam: String): GccPollCreateDialogFragment {
            val args = Bundle()
            args.putString(KEY_ROOM_TOKEN, roomTokenParam)
            val fragment = GccPollCreateDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
