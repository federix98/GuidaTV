/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.impl;

import com.mycompany.guidatv.data.DataItemImpl;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;

/**
 *
 * @author Federico Di Menna
 */
public class RicercaImpl extends DataItemImpl<Integer> implements Ricerca {
    
    private String queryString;
    
    public RicercaImpl() {
        super();
        this.queryString = "";
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public void setQueryString(String query) {
        this.queryString = query;
    }
    
}
