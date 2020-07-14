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
import com.mycompany.guidatv.data.dao.UtenteDAO;
import com.mycompany.guidatv.data.model.impl.InteresseImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 */
public class InteresseProxy extends InteresseImpl implements DataItemProxy {
    
    private int id_utente;
    private int id_canale;

    private boolean modified;
    
    private final DataLayer dataLayer;
    
    public InteresseProxy(DataLayer d) {
        super();
        
        this.dataLayer = d;
        this.modified = false;
    }
    
    @Override
    public void setUtente(Utente utente) {
        this.modified = true;
        super.setUtente(utente);
    }
    
    @Override
    public void setCanale(Canale canale) {
        this.modified = true;
        super.setCanale(canale);
    }
    
    @Override
    public void setStartTime(LocalTime startTime) {
        this.modified = true;
        super.setStartTime(startTime);
    }
    
    @Override
    public void setEndTime(LocalTime endTime) {
        this.modified = true;
        super.setEndTime(endTime);
    }
    
    public int getId_utente() {
        return id_utente;
    }

    public void setId_utente(int id_utente) {
        this.id_utente = id_utente;
    }

    public int getId_canale() {
        return id_canale;
    }

    public void setId_canale(int id_canale) {
        this.id_canale = id_canale;
    }
    
    @Override
    public Canale getCanale() {
        
        if (super.getCanale() == null && id_canale > 0) {
            try {
                super.setCanale(((CanaleDAO) dataLayer.getDAO(Canale.class)).getCanale(id_canale));
            } catch (DataException ex) {
                Logger.getLogger(InteresseProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return super.getCanale();
    }
    
    @Override
    public Utente getUtente() {
        if (super.getUtente() == null && id_utente > 0) {
            try {
                super.setUtente(((UtenteDAO) dataLayer.getDAO(Utente.class)).getUtente(id_utente));
            } catch (DataException ex) {
                Logger.getLogger(InteresseProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return super.getUtente();
    }

    @Override
    public boolean isModified() {
        return this.modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    
}
