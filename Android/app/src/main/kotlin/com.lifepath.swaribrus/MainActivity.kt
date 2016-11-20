package com.lifepath.swaribrus.lifepath

import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.NonNull
import android.widget.*
import com.lifepath.swaribrus.FuelHttpService
import com.lifepath.swaribrus.Parsers.BusStopsJson
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import ru.yandex.speechkit.*
import GPSTracker
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Vibrator
import com.lifepath.swaribrus.chekcLocationIn
import nl.komponents.kovenant.*
import nl.komponents.kovenant.DirectDispatcherContext.dispatcher
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import nl.komponents.kovenant.jvm.asDispatcher
import nl.komponents.kovenant.ui.KovenantUi
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.*
import java.util.regex.*

class MainActivity : VocalizerListener, RecognizerListener, AppCompatActivity()  {
    override fun onSoundDataRecorded(p0: Recognizer?, p1: ByteArray?) { }

    override fun onRecordingDone(p0: Recognizer?) {
        println(p0.toString())
    }

    private val API_KEY = "7fa0373b-37b5-4a98-9b97-c71df8778910"
    private var vocalizer: Vocalizer? = null
    private val REQUEST_PERMISSION_CODE = 1
    private var recognizer: Recognizer? = null

    val fuelService: FuelHttpService = FuelHttpService()
    val busStopsParser: BusStopsJson = BusStopsJson()

    val bssid = "90:a2:da:f5:10:12"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SpeechKit.getInstance().configure(applicationContext, API_KEY)

        recognize()

        resetRecognizer()
        resetVocalizer()

