#include <EEPROM.h>

const int portaLed = 11;
const int portao1 = 12;
const int portao2 = 13;
const int reed_portao1 = 7;
const int reed_portao2 = 8;
const int botao_portao1 = 9;
const int botao_portao2= 10;
const int enderecoPIN = 0;
const int tamanhoPIN =4;
const int enderecoSenha= 4;
const int tamanhoSenha=6;
const int enderecoNome = 10;
const int tamanhoNome = 15;
const int tamanhoPassKey = 12;
const int enderecoPassKey = 25;
char nomeModuloBT[15];
int inicioEndereco;
String decode="";
boolean ativo=false;

void setup(){
  Serial.begin(9600);
  pinMode(portao1, INPUT_PULLUP);
  digitalWrite(portao1, LOW);
  pinMode(portao2, INPUT_PULLUP);
    digitalWrite(portao2, LOW);
  pinMode(portaLed, INPUT_PULLUP);
  pinMode(portao1, OUTPUT);
  pinMode(portao2, OUTPUT);
  pinMode(portaLed, OUTPUT);
  pinMode(reed_portao1, INPUT);
  pinMode(reed_portao2, INPUT);
  pinMode(botao_portao1, INPUT);
  pinMode(botao_portao2, INPUT);
  
  //pisca(100,100);
  //if (!verificaPassKey()) {
//    ativo = false;
//  }
//  else {
    ativo = true;
//  }

}

void loop() {
 if (ativo==true){ 
  while(Serial.available()){
      decode += Serial.read();
        
}
  if (decode=="1") {
    abrePortao(portao1);
  }
  else if (decode=="2") {
    abrePortao(portao2);
  }
 
  if(digitalRead(botao_portao1)==HIGH) {
    abrePortao(portao1);
  }
  if(digitalRead(botao_portao2)==HIGH) {
    abrePortao(portao2);
  }
  if(digitalRead(reed_portao1)==HIGH) {
    enviaMensagem("Portao 1 Aberto");
  }
  if(digitalRead(reed_portao2)==HIGH) {
    enviaMensagem("Portao 2 Aberto");
  }
  //pisca(200,500);
}
else {
  while(Serial.available()){
    decode += Serial.read();
  }
  if (decode=="3") {
  }
  else {
    enviaMensagem("Necessária PassKey");
    }
  }
}

// Abre o portão :-p
void abrePortao(int portao) {
  digitalWrite(portao, HIGH);
  delay(200);
  digitalWrite(portao, LOW);
  //pisca(200,200);
 }

// Pisca o Led ;-0
void pisca(int tempo1, int tempo2) {
  digitalWrite(portaLed, HIGH);
  delay(tempo1);
  digitalWrite(portaLed,LOW);
  delay(tempo2);
}
// Grava dados na EEPROM do ATMega
void gravaEEProm(int tamanho, int endereco, char* valor) {
  inicioEndereco=endereco;
  for (int i=0; i<tamanho; i++) {
  EEPROM.write(inicioEndereco,valor[i]); 
  inicioEndereco++;
  delay(100);
  }
}

// Lê um endereço específico de memória
String leEEProm(int endereco, int tamanho) {
  String valor;  
  for (int i=0; i<tamanho; i++) {
    valor+=EEPROM.read(inicioEndereco+i); 
    delay(100);
  }
  return valor;
}

// Verifica se existe uma senha cadastrada na EEPROM
boolean verificaPassKey() {
  if(leEEProm(enderecoPassKey, tamanhoPassKey) != "")
    return true;
  else
    return false;
}

// Implementa gravação da senha na EEPRom
void setaPassKey(char* senha) {
  gravaEEProm(tamanhoPassKey, enderecoPassKey, senha);
}

// Envia mensagem para o Smartphone/Tablet
void enviaMensagem(String mensagem) {
  if (Serial.available()) {
    Serial.print(mensagem);
  }
}
