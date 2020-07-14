/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.impl;

import com.mycompany.guidatv.data.DataItemImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author Federico Di Menna
 */
public class ProgrammazioneImpl extends DataItemImpl<Integer> implements Programmazione, Comparable {

    private Canale canale;
    private Programma programma;
    private LocalDateTime startTime;
    private Integer durata;
    
    public ProgrammazioneImpl() {
        super();
        this.canale = null;
        this.programma = null;
        this.startTime = null;
        this.durata = 0;
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
    public Programma getProgramma() {
        return programma;
    }

    @Override
    public void setProgramma(Programma programma) {
        this.programma = programma;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(LocalDateTime time) {
        this.startTime = time;
    }

    @Override
    public String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formatDateTime = startTime.format(formatter);
        String nowDate = LocalDateTime.now().format(formatter);
        if(formatDateTime.equals(nowDate)) return "Oggi";
        return formatDateTime;
    }
    
    @Override
    public String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formatDateTime = startTime.format(formatter);
        return formatDateTime;
    }

    @Override
    public Integer getDurata() {
        return durata;
    }

    @Override
    public void setDurata(Integer durata) {
        this.durata = durata;
    }

    @Override
    public String getEndTime() {
        LocalDateTime endTime = startTime.plusMinutes(this.durata);
        String formatDateTime = endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return formatDateTime;
    }
    
    @Override     
    public int compareTo(Object obj) {
        if(obj instanceof Programmazione) {
            Programmazione p = (Programmazione) obj;
            return (this.getStartTime().isBefore( p.getStartTime()) ? -1 : (this.getStartTime().equals(p.getStartTime()) ? 0 : 1 ) );
        }
        else return super.compareTo(obj); 
    }

    @Override
    public String getStartTimeFormatted(String pattern) {
        return startTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    @Override
    public int getHour() {
        return startTime.toLocalTime().getHour();
    }
    
    @Override
    public boolean inOnda() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(startTime.plusMinutes(durata));
    }
}
