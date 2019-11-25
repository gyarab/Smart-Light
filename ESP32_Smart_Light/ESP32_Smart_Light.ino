#include "BluetoothSerial.h"
#include "WiFi.h"


BluetoothSerial ESP_BT;

String verification = "$=EF%@gR;[M+SLWx*i%m@qA}x6kDl.";
String data = "";
struct Packet {
  String head;
  String body;
  String checkSum;
  int counter;
};
Packet p = {"", "", "", 0};



void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  ESP_BT.begin("Smart Light");
  WiFi.mode(WIFI_STA);
  //WiFi.disconnect();
  pinMode(2, OUTPUT);




}

void loop() {
  // put your main code here, to run repeatedly:

  char in;
  if (ESP_BT.available()) {
    digitalWrite(2, HIGH);
    in = ESP_BT.read();
    switch (in) {
      case (byte)0x01:
        p.counter = 1;
        break;
      case (byte)0x02:

        p.counter = 2;
        break;
      case (byte)0x03:
        p.counter = 3;
        break;
      case (byte)0x04:
        executeCommand(p);
        p = {"", "", "", 0};

        break;
      default:
        switch (p.counter) {
          case 1:
            p.head += in;
            break;
          case 2:
            p.body += in;
            break;
          case 3:
            p.checkSum += in;
            break;

        }

        break;
    }
  }
}


void executeCommand(Packet pac) {
  Serial.print("Head: ");
  Serial.print(p.head);
  Serial.println();
  Serial.print("Body: ");
  Serial.print(p.body);
  Serial.println();
  Serial.print("Check: ");
  Serial.println();
  if (pac.head == "VER") {

    Serial.print(p.body == verification);
  } else if (pac.head == "GET_WIFI") {
    int n = WiFi.scanNetworks();
    Serial.println(n);
    if (n == 0) {
      Serial.println("no networks");
    } else {
      for (int i = 0; i < n; i++) {
        Serial.print(WiFi.SSID(i));
        Packet wifiList = {"WILI", WiFi.SSID(i), "", 0};
        sendData(wifiList);
      }
    }
  } else if (pac.head == "CONNECT") {
    String ssid = "";
    String pass = "";
    bool divider = false;
    for (char c : pac.body) {
      if (c != ';' && !divider) {
        ssid += c;
      } else {
        divider = true;
      }
      if (c != ';' && divider) {
        pass += c;
      }
    }
    char ssidC[ssid.length() + 1];
    char passC[pass.length() + 1];
    ssid.toCharArray(ssidC, ssid.length() + 1);
    pass.toCharArray(passC, pass.length() + 1);
    Serial.println(ssid);
    Serial.println(ssidC);
    Serial.println(pass);
    Serial.println(passC);
    WiFi.begin(ssidC, passC);
    Packet connecting = {"CONNECTING", "1", "", 0};
    sendData(connecting);
    int i = 1;
    while (WiFi.status() != WL_CONNECTED) {
      delay(500);
      if ((i++ % 11) == 0) {
        connecting = {"CONNECTING", "2", "", 0};
        sendData(connecting);
        break;
      }
    }
    if (WiFi.status() == WL_CONNECTED) {
      connecting = {"CONNECTING", "0", "", 0};
      sendData(connecting);
      connecting = {"IPAD", ipToString(WiFi.localIP()), "", 0};
      sendData(connecting);
     


    }

  }

}


void sendData(Packet pac) {
  String dat = "";
  dat += ((char) 1);
  dat += pac.head;
  dat += ((char) 2);
  dat += pac.body;
  dat += ((char) 3);
  dat += fletcherCheckSum(dat);
  dat += ((char) 4);
  ESP_BT.println(dat);

}


String fletcherCheckSum(String s) {
  long c1 = 0;
  long c2 = 0;
  for (char c : s) {
    c1 += c;
    c2 += c1;
  }
  String check = String((int)c1 % 255, HEX);
  check += String((int)c2 % 255, HEX);
  return check;

}

String ipToString(IPAddress ip) {
  String s = "";
  for (int i = 0; i < 4; i++)
    s += i  ? "." + String(ip[i]) : String(ip[i]);
  return s;
}
