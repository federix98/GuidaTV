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
public interface Genere extends DataItem<Integer>{
    
    void setNome(String nome);
    
    String getNome();
    
    void setDescrizione(String descrizione);
    
    String getDescrizione();
    
}
