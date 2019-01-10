/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hotelApp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Date;

/**
 * Classe Réservation pour le projet HTL
 * @author BTS_SN
 */
public class C_Reservation {
    
    private     int NumeroReservation;
    private     int IDClient;
    private     int NumeroChambre;
    private     int CodePorte;
    private     String Nom, Prenom;
    private     String Mail;
    private     int NbPersonnes;
    private     Boolean CommandePaye;
    private     float PrixRestantAPayer;
    private     Date DateArrivee;
    private     Date DateFin;

    
    /* Variable erreur pour les appels MySQL */
    private     String errno;
    
    /**
     * Constructeur par défaut, appelé quand le numéro de réservation n'est pas
     * renseigné
     */
    public C_Reservation()
    {
        
    }
    
    /**
     * Fait une requête vers la base de données et initialise les attributs en 
     * conséquence
     * @param numeroRes int, Numéro de réservation
     * @param handler C_BDD, handler de la base de données
     */
    public C_Reservation(int numeroRes, C_BDD handler)
    {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT * "
                    + "FROM `t_reservation` r "
                    + "INNER JOIN `t_client` c "
                    + "ON r.id_client = c.id_client "
                    + "INNER JOIN `t_chambre` b "
                    + "ON b.id_chambre = r.id_chambre "
                    + "WHERE `id_reservation`='"+numeroRes+"'";
            ResultSet resultat = statement.executeQuery(requete);
            while (resultat.next())
            {
                setIDClient(resultat.getInt(2));
                setNumeroChambre(resultat.getInt(17));
                setCodePorte(resultat.getInt(6));
                setCommandePaye(resultat.getBoolean(9));
                setNom(resultat.getString(13));
                setPrenom(resultat.getString(14));
                setDateArrivee(resultat.getDate(4));
                setDateFin(resultat.getDate(5));
                
                if (CommandePaye)
                    setPrixRestantAPayer(0);
                else setPrixRestantAPayer(resultat.getFloat(8) - resultat.
                        getFloat(7) );
                
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(C_Reservation.class.getName()).log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
        }
    }

    /**
     * GETTERS ET SETTERS
     */
    
    /* =================================================================*/
    // METHODES GET
    
    /**
     * IDClient : int
     * @return IDClient
     */
    public int getIDClient() {
        return IDClient;
    }

    /**
     * NbPersonnes : int
     * @return NbPersonnes
     */
    public int getNbPersonnes() {
        return NbPersonnes;
    }

    /**
     * NumeroReservation : int
     * @return NumeroReservation
     */
    public int getNumeroReservation() {
        return NumeroReservation;
    }

    /**
     * Prenom : String
     * @return Prenom
     */
    public String getPrenom() {
        return Prenom;
    }

    /**
     * Nom : String
     * @return Nom
     */
    public String getNom() {
        return Nom;
    }

    /**
     * Mail : String
     * @return Mail
     */
    public String getMail() {
        return Mail;
    }

    /**
     * PrixRestantAPayer : float
     * @return PrixRestantAPayer
     */
    public float getPrixRestantAPayer() {
        return PrixRestantAPayer;
    }

    /**
     * NumeroChambre : int
     * @return NumeroChambre
     */
    public int getNumeroChambre() {
        return NumeroChambre;
    }

    /**
     * errno : String
     * @return errno : /!\ Remplacée à chaque levée d'exception
     */
    public String getErrno() {
        return errno;
    }

    /**
     * CodePorte : int
     * @return CodePorte
     */
    public int getCodePorte() {
        return CodePorte;
    }

    /**
     * CommandePaye : Boolean
     * @return CommandePaye
     */
    public Boolean getCommandePaye() {
        return CommandePaye;
    }

    public Date getDateArrivee() {
        return DateArrivee;
    }

    public Date getDateFin() {
        return DateFin;
    }

    /* =================================================================*/
    // METHODES SET
    
    /**
     * @param IDClient : int
     */
    public void setIDClient(int IDClient) {
        this.IDClient = IDClient;
    }
    
    /**
     * @param Mail : String
     */
    public void setMail(String Mail) {
        this.Mail = Mail;
    }

    /**
     * @param NbPersonnes : String
     */
    public void setNbPersonnes(int NbPersonnes) {
        this.NbPersonnes = NbPersonnes;
    }

    public void setNom(String Nom) {
        this.Nom = Nom;
    }

    public void setNumeroReservation(int NumeroReservation) {
        this.NumeroReservation = NumeroReservation;
    }

    public void setPrenom(String Prenom) {
        this.Prenom = Prenom;
    }

    public void setPrixRestantAPayer(float PrixRestantAPayer) {
        this.PrixRestantAPayer = PrixRestantAPayer;
    }

    public void setErrno(String errno) {
        this.errno = errno;
    }

    public void setNumeroChambre(int NumeroChambre) {
        this.NumeroChambre = NumeroChambre;
    }

    /**
     * Le code de la porte ne peut être supérieur à 9999
     * @param CodePorte : int
     */
    public void setCodePorte(int CodePorte) {
        if (CodePorte > 10000)
            errno = "Code de la porte incorrect !";
        else this.CodePorte = CodePorte;
    }
    
    /**
     * Set classique pour la classe (opérations simples)
     * @param CommandePaye 
     */
    public void setCommandePaye(Boolean CommandePaye) {
        this.CommandePaye = CommandePaye;
    }
    
    /**
     * Change la valeur dans la base de données et dans la classe
     * @param CommandePaye
     * @param handler
     * @return 0 en cas de succès. -1 si erreur et errno mis à jour
     */
    public int setCommandePaye(Boolean CommandePaye, C_BDD handler) {
        this.CommandePaye = CommandePaye;
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "UPDATE `t_reservation` "
                    + "SET `commandePaye`='"+CommandePaye+"' "
                    + "WHERE `id_reservation`='"+NumeroReservation+"'";
            return statement.executeUpdate(requete);
        } catch (SQLException ex) {
            Logger.getLogger(C_Reservation.class.getName()).log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
            return -1;
        }
    }

    public void setDateArrivee(Date DateArrivee) {
        this.DateArrivee = DateArrivee;
    }

    public void setDateFin(Date DateFin) {
        this.DateFin = DateFin;
    }
}
