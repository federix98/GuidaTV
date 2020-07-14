/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.proxy;

import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.model.impl.ClassificazioneImpl;

/**
 *
 * @author Federico Di Menna
 */
public class ClassificazioneProxy extends ClassificazioneImpl implements DataItemProxy {

    private boolean modified;
    
    private final DataLayer dataLayer;
    
    public ClassificazioneProxy(DataLayer d) {
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
    public void setMinAge(int minAge) {
        super.setMinAge(minAge);
        this.modified  = true;
    }
    
}
