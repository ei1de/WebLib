package com.helper.weblib.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.CookieManager
import android.webkit.ValueCallback
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.helper.weblib.R
import com.helper.weblib.databinding.ActivityWebBinding
import com.helper.weblib.ui.web_view.ClientsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WebActivity : AppCompatActivity(), ClientsController {
    private lateinit var binding: ActivityWebBinding

    private val viewModel by lazy {  ViewModelProvider(this)[WebViewModel::class.java]}

    private lateinit var mainUrl: String
    private var backUrl: String? = null

    private var shouldClear = false
    private var doubleClicked = false

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        sendPermission?.invoke(it)
        if(it){
            showChooseDialog()
        }else{
            launchGallery()
        }
    }
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.getImage(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sendOpen?.invoke()
        mainUrl = intent.getStringExtra(URL) ?: ""
        binding.webView.initClients(this)
        setBackHandler()
        binding.webView.loadUrl(mainUrl)

    }

    private fun setBackHandler() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (binding.webView).apply {
                    if (doubleClicked) {
                        if (url != backUrl && backUrl != "") {
                            backUrl?.let {
                                Log.d("TAGG", "BACK $it")
                                loadUrl(it)
                            }
                            shouldClear = true
                        }
                        doubleClicked = false
                    } else {
                        doubleClicked = true
                        if (url != backUrl && canGoBack()) {
                            goBack()
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            delay(1500)
                            doubleClicked = false
                        }
                    }
                }
            }
        })
    }
    private fun showChooseDialog() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val chooser = Intent.createChooser(intent, getString(R.string.choose_source))
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(camera))
        launcher.launch(chooser)

    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        launcher.launch(intent)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            if (this::binding.isInitialized) binding.webView.restoreState(savedInstanceState)
        } catch (e: Exception) {
            record?.invoke(e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            if (this::binding.isInitialized) binding.webView.saveState(outState)
        } catch (e: Exception) {
            record?.invoke(e)
        }
    }

    override fun onPause() {
        try {
            CookieManager.getInstance().flush()
            super.onPause()
        } catch (e: Exception) {
            record?.invoke(e)
        }
    }


    override fun onFinishLoading(result: Boolean, url: String) {
        when (result) {
            true -> {
                closeWeb?.invoke()
            }
            false -> {
                if (backUrl == null) {
                    saveUrl?.invoke(mainUrl)
                    sendFinish?.invoke(binding.webView.settings.userAgentString)
                    backUrl = url
                }
            }
        }
    }

    override fun clearHistory() {
        if (shouldClear) {
            binding.webView.clearHistory()
            shouldClear = false
        }
    }

    override fun handleFileChoose(valueCallback: ValueCallback<Array<Uri>>?) {
        viewModel.handleFileChoose(valueCallback, permissionRequest)
    }

    companion object {
        private const val URL = "GZ"
        private var record: ((t: Throwable) -> Unit)? = null
        private var sendPermission: ((Boolean) -> Unit)? = null
        private var sendOpen: (() -> Unit)? = null
        private var sendFinish: ((String) -> Unit)? = null
        private var closeWeb: (() -> Unit)? = null
        private var saveUrl: ((String) -> Unit)? = null

        fun createIntent(
            context: Context,
            url: String,
            rec: ((t: Throwable) -> Unit)? = null,
            sendP: ((Boolean) -> Unit)? = null,
            sendO: (() -> Unit)? = null,
            sendFi: ((String) -> Unit)? = null,
            close: (() -> Unit)? = null,
            save: ((String) -> Unit)? = null
        ): Intent {
            val intent = Intent(context, WebActivity::class.java).apply {
                record = rec
                sendPermission = sendP
                sendOpen = sendO
                sendFinish = sendFi
                closeWeb = close
                saveUrl = save
                val bundle = Bundle()
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                bundle.putString(URL, url)
                putExtras(bundle)
            }
            return intent
        }
    }
}