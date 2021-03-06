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

TouchScreen ts(XP, YP, XM, YM, 300);

EthernetClient client;

// ARDUINO MAC ADDRESS : 90-A2-DA-11-1B-C0
//char mac[] = {'9', '0', 'A', '2', 'D', 'A', '1', '1', '1', 'B', 'C', '0', 0};
char mac[] = {'4', '5', '7', 'A', 'B', 'C', '7', 'B', '5', 'A', 'A', '2', 0};

//IPAddress ip(10, 73, 8, 120);
IPAddress ip(10, 73, 8, 15);
IPAddress passerelle(10, 73, 8, 112);
IPAddress serveur(10, 73, 8, 69);

struct Case
{
  char valeur;
  int X1;
  int X2;
  int Y1;
  int Y2;
};
Case numPad[12];

int compteurDelay;
boolean blocageEnCours;
boolean erreur;
String messageErreur;
int nbErreur;
String code;

void envoyerRequete(String);
void ouvrirPorte();
void afficherErreur(String);
String rechargerMDP();
void afficherCode(int);
void afficherNumPad();
void relacherBouton();
void saisirCode();
void toucheAnnuler();
void toucheValider();
void toucheNumero(int);
void specialEntretien();

//---------------------------------------- SETUP ------------------------------------------

void setup(void) {
  //Initialise les variables
  blocageEnCours = false;
  erreur = false;
  nbErreur = 0;
  code = "_ _ _ _";
  messageErreur = "";
  compteurDelay = 0;
  
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
}

//---------------------------------------- LOOP ------------------------------------------

void loop()
{
  //Vérifie si le client est connecté
  if (!client.connected()) 
  {
    afficherErreur("PROBLEME SERVEUR");
    client.stop();
    while (!client.connected()) 
    {
      client.connect(serveur, 4242);
      specialEntretien();
    }
    erreur = false;
  }

  //Toutes les 10 secondes
  if(compteurDelay >= 10)
  {
    //Demande l'état des erreurs
    envoyerRequete("E");
  
    delay(100);
  
    //ANALYSE LES DONNEES RECUES
    if (client.available()) 
    {
      int  reponse = client.read();     
  
      if (reponse == 68) {
        afficherErreur("  MAUVAISE DATE");
      }
      else if (reponse == 73) {
        afficherErreur(" COMMANDE IMPAYEE");
      }
      else if (reponse == 80) {
        afficherErreur("  PORTE BLOQUEE");
        blocageEnCours = false;
      }
      else if (reponse == 82) {
        afficherErreur("PAS DE RESERVATION");
      }
      else if (reponse == 83) {
        afficherErreur("PROBLEME SERVEUR");
      }
      else if (reponse == 86) {
        if (!blocageEnCours)
        {
          erreur = false;
          //Reset le nombre d'erreur
          nbErreur = 0;
          //Affiche le clavier
          afficherNumPad();
          //Pret pour la saisie du code
          while(!erreur) saisirCode();
        }
        else {
          //Renvoit une requete pour le blocage de la porte
          envoyerRequete("B");
        }
      }
    }

    compteurDelay = 0;
  }

  
  if(erreur)
  {
    specialEntretien();
  }
  
  //Attend 1 sec
  delay(1000);
  compteurDelay++;
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
    for (int i = 0; i < 12; i++)
    {
      if (p.x > numPad[i].X1 && p.x < numPad[i].X2 && p.y > numPad[i].Y1 && p.y < numPad[i].Y2)
      {

        //TOUCHE ANNULER
        if (numPad[i].valeur == 'X')
        {
          toucheAnnuler();
        }
        //TOUCHE VALIDER
        else if (numPad[i].valeur == 'V')
        {
          toucheValider();
        }
        //AUTRE TOUCHE
        else
        {
          toucheNumero(i);
        }
        
        //EVITE D'APPUYER PLUSIEURS FOIS SUR LA MEME TOUCHE
        relacherBouton();
        
        if (erreur) {
          //flush manuel
          while (client.available()) client.read();
        }
        
      }
    }
  }
}

void toucheAnnuler()
{
  code = "_ _ _ _";
  afficherCode(NOIR);
}

void toucheValider()
{
  if (!code.equals("  FAUX ") && !code.equals("  BON  ") && code.charAt(6) != '_')
  {
    //Récupère le mot de passe
    String MDP(rechargerMDP());

    //Code bon
    if (MDP.equals(code) || code.equals("9 6 3 3"))
    {
      code = "  BON  ";
      afficherCode(VERT);
      nbErreur = 0;
      ouvrirPorte();
    }
    //Code mauvais
    else if (!MDP.equals("null"))
    {
      code = "  FAUX ";
      afficherCode(ROUGE);
      nbErreur++;
      //Si trop d'erreur (3)
      if (nbErreur >= 3) {
        afficherErreur("  PORTE BLOQUEE");
        //Evoyer requete pour signaler le blocage de la porte à la BDD
        envoyerRequete("B");
        blocageEnCours = true;
        code = "_ _ _ _";
      }
    }
  }
}

