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
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.data.proxy.ProgrammazioneProxy;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 */
public class ProgrammazioneDAO_MySQL extends DAO implements ProgrammazioneDAO {

    PreparedStatement getProgrammazioneByTimestamp, getProgrammazioneByID, getCurrentProgrammazioneCanale, getProgrammazioneCorrente, getProgrammazioneByTimestampCanale;
    PreparedStatement getProgrammazioneSpecifica, getProgrammazioneSerie, getProgrammazioniPaginated;
    PreparedStatement getNumeroProgrammazioni;
    PreparedStatement iProgrammazione, uProgrammazione, dProgrammazione;
    /**
     * Costruttore: setta il datalayer
     * @param dl 
     */
    public ProgrammazioneDAO_MySQL(DataLayer dl) {
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
            getProgrammazioneByTimestamp = connection.prepareStatement("SELECT * FROM Programmazione WHERE start_time BETWEEN ? AND ?");
            getProgrammazioneByTimestampCanale = connection.prepareStatement("SELECT * FROM Programmazione WHERE id_canale = ? AND start_time BETWEEN ? AND ?");
            getProgrammazioneByID = connection.prepareStatement("SELECT * FROM Programmazione WHERE id = ?");
            getCurrentProgrammazioneCanale = connection.prepareStatement("SELECT * FROM Programmazione WHERE id_canale = ? AND start_time < NOW() AND ( UNIX_TIMESTAMP(start_time) + durata * 60) > UNIX_TIMESTAMP(NOW()) ORDER BY id_canale ASC");
            getProgrammazioneCorrente = connection.prepareStatement("SELECT * FROM Programmazione WHERE start_time <= NOW() AND ( UNIX_TIMESTAMP(start_time) + durata * 60) > UNIX_TIMESTAMP(NOW()) ORDER BY id_canale ASC");
            getProgrammazioneSpecifica = connection.prepareStatement("SELECT * FROM Programmazione WHERE id_programma = ? AND DATE(start_time) BETWEEN ? AND ? ORDER BY id_canale ASC, start_time DESC");
            getProgrammazioneSerie = connection.prepareStatement("SELECT * FROM Programmazione WHERE id_programma IN (SELECT id FROM webeng_proj.programma where id_serie = ? ORDER BY stagione, episodio ASC) AND start_time BETWEEN (NOW() - INTERVAL 1 MONTH) AND NOW() ORDER BY start_time DESC");
            getProgrammazioniPaginated = connection.prepareStatement("SELECT * FROM Programmazione WHERE DATE(start_time) BETWEEN ? AND ? ORDER BY id_canale, start_time DESC LIMIT ? OFFSET ?");
            getNumeroProgrammazioni = connection.prepareStatement("SELECT COUNT(*) AS num FROM Programmazione WHERE DATE(start_time) BETWEEN ? AND ?");
            iProgrammazione = connection.prepareStatement("INSERT INTO programmazione(id_canale, id_programma, start_time, durata) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            uProgrammazione = connection.prepareStatement("UPDATE programmazione SET id_canale=?, id_programma=?, start_time=?, durata=?, version=? WHERE id = ? AND version = ?");
            
        } catch (SQLException ex) {
            Logger.getLogger("Errore nell'inizializzazione del DAO Programmazione");
        }
    }
    
