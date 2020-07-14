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
import com.mycompany.guidatv.data.OptimisticLockException;
import com.mycompany.guidatv.data.model.interfaces.Classificazione;
import com.mycompany.guidatv.data.proxy.ClassificazioneProxy;
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
public class ClassificazioneDAO_MySQL extends DAO implements ClassificazioneDAO {

    private PreparedStatement getClassificazioneByID, getNumeroClassificazioni, getClassificazioniPaginated, getAllClassificazioni;
    private PreparedStatement iClassificazione, uClassificazione, dClassificazione;
    
    public ClassificazioneDAO_MySQL(DataLayer dl) {
        super(dl);
    }
    
    public void init() throws DataException {
        
        super.init();
        
        try {
            
            getClassificazioneByID = connection.prepareStatement("SELECT * FROM Classificazione WHERE id = ?");
            getNumeroClassificazioni = connection.prepareStatement("SELECT COUNT(*) AS num FROM Classificazione");
            getClassificazioniPaginated = connection.prepareStatement("SELECT * FROM Classificazione LIMIT ? OFFSET ?");
            getAllClassificazioni = connection.prepareStatement("SELECT * FROM Classificazione");
            iClassificazione = connection.prepareStatement("INSERT INTO classificazione(nome, descrizione, min_age) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            uClassificazione = connection.prepareStatement("UPDATE classificazione SET nome=?, descrizione=?, min_age=?, version=? WHERE id = ? AND version = ?");
            dClassificazione = connection.prepareStatement("DELETE FROM classificazione WHERE id = ?");
            
            
        } catch (SQLException ex) {
            Logger.getLogger("Errore nell'inizializzazione del DAO Classificazione");
        }
        
    }
    
    public void destroy() {
        
        try {
            /**
             * CLOSE ALL STATEMENTS
             */ 
            getClassificazioneByID.close();
            getNumeroClassificazioni.close();
            getClassificazioniPaginated.close();
            getAllClassificazioni.close();
            iClassificazione.close();
            uClassificazione.close();
            dClassificazione.close();
        } catch (SQLException ex) {
            Logger.getLogger(UtenteDAO_MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public Classificazione getClassificazione(int key) throws DataException {
        Classificazione classificazione = null;
        
        if (dataLayer.getCache().has(Classificazione.class, key)) {
            // Se l'oggett è in cache lo restituisco
            classificazione = dataLayer.getCache().get(Classificazione.class, key);
        } else {
            //altrimenti lo carichiamo dal database
            try {
                getClassificazioneByID.setInt(1, key);
                try (ResultSet rs = getClassificazioneByID.executeQuery()) {
                    if (rs.next()) {
                        classificazione = createClassificazione(rs);
                        //e lo mettiamo anche nella cache
                        dataLayer.getCache().add(Classificazione.class, classificazione);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load classificazione by ID", ex);
            }
        }
        return classificazione;
    }
    
    public Classificazione createClassificazione(ResultSet rs) throws DataException {
        ClassificazioneProxy cl = createClassificazione();
        try {
            cl.setKey(rs.getInt("id"));
            cl.setNome(rs.getString("nome"));
            cl.setMinAge(rs.getInt("min_age"));
            cl.setVersion(rs.getInt("version"));
            cl.setDescrizione(rs.getString("descrizione"));
        } catch(SQLException ex) {
            throw new DataException("Unable to create Classificazione object form ResultSet", ex);
        }
        return cl;
    }
    
    public ClassificazioneProxy createClassificazione() {
        return new ClassificazioneProxy(getDataLayer());
    }

    @Override
    public int getNumeroClassificazioni() throws DataException {
        int result = 0;
        
        try {
            try (ResultSet rs = getNumeroClassificazioni.executeQuery()) {
            
                while(rs.next()) {
                    result = rs.getInt("num");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get numero classificazioni", ex);
        }
        
        return result;
    }

    @Override
    public List<Classificazione> getClassificazioniPaginated(int start_item, int elements) throws DataException {
        List<Classificazione> returnList = new ArrayList<>();
        
        try {
            getClassificazioniPaginated.setInt(1, elements);
            getClassificazioniPaginated.setInt(2, start_item);
            try (ResultSet rs = getClassificazioniPaginated.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Classificazione) getClassificazione(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco classificazioni paginato", ex);
        }
        
        return returnList;
         
    }

    @Override
    public List<Classificazione> getAllClassificazioni() throws DataException {
        List<Classificazione> returnList = new ArrayList<>();
        
        try {
            try (ResultSet rs = getAllClassificazioni.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Classificazione) getClassificazione(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco classificazioni", ex);
        }
        
        return returnList;
    }

    @Override
    public void storeClassificazione(Classificazione classificazione) throws DataException {
        try {
            if (classificazione.getKey() != null && classificazione.getKey() > 0) { //update
                // Se proxy non modificato non facciamo nulla
                if (classificazione instanceof DataItemProxy && !((DataItemProxy) classificazione).isModified()) {
                    return;
                }
                
                // Altrimenti
                uClassificazione.setString(1, classificazione.getNome());
                uClassificazione.setString(2, classificazione.getDescrizione());
                uClassificazione.setInt(3, classificazione.getMinAge());
                
                long current_version = classificazione.getVersion();
                long next_version = current_version + 1;
                
                uClassificazione.setLong(4, next_version);
                uClassificazione.setInt(5, classificazione.getKey());
                uClassificazione.setLong(6, current_version);

                if (uClassificazione.executeUpdate() == 0) {
                    throw new OptimisticLockException(classificazione);
                }
                classificazione.setVersion(next_version);
            } else { //insert
                iClassificazione.setString(1, classificazione.getNome());
                iClassificazione.setString(2, classificazione.getDescrizione());
                iClassificazione.setInt(3, classificazione.getMinAge());
                
                if (iClassificazione.executeUpdate() == 1) {
                    //getGeneratedKeys per leggere chiave generata
                    try (ResultSet keys = iClassificazione.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            classificazione.setKey(key);
                            dataLayer.getCache().add(Classificazione.class, classificazione);
                        }
                    }
                }
            }

            //se abbiamo un proxy, resettiamo il suo attributo dirty
            if (classificazione instanceof DataItemProxy) {
                ((DataItemProxy) classificazione).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store classificazione", ex);
        }
    }

    @Override
    public void deleteClassificazione(int key) throws DataException {
        try {
            dClassificazione.setInt(1, key);
            int rows = dClassificazione.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete classificazione by ID", ex);
        }
    }
    
}
