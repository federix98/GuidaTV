/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.impl;

import com.mycompany.guidatv.data.DataItemImpl;
import com.mycompany.guidatv.data.model.interfaces.Genere;

/**
 *
 * @author Federico Di Menna
 */
public class GenereImpl extends DataItemImpl<Integer> implements Genere {
    
    private String nome;
    private String descrizione;
    
    public GenereImpl() {
        super();
        this.nome = "";
        this.descrizione = "";
    }
    
    @Override
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    @Override
    public String getDescrizione() {
        return descrizione;
    }
    
}
