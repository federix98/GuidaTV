/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface ProgrammaDAO {

    // SERVE A CREARE IL PROGRAMMA
    Programma createProgramma();
    
    // CONTA I PROGRAMMI PRESENTI SUL DB
    Integer getNumeroProgrammi() throws DataException;
    
    // RESTITUISCE TUTTI I PROGRAMMI PRESENTI 
    List<Programma> getProgrammi() throws DataException;
    
    // RESTITUISCE TUTTI I PROGRAMMI PRESENTI MA SE SONO SERIE SOLTANTO UNA VOLTA
    List<Programma> getProgrammiDistinctSerie() throws DataException;
    
    // RESTITUISCE TUTTI I PRIMI EPISODI DELLE SERIE
    List<Programma> getIdSerie() throws DataException;
    
    // RESTITUISCE UNA LISTA PAGINATA DI PROGRAMMI
    List<Programma> getProgrammiPaginated(int start_item, int elements) throws DataException;
    
    // RESTITUISCE IL PROGRAMMA DALLA CHIAVE
    Programma getProgramma(int key) throws DataException;
    
    // EFFETTUA LA RICERCA DEI PROGRAMMI PER NOME E PER GENERE (Tutte le diverse combinazioni)
    List<Programma> cercaProgrammi(String nome, int genere_key) throws DataException;
    
    List<Programma> getRelatedPrograms(Programma programma) throws DataException;
    
    // INSERISCE O AGGIORNA UN PROGRAMMA
    void storeProgramma(Programma p) throws DataException;
    
    // ELIMINA UN PROGRAMMA
    void deleteProgramma(int key) throws DataException;
}
