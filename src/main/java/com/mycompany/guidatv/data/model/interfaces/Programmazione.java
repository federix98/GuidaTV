/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.interfaces;

import com.mycompany.guidatv.data.DataItem;
import java.time.LocalDateTime;

/**
 *
 * @author Federico Di Menna
 */
public interface Programmazione extends DataItem<Integer> {
    
    Canale getCanale();
    
    void setCanale(Canale canale);
    
    Programma getProgramma();
    
    void setProgramma(Programma programma);
    
    LocalDateTime getStartTime();
    
    void setStartTime(LocalDateTime time);
    
    String getStartTimeFormatted(String pattern);
    
    // FROM TIMESTAMP
    String getDate(); 
    
    String getTime();
    
    int getHour();
    
    Integer getDurata();
    
    String getEndTime();
    
    void setDurata(Integer durata);
    
    boolean inOnda();
    
}
