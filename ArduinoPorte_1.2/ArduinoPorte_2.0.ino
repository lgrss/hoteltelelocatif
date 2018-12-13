//---------------------------------------- INCLUDE ------------------------------------------ 

#include <Adafruit_TFTLCD.h>
#include <Adafruit_GFX.h>
#include <TouchScreen.h>
#include <Ethernet2.h>

//---------------------------------------- DEFINE ------------------------------------------ 

//Ports pour le tactile
#define YP A1  // analog
#define XM A2  // analog
#define YM 7   // digital
#define XP 6   // digital

//Taille écran pour le tactile
#define TS_MINX  150
#define TS_MINY  120
#define TS_MAXX  920
#define TS_MAXY  940

//Ports pour écran
#define LCD_CS A3
#define LCD_CD A2
#define LCD_WR A1
#define LCD_RD A0
#define LCD_RESET A4

//Définition des couleurs
#define NOIR    0x0000
#define BLEU    0x001F
#define ROUGE   0xF800
#define VERT    0x07E0
#define CYAN    0x07FF
#define MAGENTA 0xF81F
#define JAUNE   0xFFE0
#define BLANC   0xFFFF

//Calibrage pression
#define MINPRESSION 10
#define MAXPRESSION 1000

//Taille écran
#define LARGEUR    tft.width()
#define HAUTEUR    tft.height()-60

//Taille Boutons
#define L_BOUTON   60
#define H_BOUTON   50

//---------------------------------------- VARIABLES ------------------------------------------ 

Adafruit_TFTLCD tft(LCD_CS, LCD_CD, LCD_WR, LCD_RD, LCD_RESET);

TouchScreen ts = TouchScreen(XP, YP, XM, YM, 300);

EthernetClient client;

// ARDUINO MAC ADDRESS : 90-A2-DA-11-1B-C0
char mac[] = {'9','0','A','2','D','A','1','1','1','B','C','0',0};

IPAddress ip(10, 73, 8, 120);
IPAddress passerelle(10, 73, 8, 112);
IPAddress serveur(10,73,8,49);

struct Case
{
  char valeur;
  int X1;
  int X2;
  int Y1;
  int Y2;
};
Case numPad[12];

boolean erreur = true;

int erreurTotale = 0;
String newMDP  = "_ _ _ _";
String code    = "_ _ _ _";
String MDP     = "       ";

//---------------------------------------- SETUP ------------------------------------------ 

void setup(void) {
  //Configure le port série
  Serial.begin(9600);

  //Configure l'IP
  Ethernet.begin(mac, ip, passerelle);
  delay(1000);
  
  //Configure l'écran tactile
  tft.reset();
  uint16_t identifiant = tft.readID();
  tft.begin(identifiant);
  tft.fillScreen(NOIR);
  
  //Essaye de se connecter au serveur
  Serial.println("connecting...");
  while (!client.connect(serveur, 4242)) 
  {
    afficherErreur("PROBLEME SERVEUR");
    Serial.println("failed to connect, trying again...");
  } 
  Serial.println("connected");

  
}

//---------------------------------------- LOOP ------------------------------------------ 

void loop()
{
  if (!client.connected()) {
      Serial.println("disconnected");
      client.stop();
      afficherErreur("PROBLEME SERVEUR");
      Serial.println("reconnecting...");
      while (!client.connect(serveur, 4242)) 
      {
        Serial.println("failed to reconnect, trying again...");
      } 
      Serial.println("reconnected");
    }

    envoyerRequete("E");
    delay(100);
    
    //ANALYSE LES DONNEES RECUES
    if (client.available()){
        byte reponse = client.read();
        
        if (reponse == 80){
          erreur = true;
          afficherErreur("  PORTE BLOQUEE");
        }
        else if (reponse == 82){
          erreur = true;
          afficherErreur("PAS DE RESERVATION");
        }
        else if (reponse == 83){
          erreur = true;
          afficherErreur("PROBLEME SERVEUR");
        }
        else if (reponse == 86){
          erreur = false;
          erreurTotale = 0;
          afficherNumPad();
          while(!erreur) saisirCode();
        }
    }
    
    delay(10000);
}

//---------------------------------------- FONCTIONS ------------------------------------------ 

void saisirCode()
{
  //RECUPERE LA POSITION DU DOIGT
  TSPoint p = ts.getPoint();
  pinMode(XM, OUTPUT);
  pinMode(YP, OUTPUT);

  //VERIFIE SI ON APPUIE
  if (p.z > MINPRESSION && p.z < MAXPRESSION) 
  {
    //MET LES COORDONNEES A L'ECHELLE DE L'ECRAN
    p.x = map(p.x, TS_MINX, TS_MAXX, tft.width(), 0);
    p.y = map(p.y, TS_MINY, TS_MAXY, tft.height(), 0);

    //TEST QUEL EST LA TOUCHE
    for(int i=0; i<12; i++)
    { 
      if(p.x>numPad[i].X1 && p.x<numPad[i].X2 && p.y>numPad[i].Y1 && p.y<numPad[i].Y2)
      {   
        int couleur;
        
        //TOUCHE ANNULER
        if(numPad[i].valeur == 'X')
        {
          couleur = toucheAnnuler();
        }
        //TOUCHE VALIDER
        else if(numPad[i].valeur == 'V')
        {
          couleur = toucheValider();
        }
        //AUTRE TOUCHE
        else
        {
          couleur = toucheNumero(i);
        }
        if (!erreur){
          //AFFICHE LE NOUVEAU CODE
          afficherCode(couleur);
    
          //EVITE D'APPUYER PLUSIEURS FOIS SUR LA MEME TOUCHE
          relacherBouton();
        }
      }
    }
  }
}

