/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DAO;
import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.model.interfaces.Ruolo;
import com.mycompany.guidatv.data.proxy.RuoloProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 */
public class RuoloDAO_MySQL extends DAO implements RuoloDAO {
    
    private PreparedStatement getRuoloUtente, getRuolo, getRuoli;

    public RuoloDAO_MySQL(DataLayer dl) {
        super(dl);
    }
    
    @Override
    public void init() throws DataException {
        super.init();
        
        try {
            
            // INIT STATEMENTS
            getRuoloUtente = connection.prepareStatement("SELECT r.* FROM Ruolo R INNER JOIN Utente u ON r.id = u.id_ruolo WHERE u.id = ?");
            getRuolo = connection.prepareStatement("SELECT * FROM Ruolo WHERE id = ?");
            getRuoli = connection.prepareStatement("SELECT * FROM Ruolo");
            
        } catch (SQLException ex) {
            Logger.getLogger("Errore nell'inizializzazione del DAO Genere");
        }
    }
    
    public void destroy() {
        
        try {
            // CLOSE ALL STATEMENTS
            getRuoloUtente.close();
            getRuolo.close();
            getRuoli.close();
             
        } catch (SQLException ex) {
            Logger.getLogger(UtenteDAO_MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Override
    public Ruolo getRuoloUtente(int utente_key) throws DataException {
        Ruolo ruolo = null;
        
        //altrimenti lo carichiamo dal database
        try {
            getRuoloUtente.setInt(1, utente_key);
            try (ResultSet rs = getRuoloUtente.executeQuery()) {
                if (rs.next()) {
                    ruolo = createRuolo(rs);
                    //e lo mettiamo anche nella cache
                    dataLayer.getCache().add(Ruolo.class, ruolo);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load role by user", ex);
        }
        return ruolo;
    }

    @Override
    public RuoloProxy createRuolo() {
        return new RuoloProxy(this.dataLayer);
    }
    
    public Ruolo createRuolo(ResultSet rs) throws DataException {
        RuoloProxy r = createRuolo();
        try {
            r.setKey(rs.getInt("id"));
            r.setNome(rs.getString("nome"));
            r.setDescrizione(rs.getString("descrizione"));
        } catch(SQLException ex) {
            throw new DataException("Unable to create ruolo object form ResultSet", ex);
        }
        return r;
    }

    @Override
    public Ruolo getRuolo(int key) throws DataException {
        Ruolo ruolo = null;
        
        if (dataLayer.getCache().has(Ruolo.class, key)) {
            // Se l'oggett è in cache lo restituisco
            ruolo = dataLayer.getCache().get(Ruolo.class, key);
        } else {
            //altrimenti lo carichiamo dal database
            try {
                getRuolo.setInt(1, key);
                try (ResultSet rs = getRuolo.executeQuery()) {
                    if (rs.next()) {
                        ruolo = createRuolo(rs);
                        //e lo mettiamo anche nella cache
                        dataLayer.getCache().add(Ruolo.class, ruolo);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load role by ID", ex);
            }
        }
        return ruolo;
    }
    
    @Override
    public List<Ruolo> getRuoli() throws DataException {
        List<Ruolo> returnList = new ArrayList<>();
        
        try {
            try (ResultSet rs = getRuoli.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Ruolo) getRuolo(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable return ruolo list", ex);
        }
         
        
        return returnList;
    }

}
