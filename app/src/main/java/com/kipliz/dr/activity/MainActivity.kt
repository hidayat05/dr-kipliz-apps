package com.kipliz.dr.activity

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobileconnectors.lex.interactionkit.InteractionClient
import com.amazonaws.mobileconnectors.lex.interactionkit.Response
import com.amazonaws.mobileconnectors.lex.interactionkit.config.InteractionConfig
import com.amazonaws.mobileconnectors.lex.interactionkit.continuations.LexServiceContinuation
import com.amazonaws.mobileconnectors.lex.interactionkit.listeners.InteractionListener
import com.amazonaws.mobileconnectors.lex.interactionkit.ui.InteractiveVoiceView
import com.amazonaws.regions.Regions
import com.amazonaws.services.lexrts.model.DialogState
import com.bumptech.glide.Glide
import com.kipliz.dr.R
import com.kipliz.dr.R.drawable
import com.kipliz.dr.R.layout
import com.kipliz.dr.adapter.ChatRecyclerAdapter
import com.kipliz.dr.entity.MessageEntity
import com.kipliz.dr.model.MainViewModel
import kotlinx.android.synthetic.main.activity_main.botAnimations
import kotlinx.android.synthetic.main.activity_main.etMessage
import kotlinx.android.synthetic.main.activity_main.ivMicrophone
import kotlinx.android.synthetic.main.activity_main.recycler
import kotlinx.android.synthetic.main.activity_main.voiceInterface
import java.text.DateFormat
import java.util.Date

class MainActivity : AppCompatActivity(), InteractiveVoiceView.InteractiveVoiceListener {

    companion object {
        private const val REQUEST_RECORDING_PERMISSIONS_RESULT = 75
        private const val TAG = "mainActivity"
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var serviceContinuation: LexServiceContinuation
    private lateinit var interactionClient: InteractionClient
    private lateinit var voiceView: InteractiveVoiceView
    private lateinit var recyclerAdapter: ChatRecyclerAdapter
    private var inConversation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        recyclerAdapter = ChatRecyclerAdapter(this, mutableListOf())
        recycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recyclerAdapter
        }

        mainViewModel = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
        mainViewModel.getAllMessageEntity().observe(this, Observer<List<MessageEntity>> { t ->
            t?.let {
                recyclerAdapter.addData(it)
                recycler.scrollToPosition(it.lastIndex)
            }
        })

        Glide.with(this)
                .asGif()
                .load(R.drawable.bot_selma)
                .into(botAnimations)

        init()
        initLexSdk()
    }


    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                setStateButtonVoice(false)
            } else {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORDING_PERMISSIONS_RESULT)
            }
        } else {
            setStateButtonVoice(true)
        }

        etMessage.setOnKeyListener { _, keycode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN
                    && keycode == KeyEvent.KEYCODE_ENTER) {
                sendTextInput()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    private fun initLexSdk() {
        val credentialProvider = CognitoCredentialsProvider(
                getString(R.string.identity_id_test),
                Regions.fromName(getString(R.string.cognito_region)))

        interactionClient = InteractionClient(this,
                credentialProvider,
                Regions.fromName(getString(R.string.lex_region)),
                getString(R.string.bot_name),
                getString(R.string.bot_alias))
        interactionClient.setInteractionListener(interactionListener)
        voiceView = voiceInterface as InteractiveVoiceView
        voiceView.setInteractiveVoiceListener(this)
        voiceView.viewAdapter.apply {
            setCredentialProvider(credentialProvider)
            setInteractionConfig(InteractionConfig(
                    getString(R.string.bot_name),
                    getString(R.string.bot_alias)))
            awsRegion = getString(R.string.lex_region)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORDING_PERMISSIONS_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext,
                        "ga iso nganggo speach", Toast.LENGTH_SHORT).show()

                setStateButtonVoice(false)
            } else {
                setStateButtonVoice(true)
            }
        }
    }

    private val interactionListener: InteractionListener = object : InteractionListener {
        override fun onReadyForFulfillment(response: Response) {
            Log.d("mainActivity", "Transaction completed successfully")
            inConversation = false
        }

        override fun promptUserToRespond(response: Response,
                                         continuation: LexServiceContinuation?) {
            Log.e(TAG, "response from lex" + response.textResponse)

            if (DialogState.ReadyForFulfillment.toString() != response.dialogState
                    && DialogState.Fulfilled.toString() != response.dialogState) {
                mainViewModel.addData(MessageEntity(0, response.textResponse, "tx", getCurrentTimeStamp()))
                readUserText(continuation)
            } else if (DialogState.Fulfilled.toString() == response.dialogState) {
                inConversation = false
                mainViewModel.addData(MessageEntity(0, response.textResponse, "tx", getCurrentTimeStamp()))
            }
        }

        override fun onInteractionError(response: Response?, e: Exception) {
            inConversation = if (response != null) {
                DialogState.Failed.toString() != response.dialogState
            } else {
                false
            }
        }
    }

    private fun readUserText(continuation: LexServiceContinuation?) {
        continuation?.let {
            serviceContinuation = it
        }
        inConversation = true
        etMessage.text.clear()
    }

    private fun setStateButtonVoice(isEnable: Boolean) {
        ivMicrophone.isEnabled = isEnable
        ivMicrophone.setImageDrawable(ContextCompat.getDrawable(this,
                if (isEnable) drawable.ic_microphone else drawable.ic_microphone_blocked))
    }


    private fun sendTextInput() {
        val text = etMessage.text.toString()
        if (!inConversation) {
            Log.e(TAG, "-- new conversations")
            mainViewModel.addData(MessageEntity(0, text, "Me", getCurrentTimeStamp()))
            interactionClient.textInForTextOut(text, null)
            startConversations()
        } else {
            mainViewModel.addData(MessageEntity(0, text, "tx", getCurrentTimeStamp()))
            serviceContinuation.continueWithTextInForTextOut(text)
        }
        etMessage.text.clear()
    }

    private fun startConversations() {
        inConversation = true
        etMessage.text.clear()
    }

    private fun getCurrentTimeStamp(): String {
        return DateFormat.getDateTimeInstance().format(Date())
    }

    /**
     * response from voice
     */
    override fun dialogReadyForFulfillment(slots: MutableMap<String, String>?, intent: String?) {

    }

    override fun onResponse(response: Response?) {
        // for response from lex response.getTextResponse()
        // for input text response.getInputTranscript()
    }

    override fun onError(responseText: String?, e: java.lang.Exception?) {

    }
}