int toucheAnnuler()
{
  code = "_ _ _ _";
  return NOIR;
}

int toucheValider()
{
  if(!code.equals("  FAUX ") && !code.equals("  BON  ") && code.charAt(6) != '_')
  {
    //Récupère le mot de passe
    rechargerMDP();
    
    if(MDP.equals(code))
    {
      code = "  BON  ";
      erreurTotale = 0;
      ouvrirPorte();
      return VERT;
    }
    else
    {
      code = "  FAUX ";
      erreurTotale++;
      if (erreurTotale >= 3){
        erreur = true;
        afficherErreur("  PORTE BLOQUEE");
        envoyerRequete("B");
      }
      return ROUGE;
    }
  }
}

int toucheNumero(int i)
{
  if(code.equals("  FAUX ") || code.equals("  BON  ")) code = "_ _ _ _";
  for(int a=0; a<7; a+=2)
  {
    if(code.charAt(a) == '_')
    {
      code[a] = numPad[i].valeur;
      break; //QUITTE LE FOR
    }
  }
  return NOIR;
}

void relacherBouton()
{
  bool test = true;
  int compteur;
  do{
    TSPoint p = ts.getPoint();
    if (p.z < MINPRESSION) 
    { 
      compteur++;
      if(compteur >10) test = false;
    }
    else
    {
      compteur=0;
    }
  }while(test);
}

void afficherNumPad()
{
  tft.fillScreen(NOIR);
  afficherCode(NOIR);
  tft.setTextColor(NOIR);
  tft.setTextSize(3);

  char keys[12]={'X','0','V','1','2','3','4','5','6','7','8','9'};
  int i=0;
  for(int y=HAUTEUR; y>=HAUTEUR-(H_BOUTON+5)*3; y-=H_BOUTON+5)
  {
    for(int x=25; x<=25+(L_BOUTON+5)*2; x+=L_BOUTON+5)
    {
      tft.fillRect(x, y, L_BOUTON, H_BOUTON, BLANC);
      tft.setCursor(x+20, y+15);
      tft.print(keys[i]);
      
      numPad[i].valeur = keys[i];
      numPad[i].X1 = x;
      numPad[i].Y1 = y;
      numPad[i].X2 = x+L_BOUTON;
      numPad[i].Y2 = y+H_BOUTON;
      
      i++;
    }
  }
}

void afficherCode(int couleur)
{
  tft.fillRect(LARGEUR/4, 0, LARGEUR/2, 50, BLANC);
  tft.setTextSize(2);
  tft.setCursor(LARGEUR/4+20,25);
  tft.setTextColor(couleur);
  tft.print(code);
}

void rechargerMDP()
{
  boolean fini = false;
  int nbReponse = -1;
  
  //flush manuel
  while (client.available()) client.read();
  
  while(!fini)
  {
    //VERIFIE QUE L'ARDUINO EST TOUJOURS CONNECTE
    if (!client.connected()) {
      Serial.println("disconnected");
      client.stop();
      afficherErreur("PROBLEME SERVEUR");
      Serial.println("reconnecting...");
      while (!client.connect(serveur, 4242)) 
      {
        Serial.println("failed to reconnect, trying again...");
      } 
      Serial.println("reconnected");
      afficherNumPad();
    }
    
    //Si aucune réponse n'est en train d'être récupérer
    if(nbReponse < 0 || nbReponse >= 4)
    {
      //Demander le code
      Serial.println("renew Code");
      envoyerRequete("C");
    }
    
    delay(100);
      
    //ANALYSE LES DONNEES RECUES
    if (client.available()){
      
      int reponse = client.read();
      
      if (reponse == 67){
        nbReponse = 0;
      }
      else if (reponse == 80){
        erreur = true;
        afficherErreur("  PORTE BLOQUEE");
        fini = true;
      }
      else if (reponse == 82){
        erreur = true;
        afficherErreur("PAS DE RESERVATION");
        fini = true;
      }
      else if (reponse == 83){
        erreur = true;
        afficherErreur("PROBLEME SERVEUR");
        fini = true;
      }
      else{
        if (nbReponse >= 0 && nbReponse < 4){
          newMDP[nbReponse*2] = String(reponse)[0];
          nbReponse++;
          if(nbReponse == 4) 
          {
            MDP = newMDP;
            Serial.println("Le mot de passe est : " + MDP);
            fini = true;
          }
        }
      }
    }
  }
  //flush manuel
  while (client.available()) client.read();
}

void afficherErreur(String message)
{
  tft.fillScreen(NOIR);
  tft.fillCircle((LARGEUR/2), 155, (LARGEUR/2), ROUGE);
  tft.fillRect(60, 135, 120, 45, BLANC);
  tft.setTextSize(2);
  tft.setCursor(20,100);
  tft.setTextColor(BLANC);
  tft.print(message);
}

void ouvrirPorte()
{
  tft.fillScreen(BLANC);
  tft.fillTriangle(60,120,80,120,100,220,VERT);
  tft.fillTriangle(100,220,200,70,220,70,VERT);
  delay(5000);
  afficherNumPad();
}

void envoyerRequete(String type)
{
  String debut("#");
  debut = debut+type;
  debut = debut+":";
  String milieu(mac);
  String fin("!");
  String message(debut + milieu + fin);
  client.print(message);
}
