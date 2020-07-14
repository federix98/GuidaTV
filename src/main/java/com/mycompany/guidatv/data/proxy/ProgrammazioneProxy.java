/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.proxy;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.dao.CanaleDAO;
import com.mycompany.guidatv.data.dao.ProgrammaDAO;
import com.mycompany.guidatv.data.dao.ProgrammazioneDAO;
import com.mycompany.guidatv.data.model.impl.ProgrammazioneImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 */
public class ProgrammazioneProxy extends ProgrammazioneImpl implements DataItemProxy {

    private boolean modified;
    private int canale_key, programma_key;
    
    private final DataLayer dataLayer;
    
    public ProgrammazioneProxy(DataLayer dataLayer) {
        super();
        
        this.modified = false;
        this.canale_key = 0;
        this.programma_key = 0;
        
        this.dataLayer = dataLayer;
    }

    public int getCanale_key() {
        return canale_key;
    }

    public void setCanale_key(int canale_key) {
        this.canale_key = canale_key;
    }

    public int getProgramma_key() {
        return programma_key;
    }

    public void setProgramma_key(int programma_key) {
        this.programma_key = programma_key;
    }
    
    
    
    @Override
    public boolean isModified() {
        return this.modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    
    @Override
    public Canale getCanale() {
        
        if (super.getCanale() == null && canale_key > 0) {
            try {
                super.setCanale(((CanaleDAO) dataLayer.getDAO(Canale.class)).getCanale(canale_key));
            } catch (DataException ex) {
                Logger.getLogger(ProgrammazioneProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return super.getCanale();
    }

    @Override
    public void setCanale(Canale canale) {
        super.setCanale(canale);
        this.modified = true;
    }

    @Override
    public Programma getProgramma() {
        
        if (super.getProgramma() == null && programma_key > 0) {
            //UtilityMethods.debugConsole(this.getClass(), "getProgramma()", "Getting Program " + programma_key + " datalayer: " + dataLayer + " obj: " + dataLayer.getDAO(Programma.class));
            try {
                super.setProgramma(((ProgrammaDAO) dataLayer.getDAO(Programma.class)).getProgramma(programma_key));
            } catch (DataException ex) {
                Logger.getLogger(ProgrammazioneProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return super.getProgramma();
    }

    @Override
    public void setProgramma(Programma programma) {
        super.setProgramma(programma);
        this.modified = true;
    }

    @Override
    public void setStartTime(LocalDateTime time) {
        super.setStartTime(time);
        this.modified = true;
    }

    @Override
    public void setDurata(Integer durata) {
        super.setDurata(durata);
        this.modified = true;
    }
    
}