    /**
     * Chiude tutti gli statements aperti
     * 
     * @throws DataException 
     */
    public void destroy() throws DataException {
        try {
            getProgrammazioneByTimestamp.close();
            getProgrammazioneByTimestampCanale.close();
            getProgrammazioneByID.close();
            getCurrentProgrammazioneCanale.close();
            getProgrammazioneCorrente.close();
            getProgrammazioneSpecifica.close();
            getProgrammazioniPaginated.close();
            getNumeroProgrammazioni.close();
            iProgrammazione.close();
            uProgrammazione.close();
            dProgrammazione.close();
        } catch (SQLException ex) {
            Logger.getLogger(ProgrammazioneDAO_MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Crea l'oggetto programmazioneProxy settando il datalayer
     * @return 
     */
    @Override
    public ProgrammazioneProxy createProgrammazione() {
        return new ProgrammazioneProxy(getDataLayer());
    }
    
    /**
     * Crea l'oggetto programmazione dal result set ottenuto
     * 
     * @param rs
     * @return
     * @throws DataException 
     */
    public Programmazione createProgrammazione(ResultSet rs) throws DataException {
        ProgrammazioneProxy p = createProgrammazione();
        try {
            p.setKey(rs.getInt("id"));
            p.setStartTime(rs.getObject("start_time", LocalDateTime.class));
            p.setDurata(rs.getInt("durata"));
            p.setCanale_key(rs.getInt("id_canale"));
            p.setProgramma_key(rs.getInt("id_programma"));
        } catch(SQLException ex) {
            throw new DataException("Unable to create programmazione object form ResultSet", ex);
        }
        
        return p;
    }

    /**
     * Restituisce la programmazione nell'intervallo di tempo considerato
     * 
     * @param start
     * @param end
     * @return
     * @throws DataException 
     */
    @Override
    public List<Programmazione> getProgrammazione(LocalDateTime start, LocalDateTime end)  throws DataException {
        List<Programmazione> progs = null;
        
        
        try {
            getProgrammazioneByTimestamp.setString(1, getTimestampFormat(start));
            getProgrammazioneByTimestamp.setString(2, getTimestampFormat(end));
            
            try (ResultSet rs = getProgrammazioneByTimestamp.executeQuery()) {
                progs = new ArrayList<>();
                
                while (rs.next()) {
                    progs.add((Programmazione) getProgrammazione(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load programmazione by Timestamp", ex);
        }
        
        return progs;
    }
    
    /**
     * Restitusce la programmazione di un canale particolare nell'intervallo di tempo considerato
     * 
     * @param canale_key
     * @param start
     * @param end
     * @return
     * @throws DataException 
     */
    @Override
    public List<Programmazione> getProgrammazione(int canale_key, LocalDateTime start, LocalDateTime end)  throws DataException {
        List<Programmazione> progs = null;
        
        
        try {
            getProgrammazioneByTimestampCanale.setInt(1, canale_key);
            getProgrammazioneByTimestampCanale.setString(2, getTimestampFormat(start));
            getProgrammazioneByTimestampCanale.setString(3, getTimestampFormat(end));
            //UtilityMethods.debugConsole(this.getClass(), "getProgrammazione(key, start, end)", "Query : " + getProgrammazioneByTimestampCanale.toString());
            
            try (ResultSet rs = getProgrammazioneByTimestampCanale.executeQuery()) {
                progs = new ArrayList<>();
                
                while (rs.next()) {
                    progs.add((Programmazione) getProgrammazione(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load programmazione by Timestamp", ex);
        }
        
        return progs;
    }
    
    /**
     * Helper method: restituisce la stringa del formato corretto del timestamp
     * 
     * @param t
     * @return 
     */
    private String getTimestampFormat(LocalDateTime t) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = t.format(formatter);
        return formatDateTime;
    }

    /**
     * Restituisce l'oggetto programmazione data la chiave
     * 
     * @param key
     * @return
     * @throws DataException 
     */
    @Override
    public Programmazione getProgrammazione(int key) throws DataException {
        Programmazione p = null;
        
        if (dataLayer.getCache().has(Programmazione.class, key)) {
            p = dataLayer.getCache().get(Programmazione.class, key);
        } else {
            
            try {
                getProgrammazioneByID.setInt(1, key);
                try (ResultSet rs = getProgrammazioneByID.executeQuery()) {
                    if (rs.next()) {
                        p = createProgrammazione(rs);
                        
                        dataLayer.getCache().add(Programmazione.class, p);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load programmazione by ID", ex);
            }
        }
        return p;
    }
    
    

    /**
     * Restituisce la programmazione di un canale particolare
     * 
     * @param canale_key
     * @return
     * @throws DataException 
     */
    @Override
    public Programmazione currentProgram(int canale_key) throws DataException {
        Programmazione p = null;
            
            try {
                getCurrentProgrammazioneCanale.setInt(1, canale_key);
                try (ResultSet rs = getCurrentProgrammazioneCanale.executeQuery()) {
                    if (rs.next()) {
                        p = (Programmazione) getProgrammazione(rs.getInt("id"));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load programmazione attuale from canale", ex);
            }
        return p;
    }

    /**
     * Restituisce la lista delle programmazioni in onda ora
     * 
     * @return
     * @throws DataException 
     */
    @Override
    public List<Programmazione> getProgrammazioneCorrente() throws DataException {
        List<Programmazione> p = null;
            
            try {
                try (ResultSet rs = getProgrammazioneCorrente.executeQuery()) {
                    p = new ArrayList<>();
                    while (rs.next()) {
                        p.add( (Programmazione) getProgrammazione(rs.getInt("id")));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load programmazione attuale from canale", ex);
            }
        return p;
    }

    
    /**
     * Restituisce la programmazione specifica in base ai parametri specificati
     * 
     * @param programma_key
     * @param start
     * @param end
     * @param start_min
     * @param start_max
     * @return
     * @throws DataException 
     */
    @Override
    public List<Programmazione> getProgrammazioneSpecifica(int programma_key, LocalDate start, LocalDate end, LocalTime start_min, LocalTime start_max) throws DataException {
        List<Programmazione> p = null;
        
        if( start_min != null || start_max != null ) {
            try {
                getProgrammazioneSpecifica.setInt(1, programma_key);
                getProgrammazioneSpecifica.setString(2, start.format(DateTimeFormatter.ISO_DATE));
                getProgrammazioneSpecifica.setString(3, end.format(DateTimeFormatter.ISO_DATE));
                try (ResultSet rs = getProgrammazioneSpecifica.executeQuery()) {
                    p = new ArrayList<>();
                    while (rs.next()) {
                        boolean valid = true;
                        
                        if(start_min != null && start_min.isAfter( rs.getObject("start_time", LocalDateTime.class).toLocalTime() )) valid = false;
                        if(start_max != null && start_max.isBefore( rs.getObject("start_time", LocalDateTime.class).toLocalTime() )) valid = false;
                        
                        if(valid) p.add( (Programmazione) getProgrammazione(rs.getInt("id")));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load programmazione specifica by canale", ex);
            }
        }
        else {
            try {
                getProgrammazioneSpecifica.setInt(1, programma_key);
                getProgrammazioneSpecifica.setString(2, start.format(DateTimeFormatter.ISO_DATE));
                getProgrammazioneSpecifica.setString(3, end.format(DateTimeFormatter.ISO_DATE));
                try (ResultSet rs = getProgrammazioneSpecifica.executeQuery()) {
                    p = new ArrayList<>();
                    while (rs.next()) {
                        p.add( (Programmazione) getProgrammazione(rs.getInt("id")));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load programmazione specifica by canale", ex);
            }
        }
        
                
            
            
        return p;
    }

    /**
     * Restituisce la programmazione della serie considerata negli ultimi 30 giorni
     * 
     * @param id_serie
     * @return
     * @throws DataException 
     */
    @Override
    public List<Programmazione> getProgrammazioneSerie(int id_serie) throws DataException {
        List<Programmazione> p = null;
            
            try {
                getProgrammazioneSerie.setInt(1, id_serie);
                try (ResultSet rs = getProgrammazioneSerie.executeQuery()) {
                    p = new ArrayList<>();
                    while (rs.next()) {
                        p.add( (Programmazione) getProgrammazione(rs.getInt("id")));
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load programmazione della serie by id_serie", ex);
            }
        return p;
    }

    @Override
    public List<Programmazione> getProgrammazioniPaginated(LocalDate start, LocalDate end, int start_item, int elements) throws DataException {
        List<Programmazione> returnList = new ArrayList<>();
        
        if( start == null ) start = LocalDate.now().minusDays(30);
        if( end == null ) end = LocalDate.now().plusDays(30);
        
        try {
            getProgrammazioniPaginated.setString(1, start.format(DateTimeFormatter.ISO_DATE));
            getProgrammazioniPaginated.setString(2, end.format(DateTimeFormatter.ISO_DATE));
            getProgrammazioniPaginated.setInt(3, elements);
            getProgrammazioniPaginated.setInt(4, start_item);
            UtilityMethods.debugConsole(this.getClass(), "getProgrammazioniPaginated", "day: " + start);
            UtilityMethods.debugConsole(this.getClass(), "getProgrammazioniPaginated", "Query: " + getProgrammazioniPaginated.toString());
            try (ResultSet rs = getProgrammazioniPaginated.executeQuery()) {
            
                while(rs.next()) {
                    returnList.add((Programmazione) getProgrammazione(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get elenco programmazioni paginato", ex);
        }
         
        
        return returnList;
    }

    @Override
    public int getNumeroProgrammazioni() throws DataException {
        return getNumeroProgrammazioni(LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1));
    }
    
    @Override
    public int getNumeroProgrammazioni(LocalDate start, LocalDate end) throws DataException {
        int result = 0;
        if( start == null ) start = LocalDate.now().minusMonths(1);
        if( end == null ) end = LocalDate.now().plusMonths(1);
        try {
            getNumeroProgrammazioni.setString(1, start.format(DateTimeFormatter.ISO_DATE));
            getNumeroProgrammazioni.setString(2, end.format(DateTimeFormatter.ISO_DATE));
            try (ResultSet rs = getNumeroProgrammazioni.executeQuery()) {
            
                while(rs.next()) {
                    result = rs.getInt("num");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable get numero programmazioni", ex);
        }
        
        return result;
    }

    @Override
    public void storeProgrammazione(Programmazione programmazione) throws DataException {
        try {
            if (programmazione.getKey() != null && programmazione.getKey() > 0) { //update
                // Se proxy non modificato non facciamo nulla
                if (programmazione instanceof DataItemProxy && !((DataItemProxy) programmazione).isModified()) {
                    return;
                }
                
                // Altrimenti
                uProgrammazione.setInt(1, programmazione.getCanale().getKey());
                uProgrammazione.setInt(2, programmazione.getProgramma().getKey());
                uProgrammazione.setString(3, programmazione.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                uProgrammazione.setInt(4, programmazione.getDurata());
                long current_version = programmazione.getVersion();
                long next_version = current_version + 1;
                

                uProgrammazione.setLong(5, next_version);
                uProgrammazione.setInt(6, programmazione.getKey());
                uProgrammazione.setLong(7, current_version);

                
                
                if (uProgrammazione.executeUpdate() == 0) {
                    throw new OptimisticLockException(programmazione);
                }
                programmazione.setVersion(next_version);
            } else { //insert
                iProgrammazione.setInt(1, programmazione.getCanale().getKey());
                iProgrammazione.setInt(2, programmazione.getProgramma().getKey());
                iProgrammazione.setString(3, programmazione.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                iProgrammazione.setInt(4, programmazione.getDurata());
                
                if (iProgrammazione.executeUpdate() == 1) {
                    //getGeneratedKeys per leggere chiave generata
                    try (ResultSet keys = iProgrammazione.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            programmazione.setKey(key);
                            dataLayer.getCache().add(Programmazione.class, programmazione);
                        }
                    }
                }
            }

            //se abbiamo un proxy, resettiamo il suo attributo dirty
            if (programmazione instanceof DataItemProxy) {
                ((DataItemProxy) programmazione).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store programmazione", ex);
        }
    }

    @Override
    public void deleteProgrammazione(int key) throws DataException {
        try {
            dProgrammazione.setInt(1, key);
            int rows = dProgrammazione.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete programmazione by ID", ex);
        }
    }

    @Override
    public List<Programmazione> getLatest(int n) throws DataException {
        // Sfrutto il metodo getProgrammazioniPaginated per restituire le ultime n programmazioni
        return getProgrammazioniPaginated(0, n);
    }
    
    @Override
    public List<Programmazione> getProgrammazioniPaginated(int start_item, int elements) throws DataException {
        return getProgrammazioniPaginated(LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1), start_item, elements);
    }

    
    
}
