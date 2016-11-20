package com.lifepath.swaribrus

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import ru.yandex.speechkit.Error
import ru.yandex.speechkit.gui.ResourceRegistery
import ru.yandex.speechkit.gui.callback.OnCancelListener
import ru.yandex.speechkit.gui.callback.OnLanguageChangedListener
import ru.yandex.speechkit.gui.callback.OnRecognitionDoneListener
import ru.yandex.speechkit.gui.callback.OnRepeatRecognitionListener
import ru.yandex.speechkit.gui.fragment.SearchFragment
import ru.yandex.speechkit.gui.util.ConfigUtil

class OneTouchActivity : AppCompatActivity(), OnCancelListener, OnRepeatRecognitionListener, OnRecognitionDoneListener, OnLanguageChangedListener {
    private var R: ResourceRegistery? = null
    private var needFinishOnBackPressed = true
    private var canceling: Boolean = false
    var model = "general"
        private set
    private val curScreenName = ""

    override fun onCreate(arg: Bundle?) {
        super.onCreate(arg)
        ResourceRegistery.initialize(this.resources, this.packageName)
        this.R = ResourceRegistery.getInstance()
        ConfigUtil.setDeviceType(this)
        this.setContentView(this.R!!.get("layout", "ysk_activity_main"))
        val model = this.intent.getStringExtra("ru.yandex.speechkit.gui.model")
        if (model != null) {
            this.model = model
        }

        println("onCreate...............")

        val language = this.intent.getStringExtra("ru.yandex.speechkit.gui.language")
        if (language != null) {
            ConfigUtil.putLanguage(this, language)
        } else if (ConfigUtil.getLanguage(this) == null) {
            val locale = this.resources.configuration.locale
            ConfigUtil.putLanguage(this, locale.language + "-" + locale.country)
        }

        ConfigUtil.showPartialResults = this.intent.getBooleanExtra("ru.yandex.speechkit.gui.show_partial_results", true)
        ConfigUtil.showResultsList = this.intent.getBooleanExtra("ru.yandex.speechkit.gui.show_results_list", true)
    }

    override fun onStart() {
        super.onStart()
//        this.switchFragment(SearchFragment(), false, SearchFragment.TAG)
    }

    override fun onStop() {
        this.doCancel()
        super.onStop()
    }

    override fun onCancel(needFinish: Boolean) {
        ConfigUtil.debugLog(TAG, "onCancel needFinish: " + needFinish + ", canceling: " + this.canceling)
        if (!this.canceling && needFinish) {
            this.doCancel()
        }

    }

    fun doCancel() {
        ConfigUtil.debugLog(TAG, "doCancel: canceling: " + this.canceling)
        if (!this.canceling) {
            this.canceling = true
            var error: Error? = null

            if (error == null) {
                error = Error(9, "Recognition canceled")
            }

            ConfigUtil.debugLog(TAG, "doCancel: error = " + error.string)
            this.finishWithError(error)
        }

    }

    override fun onRepeatRecognition() {
        ConfigUtil.debugLog(TAG, "onRepeatRecognition")
    }

    override fun onRecognitionDone(result: String) {
        ConfigUtil.debugLog(TAG, "onRecognitionDone: " + result)
        this.finishWithResult(result)
    }

    override fun onLanguageChanged() {
        ConfigUtil.debugLog(TAG, "onLanguageChanged")
//        this.switchFragment(SearchFragment(), false, SearchFragment.TAG)
    }

    fun finishWithResult(result: String) {
        ConfigUtil.debugLog(TAG, "finishWithResult: " + result + ", isFinishing(): " + this.isFinishing)
        if (!this.isFinishing) {
            val data = Intent()
            data.putExtra("ru.yandex.speechkit.gui.result", result)
            data.putExtra("ru.yandex.speechkit.gui.language", ConfigUtil.getLanguage(this))
            this.setResult(1, data)
            this.finish()
        }

    }

    fun finishWithError(error: Error) {
        ConfigUtil.debugLog(TAG, "finishWithError: " + error.string + ", isFinishing(): " + this.isFinishing)
        if (!this.isFinishing) {
            val data = Intent()
            data.putExtra("ru.yandex.speechkit.gui.error", error)
            data.putExtra("ru.yandex.speechkit.gui.language", ConfigUtil.getLanguage(this))
            this.setResult(2, data)
            this.finish()
        }

    }

    override fun onBackPressed() {
        ConfigUtil.debugLog(TAG, "onBackPressed needFinishOnBackPressed: " + this.needFinishOnBackPressed)
        if (this.needFinishOnBackPressed) {
            this.onCancel(true)
        }

        super.onBackPressed()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == 1) {
            val r = Rect()
            this.window.decorView.getHitRect(r)
            val intersects = r.contains(event.x.toInt(), event.y.toInt())
            if (!intersects) {
                this.onCancel(true)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    companion object {
        private val TAG = OneTouchActivity::class.java.name
        val EXTRA_LANGUAGE = "ru.yandex.speechkit.gui.language"
        val EXTRA_MODEL = "ru.yandex.speechkit.gui.model"
        val EXTRA_ERROR = "ru.yandex.speechkit.gui.error"
        val EXTRA_RESULT = "ru.yandex.speechkit.gui.result"
        val EXTRA_SHOW_PARTIAL_RESULTS = "ru.yandex.speechkit.gui.show_partial_results"
        val EXTRA_SHOW_RESULTS_LIST = "ru.yandex.speechkit.gui.show_results_list"
        val RESULT_OK = 1
        val RESULT_ERROR = 2
    }
}
