/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.interfaces;

import com.mycompany.guidatv.data.DataItem;
import java.time.LocalTime;

/**
 *
 * @author Federico Di Menna
 */
public interface Interesse extends DataItem<Integer> {
    
    Utente getUtente();
    
    void setUtente(Utente utente);
    
    Canale getCanale();
    
    void setCanale(Canale canale);
    
    LocalTime getStartTime();
    
    void setStartTime(LocalTime startTime);
    
    LocalTime getEndTime();
    
    void setEndTime(LocalTime endTime);
    
}
