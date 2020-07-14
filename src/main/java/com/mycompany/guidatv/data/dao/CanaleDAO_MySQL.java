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
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.proxy.CanaleProxy;
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
public class CanaleDAO_MySQL extends DAO implements CanaleDAO {
    
    private PreparedStatement getCanali, getCanaleByID, getCanaliPaginate, getNumeroCanali;
    private PreparedStatement iCanale, uCanale, dCanale;
    
    public CanaleDAO_MySQL(DataLayer dl) {
        super(dl);
    }
    
    @Override
    public void init() throws DataException {
        super.init();
        
        try {
            
            getCanali = connection.prepareStatement("SELECT * FROM Canale ORDER BY numero ASC");
            getCanaliPaginate = connection.prepareStatement("SELECT * FROM Canale ORDER BY numero ASC LIMIT ? OFFSET ?");
            getCanaleByID = connection.prepareStatement("SELECT * FROM Canale WHERE id = ?");
            getNumeroCanali = connection.prepareStatement("SELECT COUNT(*) AS num FROM Canale");
            iCanale = connection.prepareStatement("INSERT INTO Canale(numero, nome, logo_ref) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            uCanale = connection.prepareStatement("UPDATE Canale SET numero=?, nome=?, logo_ref=?, version=? WHERE ID = ? AND Version = ?");
            dCanale = connection.prepareStatement("DELETE FROM Canale WHERE id = ?");
            
        } catch (SQLException ex) {
            Logger.getLogger("Errore nell'inizializzazione del DAO Genere");
        }
    }
    
    public void destroy() {
        try {
            /**
             * CLOSE ALL STATEMENTS
             */ 
            getCanali.close();
            getCanaleByID.close();
            getCanaliPaginate.close();
            getNumeroCanali.close();
            iCanale.close();
            uCanale.close();
            dCanale.close();
        } catch (SQLException ex) {
            Logger.getLogger(UtenteDAO_MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public CanaleProxy createCanale() {
        return new CanaleProxy(getDataLayer());
    }
    
    public Canale createCanale(ResultSet rs) throws DataException {
        CanaleProxy c = createCanale();
        try {
            c.setKey(rs.getInt("id"));
            c.setVersion(rs.getInt("version"));
            c.setNome(rs.getString("nome"));
            c.setNumero(rs.getInt("numero"));
            c.setLogoRef(rs.getString("logo_ref"));
        } catch(SQLException ex) {
            throw new DataException("Unable to create canale object form ResultSet", ex);
        }
        return c;
    }

    @Override
    public Canale getCanale(int key) throws DataException {
        Canale canale = null;
        
        if (dataLayer.getCache().has(Canale.class, key)) {
            // Se l'oggett è in cache lo restituisco
            canale = dataLayer.getCache().get(Canale.class, key);
        } else {
            //altrimenti lo carichiamo dal database
            try {
                getCanaleByID.setInt(1, key);
                try (ResultSet rs = getCanaleByID.executeQuery()) {
                    if (rs.next()) {
                        canale = createCanale(rs);
                        //e lo mettiamo anche nella cache
                        dataLayer.getCache().add(Canale.class, canale);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load canale by ID", ex);
            }
        }
        return canale;
    }

    @Override
    public List<Canale> getListaCanali() throws DataException {
        List<Canale> returnList = new ArrayList<>();
        
        try {
            try (ResultSet rs = getCanali.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Canale) getCanale(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco canali", ex);
        }
         
        
        return returnList;
    }

    @Override
    public List<Canale> getListaCanali(int page, int elements) throws DataException {
        List<Canale> returnList = new ArrayList<>();
        
        try {
            getCanaliPaginate.setInt(1, elements);
            getCanaliPaginate.setInt(2, page * elements);
            try (ResultSet rs = getCanaliPaginate.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Canale) getCanale(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco canali paginato", ex);
        }
         
        
        return returnList;
    }

    @Override
    public int getNumeroCanali() throws DataException {
        int result = 0;
        
        try {
            try (ResultSet rs = getNumeroCanali.executeQuery()) {
            
                while(rs.next()) {
                    result = rs.getInt("num");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get numero canali", ex);
        }
        
        return result;
    }

    @Override
    public List<Canale> getListaCanaliPaginated(int first_index, int elements) throws DataException {
        List<Canale> returnList = new ArrayList<>();
        
        try {
            getCanaliPaginate.setInt(1, elements);
            getCanaliPaginate.setInt(2, first_index);
            try (ResultSet rs = getCanaliPaginate.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Canale) getCanale(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco canali paginato", ex);
        }
         
        
        return returnList;
    }

    @Override
    public void storeCanale(Canale canale) throws DataException {
        try {
            if (canale.getKey() != null && canale.getKey() > 0) { //update
                // Se proxy non modificato non facciamo nulla
                if (canale instanceof DataItemProxy && !((DataItemProxy) canale).isModified()) {
                    return;
                }
                
                // Altrimenti
                uCanale.setInt(1, canale.getNumero());
                uCanale.setString(2, canale.getNome());
                if (canale.getLogoRef() != null && !canale.getLogoRef().isBlank()) {
                    uCanale.setString(3, canale.getLogoRef());
                } else {
                    uCanale.setNull(3, java.sql.Types.INTEGER);
                }
                
                long current_version = canale.getVersion();
                long next_version = current_version + 1;
                

                uCanale.setLong(4, next_version);
                uCanale.setInt(5, canale.getKey());
                uCanale.setLong(6, current_version);

                if (uCanale.executeUpdate() == 0) {
                    throw new OptimisticLockException(canale);
                }
                canale.setVersion(next_version);
            } else { //insert
                iCanale.setInt(1, canale.getNumero());
                iCanale.setString(2, canale.getNome());
                if (canale.getLogoRef() != null && !canale.getLogoRef().isBlank()) {
                    iCanale.setString(3, canale.getLogoRef());
                } else {
                    iCanale.setNull(3, java.sql.Types.INTEGER);
                }
                
                if (iCanale.executeUpdate() == 1) {
                    //getGeneratedKeys per leggere chiave generata
                    try (ResultSet keys = iCanale.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            canale.setKey(key);
                            dataLayer.getCache().add(Canale.class, canale);
                        }
                    }
                }
            }

            //se abbiamo un proxy, resettiamo il suo attributo dirty
            if (canale instanceof DataItemProxy) {
                ((DataItemProxy) canale).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store canale", ex);
        }
    }

    @Override
    public void deleteCanale(int key) throws DataException {
        try {
            dCanale.setInt(1, key);
            int rows = dCanale.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete canale by ID", ex);
        }
    }
    
}
