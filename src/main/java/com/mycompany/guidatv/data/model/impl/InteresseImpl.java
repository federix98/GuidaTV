/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.impl;

import com.mycompany.guidatv.data.DataItemImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Interesse;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import java.time.LocalTime;

/**
 *
 * @author Federico Di Menna
 */
public class InteresseImpl extends DataItemImpl<Integer> implements Interesse {
    
    private Canale canale;
    private Utente utente;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public InteresseImpl() {
        super();
        this.canale = null;
        this.utente = null;
        this.startTime = null;
        this.endTime = null;
    }

    @Override
    public Utente getUtente() {
        return utente;
    }

    @Override
    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    @Override
    public Canale getCanale() {
        return canale;
    }

    @Override
    public void setCanale(Canale canale) {
        this.canale = canale;
    }

    @Override
    public LocalTime getStartTime() {
        return startTime; 
    }

    @Override
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
}
