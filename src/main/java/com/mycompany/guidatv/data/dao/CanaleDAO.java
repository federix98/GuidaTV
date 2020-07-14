/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface CanaleDAO {
    
    Canale createCanale();
    
    Canale getCanale(int key) throws DataException;
    
    int getNumeroCanali() throws DataException;
    
    List<Canale> getListaCanali() throws DataException;
    
    List<Canale> getListaCanali(int page, int elements) throws DataException;
    
    List<Canale> getListaCanaliPaginated(int start_element, int elements) throws DataException;
    
    void storeCanale(Canale c) throws DataException;
    
    void deleteCanale(int key) throws DataException;
    
}
