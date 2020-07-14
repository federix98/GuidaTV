/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Federico Di Menna
 */
public class DataCache {
    
    public Map<Class, Map<Object, Object>> cache;
    
    public DataCache() {
        this.cache = new HashMap<>();
    }
    
    
    /**
     * Qualsiasi oggetto del model (cioè che estende DataItem) puo
     * essere contenuto nella mappa.
     * Se la cache non contiene l'oggetto creo la mappa
     * cache come <Classe, mappa>.
     * Altrimenti se la cache contiene la classe aggiungo l'oggetto, mi prendo
     * la coppia corrispondente <Classe, mappa> e aggiungo alla mappa l'oggetto 
     * del tipo <id, obj>.
     * 
     * @param <C>
     * @param c
     * @param o 
     */
    public <C extends DataItem> void add(Class<C> c, C o) {
        //Logger.getLogger("DataCache").log(Level.INFO, "Cache add: object of class {0} with key {1}", new Object[]{c.getName(), o.getKey()});
        if (!cache.containsKey(c)) {
            cache.put(c, new HashMap<>());
        }
        cache.get(c).put(o.getKey(), o);
    }
    
    
    /**
     * Controlla se nella mappa cache esiste la classe c e se quella sottomappa
     * ha un oggetto con quella chiave.
     * 
     * @param c
     * @param key
     * @return 
     */
    public boolean has(Class c, Object key) {
        //Logger.getLogger("DataCache").log(Level.INFO, "Cache lookup: object of class {0} with key {1}", new Object[]{c.getName(), key});
        return cache.containsKey(c) && cache.get(c).containsKey(key);
    }
    
    /**
     * Controlla se nella mappa cache esiste la Classe c e se quella sottomappa
     * corrispondente alla classe C ha l'oggetto con quella chiave
     * 
     * @param <C>
     * @param c
     * @param o
     * @return 
     */
    public <C extends DataItem> boolean has(Class<C> c, C o) {
        //Logger.getLogger("DataCache").log(Level.INFO, "Cache lookup: object of class {0} with key {1}", new Object[]{c.getName(), o.getKey()});
        return cache.containsKey(c) && cache.get(c).containsKey(o.getKey());
    }
    
    /**
     * Rimuove un oggetto del model dalla mappa. Controllo se la mappa contiene quell'oggetto.
     * Se lo contiene lo rimuovo
     * 
     * @param <C>
     * @param c
     * @param o 
     */
    public <C extends DataItem> void delete(Class<C> c, C o) {
        if (has(c, o.getKey())) {
            cache.get(c).remove(o.getKey());
        }
    }
    
    /**
     * Controllo se la mappa ha un oggetto di di Class c con
     * chiave key e se sì lo elimino.
     * 
     * @param c
     * @param key 
     */
    public void delete(Class c, Object key) {
        if (has(c, key)) {
            cache.get(c).remove(key);
        }
    }
    
    /**
     * Controllo se la mappa contiene un oggetto di tipo c con chiave key.
     * Se si restituisco l'oggetto, altrimenti torno null.
     * 
     * @param <C>
     * @param c
     * @param key
     * @return 
     */
    public <C extends DataItem> C get(Class<C> c, Object key) {
        if (has(c, key)) {
            //Logger.getLogger("DataCache").log(Level.INFO, "Cache hit: object of class {0} with key {1}", new Object[]{c.getName(), key});
            return (C) cache.get(c).get(key);
        } else {
            return null;
        }
    }
    
}
