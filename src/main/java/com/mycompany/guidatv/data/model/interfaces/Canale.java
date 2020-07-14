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
public interface Canale extends DataItem<Integer> {
    
    void setNumero(Integer numero);
    
    Integer getNumero();
    
    void setNome(String nome);
    
    String getNome();
    
    void setLogoRef(String link);
    
    String getLogoRef();
    
    void setProgrammazioneCorrente(Programmazione programmazioneCorrente);
    
    Programmazione getProgrammazioneCorrente();
    
}
