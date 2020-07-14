/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.impl;

import com.mycompany.guidatv.data.DataItemImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Interesse;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;
import com.mycompany.guidatv.data.model.interfaces.Ruolo;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Federico Di Menna
 */
public class UtenteImpl extends DataItemImpl<Integer> implements Utente {

    private String username;
    private String email;
    private String password;
    private LocalDate dataNascita;
    private Boolean sendEmail;
    private Ruolo ruolo;
    private String token;
    private LocalDate emailVerifiedAt;
    private LocalDate expirationDate;
    private List<Ricerca> ricerche;
    private List<Interesse> interessi;
    
    /**
     * Empty Constructor
     */
    public UtenteImpl() {
        super();
        this.username = "";
        this.email = "";
        this.password = "";
        this.dataNascita = null;
        this.sendEmail = false;
        this.ruolo = null;
        this.token = "";
        this.emailVerifiedAt = null;
        this.ricerche = null;
        this.interessi = null;
    }
    
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public LocalDate getDataNascita() {
        return dataNascita;
    }

    @Override
    public void setDataNascita(LocalDate data) {
        this.dataNascita = data;
    }

    @Override
    public Boolean getSendEmail() {
        return sendEmail;
    }

    @Override
    public void setSendEmail(Boolean send) {
        this.sendEmail = send;
    }

    @Override
    public Ruolo getRuolo() {
        return ruolo;
    }

    @Override
    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public LocalDate getEmailVerifiedAt() {
        return this.emailVerifiedAt;
    }

    @Override
    public void setEmailVerifiedAt(LocalDate ldt) {
        this.emailVerifiedAt = ldt;
    }
    
    @Override
    public LocalDate getExpirationDate() {
        return this.expirationDate;
    }

    @Override
    public void setExpirationDate(LocalDate ldt) {
        this.expirationDate = ldt;
    }

    @Override
    public List<Ricerca> getRicerche() {
        return ricerche;
    }

    @Override
    public void setRicerche(List<Ricerca> ricerche) {
        this.ricerche = ricerche;
    }

    @Override
    public List<Interesse> getInteressi() {
        return interessi;
    }

    @Override
    public void setInteressi(List<Interesse> interessi) {
        this.interessi = interessi;
    }

    @Override
    public boolean interestsChannel(Canale canale) {
        boolean res = false;
        if(getInteressi() != null) {
            for(Interesse intrs : getInteressi()) 
                if(Objects.equals(intrs.getCanale().getKey(), canale.getKey())) return true;
        }
        return res;
    }

    @Override
    public void cleanInteressi() {
        this.interessi = null;
    }

    @Override
    public List<Integer> getIdCanaliInteressati() {
        List<Integer> ret = new ArrayList<>();
        if(getInteressi() != null) {
            for(Interesse i : getInteressi()) {
                if(!ret.contains(i.getCanale().getKey())) ret.add(i.getCanale().getKey());
            }
        }
        return ret;
    }

    @Override
    public boolean interestsTime(int i) {
        boolean res = false;
        if(getInteressi() != null) {
            for(Interesse intrs : getInteressi()) 
                if(UtilityMethods.getOrarioInizioFascia(i).equals(intrs.getStartTime())
                        && UtilityMethods.getOrarioFineFascia(i).equals(intrs.getEndTime())) {
                    UtilityMethods.debugConsole(this.getClass(), "update", "Tornato vero perche " + UtilityMethods.getOrarioInizioFascia(i) + " = " + intrs.getStartTime() + " && " + UtilityMethods.getOrarioFineFascia(i) + " = " + intrs.getEndTime());
                    return true;
                }
        }
        return res;
    }

    @Override
    public int getAge() {
        return LocalDate.now().getYear() - this.dataNascita.getYear();
    }
    
    
}
