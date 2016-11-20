/*
  Yún HTTP Client Console version for Arduino Uno and Mega using Yún Shield

 This example for the YunShield/Yún shows how create a basic
 HTTP client that connects to the internet and downloads
 content. In this case, you'll connect to the Arduino
 website and download a version of the logo as ASCII text.

 created by Tom igoe
 May 2013
 modified by Marco Brianza to use Console

 This example code is in the public domain.

 http://www.arduino.cc/en/Tutorial/HttpClient

 */

#include <Bridge.h>
#include <HttpClient.h>
#include <Console.h>

void setup() {
  // Bridge takes about two seconds to start up
  // it can be helpful to use the on-board LED
  // as an indicator for when it has initialized
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);
  Bridge.begin();
  digitalWrite(13, HIGH);
  Serial.begin(9600);

}



void loop() {
  // Initialize the client library
  SerialUSB.print('0');
  HttpClient client;

  // GET GPS
  //SEND 
  //60.019950/30.366722
  ///60.016792/30.367280
  //60.014644/30.368138
  //60.012968/30.368739
  //60.011292/30.368997
  //60.009530/30.369254
  //60.007102/30.370026
  //60.003836/30.367880
  //60.001622/30.366766
  //59.999965/30.365502
  client.get("http://192.168.1.44:51580/api/update/1/60.019950/30.366722");
  delay(10000);
  SerialUSB.print('1');
  client.get("http://192.168.1.44:51580/api/update/1/60.016792/30.367280");
  delay(10000);
  SerialUSB.print('2');
  client.get("http://192.168.1.44:51580/api/update/1/60.014644/30.368138");
  delay(10000);
  SerialUSB.print('3');
  client.get("http://192.168.1.44:51580/api/update/1/60.012968/30.368739");
  delay(10000);
  SerialUSB.print('4');
  client.get("http://192.168.1.44:51580/api/update/1/60.011292/30.368997");
  delay(10000);
  SerialUSB.print('5');
  client.get("http://192.168.1.44:51580/api/update/1/60.009530/30.369254");
  delay(10000);
  SerialUSB.print('6');
  client.get("http://192.168.1.44:51580/api/update/1/60.007102/30.370026");
  delay(10000);
  client.get("http://192.168.1.44:51580/api/update/1/60.003836/30.367880");
  delay(10000);
  SerialUSB.print('7');
  client.get("http://192.168.1.44:51580/api/update/1/60.001622/30.366766");
  delay(10000);
  SerialUSB.print('8');
  client.get("http://192.168.1.44:51580/api/update/1/59.999965/30.365502");
  SerialUSB.print('9');
  delay(60000);

  // if there are incoming bytes available
  // from the server, read them and print them:
 // while (client.available()) {
  //  char c = client.read();
 //   SerialUSB.print(c);



  delay(5000);
}
