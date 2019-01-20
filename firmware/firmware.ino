#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11); // RX, TX
const int pin_TAB    = 3;
const int pin_SELECT = 4;
const int pin_A      = 5;
const int pin_B      = 7;
int sensorLR         = A1;

#define SERIAL_SPEED  9600

int func_val = 650;

bool logg[8] = {0,0,0,0, 0,0,0,0};
int tick = 0;
bool changes = false;
uint16_t potidelay = 0;

int valA = 0;
int valB = 0;
int valA2 = 0;
int valB2 = 0;

char function[] = "k";

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
  
  mySerial.println("at+namePilpCOM");
}

void loop() {
  potidelay++;
  changes = false;
  if (potidelay%2048 == 0) mySerial.print(function);
  func_val = analogRead(sensorLR);
  valA = digitalRead(pin_A);
  valB = digitalRead(pin_B);
  
  if (digitalRead(pin_TAB) == LOW) {
    mySerial.print("t"); // tab
    delay(200);
  }
  if (digitalRead(pin_SELECT) == LOW) {
    mySerial.print("s"); // select
    delay(200);
  }

  if (func_val>3800) {
    function[0] = 'o';
  } else if (func_val>3200) {
    function[0] = 'm';
  } else if (func_val>2600) {
    function[0] = 'j';
  } else if (func_val>2000) {
    function[0] = 'i';
  } else {
    function[0] = 'k';
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
    }    
  }    
  
  //delay(250);

  //if (mySerial.available()) Serial.write(mySerial.read());
  //if (Serial.available()) mySerial.write(Serial.read());
}
