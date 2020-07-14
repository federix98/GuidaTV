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
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.data.proxy.ProgrammaProxy;
import com.mycompany.guidatv.utility.UtilityMethods;
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
public class ProgrammaDAO_MySQL extends DAO implements ProgrammaDAO {

        
    private PreparedStatement countPrograms;
    private PreparedStatement getProgrammi, getProgrammiPaginated, getProgrammiDistinctSerie;
    private PreparedStatement getProgrammaByID, getIDSerie, getRelated;
    private PreparedStatement getProgrammiByNome, getProgrammiByGenere, getProgrammiByNomeGenere;
    private PreparedStatement iProgramma, uProgramma, dProgramma;
    
    public ProgrammaDAO_MySQL(DataLayer dl) {
        super(dl);
    }
    
    @Override
    public void init() throws DataException {
        super.init();
        
        try {
            
            // PREPARE STATEMENTS
            countPrograms = connection.prepareStatement("SELECT COUNT(*) AS NumeroProgrammi FROM Programma");
            getProgrammi = connection.prepareStatement("SELECT * FROM programma");
            getProgrammaByID = connection.prepareStatement("SELECT * FROM programma WHERE id = ?");
            getProgrammiPaginated = connection.prepareStatement("SELECT * FROM programma LIMIT ? OFFSET ?");
            getProgrammiByNome = connection.prepareStatement("SELECT * FROM programma WHERE nome COLLATE UTF8_GENERAL_CI LIKE ?");
            getProgrammiByGenere = connection.prepareStatement("SELECT * FROM programma WHERE id_genere = ?");
            getIDSerie = connection.prepareStatement("SELECT * FROM programma WHERE id = id_serie");
            getProgrammiByNomeGenere = connection.prepareStatement("SELECT * FROM programma WHERE nome COLLATE UTF8_GENERAL_CI LIKE ? AND id_genere = ?");
            iProgramma = connection.prepareStatement("INSERT INTO programma(nome, descrizione, link_ref_img, link_ref_details, id_serie, stagione, episodio, id_classificazione, id_genere, durata) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            uProgramma = connection.prepareStatement("UPDATE programma SET nome=?, descrizione=?, link_ref_img=?, link_ref_details=?, id_serie=?, stagione=?, episodio=?, id_classificazione=?, id_genere=?, durata=?, version=? WHERE id=? AND version=?");
            dProgramma = connection.prepareStatement("DELETE FROM Programma WHERE id = ?");
            getProgrammiDistinctSerie = connection.prepareStatement("SELECT * FROM programma WHERE id_serie IS NULL OR id_serie = id");
            getRelated = connection.prepareStatement("SELECT * FROM Programma WHERE id <> ? AND id_genere = ? LIMIT 4");
            
        } catch (SQLException ex) {
            Logger.getLogger("Errore nell'inizializzazione del DAO Programma");
        }
    }
    
    public void destroy() throws DataException {
        try {
            /**
             * CLOSE ALL STATEMENTS
             */ 
            countPrograms.close();
            getProgrammi.close();
            getProgrammaByID.close();
            getProgrammiPaginated.close();
            getIDSerie.close();
            getProgrammiDistinctSerie.close();
            getRelated.close();
            iProgramma.close();
            uProgramma.close();
            dProgramma.close();
        } catch (SQLException ex) {
            Logger.getLogger(ProgrammazioneDAO_MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Integer getNumeroProgrammi() throws DataException {
        /**
         * Implementare
         */
        Integer returnValue = -1;
        
        try {
            try (ResultSet rs = countPrograms.executeQuery()) {
            
                if(rs.next()) {
                    returnValue = rs.getInt("NumeroProgrammi");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable count programs", ex);
        }
         
        
        return returnValue;
    }

    @Override
    public List<Programma> getProgrammi() throws DataException {
        
        List<Programma> returnList = new ArrayList<>();
        
        try {
            try (ResultSet rs = getProgrammi.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Programma) getProgramma(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable return program list", ex);
        }
         
        
        return returnList;
        
    }
    
    /**
     * HELPER PER CREARE PROGRAMMA DA RESULTSET 
     * 
     * @param rs
     * @return
     * @throws DataException 
     */
    public Programma createProgramma(ResultSet rs) throws DataException {
        ProgrammaProxy p = createProgramma();
        try {
            p.setKey(rs.getInt("id"));
            p.setNome(rs.getString("nome"));
            p.setDescrizione(rs.getString("descrizione"));
            p.setLinkRefImg(rs.getString("link_ref_img"));
            p.setLinkRefDetails(rs.getString("link_ref_details"));
            p.setIdSerie(rs.getInt("id_serie"));
            p.setDurata(rs.getInt("durata"));
            p.setVersion(rs.getInt("version"));
            if(p.getIdSerie() != null) {
                p.setStagione(rs.getInt("stagione"));
                p.setEpisodio(rs.getInt("episodio"));
            }
            p.setClassificazione_key(rs.getInt("id_classificazione"));
            p.setGenere_key(rs.getInt("id_genere"));
        } catch(SQLException ex) {
            throw new DataException("Unable to create programma object form ResultSet", ex);
        }
        
        return p;
    }

    @Override
    public ProgrammaProxy createProgramma() {
        return new ProgrammaProxy(getDataLayer());
    }

    @Override
    public Programma getProgramma(int key) throws DataException {
        Programma prog = null;
        //prima vediamo se l'oggetto è già stato caricato
        if (dataLayer.getCache().has(Programma.class, key)) {
            prog = dataLayer.getCache().get(Programma.class, key);
        } else {
            //altrimenti lo carichiamo dal database
            try {
                getProgrammaByID.setInt(1, key);
                try (ResultSet rs = getProgrammaByID.executeQuery()) {
                    if (rs.next()) {
                        prog = createProgramma(rs);
                        //e lo mettiamo anche nella cache
                        //and put it also in the cache
                        dataLayer.getCache().add(Programma.class, prog);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load programma by ID", ex);
            }
        }
        return prog;
    }

    /**
     * Effettua la ricerca dei programmi
     * 
     * @param nome
     * @param genere_key
     * @return
     * @throws DataException 
     */
    @Override
    public List<Programma> cercaProgrammi(String nome, int genere_key) throws DataException {
        List<Programma> returnList = new ArrayList<>();
        
        if(nome == null && genere_key > 0) {
            
            try {
                getProgrammiByGenere.setInt(1, genere_key);
                try (ResultSet rs = getProgrammiByGenere.executeQuery()) {

                    while(rs.next()) {
                        returnList.add((Programma) getProgramma(rs.getInt("id")));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable return program list", ex);
            }
            
        }
        else if(nome != null && genere_key == 0 ) {
            
            try {
                //UtilityMethods.debugConsole(this.getClass(), "cerca", getProgrammiByNome.toString());
                getProgrammiByNome.setString(1, "%" + nome + "%");
                //UtilityMethods.debugConsole(this.getClass(), "cerca", getProgrammiByNome.toString());
                try (ResultSet rs = getProgrammiByNome.executeQuery()) {

                    while(rs.next()) {
                        returnList.add((Programma) getProgramma(rs.getInt("id")));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable return program list", ex);
            }
            
        } else if(nome != null && genere_key > 0 ) {
            
            try {
                getProgrammiByNomeGenere.setString(1, "%" + nome + "%");
                getProgrammiByNomeGenere.setInt(2, genere_key);
                try (ResultSet rs = getProgrammiByNomeGenere.executeQuery()) {
                    while(rs.next()) {
                        returnList.add((Programma) getProgramma(rs.getInt("id")));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable return program list", ex);
            }
            
        } else return getProgrammi();
         
        
        return returnList;
    }

    @Override
    public List<Programma> getProgrammiPaginated(int start_item, int elements) throws DataException {
        List<Programma> returnList = new ArrayList<>();
        
        try {
            getProgrammiPaginated.setInt(1, elements);
            getProgrammiPaginated.setInt(2, start_item);
            try (ResultSet rs = getProgrammiPaginated.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Programma) getProgramma(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco programmi paginato", ex);
        }
         
        
        return returnList;
    }

    @Override
    public List<Programma> getIdSerie() throws DataException {
        List<Programma> returnList = new ArrayList<>();
        
        try {
            try (ResultSet rs = getIDSerie.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Programma) getProgramma(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco Serie TV", ex);
        }
         
        return returnList;
    }

    @Override
    public void storeProgramma(Programma programma) throws DataException {
        try {
            if (programma.getKey() != null && programma.getKey() > 0) { //update
                // Se proxy non modificato non facciamo nulla
                if (programma instanceof DataItemProxy && !((DataItemProxy) programma).isModified()) {
                    return;
                }
                
                // Altrimenti
                uProgramma.setString(1, programma.getNome());
                uProgramma.setString(2, programma.getDescrizione());
                if (programma.getLinkRefImg() != null && !programma.getLinkRefImg().isBlank()) {
                    uProgramma.setString(3, programma.getLinkRefImg());
                } else {
                    uProgramma.setNull(3, java.sql.Types.INTEGER);
                }
                uProgramma.setString(4, programma.getLinkRefDetails());
                if (programma.getIdSerie() != null) {
                    uProgramma.setInt(5, programma.getIdSerie());
                } else {
                    uProgramma.setNull(5, java.sql.Types.INTEGER);
                }
                if (programma.getStagione() != null) {
                    uProgramma.setInt(6, programma.getStagione());
                } else {
                    uProgramma.setNull(6, java.sql.Types.INTEGER);
                }
                if (programma.getEpisodio() != null) {
                    uProgramma.setInt(7, programma.getEpisodio());
                } else {
                    uProgramma.setNull(7, java.sql.Types.INTEGER);
                }
                if (programma.getClassificazione() != null) {
                    uProgramma.setInt(8, programma.getClassificazione().getKey());
                } else {
                    uProgramma.setNull(8, java.sql.Types.INTEGER);
                }
                if (programma.getGenere() != null) {
                    uProgramma.setInt(9, programma.getGenere().getKey());
                } else {
                    uProgramma.setNull(9, java.sql.Types.INTEGER);
                }
                uProgramma.setInt(10, programma.getDurata());
                
                long current_version = programma.getVersion();
                long next_version = current_version + 1;
                

                uProgramma.setLong(11, next_version);
                uProgramma.setInt(12, programma.getKey());
                uProgramma.setLong(13, current_version);

                if (uProgramma.executeUpdate() == 0) {
                    throw new OptimisticLockException(programma);
                }
                programma.setVersion(next_version);
            } else { //insert
                iProgramma.setString(1, programma.getNome());
                iProgramma.setString(2, programma.getDescrizione());
                if (programma.getLinkRefImg() != null && !programma.getLinkRefImg().isBlank()) {
                    iProgramma.setString(3, programma.getLinkRefImg());
                } else {
                    iProgramma.setNull(3, java.sql.Types.INTEGER);
                }
                iProgramma.setString(4, programma.getLinkRefDetails());
                if (programma.getIdSerie() != null) {
                    iProgramma.setInt(5, programma.getIdSerie());
                } else {
                    iProgramma.setNull(5, java.sql.Types.INTEGER);
                }
                if (programma.getStagione() != null) {
                    iProgramma.setInt(6, programma.getStagione());
                } else {
                    iProgramma.setNull(6, java.sql.Types.INTEGER);
                }
                if (programma.getEpisodio() != null) {
                    iProgramma.setInt(7, programma.getEpisodio());
                } else {
                    iProgramma.setNull(7, java.sql.Types.INTEGER);
                }
                if (programma.getClassificazione() != null) {
                    iProgramma.setInt(8, programma.getClassificazione().getKey());
                } else {
                    iProgramma.setNull(8, java.sql.Types.INTEGER);
                }
                if (programma.getGenere() != null) {
                    iProgramma.setInt(9, programma.getGenere().getKey());
                } else {
                    iProgramma.setNull(9, java.sql.Types.INTEGER);
                }
                iProgramma.setInt(10, programma.getDurata());
                
                if (iProgramma.executeUpdate() == 1) {
                    //getGeneratedKeys per leggere chiave generata
                    try (ResultSet keys = iProgramma.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            programma.setKey(key);
                            dataLayer.getCache().add(Programma.class, programma);
                        }
                    }
                }
            }

            //se abbiamo un proxy, resettiamo il suo attributo dirty
            if (programma instanceof DataItemProxy) {
                ((DataItemProxy) programma).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store programma", ex);
        }
    }

    @Override
    public void deleteProgramma(int key) throws DataException {
        try {
            dProgramma.setInt(1, key);
            int rows = dProgramma.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete programma by ID", ex);
        }
    }

    @Override
    public List<Programma> getProgrammiDistinctSerie() throws DataException {
        List<Programma> returnList = new ArrayList<>();
        
        try {
            try (ResultSet rs = getProgrammiDistinctSerie.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Programma) getProgramma(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable return program list", ex);
        }
         
        
        return returnList;
    }

    @Override
    public List<Programma> getRelatedPrograms(Programma prgrm) throws DataException {
        List<Programma> returnList = new ArrayList<>();
        
        try {
            getRelated.setInt(1, prgrm.getKey());
            getRelated.setInt(2, prgrm.getGenere().getKey());
            try (ResultSet rs = getRelated.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Programma) getProgramma(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get related programs", ex);
        }
         
        
        return returnList;
    }
    
}
