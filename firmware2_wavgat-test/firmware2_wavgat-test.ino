#include <SoftwareSerial.h>

#include <Wire.h>
#include <radio.h>
#include <RDA5807M.h>
#include <RDSParser.h>

#include <MsTimer2.h>

RDA5807M radio;
SoftwareSerial mySerial(10, 11); // RX, TX
RDSParser rds;

#include <Adafruit_GFX.h>
#include "Adafruit_SSD1306.h"
Adafruit_SSD1306 oled(-1);

const int pin_TAB    = 3;
const int pin_SELECT = 4;
const int pin_A      = 5;
const int pin_B      = 7;
int sensorLR         = A1;
const int VCCMESURE  = A0; // I try to use a 10k + 28k Resistor, to get 1.1V from 4.2 on A0, but mesure internal on WAVGAT is with stupid
const int SOUNDIN    = A2; // Visualisation Sound in

#define SERIAL_SPEED  9600
#define STOREAGE      16

int func_val = 650;
int dati = 0;

bool logg[8] = {0,0,0,0, 0,0,0,0};
int tick = 0;
bool changes = false;
uint16_t potidelay = 0;

int valA = 0;
int valB = 0;
int valA2 = 0;
int valB2 = 0;

int vol1 = 0;
int vol2 = 0;

char function[] = "k";

struct Midways {
  byte _val[STOREAGE];
  int  _nxt;
  byte _max;
  byte _min;

  Midways() {
    _nxt = 0;
    for (int i=0; i<STOREAGE; ++i) { 
      _val[i] = 128;
    }
  }

  void add(byte val) {
    _val[_nxt] = val;
    _nxt++;
    if (_nxt == STOREAGE) {
      _nxt = 0;
    }
  }

  byte last() {
    int l = _nxt -1;
    if (l < 0) l += STOREAGE;
    return _val[l];
  }

  byte midget() {
    int mid = 0;
    _min = 255;
    _max = 0;
    for (int i=0; i<STOREAGE; ++i) {
      if (_val[i] > _max) _max=_val[i];
      if (_val[i] < _min) _min=_val[i];
      mid += _val[i];
    }
    
    return (mid/STOREAGE);
  }

  void draw(int x, int y) {
    int id = _nxt-1;
    byte mid = midget();
    
    byte lastx,lasty;
    byte dx = x + STOREAGE;
    short dy = y - (_val[id] - mid);
    
    if (id < 0) id += STOREAGE;
    for (int i=0; i<STOREAGE; ++i) {
      lastx = dx;
      lasty = dy;
      
      dx = x+STOREAGE-i;
      dy = y - (_val[id] - mid);
      if (dy < 0) dy = 0;
      if (dy > 31) dy = 31;
      oled.drawLine(lastx, lasty, dx, dy, WHITE); 
      id--;
      if (id < 0) id += STOREAGE;
    }
  }
} daten;

int idx = 1;
RADIO_FREQ preset[] = {
  8700,
  8770,
  8880,
  8940,
  9510,
  9650,
  9920,
  10130,
  10280,
  10330,
  10510,
  10670
};

RADIO_INFO rinfo;
int  vcc;
char timestr[10];
char namestr[] = "PilpCOM   ";

uint8_t hour, minute, seconds;

void readVcc() {
  analogReference(INTERNAL);
  // 1023=1.1 V internal voltage is maximum. need voltage devider to get 1.1V instead of 4.2 on pin A0
  vcc = analogRead(VCCMESURE);
  analogReference(DEFAULT);
  if (vcc > 3970) vcc = 3970;
  if (vcc < 3920) vcc = 3920;
}

void DisplayServiceName(char *name) {
  sprintf(namestr, "%s", name);
}

void tickTime() {
  seconds++;
  if (seconds >= 60) {minute++; seconds=0;}
  if (minute  >= 60) {hour++; minute=0;}
  if (hour    >= 24) {hour = hour%24;}
  sprintf(timestr, "%02i:%02i:%02i", hour, minute, seconds);
}

void RDS_process(uint16_t block1, uint16_t block2, uint16_t block3, uint16_t block4) {
  rds.processData(block1, block2, block3, block4);
}

void setup() {
  Serial.begin(SERIAL_SPEED);
  mySerial.begin(SERIAL_SPEED);
    while (!Serial) {
    ; // wait for serial port to connect.
  }
  
  pinMode(pin_TAB, INPUT_PULLUP);
  pinMode(pin_SELECT, INPUT_PULLUP);
  pinMode(pin_A, INPUT_PULLUP);
  pinMode(pin_B, INPUT_PULLUP);
  pinMode(VCCMESURE, INPUT);
  pinMode(SOUNDIN, INPUT);
  
  mySerial.println("at+namePilpCOM");
  delay(500);
  
  oled.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  oled.clearDisplay();
  oled.setTextSize(1);
  oled.setTextColor(WHITE);
  oled.display();
    
  radio.init();
  radio.setBandFrequency(RADIO_BAND_FMWORLD, preset[idx]);
  delay(100);
  radio.setMono(true);
  radio.setMute(false);
  radio.attachReceiveRDS(RDS_process);
  rds.attachServicenNameCallback(DisplayServiceName);

  hour=0;
  minute=0;
  seconds=0;
  
  MsTimer2::set(1000, tickTime);
  MsTimer2::start();
}

