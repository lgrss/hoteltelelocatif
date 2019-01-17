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

/**
 *
 * @author Karl La Grassa
 */
public class C_Chambre {

    /* ATTRIBUTS */
    private int Numero;
    private int NbLitSimple;
    private int NbLitDouble;
    private Boolean SdB;
    private Boolean WC;
    private char CodeTarif;
    private String AddrMac;
    private String errno;
    private int idHotel;

    /* METHODES */
    /**
     * Constructeur par défaut
     */
    public C_Chambre() {
        this.Numero = 0;
        this.NbLitDouble = 0;
        this.NbLitSimple = 0;
        this.SdB = false;
        this.WC = false;
        this.CodeTarif = 'F';
        this.AddrMac = "00:00:00:00:00:00";
    }

    /**
     * Constructeur avec tous les paramètres
     *
     * @param Numero
     * @param NbLitSimple
     * @param NbLitDouble
     * @param aUneSdB
     * @param aUnWC
     * @param CodeTarif
     * @param AddrMac
     */
    public C_Chambre(int Numero,
            int NbLitSimple,
            int NbLitDouble,
            Boolean aUneSdB,
            Boolean aUnWC,
            char CodeTarif,
            String AddrMac) {
        this.Numero = Numero;
        this.NbLitDouble = NbLitDouble;
        this.NbLitSimple = NbLitSimple;
        this.SdB = aUneSdB;
        this.WC = aUnWC;
        this.CodeTarif = CodeTarif;
        this.AddrMac = AddrMac;
    }

