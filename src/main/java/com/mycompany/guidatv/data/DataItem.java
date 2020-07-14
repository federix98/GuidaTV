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
 * e conterrà i metodi che condividono tutti essi. Ovvero
 * Getter e Setter della chiave e della versione. 
 * Viene definita utilizzando i generics in modo che 
 * per ogni oggetto del model posso definire uno specifico
 * tipo di chiave.
 */
public interface DataItem<KT> extends Comparable {
    
    KT getKey();
    
    long getVersion();
    
    void setKey(KT key);
    
    void setVersion(long version);
    
}
