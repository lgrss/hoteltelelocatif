package com.hotelApp;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Classe BDD qui permet d'effectuer les opérations de connexion à la base de
 * données
 * @author Karl La Grassa
 */
public class C_BDD {
    /* Attributs */
    private     String url;
    private     String user;
    private     String password;
    //private     String BDDname;
    private     Connection connection;
    
    /* Variable qui récupère les exceptions, se met a jour a chaque
    exception lancée */
    private     String errno;
    
    /* Méthodes */
    public C_BDD(String newUrl, String newUser, String newPassword)
    {
        url = newUrl;
        user = newUser;
        password = newPassword;
    }
    
    /**
     * Méthode de connexion a la base de donnée
     * @return 0 en cas de succès
     *         -1 en cas d'échec
     */
    public int Connecter()
    {
        try {
            connection = DriverManager.getConnection(url, user, password);
            return 0;
            
        } catch (SQLException ex) {
            Logger.getLogger(C_BDD.class.getName()).log(Level.SEVERE, null, ex);
            errno = ex.getMessage();
            return -1;
        }
    }
    
    /// GETTERS ET SETTERS    
    
    /**
     * 
     * @return errno
     */
    public String getErrno()
    {
        return errno;
    }
    
    /**
     * Permet de modifier (pour mettre à jour) la variable d'erreur a chaque
     * fois qu'une exception est levée.
     * @param message 
     */
    public void setErrno(String message)
    {
        errno = message;
    }
    
    /**
     * Renvoie le descripteur de la connection MySQL
     * @return connection
     */
    public Connection getConnection()
    {
        return connection;
    }
    
    
}
