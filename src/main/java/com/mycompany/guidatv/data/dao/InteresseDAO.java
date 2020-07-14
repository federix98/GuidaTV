/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Interesse;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface InteresseDAO {
    
    Interesse createInteresse();
    
    Interesse getInteresse(int id_interesse) throws DataException;
    
    List<Interesse> getInteressiUtente(Utente utente) throws DataException;
    
    void storeInteresse(Interesse interesse) throws DataException;
    
    boolean removeInteresse(int id_interesse) throws DataException;
    
}
