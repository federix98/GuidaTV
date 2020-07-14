/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data;

import java.sql.Connection;

/**
 *
 * @author Federico Di Menna
 */
public class DAO {
    
    protected DataLayer dataLayer;
    protected Connection connection;
    
    public DAO(DataLayer dl) {
        this.dataLayer = dl;
        this.connection = dl.getConnection();
    }
    
    protected DataLayer getDataLayer() {
        return dataLayer;
    }
    
    public void init() throws DataException {
        
    }
    
}
