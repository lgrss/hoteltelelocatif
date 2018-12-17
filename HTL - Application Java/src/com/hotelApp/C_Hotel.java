/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hotelApp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Karl La Grassa
 */
public class C_Hotel {

    /*  ATTRIBUTS */
    private String Nom;
    private String Adresse;
    private String Ville;
    private String CP;
    private String NumTel;
    private ArrayList Services;
    private int Id;

    private String errno;

    /* METHODES */
    /**
     * Constructeur par défaut : Récupère les données en vu de modification
     */
    public C_Hotel() {
        this.Nom = "";
        this.Adresse = "";
        this.Ville = "";
        this.CP = "";
        this.NumTel = "";
        this.Services = new ArrayList();
    }

    /**
     * Récupère les données de l'hôtel et les met dans les attributs.
     *
     * @param nomHotel
     */
    public C_Hotel(String nomHotel, C_BDD handler) {
        this.Nom = getNom(nomHotel, handler);
        this.Adresse = getAdresse(nomHotel, handler);
        this.Ville = getVille(nomHotel, handler);
        this.CP = getCP(nomHotel, handler);
        this.NumTel = getNumTel(nomHotel, handler);
        this.Id = getId(nomHotel, handler);
    }

    /**
     * Constructeur surchargé avec tous les paramètres en vu d'ajout de l'hôtel
     * NumTel de la forme : "06.01.02.03.04"
     *
     * @param Nom
     * @param Adresse
     * @param Ville
     * @param Cp
     * @param NumTel
     */
    public C_Hotel(String Nom,
            String Adresse,
            String Ville,
            String Cp,
            String NumTel) {
        /* On passe par les setters pour réaliser la gestion d'erreur */
        setNom(Nom);
        setVille(Ville);
        setCP(Cp);
        setNumTel(NumTel);
        setAdresse(Adresse);
    }

