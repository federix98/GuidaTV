/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data;

/**
 *
 * @author Federico Di Menna
 * @param <KT> key type
 * 
 * L'interfaccia DataItem viene utilizzata per implementare
 * una classe (DataItemImpl) che sarà ereditata da tutti gli oggetti del model
 * e conterrà i metodi che condividono tutti essi. 
 */
public interface DataItem<KT> extends Comparable {
    
    KT getKey();
    
    long getVersion();
    
    void setKey(KT key);
    
    void setVersion(long version);
    
}
