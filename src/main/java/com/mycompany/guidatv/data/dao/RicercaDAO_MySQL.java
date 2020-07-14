/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DAO;
import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.data.proxy.RicercaProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 */
public class RicercaDAO_MySQL extends DAO implements RicercaDAO {
    
    PreparedStatement insertRicerca, deleteRicerca, getRicercaByID, getRicercheUtente;

    public RicercaDAO_MySQL(DataLayer dl) {
        super(dl);
    }
    
    /**
     * Inizializza i PreparedStatements
     * @throws DataException 
     */
    @Override
    public void init() throws DataException {
        super.init();
        
        try {
            
            // PREPARE STATEMENTS
            getRicercaByID = connection.prepareStatement("SELECT * FROM ricerca_salvata WHERE id = ?");
            insertRicerca = connection.prepareStatement("INSERT INTO ricerca_salvata (query_string, id_utente) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
            deleteRicerca = connection.prepareStatement("DELETE from ricerca_salvata WHERE id = ?");
            getRicercheUtente = connection.prepareStatement("SELECT id FROM ricerca_salvata WHERE id_utente = ?");
        } catch (SQLException ex) {
            Logger.getLogger("Errore nell'inizializzazione del DAO Programmazione");
        }
    }

    /**
     * Chiude tutti i PreparedStatements
     * @throws DataException 
     */
    public void destroy() throws DataException {
        try {
            /**
             * CLOSE ALL STATEMENTS
             */ 
            insertRicerca.close();
            getRicercaByID.close();
            deleteRicerca.close();
            getRicercheUtente.close();
        } catch (SQLException ex) {
            Logger.getLogger(RicercaDAO_MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private RicercaProxy createRicerca(ResultSet rs) throws DataException {
        RicercaProxy ricerca = createRicerca();
        try {
            ricerca.setKey(rs.getInt("id"));
            ricerca.setQueryString(rs.getString("query_string"));
        } catch(SQLException ex) {
            throw new DataException("Unable to create ricerca object form ResultSet", ex);
        }
        return ricerca;
    }
    
    @Override
    public RicercaProxy createRicerca() {
        return new RicercaProxy(getDataLayer());
    }
    
    @Override
    public Ricerca getRicerca(int id_ricerca) throws DataException {
        Ricerca ricerca = null;
        
        if (dataLayer.getCache().has(Ricerca.class, id_ricerca)) {
            ricerca = dataLayer.getCache().get(Ricerca.class, id_ricerca);
        } else {
            try {
                getRicercaByID.setInt(1, id_ricerca);
                try (ResultSet rs = getRicercaByID.executeQuery()) {
                    if (rs.next()) {
                        ricerca = createRicerca(rs);
                        dataLayer.getCache().add(Ricerca.class, ricerca);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load ricerca by ID", ex);
            }
        }
        return ricerca;
    }

    @Override
    public List<Ricerca> getRicercheUtente(Utente utente) throws DataException {
        List<Ricerca> ricerche = null;
        
        
        try {
            getRicercheUtente.setInt(1, utente.getKey());
            
            try (ResultSet rs = getRicercheUtente.executeQuery()) {
                ricerche = new ArrayList<>();
                
                while (rs.next()) {
                    ricerche.add(getRicerca(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load ricerca by Utente", ex);
        }
        
        return ricerche;
    }

    @Override
    public void storeRicerca(Ricerca ricerca, int id_utente) throws DataException{
        try {
            if (ricerca.getKey() != null && ricerca.getKey() > 0) { 
                // UPDATE NOT IMPLEMENTED - Non necessario
            } else { 
                // INSERT RICERCA
                insertRicerca.setString(1, ricerca.getQueryString());
                insertRicerca.setInt(2, id_utente);
                
                if (insertRicerca.executeUpdate() == 1) {
                    //getGeneratedKeys per leggere chiave generata
                    try (ResultSet keys = insertRicerca.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            ricerca.setKey(key);
                            dataLayer.getCache().add(Ricerca.class, ricerca);
                        }
                    }
                }
            }

            if (ricerca instanceof DataItemProxy) {
                ((DataItemProxy) ricerca).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store ricerca", ex);
        }
    }

    @Override
    public boolean removeRicerca(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        // RIMUOVERLO DALLA CACHE ANCHE
    }
    
}
