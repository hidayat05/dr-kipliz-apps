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
import com.kipliz.dr.R.drawable
import com.kipliz.dr.R.layout
import com.kipliz.dr.R.string
import com.kipliz.dr.adapter.ChatRecyclerAdapter
import com.kipliz.dr.entity.MessageEntity
import com.kipliz.dr.model.MainViewModel
import kotlinx.android.synthetic.main.activity_main.etMessage
import kotlinx.android.synthetic.main.activity_main.ivMicrophone
import kotlinx.android.synthetic.main.activity_main.recycler
import kotlinx.android.synthetic.main.activity_main.voiceInterface

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
                getString(string.identity_id_test),
                Regions.fromName(getString(string.cognito_region)))

        interactionClient = InteractionClient(this,
                credentialProvider,
                Regions.fromName(getString(string.lex_region)),
                getString(string.bot_name),
                getString(string.bot_alias))
        interactionClient.setInteractionListener(interactionListener)
        voiceView = voiceInterface as InteractiveVoiceView
        voiceView.viewAdapter.apply {
            setCredentialProvider(credentialProvider)
            setInteractionConfig(InteractionConfig(
                    getString(string.bot_name),
                    getString(string.bot_alias)))
            awsRegion = getString(string.lex_region)
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
                                         continuation: LexServiceContinuation) {
            readUserText(continuation)
        }

        override fun onInteractionError(response: Response?, e: Exception) {
            if (response != null) {
                if (DialogState.Failed.toString() == response.dialogState) {
                    inConversation = false
                } else {

                }
            } else {
                inConversation = false
            }
        }
    }

    private fun readUserText(continuation: LexServiceContinuation) {
        serviceContinuation = continuation
        inConversation = true
    }

    private fun setStateButtonVoice(isEnable: Boolean) {
        ivMicrophone.isEnabled = isEnable
        ivMicrophone.setImageDrawable(ContextCompat.getDrawable(this,
                if (isEnable) drawable.ic_microphone else drawable.ic_microphone_blocked))
    }


    private fun sendTextInput() {
        val text = etMessage.text.toString()
        mainViewModel.addData(MessageEntity(0,text, "kipli", "" ))
//        if (!inConversation) {
//            Log.e(TAG, "-- new conversations")
//            startConversations()
//            // add data
//            interactionClient.textInForAudioOut("", null)
//            inConversation = true
//        } else {
//            // add data
//            serviceContinuation.continueWithTextInForTextOut(text)
//        }
        etMessage.text.clear()
    }

    private fun startConversations() {
        inConversation = false
        etMessage.text.clear()
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