        // Getting coordinate
        find<ImageButton>(R.id.speak_btn).setOnClickListener {
            recognize()

//            val promise = monitoringWifiTask("90:a2:da:f5:10:12")

//            val mp = MediaPlayer.create(this, R.raw.alarm)
//            mp.start()

//            val v = applicationContext.getSystemService(applicationContext.VIBRATOR_SERVICE) as Vibrator
//            v.vibrate(500)
        }
    }

    private fun getCurrentLocation(): Location? {
        val gps: GPSTracker = GPSTracker(this@MainActivity)
        if (gps.canGetLocation()) {
            toast(gps.getLatitude().toString())
            toast(gps.getLongitude().toString())
        } else {
            toast("Gps disabled")
        }
        return gps.location
    }

    private fun selectionRoute(message: String): Int? {
        val msg = message.toLowerCase()

        val pat = Pattern.compile("[-]?[0-9]+(.[0-9]+)?")
        val matcher = pat.matcher(msg)
        while (matcher.find()) {
            return matcher.group().toInt()
        }

        return null
    }

    private fun checkAutoBusStop(message: String) {
        val location = getCurrentLocation()

        // Проверяем находится ли пользователь в области остановки
        val numberRoute = selectionRoute(message) // Передать на сервер, должен определять находимся ли мы
        if (numberRoute == null) {
            speak("Извените, назовите номер маршрута!")
            return
        }
        println("number route: " + numberRoute.toString())

        val wifi = applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as WifiManager
        wifi.startScan()

        var status = false

        class ScheduledTasksRunnable : Runnable {
            override fun run() {
                if (status) return
                try {
                    wifi.startScan()
                    val wifiScanList = wifi.scanResults
                    println("Count: " + wifiScanList.size)
                    for (i in 0..wifiScanList.size - 1) {
                        val wifi_b = wifiScanList[i]
                        if (Objects.equals(wifi_b.BSSID, bssid)) {
                            try {
                                println("UEUEUEUE")
                                runOnUiThread {
                                    speak("Автобус прибыл на остановку!")
                                }
                                status = true
                                return@run
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                    val requestURL = "http://192.168.1.44:51580/api/trip/create/" + location!!.latitude.toString() + "/" + location.longitude.toString() + "/1"
                    println(requestURL)
                    val resultPromise = fuelService.textUrl(requestURL)
                    println("resultPromise@!!!!!!!!!")
                    resultPromise.then { msg ->
                        msg
                    } successUi {
                        result ->
                        run {
                            println("successUi")
                            runOnUiThread {
                                find<TextView>(R.id.name_app).text = "update UI"
                            }
                        }
                    } failUi {
                        runOnUiThread {
                            toast("${it.message}")
                        }
                    }
                    runOnUiThread {
                        speak("Водитель оповещен!")
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }
        }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasksRunnable(),
                0, 5.toLong(), TimeUnit.SECONDS)
    }

    private fun monitoringWifiTask(bssid: String): Promise<String, Exception> {
        return task {
            val wifi = applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as WifiManager
            wifi.startScan()
            while (true) {
                try {
                    sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    return@task e
                }

                wifi.startScan()
                val wifiScanList = wifi.scanResults
                println("Count: " + wifiScanList.size)
                for (i in 0..wifiScanList.size - 1) {
                    val wifi_b = wifiScanList[i]
                    if (Objects.equals(wifi_b.BSSID, bssid)) {
                        try {
                            println("UEUEUEUE")
                            return@task true
                        } catch (e: Exception) {
                            return@task e
                        }

                    }
                }
            }
        } then {
            i -> "result: $i"
        } success {
            msg -> println(msg)
        } fail {
            e -> println(e)
        }

    }

    private fun recognize() {
        resetRecognizer()
        recognizer = Recognizer.create(Recognizer.Language.RUSSIAN, Recognizer.Model.NOTES, this@MainActivity)
        recognizer!!.start()
    }

    private fun speak(text: String) {
        resetVocalizer()
        vocalizer = Vocalizer.createVocalizer(Vocalizer.Language.RUSSIAN, text, true, Vocalizer.Voice.ERMIL)
        (vocalizer as Vocalizer?)!!.setListener(this@MainActivity)
        (vocalizer as Vocalizer?)!!.start()
    }

    override fun onPause() {
        super.onPause()
        resetVocalizer()
        resetRecognizer()
    }

    private fun resetVocalizer() {
        if (vocalizer != null) {
            vocalizer!!.cancel()
            vocalizer = null
        }
    }

    private fun resetRecognizer() {
        if (recognizer != null) {
            recognizer!!.cancel()
            recognizer = null
        }
    }

    override fun onSynthesisBegin(vocalizer: Vocalizer) {
        println("Synthesis begin")
    }

    override fun onSynthesisDone(vocalizer: Vocalizer, synthesis: Synthesis) {
        println("Synthesis done")
    }

    override fun onPlayingBegin(vocalizer: Vocalizer) {
        println("Playing begin")
    }

    override fun onPlayingDone(vocalizer: Vocalizer) {
        println("Playing done")
    }

    override fun onVocalizerError(vocalizer: Vocalizer, error: ru.yandex.speechkit.Error) {
        println("Error occurred " + error.string)
        resetVocalizer()
    }

    override fun onRecordingBegin(recognizer: Recognizer) {
        println("Recording begin")
    }

    override fun onSpeechDetected(recognizer: Recognizer) {
        println("Speech detected")
    }

    override fun onSpeechEnds(recognizer: Recognizer) {
        println("Speech ends")
    }

    override fun onPowerUpdated(recognizer: Recognizer, power: Float) { }

    override fun onPartialResults(recognizer: Recognizer, recognition: Recognition, b: Boolean) {
        println("Partial results " + recognition.bestResultText)
    }

    override fun onRecognitionDone(recognizer: Recognizer, recognition: Recognition) {
        println("msg: " + recognition.bestResultText)
        checkAutoBusStop(recognition.bestResultText)
    }

    override fun onError(recognizer: Recognizer, error: ru.yandex.speechkit.Error) {
        if (error.code == Error.ERROR_CANCELED) {
            println("Cancelled")
        } else {
            println("Error occurred " + error.string)
            resetRecognizer()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            @NonNull permissions: Array<String>,
                                            @NonNull grantResults: IntArray) {
        if (requestCode != REQUEST_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        println("Record audio permission was not granted")
    }
}
