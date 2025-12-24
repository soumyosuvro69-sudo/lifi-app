#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// LCD setup (I2C address 0x27, 16x2 display)
LiquidCrystal_I2C lcd(0x27, 16, 2);

int ldrPin = A0;          // LDR connected to Analog pin A0
int threshold = 500;      // Adjust depending on light intensity
String receivedBits = ""; // Store incoming bits
String receivedWord = ""; // Store decoded word

void setup() {
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Li-Fi Receiver");
  delay(2000);
  lcd.clear();
  Serial.begin(9600);
}

void loop() {
  int sensorValue = analogRead(ldrPin);

  // Detect light ON (flash as "1")
  if (sensorValue > threshold) {
    receivedBits += "1";
    delay(300); // debounce for one bit
  } 
  // Detect light OFF (flash as "0")
  else {
    receivedBits += "0";
    delay(300);
  }

  // Decode after 8 bits = 1 character
  if (receivedBits.length() >= 8) {
    char letter = (char) strtol(receivedBits.c_str(), NULL, 2);

    // Append character to word
    receivedWord += letter;

    // Print word on LCD
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Received:");
    lcd.setCursor(0, 1);
    lcd.print(receivedWord);

    // Debugging on Serial Monitor
    Serial.print("Letter received: ");
    Serial.println(letter);
    Serial.print("Word so far: ");
    Serial.println(receivedWord);

    // Reset bit buffer for next character
    receivedBits = "";

    // Limit word length to 15 letters (LCD fits 16 chars per line)
    if (receivedWord.length() >= 15) {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Final Msg:");
      lcd.setCursor(0, 1);
      lcd.print(receivedWord);
      delay(5000); // Show final message for 5 sec

      // Reset for next message
      receivedWord = "";
    }
  }
}
