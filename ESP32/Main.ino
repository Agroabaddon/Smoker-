#include <OneWire.h>
#include <DallasTemperature.h>
#include <BluetoothSerial.h>
#include <PID_v1.h>

#define ONE_WIRE_BUS 21 // GPIO pin for DS18B20 data line
#define BUTTON_PIN 19   // GPIO pin for the button

// Define BluetoothSerial object
BluetoothSerial SerialBT;

// Define temperature sensor
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);

// Define PID parameters
double setpoint = 98.6; // Desired temperature in degrees Fahrenheit
double input, output;
double kp = 2, ki = 5, kd = 1; // PID tuning parameters
PID myPID(&input, &output, &setpoint, kp, ki, kd, DIRECT);

// Button variables
int buttonState = 0;
bool bluetoothConnected = false;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32 Thermometer"); // Bluetooth device name

  // Initialize temperature sensor
  sensors.begin();

  // Set PID limits and start PID controller
  myPID.SetMode(AUTOMATIC);
  myPID.SetOutputLimits(0, 255); // Adjust output limits based on your heating element

  // Set button pin as input
  pinMode(BUTTON_PIN, INPUT);
}

void loop() {
  // Read button state
  buttonState = digitalRead(BUTTON_PIN);

  // If button is pressed, initiate Bluetooth connection
  if (buttonState == HIGH && !bluetoothConnected) {
    SerialBT.begin("ESP32 Thermometer"); // Re-initiate Bluetooth connection
    bluetoothConnected = true;
  }

  // If Bluetooth is connected, handle temperature control and communication
  if (bluetoothConnected) {
    // Read temperature data
    sensors.requestTemperatures();
    input = sensors.getTempFByIndex(0);

    // Compute PID output
    myPID.Compute();

    // Control heating element based on PID output
    // Implement your heating element control logic here

    // Send temperature data over Bluetooth
    SerialBT.print("Temperature: ");
    SerialBT.println(input);
  }

  // Delay before checking button state again
  delay(100);
}
