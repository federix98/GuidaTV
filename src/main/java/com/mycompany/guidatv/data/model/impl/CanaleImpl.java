/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.impl;

import com.mycompany.guidatv.data.DataItemImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;

/**
 *
 * @author Federico Di Menna
 */
public class CanaleImpl extends DataItemImpl<Integer> implements Canale{
    
    private Integer numero;
    private String nome;
    private String logoRef;
    private Programmazione programmazioneCorrente;
    
    public CanaleImpl() {
        super();
        this.numero = null;
        this.nome = "";
        this.logoRef = "";
        this.programmazioneCorrente = null;
    }

    @Override
    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    @Override
    public Integer getNumero() {
        return numero;
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
    public void setLogoRef(String link) {
        this.logoRef = link;
    }

    @Override
    public String getLogoRef() {
        return logoRef;
    }
    
    @Override
    public void setProgrammazioneCorrente(Programmazione programmazioneCorrente) {
        this.programmazioneCorrente = programmazioneCorrente;
    }
    
    @Override
    public Programmazione getProgrammazioneCorrente() {
        return this.programmazioneCorrente;
    }

    /**
     * Ordino in base al canale
     * @param o
     */
    @Override
    public int compareTo(Object o) {
        if(o instanceof CanaleImpl) {
            if( this.getNumero() < ((CanaleImpl) o).getNumero() ) {
                return -1;
            } 
            else if( this.getNumero() > ((CanaleImpl) o).getNumero() ) {
                return 1;
            } 
            else return 0;
        }
        else return super.compareTo(o);
    }

    
    
}
