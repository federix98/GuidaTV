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
public interface Programma extends DataItem<Integer>{
   
    void setNome(String nome);
    
    String getNome();
    
    void setDescrizione(String descrizione);
    
    String getDescrizione();
    
    void setLinkRefImg(String link);
    
    String getLinkRefImg();
    
    void setLinkRefDetails(String link);
    
    String getLinkRefDetails();
   
    void setIdSerie(Integer serie);
    
    Integer getIdSerie();
   
    void setStagione(Integer stagione);
    
    Integer getStagione();
    
    void setEpisodio(Integer episodio);
    
    Integer getEpisodio();
    
    void setDurata(Integer durata);
    
    Integer getDurata();
    
    /**
     * FOREIGN KEYS
     */
    
   
    void setClassificazione(Classificazione classificazione);
    
    Classificazione getClassificazione();
    
    void setGenere(Genere genere);
    
    Genere getGenere();
    
    
}
