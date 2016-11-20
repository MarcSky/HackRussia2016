//
//  ViewController.swift
//  Siri
//
//  Created by Sahand Edrisian on 7/14/16.
//  Copyright © 2016 Sahand Edrisian. All rights reserved.
//

import AVFoundation
import UIKit
import Speech
import CoreLocation
import AudioToolbox
import Alamofire

class ViewController: UIViewController, SFSpeechRecognizerDelegate, CLLocationManagerDelegate {
    
//    var myUtterance = AVSpeechUtterance(string: "")

    let startRecordingMessage: String = "Пожалуйста, говорите"
    let stopRecordingMessage: String = "Запись закончена"
    let startMessage: String = "Какой автобус вам нужен?"
    var message: String = ""
    let locationManager = CLLocationManager()
    var coordinations: CLLocationCoordinate2D?
	@IBOutlet weak var textView: UITextView!
	@IBOutlet weak var microphoneButton: UIButton!
	
    private let speechRecognizer = SFSpeechRecognizer(locale: Locale.init(identifier: "ru-RU"))!
    
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let audioEngine = AVAudioEngine()
    var timer: Timer?
    
	override func viewDidLoad() {
        super.viewDidLoad()
        
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
        microphoneButton.isEnabled = false
        speechRecognizer.delegate = self

        self.tellPleaseText(text: self.startMessage)

        SFSpeechRecognizer.requestAuthorization { (authStatus) in
            
            var isButtonEnabled = false
            
            switch authStatus {
            case .authorized:
                isButtonEnabled = true
                
            case .denied:
                isButtonEnabled = false
                print("User denied access to speech recognition")
                
            case .restricted:
                isButtonEnabled = false
                print("Speech recognition restricted on this device")
                
            case .notDetermined:
                isButtonEnabled = false
                print("Speech recognition not yet authorized")
            }
            
            OperationQueue.main.addOperation() {
                self.microphoneButton.isEnabled = isButtonEnabled
            }
        }
//        self.startTimer()

	}
    
    func startTimer() {
        self.timer = Timer.scheduledTimer(timeInterval: 5, target: self, selector: #selector(self.longPullingCallback), userInfo: nil, repeats: true)
    }
    
    func endTimer() {
        if self.timer != nil {
            self.timer?.invalidate()
            self.timer = nil
        }
    }
    
    func longPullingCallback() {
        locationManager.startUpdatingLocation()
        if let coordinates = self.coordinations {
            let lat = "\(coordinates.latitude)"
            let lon = "\(coordinates.longitude)"
            let requestURL = "http://192.168.1.44:51580/api/trip/create/" + lat + "/" + lon + "/1"
            print(requestURL)
            Alamofire.request(requestURL).responseJSON { response in
                if let JSON = response.result.value {
                    DispatchQueue.main.async {
                        self.tellPleaseText(text: "Все отлично")
                        self.message = ""
                        self.endTimer()
                        print("\(JSON)")
                    }
                }
            }
        }
    }
    
    func tellPleaseText(text: String) {
        let utterance = AVSpeechUtterance(string: text)
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        utterance.voice = AVSpeechSynthesisVoice(language: "ru-RU")
        let synth = AVSpeechSynthesizer()
        synth.speak(utterance)
    }
    
	@IBAction func microphoneTapped(_ sender: AnyObject) {
        if audioEngine.isRunning {

            self.audioEngine.stop()
            self.recognitionRequest?.endAudio()
            self.microphoneButton.isEnabled = false
            self.message = self.textView.text
            self.textView.text = ""
            print("message: \(self.message)")
            var number:Int?
            let strArr = self.message.characters.split{$0 == " "}.map(String.init)
            for item in strArr {
                let components = item.components(separatedBy: NSCharacterSet.decimalDigits.inverted)
                let part = components.joined(separator: "")
                if let intVal = Int(part) {
                    number = intVal
                    break
                }
            }
            if number != nil {
                print("number: \(number!)")
                let bus = "Автобус \(number!) успешно выбран"
                self.tellPleaseText(text: bus)
                AudioServicesPlayAlertSound(kSystemSoundID_Vibrate)
                self.startTimer()
            }
        } else {
            self.endTimer()
            self.startRecording()
        }
	}

    func startRecording() {
        
        if recognitionTask != nil {  //1
            recognitionTask?.cancel()
            recognitionTask = nil
        }
        
        let audioSession = AVAudioSession.sharedInstance()  //2
        do {
            try audioSession.setCategory(AVAudioSessionCategoryPlayAndRecord)
            try audioSession.setMode(AVAudioSessionModeMeasurement)
            try audioSession.setActive(true, with: .notifyOthersOnDeactivation)
        } catch {
            print("audioSession properties weren't set because of an error.")
        }
        
        recognitionRequest = SFSpeechAudioBufferRecognitionRequest()  //3
        
        guard let inputNode = audioEngine.inputNode else {
            fatalError("Audio engine has no input node")
        }  //4
        
        guard let recognitionRequest = recognitionRequest else {
            fatalError("Unable to create an SFSpeechAudioBufferRecognitionRequest object")
        } //5
        
        recognitionRequest.shouldReportPartialResults = true  //6
        
        recognitionTask = speechRecognizer.recognitionTask(with: recognitionRequest, resultHandler: { (result, error) in  //7
            
            var isFinal = false  //8
            
            if result != nil {
                
                self.textView.text = result?.bestTranscription.formattedString  //9
                self.tellPleaseText(text: self.textView.text)
                print(self.message)
                isFinal = (result?.isFinal)!
            }
            
            if error != nil || isFinal {  //10
                self.audioEngine.stop()
                inputNode.removeTap(onBus: 0)
                
                self.recognitionRequest = nil
                self.recognitionTask = nil
                
                self.microphoneButton.isEnabled = true
            }
        })
        
        let recordingFormat = inputNode.outputFormat(forBus: 0)  //11
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { (buffer, when) in
            self.recognitionRequest?.append(buffer)
        }
        
        audioEngine.prepare()  //12
        
        do {
            try audioEngine.start()
        } catch {
            print("audioEngine couldn't start because of an error.")
        }
        
    }
    
    func speechRecognizer(_ speechRecognizer: SFSpeechRecognizer, availabilityDidChange available: Bool) {
        if available {
            microphoneButton.isEnabled = true
        } else {
            microphoneButton.isEnabled = false
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let userLocation:CLLocation = locations[0] as CLLocation
        
        manager.stopUpdatingLocation()
        
        self.coordinations = CLLocationCoordinate2D(latitude: userLocation.coordinate.latitude,longitude: userLocation.coordinate.longitude)
//        let span = MKCoordinateSpanMake(0.2,0.2)
//        let region = MKCoordinateRegion(center: coordinations, span: span)
        
//        mapView.setRegion(region, animated: true)
        
    }
}