    /**
     * Constructeur qui récupère les données à partir de la base de données
     * @param Numero
     * @param handler
     */
    public C_Chambre(String nomHotel, int Numero, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT * "
                    + "FROM `t_chambre` c "
                    + "INNER JOIN `t_hotel` h "
                    + "WHERE c.id_hotel = h.id_hotel "
                    + "AND `nom`='"+nomHotel+"' "
                    + "AND `numero`='"+Numero+"'";
            ResultSet resultat = statement.executeQuery(requete);

            while (resultat.next()) {
                this.setIdHotel(resultat.getInt(2));
                this.SetNumero(resultat.getInt(3));
                this.SetNbLitSimple(resultat.getInt(4));
                this.SetNbLitDouble(resultat.getInt(5));
                this.SetSdB(resultat.getBoolean(6));
                this.SetWC(resultat.getBoolean(7));
                this.SetCodeTarif(resultat.getString(8).charAt(0));
                this.SetAddrMac(resultat.getString(9));
            }
        } catch (SQLException ex) {
            Logger.getLogger(C_Chambre.class.getName())
                    .log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
        }
    }

    public C_Chambre(int idHotel, int Numero, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT * FROM `t_chambre` WHERE "
                    + "`numero`='" + Numero + "'";
            ResultSet resultat = statement.executeQuery(requete);

            while (resultat.next()) {
                this.setIdHotel(resultat.getInt(2));
                this.SetNumero(resultat.getInt(3));
                this.SetNbLitSimple(resultat.getInt(4));
                this.SetNbLitDouble(resultat.getInt(5));
                this.SetSdB(resultat.getBoolean(6));
                this.SetWC(resultat.getBoolean(7));
                this.SetCodeTarif(resultat.getString(8).charAt(0));
                this.SetAddrMac(resultat.getString(9));
            }
        } catch (SQLException ex) {
            Logger.getLogger(C_Chambre.class.getName())
                    .log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
        }
    }

    /**
     * Mise a jour vers la BDD des caractéristiques d'une chambre
     *
     * @param newNumero
     * @param newNbLitSimple
     * @param newNbLitDouble
     * @param newaUneSdB
     * @param newaUnWC
     * @param newCodeTarif
     * @param newAddrMac
     * @param handler
     * @return 1 - Doublon présent | 0 - Réussite | -1 Echec MySQL
     */
    public int MettreAJour(int newNumero,
            int newNbLitSimple,
            int newNbLitDouble,
            Boolean newaUneSdB,
            Boolean newaUnWC,
            char newCodeTarif,
            String newAddrMac,
            String nomHotel,
            C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `numero`, `nbLitSimple`,`nbLitDouble`,"
                    + "`SDB`, `WC`,`codeTarif`,`addrMac` "
                    + "FROM `t_chambre` c "
                    + "INNER JOIN `t_hotel` h "
                    + "ON c.id_hotel = h.id_hotel "
                    + "WHERE `nom`='"+nomHotel+"' "
                    + "AND `numero`='"+this.GetNumero()+"'";
            ResultSet resultat = statement.executeQuery(requete);

            /* Valeurs modifiées */
            while (resultat.next()) {
                if (newNumero != resultat.getInt(1)
                        || newNbLitSimple != resultat.getInt(2)
                        || newNbLitDouble != resultat.getInt(3)
                        || newaUneSdB != resultat.getBoolean(4)
                        || newaUnWC != resultat.getBoolean(5)
                        || newCodeTarif != resultat.getString(6).charAt(0)
                        || !newAddrMac.equals(resultat.getString(7))) {
                    int SdB = (newaUneSdB) ? 1 : 0;
                    int WC = (newaUnWC) ? 1 : 0;
                    requete = "UPDATE `t_chambre` c "
                            + "INNER JOIN `t_hotel` h "
                            + "ON c.id_hotel = h.id_hotel "
                            + "SET `numero`='"+newNumero+"', "
                            + "`nbLitSimple`='"+newNbLitSimple+"', "
                            + "`nbLitDouble`='"+newNbLitDouble+"', "
                            + "`SDB`='"+SdB+"', "
                            + "`WC`='"+WC+"', "
                            + "`codeTarif`='"+newCodeTarif+"', "
                            + "`addrMac`='"+newAddrMac+"' "
                            + "WHERE c.numero = '"+this.GetNumero()+"' "
                            + "AND h.nom = '"+nomHotel+"'";
                    return statement.executeUpdate(requete);
                } else {
                    return 0;
                }
            }

            requete = "UPDATE `t_chambre` "
                    + "SET `numero`=" + newNumero + ", "
                    + "`nbLitSimple`=" + newNbLitSimple + ", "
                    + "`nbLitDouble`=" + newNbLitDouble + ", "
                    + "`SDB`=" + newaUneSdB + ", "
                    + "`WC`=" + newaUnWC + ", "
                    + "`codeTarif`='" + newCodeTarif + "', "
                    + "`addrMac`='" + newAddrMac
                    + "' WHERE `numero`=" + this.GetNumero();
            return statement.executeUpdate(requete);
        } catch (SQLException ex) {
            Logger.getLogger(C_Chambre.class.getName())
                    .log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
            return -1;
        }
    }

    /**
     * Ajoute une chambre à la base de données
     *
     * @param newNumero
     * @param newNbLitSimple
     * @param newNbLitDouble
     * @param newaUneSdB - 1 = true, 0 = false
     * @param newaUnWC - 1 = true, 0 = false
     * @param newCodeTarif
     * @param newAddrMac
     * @param handler
     * @return 1 si une chambre identique existe, 0 si succès, -1 erreur MySQL
     */
    public int Ajouter(int newNumero,
            int newNbLitSimple,
            int newNbLitDouble,
            int newaUneSdB,
            int newaUnWC,
            char newCodeTarif,
            String newAddrMac,
            String hotelChoisi,
            C_BDD handler) {
        int doublon = 1;
        C_Hotel hotel = new C_Hotel(hotelChoisi, handler);
        int id_hotel = hotel.getId();
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `numero`"
                    + " FROM `t_chambre` "
                    + "WHERE `id_hotel` =" + id_hotel;
            ResultSet resultat = statement.executeQuery(requete);

            /* On teste si une chambre avec le même numéro existe */
            while (resultat.next()) {
                if (resultat.getInt(1) == newNumero) {
                    return doublon;
                }
            }

            /* Si non, on peut ajouter la chambre dans la base de données */
            requete = "INSERT INTO `hotel`.`t_chambre` "
                    + "(`id_hotel`, `numero`, `nbLitSimple`, "
                    + "`nbLitDouble`, `SDB`, `WC`, `codeTarif`, "
                    + "`addrMac`) "
                    + "VALUES ('" + id_hotel + "', "
                    + "'" + newNumero + "', "
                    + "'" + newNbLitSimple + "', "
                    + "'" + newNbLitDouble + "', "
                    + "'" + newaUneSdB + "', "
                    + "'" + newaUnWC + "', "
                    + "'" + newCodeTarif + "', "
                    + "'" + newAddrMac + "')";

            statement.executeUpdate(requete);
            return 0;
        } catch (SQLException ex) {
            Logger.getLogger(C_Chambre.class.getName()).log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
            return -1;
        }
    }

    /**
     * Supprime la chambre de la base de donnée
     *
     * @param numero - Numero de la chambre à supprimer
     * @param handler - Handler MySQL
     * @return -1 si erreur SQL; autre si requete effectuée
     */
    public int Supprimer(int numero, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "DELETE FROM `hotel`.`t_chambre` "
                    + "WHERE `t_chambre`.`numero` = '" + numero + "'";
            return statement.executeUpdate(requete);
        } catch (SQLException ex) {
            Logger.getLogger(C_Chambre.class.getName()).log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
            return -1;
        }
    }

    /* GETTERS ET SETTERS */
 /* GETTERS */
 /* ============================================================= */
    public int GetNumero() {
        return Numero;
    }

    public int GetNbLitSimple() {
        return NbLitSimple;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public int GetNbLitDouble() {
        return NbLitDouble;
    }

    public Boolean aUneSdB() {
        return SdB;
    }

    public Boolean aUnWC() {
        return WC;
    }

    public char GetCodeTarif() {
        return CodeTarif;
    }

    public String GetAddrMac() {
        return AddrMac;
    }

    public String getErrno() {
        return errno;
    }

    /* SETTERS */
 /* ============================================================= */
 /* La mise à jour des données se fait via les méthodes MettreAJour() 
    ou Ajouter() !
     */
 /* Error codes 
    0 : Succès
    -1 : Echec
     */
    /**
     * Change le numéro de la chambre
     *
     * @param numero
     * @return Error code
     */
    public int SetNumero(int numero) {
        if (numero > 0) {
            this.Numero = numero;
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Change le nombre de lits doubles de la chambre Limité à 2 lits
     *
     * @param nbr
     * @return Error code
     */
    public int SetNbLitDouble(int nbr) {
        if (nbr > 0 && nbr < 3) {
            this.NbLitDouble = nbr;
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Change le nombre de lits simples de la chambre Limité à 4
     *
     * @param NbLitSimple
     * @return Error code
     */
    public int SetNbLitSimple(int NbLitSimple) {
        if (NbLitSimple > 0 && NbLitSimple < 5) {
            this.NbLitSimple = NbLitSimple;
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Modifie la présence de Salle de Bain dans la chambre
     *
     * @param aUneSdB
     */
    public void SetSdB(Boolean aUneSdB) {
        this.SdB = aUneSdB;
    }

    /**
     * Modifie la présence de WC dans la chambre
     *
     * @param aUnWC
     */
    public void SetWC(Boolean aUnWC) {
        this.WC = aUnWC;
    }

    /**
     * Change le code tarif de la chambre Pour les codes tarif voir
     * documentation externe
     *
     * @param CodeTarif
     * @return Error code
     */
    public int SetCodeTarif(char CodeTarif) {
        if (CodeTarif >= 'A' && CodeTarif <= 'F') {
            this.CodeTarif = CodeTarif;
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Modifie l'adresse MAC de la carte Arduino qui gère la poignée de la porte
     * de la chambre Format : "00:00:00:00:00:00"
     *
     * @param AddrMac
     */
    public void SetAddrMac(String AddrMac) {
        this.AddrMac = AddrMac;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

}
