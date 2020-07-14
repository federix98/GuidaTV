/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data.dao;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Ruolo;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public interface RuoloDAO {
    
    Ruolo createRuolo();
    
    Ruolo getRuoloUtente(int UtenteKey) throws DataException;
    
    Ruolo getRuolo(int key) throws DataException;
    
    List<Ruolo> getRuoli() throws DataException;
    
}
