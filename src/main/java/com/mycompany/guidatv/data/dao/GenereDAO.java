/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Genere;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface GenereDAO {
    
    Genere createGenere();
    
    //RETURN GENERE FROM KEY
    Genere getGenere(int key) throws DataException;
    
    int getNumeroGeneri() throws DataException;
    
    List<Genere> getGeneriPaginated(int start_item, int elements) throws DataException;
    
    List<Genere> getGeneri() throws DataException;
    
    void storeGenere(Genere g) throws DataException;
    
    void deleteGenere(int key) throws DataException;
}
