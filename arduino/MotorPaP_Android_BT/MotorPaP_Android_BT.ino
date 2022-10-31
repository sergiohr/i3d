#include <SoftwareSerial.h> 
 
SoftwareSerial ModBluetooth(7,6); // RX | TX 
const int stepPin = 9; 
const int dirPin = 8;
const int pinUp = 14;
const int pinDown = 15;
const int pinPrint = 16;

int stateUp = LOW;
int stateDown = LOW;
int statePrint = LOW;

void setup()  
{ 
    pinMode(stepPin,OUTPUT); 
    pinMode(dirPin,OUTPUT);
    
    pinMode(pinUp, INPUT);
    pinMode(pinDown, INPUT);
    pinMode(pinPrint, INPUT);
    
    digitalWrite(13, LOW);  
     
    ModBluetooth.begin(9600); 
    Serial.begin(9600);  
    ModBluetooth.println("MODULO CONECTADO");  
    ModBluetooth.print("#");  
}

void moveMotor(int steps, char dir){
  
    if( dir == 'B') 
    { 
      digitalWrite(dirPin,HIGH);    
    }
   
    if( dir == 'F') 
    { 
      digitalWrite(dirPin,LOW);
    }
    
    for(int x = 0; x < steps; x++) {                          //200 pasos equivale a1 vuelta, va desde el paso 0 al 199
      digitalWrite(stepPin,HIGH); 
      delayMicroseconds(4000); 
      digitalWrite(stepPin,LOW); 
      delayMicroseconds(4000); 
    }

    ModBluetooth.print("fin");
    ModBluetooth.print("#");
}

void loop()  
{ 
    stateUp = digitalRead(pinUp);
    stateDown = digitalRead(pinDown);
    statePrint = digitalRead(pinPrint);

    if (stateUp == HIGH) {
      moveMotor(50, 'B');
      delay(200);
    }

    if (stateDown == HIGH) {
      moveMotor(50, 'F');
      delay(200);
    }

    if (statePrint == HIGH) {
      ModBluetooth.print("print#");
      delay(500);
    }
      
    if (ModBluetooth.available())  
    { 

        char bufferc[10];
        char dir;
        char readc;
        int steps = 0;
        int i = 0;
        int flag = 0;
        
        while((ModBluetooth.available()> 0) && (i<10) && (!flag)){
            readc = ModBluetooth.read();
            if ( readc != '#'){
              bufferc[i] = readc;
            }
            else flag = 1;
            delay(5); 
            i++;
        }
        bufferc[i] = '\0';

        dir = ModBluetooth.read();
     
        steps = atoi(bufferc); 

        //Seteo el sentido de Giro viendo si el último caracter es una F o una A

        moveMotor(steps, dir);
    
    }      
}