    /**
     * Met à jour l'hôtel dans la base de donnée
     *
     * @param handler Handler MYSQL
     * @return nbr de champs modifiés si réussite, -1 si erreur, 0 si aucun
     * champs n'était à modifier
     */
    public int MettreAJour(String newNom,
            String newAdresse,
            String newCp,
            String newVille,
            String newNumTel,
            C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT * FROM `t_hotel` WHERE `nom` = '"
                    + this.getNom() + "'";
            ResultSet resultat = statement.executeQuery(requete);

            while (resultat.next()) {
                if (!newNom.equals(resultat.getString(2))
                        || !newAdresse.equals(resultat.getString(3))
                        || !newCp.equals(resultat.getString(4))
                        || !newVille.equals(resultat.getString(5))
                        || !newNumTel.equals(resultat.getString(6))) {
                    requete = "UPDATE `t_hotel` SET `nom`='" + newNom + "',"
                            + "`adresse`='" + newAdresse + "',"
                            + "`cp`='" + newCp + "',"
                            + "`ville`='" + newVille + "',"
                            + "`tel`='" + newNumTel + "'"
                            + "WHERE `nom`='" + this.getNom() + "'";

                    return statement.executeUpdate(requete);
                } else {
                    return 0;
                }
            }
            return 0;
        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
            return -1;
        }
    }

    /**
     * Ajoute l'hotêl crée dans la BDD
     *
     * @param handler
     * @return -1 si l'hôtel existe déjà, 0 modifiés si réussite, 2 si erreur
     * SQL,
     */
    public int Ajouter(C_BDD handler) {
        try {
            switch (this.VerifierExistenceHotel(handler)) {
                case -1:
                    return -1;
                case 0:
                    Statement statement = handler.getConnection().createStatement();
                    String requete = "INSERT INTO "
                            + "`t_hotel`(`nom`, `adresse`, `cp`, `ville`, `tel`) "
                            + "VALUES ('" + this.Nom + "','" + this.Adresse + "','"
                            + this.CP + "','" + this.Ville + "','" + this.NumTel + "')";
                    statement.executeUpdate(requete);
                    return 0;
                case 1:
                    return 1;
                default:
                    return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    /**
     * Supprime l'hôtel demandé de la base de données
     *
     * @param hotel Nom de l'hôtel
     * @param handler Handler MYSQL
     * @return 1 si l'hôtel a été supprimé, -1 si non
     */
    public int Supprimer(String hotel, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "DELETE FROM `hotel`.`t_hotel` WHERE `t_hotel`.`nom` = "
                    + "'" + hotel + "'";
            int resultat = statement.executeUpdate(requete);
            return resultat;
        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public int AjouterService(String service) {
        if (service.isEmpty()) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Vérifie si l'hôtel existe déjà dans la base de données
     *
     * @param Nom
     * @param Adresse
     * @param Ville
     * @param Cp
     * @param NumTel
     * @return 0 si il n'existe pas, 1 si il existe, -1 si une exception SQL est
     * levée
     */
    private int VerifierExistenceHotel(C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `nom`,`adresse`,`cp`,`ville`,`tel` "
                    + "FROM `t_hotel` "
                    + "WHERE `nom`='" + getNom() + "' OR `tel`='" + getNumTel() + "' "
                    + "OR `adresse`='" + getAdresse() + "'";
            ResultSet resultat = statement.executeQuery(requete);
            int doublon = 0;

            while (resultat.next()) {
                if ((getNom().equals(resultat.getString(1)))
                        || (getAdresse().equals(resultat.getString(4)))
                        || (getNumTel().equals(resultat.getString(5)))) {
                    doublon = 1;
                }
            }

            return doublon;

        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            errno = ex.getMessage();

            return -1;
        }
    }

    /* GETTERS ET SETTERS */
 /* GETTERS */
 /* =============================================================== */
    /**
     * Renvoie le nom de l'hôtel
     *
     * @param nomHotel
     * @param handler connexion a la bdd
     * @return Nom de l'hôtel si réussite, null si échoue
     */
    private String getNom(String nomHotel, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `nom` FROM `t_hotel` WHERE `nom`="
                    + "'" + nomHotel + "'";
            ResultSet resultat = statement.executeQuery(requete);
            resultat.first();

            return resultat.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Renvoie le nom de l'hôtel
     *
     * @return Nom
     */
    public String getNom() {
        return Nom;
    }

    /**
     * Retourne l'adresse de l'hotel
     *
     * @param nomHotel nom de l'hôtel
     * @param handler connexion à la BDD
     * @return
     */
    private String getAdresse(String nomHotel, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `adresse` FROM `t_hotel` WHERE `nom`="
                    + "'" + nomHotel + "'";
            ResultSet resultat = statement.executeQuery(requete);
            resultat.first();

            return resultat.getString(1);

        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * @return l'adresse de l'hôtel
     */
    public String getAdresse() {
        return Adresse;
    }

    /**
     * Retourne le numéro de téléphone : Version constructeur
     *
     * @param nomHotel nom de l'hôtel
     * @param handler connexion à la base de donnée
     * @return le numéro de téléphone si réussite, null si échoue
     */
    private String getNumTel(String nomHotel, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `tel` FROM `t_hotel` WHERE `nom`="
                    + "'" + nomHotel + "'";
            ResultSet resultat = statement.executeQuery(requete);
            resultat.first();

            return resultat.getString(1);

        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * @return le numéro de téléphone format "06.01.02.03.04"
     */
    public String getNumTel() {
        return NumTel;
    }

    /**
     * Renvoie le code postal : Version constructeur
     *
     * @param nomHotel nom de l'hôtel
     * @param handler connexion à la BDD
     * @return le code postal si réussite, null si échec
     */
    private String getCP(String nomHotel, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `cp` FROM `t_hotel` WHERE `nom`="
                    + "'" + nomHotel + "'";
            ResultSet resultat = statement.executeQuery(requete);
            resultat.first();

            return resultat.getString(1);

        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * @return code postal
     */
    public String getCP() {
        return CP;
    }

    /**
     * Retourne le nom de la ville
     *
     * @param nomHotel nom de l'hôtel
     * @param handler connexion à la BDD
     * @return le nom de la ville
     */
    private String getVille(String nomHotel, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `ville` FROM `t_hotel` WHERE `nom`="
                    + "'" + nomHotel + "'";
            ResultSet resultat = statement.executeQuery(requete);
            resultat.first();

            return resultat.getString(1);

        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * @return le nom de la ville d'où est situé l'hôtel
     */
    public String getVille() {
        return Ville;
    }

    /**
     * Renvoie l'id de l'hôtel
     *
     * @return Id - id de l'hôtel dans la base de données
     */
    public int getId() {
        return Id;
    }

    /**
     * Renvoie l'id de l'hôtel selon le nom donné
     *
     * @param nomHotel - Nom de l'hôtel
     * @param handler - Handler MySQL
     * @return ID de l'hôtel si succès / -1 si erreur
     */
    public int getId(String nomHotel, C_BDD handler) {
        try {
            Statement statement = handler.getConnection().createStatement();
            String requete = "SELECT `id_hotel` FROM `t_hotel` WHERE `nom`="
                    + "'" + nomHotel + "'";
            ResultSet resultat = statement.executeQuery(requete);
            resultat.first();

            return resultat.getInt(1);

        } catch (SQLException ex) {
            Logger.getLogger(C_Hotel.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    /**
     * Renvoie le message erreur de la DERNIERE exception levée
     *
     * @return errno
     */
    public String getErrno() {
        return errno;
    }

    /* SETTERS */
 /* =============================================================== */

 /* Codes d'erreur / Error code :
    0 : Succès
    -1 : Echec
     */
    /**
     * Change le nom de l'hôtel
     *
     * @param Nom
     * @return Error Code
     */
    public int setNom(String Nom) {
        if (Nom.length() == 0) {
            return -1;
        } else {
            this.Nom = Nom;
            return 0;
        }
    }

    /**
     * Change le nom de la ville Rajoutez la vérification de chiffres (pas de
     * chiffres dans les noms de villes)
     *
     * @param Ville
     * @return Error code
     */
    public int setVille(String Ville) {
        if (Ville.length() == 0) {
            return -1;
        } else {
            this.Ville = Ville;
            return 0;
        }
    }

    /**
     * Change le code postal de l'hôtel
     *
     * @param CP
     * @return Error code
     */
    public int setCP(String CP) {
        if (CP.length() == 5) {
            this.CP = CP;
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Change l'adresse de l'hôtel
     *
     * @param Adresse
     * @return Error code
     */
    public int setAdresse(String Adresse) {
        if (Adresse.length() == 0) {
            return -1;
        } else {
            this.Adresse = Adresse;
            return 0;
        }
    }

    /**
     * Change le numéro de téléphone sur lequel on peut joindre l'hôtel Format =
     * "06.01.02.03.04" (points tous les deux chiffres) Rajouter la vérification
     * : Si l'utilisateur rentre des lettres ou caractères spéciaux
     *
     * @param NumTel
     * @return
     */
    public int setNumTel(String NumTel) {
        if (NumTel.length() < 11) {
            return -1;
        } else {
            this.NumTel = NumTel;
            return 0;
        }
    }

}
