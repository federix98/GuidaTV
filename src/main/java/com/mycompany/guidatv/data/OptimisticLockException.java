package com.mycompany.guidatv.data;

/**
 *
 * @author Federico Di Menna
 * 
 */
public class OptimisticLockException extends DataException {

    private DataItem item;

    public OptimisticLockException(DataItem item) {
        super("Version mismatch (optimistic locking) for instance " + item.getKey() + " of class " + item.getClass().getCanonicalName());
        this.item = item;
    }

    public DataItem getItem() {
        return item;
    }

    public void setItem(DataItem item) {
        this.item = item;
    }

}
