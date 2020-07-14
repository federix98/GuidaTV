/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.proxy;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.dao.ProgrammazioneDAO;
import com.mycompany.guidatv.data.model.impl.CanaleImpl;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico Di Menna
 */
public class CanaleProxy extends CanaleImpl implements DataItemProxy {
    
    private boolean modified;
    
    private final DataLayer dataLayer;
    
    public CanaleProxy(DataLayer d) {
        super();
        
        this.dataLayer = d;
        this.modified = false;
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
    public void setNumero(Integer numero) {
        super.setNumero(numero);
        this.modified = true;
    }

    @Override
    public void setNome(String nome) {
        super.setNome(nome);
        this.modified = true;
    }

    @Override
    public void setLogoRef(String link) {
        super.setLogoRef(link);
        this.modified = true;
    }
    
    @Override
    public void setProgrammazioneCorrente(Programmazione corrente) {
        super.setProgrammazioneCorrente(corrente);
        this.modified = true;
    }
    
    @Override
    public Programmazione getProgrammazioneCorrente() {
        
        if (super.getProgrammazioneCorrente() == null) {
            try {
                super.setProgrammazioneCorrente(((ProgrammazioneDAO) dataLayer.getDAO(Programmazione.class)).currentProgram(getKey()));
            } catch (DataException ex) {
                Logger.getLogger(ProgrammazioneProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return super.getProgrammazioneCorrente();
    }
    
}
