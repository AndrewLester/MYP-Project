#include <SoftwareSerial.h>

#define LED 13
#define RELAY 10
#define MOISTURE_SENSOR A0

SoftwareSerial softSerial(2, 3);

byte inboundPacketTypes[] = {0x00, 0x01, 0x02};

struct Packet {
  byte type;
  byte dataSize;
  byte *data;
};

void setup() {
  pinMode(LED, OUTPUT);
  pinMode(RELAY, OUTPUT);
  pinMode(MOISTURE_SENSOR, INPUT);
  
  Serial.begin(9600);
  softSerial.begin(9600);
}

void loop() {

  if (softSerial.available() > 2) {
    struct Packet inboundPacket = constructPacket();
    handlePacket(inboundPacket);
  }

  if ((millis() % 1000) == 0) {
    int analogInput = analogReadDelay(MOISTURE_SENSOR);
    byte data[] = {highByte(analogInput), lowByte(analogInput)};
    struct Packet outboundPacket = {0x00, 0x02, data};
    sendPacket(outboundPacket);
  }

  //digitalWrite(RELAY, i % 2 == 0 ? HIGH : LOW);
  
  
}

struct Packet constructPacket() {
  byte type = softSerial.read();
  byte dataSize = softSerial.read();

//  bool valid = false;
//  for (int i = 0; i < (sizeof(inboundPacketTypes) / sizeof(byte)); i++) {
//    valid = true;
//  }
//  if (!valid) return NULL;

  byte data[dataSize];
    
  for (int i = 0; i < dataSize; i++) {
    data[i] = softSerial.read();
  }
  
  struct Packet inboundPacket = {type, dataSize, data};
  return inboundPacket;
}

void handlePacket(struct Packet inboundPacket) {
  switch (inboundPacket.type) {
    case 0x00:
    case 0x01:
      digitalWrite(LED, (inboundPacket.data[0] == 0x01 ? HIGH : LOW));
      break;
    case 0x02:
      digitalWrite(RELAY, (inboundPacket.data[0] == 0x01 ? HIGH : LOW));
      break;
  }  
}

void sendPacket(struct Packet outboundPacket) {
  softSerial.write(outboundPacket.type);
  softSerial.write(outboundPacket.dataSize);
  softSerial.write(outboundPacket.data, outboundPacket.dataSize);
}

int analogReadDelay(int pin) {
  return analogRead(pin);
  delay(1);
}
