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
 */
public class DataItemImpl<KT extends Comparable<KT>> implements DataItem<KT>{

    private KT key;
    private long version;
    
    public DataItemImpl() {
        version = 0;
    }
    
    @Override
    public KT getKey() {
        return key;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setKey(KT key) {
        this.key = key;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof DataItemImpl) {
            return this.compareTo(o);
        }
        return 0;
    }
    
}