void loop() {
  radio.checkRDS();
  potidelay++;
  changes = false;
  if (potidelay%256 == 0) {
    func_val = analogRead(sensorLR);
    vol2 = 15 - (func_val/256);
   
    vol1 = radio.getVolume();
    if (vol1 != vol2) {
      radio.setVolume(vol2);
      if (vol2<1) {
        radio.setMute(true);
      } else {
        radio.setMute(false);
      }
    }

    if (potidelay%2048 == 0) oled.clearDisplay();
    if (idx == 0) {
      
      oled.setTextSize(4);
      if (seconds%2) timestr[2] = ' ';
      oled.setCursor(0,0);
      oled.print(timestr);
      
      oled.setTextSize(1);
      oled.setCursor(52,24);
      oled.print(&timestr[6]);
      
    } else {
      
      oled.setTextSize(1);
      
      // display power
      readVcc();
      oled.drawRect(0, 0, 17, 12, WHITE);
      oled.drawRect(17, 4, 2, 4, WHITE);
      oled.fillRect(2, 2, map(vcc, 3920, 3970, 1, 13), 8, WHITE);
  
      // display time
      oled.setCursor(41,0);
      oled.print(timestr);
      
      // display signal
      radio.getRadioInfo(&rinfo);    
      if (rinfo.rssi > 4) oled.drawLine(119, 10, 119, 8, WHITE);
      if (rinfo.rssi > 10) oled.drawLine(121, 10, 121, 6, WHITE);
      if (rinfo.rssi > 22) oled.drawLine(123, 10, 123, 4, WHITE);
      if (rinfo.rssi > 28) oled.drawLine(125, 10, 125, 2, WHITE);
      if (rinfo.rssi > 32) oled.drawLine(127, 10, 127, 0, WHITE);
  
      // display volume
      oled.drawLine(62, 10, 61  -2*vol2, 12, WHITE);
      oled.drawLine(62, 10, 61  -2*vol2, 11, WHITE);
      oled.drawLine(62, 10, 61  -2*vol2, 10, WHITE);
  
      oled.drawLine(66, 10, 67  +2*vol2, 12, WHITE);
      oled.drawLine(66, 10, 67  +2*vol2, 11, WHITE);
      oled.drawLine(66, 10, 67  +2*vol2, 10, WHITE);
  
      // display radio station
      oled.setCursor(0,24);
      oled.print(namestr);
  
      // display radio freq
      oled.setCursor(90,24);
      oled.print(((float)radio.getFrequency())/100.0);

      // display audio
      daten.draw(56, 24);
    }
    oled.display();    
  }
  
  dati = analogRead(SOUNDIN);
  daten.add(dati/16);
  valA = digitalRead(pin_A);
  valB = digitalRead(pin_B);
  
  if (digitalRead(pin_TAB) == LOW) {
    mySerial.print("t"); // tab
    if (vol2 == 0) { // ------------------------ set hour up if volume is 0
      hour = (hour+1)%24;
      seconds=0;
    } else {
      if (idx==11) idx=0;
      else idx++;
      radio.setFrequency(preset[idx]);      
    }
    delay(200);
  }
  if (digitalRead(pin_SELECT) == LOW) {
    mySerial.print("s"); // select
    if (vol2 == 0) { // ------------------------ set minutes up if volume is 0
      minute = (minute+1)%60;
      seconds=0;
    } else {
      if (idx==0) idx=11;
      else idx--;
      radio.setFrequency(preset[idx]);
    }
    delay(200);
  }

  if (func_val>3800) {
    if (function[0] != 'o') {
      function[0] = 'o';
      mySerial.print(function);
    }
  } else if (func_val>3200) {
    if (function[0] != 'm') {
      function[0] = 'm';
      mySerial.print(function);
    }
  } else if (func_val>2600) {
    if (function[0] != 'j') {
      function[0] = 'j';
      mySerial.print(function);
    }
  } else if (func_val>2000) {
    if (function[0] != 'i') {
      function[0] = 'i';
      mySerial.print(function);
    }
  } else if (func_val<=2000) {
    if (function[0] != 'k') {
      function[0] = 'k';
      mySerial.print(function);
    }
  }


  if (valA != valA2 || valB != valB2) {
    if (valA && valB) {
      tick=0;
      for (int i=0;i<8;++i) {
        logg[i] = false;
      }
    }
    logg[tick] = valA;
    tick = (tick+1)%8;
    logg[tick] = valB;
    tick = (tick+1)%8;
    valA2 = valA;
    valB2 = valB;
    changes = true;
  }
  if (changes) {
    if (
      logg[0] == 1 &&
      logg[1] == 1 &&
      logg[2] == 0 &&
      logg[3] == 1 &&
      logg[4] == 0 &&
      logg[5] == 0 &&
      logg[6] == 1 &&
      logg[7] == 0
    ) {
      mySerial.print("d");
      radio.seekDown(false);   
    }
    if (
      logg[0] == 1 &&
      logg[1] == 1 &&
      logg[2] == 1 &&
      logg[3] == 0 &&
      logg[4] == 0 &&
      logg[5] == 0 &&
      logg[6] == 0 &&
      logg[7] == 1
    ) {
      mySerial.print("u");
      radio.seekUp(false);      
    }    
  }    
  
}
