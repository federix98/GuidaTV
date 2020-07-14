/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.interfaces;

import com.mycompany.guidatv.data.DataItem;

/**
 *
 * @author Federico Di Menna
 */
public interface Ricerca extends DataItem<Integer> {
    
    String getQueryString();
    
    void setQueryString(String query);
    
}
