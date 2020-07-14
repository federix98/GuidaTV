/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.interfaces;

import com.mycompany.guidatv.data.DataItem;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface Utente extends DataItem<Integer> {
    
    String getUsername();
    
    void setUsername(String username);
    
    String getEmail();
    
    void setEmail(String email);
    
    String getPassword();
    
    void setPassword(String password);
    
    LocalDate getDataNascita();
    
    void setDataNascita(LocalDate data);
    
    Boolean getSendEmail();
    
    void setSendEmail(Boolean send);
    
    Ruolo getRuolo();
    
    void setRuolo(Ruolo ruolo);
    
    String getToken();
    
    void setToken(String token);
    
    LocalDate getExpirationDate();
    
    void setExpirationDate(LocalDate date);
    
    LocalDate getEmailVerifiedAt();
    
    void setEmailVerifiedAt(LocalDate date);
    
    List<Ricerca> getRicerche();
    
    void setRicerche(List<Ricerca> ricerche);
    
    List<Interesse> getInteressi();
    
    void setInteressi(List<Interesse> interessi);
    
    boolean interestsChannel(Canale canale); 
    
    boolean interestsTime(int id_fascia);
    
    List<Integer> getIdCanaliInteressati();
    
    void cleanInteressi();
    
    int getAge();
    
}
