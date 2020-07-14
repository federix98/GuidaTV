/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.model.impl;

import com.mycompany.guidatv.data.DataItemImpl;
import com.mycompany.guidatv.data.model.interfaces.Classificazione;
import com.mycompany.guidatv.data.model.interfaces.Genere;
import com.mycompany.guidatv.data.model.interfaces.Programma;

/**
 *
 * @author Federico Di Menna
 */
public class ProgrammaImpl extends DataItemImpl<Integer> implements Programma {

    private String nome;
    private String descrizione;
    private String linkRefImg;
    private String linkRefDetails;
    private Integer idSerie;
    private Integer stagione;
    private Integer episodio;
    private Genere genere;
    private Integer durata;
    private Classificazione classificazione;
    private Programma serie;
    
    public ProgrammaImpl() {
        super();
        this.nome = "";
        this.descrizione = "";
        this.linkRefImg = "";
        this.linkRefDetails = "";
        this.idSerie = null;
        this.stagione = 0;
        this.episodio = 0;
        this.genere = null;
        this.classificazione = null;
        this.durata = 0;
        this.serie = null;
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
    public String getNome() {
        return nome;
    }

    @Override
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    @Override
    public String getLinkRefImg() {
        return linkRefImg;
    }

    @Override
    public void setLinkRefImg(String linkRefImg) {
        this.linkRefImg = linkRefImg;
    }

    @Override
    public String getLinkRefDetails() {
        return linkRefDetails;
    }

    @Override
    public void setLinkRefDetails(String linkRefDetails) {
        this.linkRefDetails = linkRefDetails;
    }

    @Override
    public Integer getIdSerie() {
        return idSerie;
    }

    @Override
    public void setIdSerie(Integer idSerie) {
        this.idSerie = idSerie;
    }

    @Override
    public Integer getStagione() {
        return stagione;
    }

    @Override
    public void setStagione(Integer stagione) {
        this.stagione = stagione;
    }

    @Override
    public Integer getEpisodio() {
        return episodio;
    }

    @Override
    public void setEpisodio(Integer episodio) {
        this.episodio = episodio;
    }

    @Override
    public Genere getGenere() {
        return genere;
    }

    @Override    
    public void setGenere(Genere genere) {
        this.genere = genere;
    }

    @Override
    public Classificazione getClassificazione() {
        return classificazione;
    }

    @Override
    public void setClassificazione(Classificazione classificazione) {
        this.classificazione = classificazione;
    }
    
    
    
}
