/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.proxy;

import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.model.impl.RuoloImpl;

/**
 *
 * @author Federico Di Menna
 */
public class RuoloProxy extends RuoloImpl implements DataItemProxy {
    
    private boolean modified;
    
    private final DataLayer dataLayer;
    
    public RuoloProxy(DataLayer dl) {
        super();
        this.modified = false;
        this.dataLayer = dl;
    }
    
    @Override
    public void setNome(String nome) {
        this.modified = true;
        super.setNome(nome);
    }
    
    @Override
    public void setDescrizione(String descrizione) {
        this.modified = true;
        super.setDescrizione(descrizione);
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