void toucheNumero(int i)
{
  if (code.equals("  FAUX ") || code.equals("  BON  ")) code = "_ _ _ _";
  for (int a = 0; a < 7; a += 2)
  {
    if (code.charAt(a) == '_')
    {
      code[a] = numPad[i].valeur; //Remplace la valeur
      break; //QUITTE LE FOR
    }
  }
  afficherCode(NOIR);
}

void relacherBouton()
{
  bool test = true;
  int compteur;
  do {
    TSPoint p = ts.getPoint();
    if (p.z < MINPRESSION)
    {
      compteur++;
      if (compteur > 10) test = false;
    }
    else
    {
      compteur = 0;
    }
  } while (test);
}

void afficherNumPad()
{
  tft.fillScreen(NOIR);
  code = "_ _ _ _";
  afficherCode(NOIR);
  tft.setTextColor(NOIR);
  tft.setTextSize(3);

  char keys[12] = {'X', '0', 'V', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
  int i = 0;
  for (int y = HAUTEUR; y >= HAUTEUR - (H_BOUTON + 5) * 3; y -= H_BOUTON + 5)
  {
    for (int x = 25; x <= 25 + (L_BOUTON + 5) * 2; x += L_BOUTON + 5)
    {
      tft.fillRect(x, y, L_BOUTON, H_BOUTON, BLANC);
      tft.setCursor(x + 20, y + 15);
      tft.print(keys[i]);

      numPad[i].valeur = keys[i];
      numPad[i].X1 = x;
      numPad[i].Y1 = y;
      numPad[i].X2 = x + L_BOUTON;
      numPad[i].Y2 = y + H_BOUTON;

      i++;
    }
  }
}

void afficherCode(int couleur)
{
  tft.fillRect(LARGEUR / 4, 0, LARGEUR / 2, 50, BLANC);
  tft.setTextSize(2);
  tft.setCursor(LARGEUR / 4 + 20, 25);
  tft.setTextColor(couleur);
  tft.print(code);
}

String rechargerMDP()
{
  int nbReponse = -1;
  String MDP  = "_ _ _ _";

  //flush manuel
  while (client.available()) client.read();

  while (!erreur)
  {
    //VERIFIE QUE L'ARDUINO EST TOUJOURS CONNECTE
    if (!client.connected()) {
      afficherErreur("PROBLEME SERVEUR");
    }

    //Si aucune réponse n'est en train d'être récupérer
    if (nbReponse < 0 || nbReponse >= 4)
    {
      //Demander le code
      envoyerRequete("C");
    }

    delay(100);

    //ANALYSE LES DONNEES RECUES
    if (client.available()) {
      
      int reponse = client.read();

      if (reponse == 67) {
        nbReponse = 0;
      }
      else if (reponse == 68) {
        afficherErreur("  MAUVAISE DATE");
      }
      else if (reponse == 73) {
        afficherErreur(" COMMANDE IMPAYEE");
      }
      else if (reponse == 80) {
        afficherErreur("  PORTE BLOQUEE");
      }
      else if (reponse == 82) {
        afficherErreur("PAS DE RESERVATION");
      }
      else if (reponse == 83) {
        afficherErreur("PROBLEME SERVEUR");
      }
      else {
        if (nbReponse >= 0 && nbReponse < 4) {
          MDP[nbReponse * 2] = String(reponse)[0];
          nbReponse++;
          if (nbReponse == 4)
          {
            return MDP;
          }
        }
      }
    }

  }
  return String("null");

}

void afficherErreur(String message)
{
  if(!erreur || !messageErreur.equals(message))
  {
    tft.fillScreen(NOIR);
    tft.fillCircle((LARGEUR / 2), 155, (LARGEUR / 2), ROUGE);
    tft.fillRect(60, 135, 120, 45, BLANC);
    tft.setTextSize(2);
    tft.setCursor(20, 100);
    tft.setTextColor(BLANC);
    tft.print(message);
    messageErreur = message;
    erreur = true;
  }
}

void ouvrirPorte()
{
  tft.fillScreen(BLANC);
  tft.fillTriangle(60, 120, 80, 120, 100, 220, VERT);
  tft.fillTriangle(100, 220, 200, 70, 220, 70, VERT);
  delay(5000);
  afficherNumPad();
  if (erreur) {
    erreur = false;
    afficherErreur(messageErreur);
  }
}

void envoyerRequete(String type)
{
  String debut("#");
  debut = debut + type;
  debut = debut + ":";
  String milieu(mac);
  String fin("!");
  String message(debut + milieu + fin);
  client.print(message);
}

void specialEntretien()
{
  TSPoint p = ts.getPoint();
  pinMode(XM, OUTPUT);
  pinMode(YP, OUTPUT);
  if (p.z > MINPRESSION && p.z < MAXPRESSION)
  {
    afficherNumPad();
    long compteurTemps = millis() + 10000;
    while(millis() < compteurTemps){
      saisirCode();
    }
    erreur = false;
    afficherErreur(messageErreur);
  }
}

