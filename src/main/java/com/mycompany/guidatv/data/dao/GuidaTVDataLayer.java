/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Classificazione;
import com.mycompany.guidatv.data.model.interfaces.Genere;
import com.mycompany.guidatv.data.model.interfaces.Interesse;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;
import com.mycompany.guidatv.data.model.interfaces.Ruolo;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author Federico Di Menna
 */
public class GuidaTVDataLayer extends DataLayer {
    
    public GuidaTVDataLayer(DataSource datasource) throws SQLException {
        super(datasource);
    }
    
    @Override
    public void init() throws DataException {
        // QUI DECIDO DI UTILIZZARE L'IMPLEMENTAZIONE DI MYSQL
        registerDAO(Utente.class, new UtenteDAO_MySQL(this));
        registerDAO(Ruolo.class, new RuoloDAO_MySQL(this));
        registerDAO(Programma.class, new ProgrammaDAO_MySQL(this));
        registerDAO(Programmazione.class, new ProgrammazioneDAO_MySQL(this));
        registerDAO(Canale.class, new CanaleDAO_MySQL(this));
        registerDAO(Genere.class, new GenereDAO_MySQL(this));
        registerDAO(Classificazione.class, new ClassificazioneDAO_MySQL(this));
        registerDAO(Ricerca.class, new RicercaDAO_MySQL(this));
        registerDAO(Interesse.class, new InteresseDAO_MySQL(this));
    }
    
    public UtenteDAO getUtenteDAO() {
        return (UtenteDAO) getDAO(Utente.class);
    }
    
    public RuoloDAO getRuoloDAO() {
        return (RuoloDAO) getDAO(Ruolo.class);
    }
    
    public CanaleDAO getCanaleDAO() {
        return (CanaleDAO) getDAO(Canale.class);
    }
    
    public ProgrammaDAO getProgrammaDAO() {
        return (ProgrammaDAO) getDAO(Programma.class);
    }
    
    public ProgrammazioneDAO getProgrammazioneDAO() {
        return (ProgrammazioneDAO) getDAO(Programmazione.class);
    }
    
    public GenereDAO getGenereDAO() {
        return (GenereDAO) getDAO(Genere.class);
    }
    
    public ClassificazioneDAO getClassificazioneDAO() {
        return (ClassificazioneDAO) getDAO(Classificazione.class);
    }
    
    public RicercaDAO getRicercaDAO() {
        return (RicercaDAO) getDAO(Ricerca.class);
    }
    
    public InteresseDAO getInteresseDAO() {
        return (InteresseDAO) getDAO(Interesse.class);
    }
    
}
