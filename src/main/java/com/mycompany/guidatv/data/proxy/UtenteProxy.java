/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.proxy;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.dao.InteresseDAO;
import com.mycompany.guidatv.data.dao.RicercaDAO;
import com.mycompany.guidatv.data.dao.RuoloDAO;
import com.mycompany.guidatv.data.model.impl.UtenteImpl;
import com.mycompany.guidatv.data.model.interfaces.Interesse;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;
import com.mycompany.guidatv.data.model.interfaces.Ruolo;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 */
public class UtenteProxy extends UtenteImpl implements DataItemProxy {
    
    private boolean modified;
    
    protected final DataLayer dataLayer;
    private int id_ruolo;
    
    public UtenteProxy(DataLayer dl) {
        super();
        
        this.modified = false;
        this.dataLayer = dl;
    }
    
    @Override
    public void setUsername(String username) {
        this.modified = true;
        super.setUsername(username);
    }
    
    @Override
    public void setEmail(String email) {
        this.modified = true;
        super.setEmail(email);
    }
        
    @Override
    public void setPassword(String pass) {
        this.modified = true;
        super.setPassword(pass);
    }
    
    @Override
    public void setDataNascita(LocalDate datanascita) {
        this.modified = true;
        super.setDataNascita(datanascita);
    }
    
    @Override
    public void setSendEmail(Boolean sendemail) {
        this.modified = true;
        super.setSendEmail(sendemail);
    }
    
    @Override
    public void setToken(String token) {
        this.modified = true;
        super.setToken(token);
    }
    
    @Override
    public void setEmailVerifiedAt(LocalDate ts) {
        this.modified = true;
        super.setEmailVerifiedAt(ts);
    }
    
    @Override
    public void setExpirationDate(LocalDate ts) {
        this.modified = true;
        super.setExpirationDate(ts);
    }
    
    public int getIdRuolo() {
        return id_ruolo;
    }

    public void setIdRuolo(int id_ruolo) {
        this.id_ruolo = id_ruolo;
    }
    
    @Override
    public void setRuolo(Ruolo ruolo) {
        this.modified = true;
        super.setRuolo(ruolo);
    }
    
    @Override
    public Ruolo getRuolo() {
        if (super.getRuolo() == null && id_ruolo > 0) {
            try {
                super.setRuolo(((RuoloDAO) dataLayer.getDAO(Ruolo.class)).getRuolo(id_ruolo));
            } catch (DataException ex) {
                Logger.getLogger(ProgrammazioneProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return super.getRuolo();
    }

    @Override
    public void setInteressi(List<Interesse> interessi) {
        this.modified = true;
        super.setInteressi(interessi);
    }

    @Override
    public List<Interesse> getInteressi() {
        if (super.getInteressi() == null) {
            try {
                super.setInteressi(((InteresseDAO) dataLayer.getDAO(Interesse.class)).getInteressiUtente(this));
            } catch (DataException ex) {
                Logger.getLogger(UtenteProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return super.getInteressi();
    }

    @Override
    public void setRicerche(List<Ricerca> ricerche) {
        this.modified = true;
        super.setRicerche(ricerche);
    }

    @Override
    public List<Ricerca> getRicerche() {
        if (super.getRicerche() == null) {
            try {
                super.setRicerche(((RicercaDAO) dataLayer.getDAO(Ricerca.class)).getRicercheUtente(this));
            } catch (DataException ex) {
                Logger.getLogger(UtenteProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return super.getRicerche();
    }

    @Override
    public void cleanInteressi() {
        this.modified = true;
        if(getInteressi() != null) {
            for(Interesse i : getInteressi()) {
                try {
                    ((InteresseDAO) dataLayer.getDAO(Interesse.class)).removeInteresse(i.getKey());
                } catch (DataException ex) {
                    Logger.getLogger(UtenteProxy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        super.cleanInteressi();
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
