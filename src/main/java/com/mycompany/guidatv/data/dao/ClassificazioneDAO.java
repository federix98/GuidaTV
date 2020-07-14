/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Classificazione;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface ClassificazioneDAO {
    
    Classificazione getClassificazione(int key) throws DataException;
    
    int getNumeroClassificazioni() throws DataException;
    
    List<Classificazione> getAllClassificazioni() throws DataException;
    
    List<Classificazione> getClassificazioniPaginated(int start_item, int elements) throws DataException;
    
    void storeClassificazione(Classificazione c) throws DataException;
    
    void deleteClassificazione(int key) throws DataException;
    
}
