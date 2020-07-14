/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.proxy;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.dao.ClassificazioneDAO;
import com.mycompany.guidatv.data.dao.GenereDAO;
import com.mycompany.guidatv.data.dao.ProgrammaDAO;
import com.mycompany.guidatv.data.model.impl.ProgrammaImpl;
import com.mycompany.guidatv.data.model.interfaces.Classificazione;
import com.mycompany.guidatv.data.model.interfaces.Genere;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 * 
 * Nel proxy faccio l'override di tutti i set delle variabili in modo
 * da settare il proxy come modified quando viene effettuata
 * un'operazione del genere.
 * Per gli gli oggetti (relazioni) sovrascrivo anche i metodi get
 * per controllare se li ho già ottenuti ottimizzando i caricamenti.
 */
public class ProgrammaProxy extends ProgrammaImpl implements DataItemProxy {
    
    private boolean modified;
    private int classificazione_key;
    private int genere_key;
    
    private final DataLayer dataLayer;
    
    public ProgrammaProxy(DataLayer d) {
        super();
        
        this.dataLayer = d;
        this.modified = false;
        this.genere_key = 0;
        this.classificazione_key = 0;
    }


    @Override
    public void setNome(String nome) {
        super.setNome(nome);
        this.modified = true;
    }

    @Override
    public void setDescrizione(String descrizione) {
        super.setDescrizione(descrizione);
        this.modified = true;
    }

    @Override
    public void setLinkRefImg(String linkRefImg) {
        super.setLinkRefImg(linkRefImg);
        this.modified = true;
    }

    @Override
    public void setLinkRefDetails(String linkRefDetails) {
        super.setLinkRefDetails(linkRefDetails);
        this.modified = true;
    }

    @Override
    public void setIdSerie(Integer idSerie) {
        super.setIdSerie(idSerie);
        this.modified = false;
    }

    @Override
    public void setStagione(Integer stagione) {
        super.setStagione(stagione);
        this.modified = false;
    }

    @Override
    public void setEpisodio(Integer episodio) {
        super.setEpisodio(episodio);
        this.modified = true;
    }

    @Override    
    public void setGenere(Genere genere) {
        super.setGenere(genere);
        this.modified = true;
    }
    
    @Override    
    public void setDurata(Integer durata) {
        super.setDurata(durata);
        this.modified = true;
    }
    
    @Override
    public Genere getGenere() {
        //UtilityMethods.debugConsole(this.getClass(), "getGenere", "sono in get genere super: " + super.getGenere() + " key " + genere_key);
        if( super.getGenere() == null && genere_key > 0 ) {
            try {
                super.setGenere(((GenereDAO) dataLayer.getDAO(Genere.class)).getGenere(genere_key));
                
            } catch (DataException ex) {
                Logger.getLogger(ProgrammaProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return super.getGenere();
    }

    @Override
    public Classificazione getClassificazione() {
        if( super.getClassificazione() == null && classificazione_key > 0 ) {
            try {
                super.setClassificazione(((ClassificazioneDAO) dataLayer.getDAO(Classificazione.class)).getClassificazione(classificazione_key));
            } catch (DataException ex) {
                Logger.getLogger(ProgrammaProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return super.getClassificazione();
    }

    @Override
    public void setClassificazione(Classificazione classificazione) {
        super.setClassificazione(classificazione);
        this.modified = true;
    }
    
    @Override
    public boolean isModified() {
        return this.modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    
    public int getClassificazione_key() {
        return classificazione_key;
    }

    public void setClassificazione_key(int classificazione_key) {
        this.classificazione_key = classificazione_key;
    }

    public int getGenere_key() {
        return genere_key;
    }

    public void setGenere_key(int genere_key) {
        this.genere_key = genere_key;
    }
}
