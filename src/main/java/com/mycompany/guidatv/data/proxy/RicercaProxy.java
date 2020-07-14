/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.proxy;

import com.mycompany.guidatv.data.DataItemProxy;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.model.impl.RicercaImpl;

/**
 *
 * @author Federico Di Menna
 */
public class RicercaProxy extends RicercaImpl implements DataItemProxy {

    private boolean modified;
    
    private final DataLayer dataLayer;
    
    public RicercaProxy(DataLayer dl) {
        super();
        
        this.modified = false;
        this.dataLayer = dl;
    }
    
    @Override
    public void setQueryString(String query) {
        this.modified = true;
        super.setQueryString(query);
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
