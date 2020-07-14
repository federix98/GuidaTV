/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface RicercaDAO {
    
    Ricerca createRicerca();
    
    Ricerca getRicerca(int id_ricerca) throws DataException;
    
    List<Ricerca> getRicercheUtente(Utente utente) throws DataException;
    
    void storeRicerca(Ricerca ricerca, int id_utente) throws DataException;
    
    boolean removeRicerca(int id_ricerca) throws DataException;
    
}
