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
import com.mycompany.guidatv.data.model.impl.GenereImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Genere;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.proxy.GenereProxy;
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
public class GenereDAO_MySQL extends DAO implements GenereDAO {

    private PreparedStatement getGenereByID, getGeneri;
    private PreparedStatement getNumeroGeneri, getGeneriPaginated;
    private PreparedStatement iGenere, uGenere, dGenere;
    
    public GenereDAO_MySQL(DataLayer dl) {
        super(dl);
    }
    
    @Override
    public void init() throws DataException {
        super.init();
        
        try {
            
            getGenereByID = connection.prepareStatement("SELECT * FROM Genere WHERE id = ?");
            getGeneri = connection.prepareStatement("SELECT * FROM Genere ORDER BY nome");
            getNumeroGeneri = connection.prepareStatement("SELECT COUNT(*) AS num FROM Genere");
            getGeneriPaginated = connection.prepareStatement("SELECT * FROM Genere LIMIT ? OFFSET ?");
            iGenere = connection.prepareStatement("INSERT INTO Genere(nome, descrizione) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            uGenere = connection.prepareStatement("UPDATE genere SET nome=?, descrizione=?, version=? WHERE id = ? AND version = ?");
            dGenere = connection.prepareStatement("DELETE FROM genere WHERE id = ?");
            
        } catch (SQLException ex) {
            Logger.getLogger("Errore nell'inizializzazione del DAO Genere");
        }
    }
    
    public void destroy() {
        
        try {
            /**
             * CLOSE ALL STATEMENTS
             */ 
            getGenereByID.close();
            getGeneri.close();
            getNumeroGeneri.close();
            getGeneriPaginated.close();
            iGenere.close();
            uGenere.close();
            dGenere.close();
        } catch (SQLException ex) {
            Logger.getLogger(UtenteDAO_MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public Genere getGenere(int key) throws DataException {
        
        Genere genere = null;
        
        if (dataLayer.getCache().has(Genere.class, key)) {
            // Se l'oggett è in cache lo restituisco
            genere = dataLayer.getCache().get(Genere.class, key);
        } else {
            //altrimenti lo carichiamo dal database
            try {
                getGenereByID.setInt(1, key);
                try (ResultSet rs = getGenereByID.executeQuery()) {
                    if (rs.next()) {
                        genere = createGenere(rs);
                        //e lo mettiamo anche nella cache
                        dataLayer.getCache().add(Genere.class, genere);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load genere by ID", ex);
            }
        }
        return genere;
    }
    
    public Genere createGenere(ResultSet rs) throws DataException {
        GenereProxy g = createGenere();
        try {
            g.setKey(rs.getInt("id"));
            g.setNome(rs.getString("nome"));
            g.setVersion(rs.getInt("version"));
            g.setDescrizione(rs.getString("descrizione"));
        } catch(SQLException ex) {
            throw new DataException("Unable to create genere object form ResultSet", ex);
        }
        return g;
    }
    
    @Override
    public GenereProxy createGenere() {
        return new GenereProxy(getDataLayer());
    }

    @Override
    public List<Genere> getGeneri() throws DataException {
        List<Genere> returnList = new ArrayList<>();
        
        try {
            try (ResultSet rs = getGeneri.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Genere) getGenere(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable return genre list", ex);
        }
         
        
        return returnList;
    }

    @Override
    public int getNumeroGeneri() throws DataException {
        int result = 0;
        
        try {
            try (ResultSet rs = getNumeroGeneri.executeQuery()) {
            
                while(rs.next()) {
                    result = rs.getInt("num");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get numero generi", ex);
        }
        
        return result;
    }

    @Override
    public List<Genere> getGeneriPaginated(int start_item, int elements) throws DataException {
        List<Genere> returnList = new ArrayList<>();
        
        try {
            getGeneriPaginated.setInt(1, elements);
            getGeneriPaginated.setInt(2, start_item);
            try (ResultSet rs = getGeneriPaginated.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Genere) getGenere(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco generi paginato", ex);
        }
         
        
        return returnList;
    }

    @Override
    public void storeGenere(Genere genere) throws DataException {
        try {
            if (genere.getKey() != null && genere.getKey() > 0) { //update
                // Se proxy non modificato non facciamo nulla
                if (genere instanceof DataItemProxy && !((DataItemProxy) genere).isModified()) {
                    return;
                }
                
                // Altrimenti
                uGenere.setString(1, genere.getNome());
                uGenere.setString(2, genere.getDescrizione());
                
                long current_version = genere.getVersion();
                long next_version = current_version + 1;
                
                uGenere.setLong(3, next_version);
                uGenere.setInt(4, genere.getKey());
                uGenere.setLong(5, current_version);

                if (uGenere.executeUpdate() == 0) {
                    throw new OptimisticLockException(genere);
                }
                genere.setVersion(next_version);
            } else { //insert
                iGenere.setString(1, genere.getNome());
                iGenere.setString(2, genere.getDescrizione());
                
                if (iGenere.executeUpdate() == 1) {
                    //getGeneratedKeys per leggere chiave generata
                    try (ResultSet keys = iGenere.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            genere.setKey(key);
                            dataLayer.getCache().add(Genere.class, genere);
                        }
                    }
                }
            }

            //se abbiamo un proxy, resettiamo il suo attributo dirty
            if (genere instanceof DataItemProxy) {
                ((DataItemProxy) genere).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store genere", ex);
        }
    }

    @Override
    public void deleteGenere(int key) throws DataException {
        try {
            dGenere.setInt(1, key);
            int rows = dGenere.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete genere by ID", ex);
        }
    }
    
    
    
}
