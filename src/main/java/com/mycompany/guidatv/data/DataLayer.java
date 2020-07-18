/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author Federico Di Menna
 * 
 * AutoCloseable server per try with resource
 */
public class DataLayer implements AutoCloseable {
    
    private DataSource ds = null; 
    private Connection connection = null;
    private Map<Class, DAO> daos = null;
    private DataCache cache = null;
    
    public DataLayer(DataSource datasource) throws SQLException {
        ds = datasource;
        connection = ds.getConnection();
        daos = new HashMap<>();
        cache = new DataCache();
    }
    
    public void init() throws DataException {
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException ex) {
            //
        }
    }
    
    public void registerDAO(Class dao_c, DAO dao) throws DataException {
        daos.put(dao_c, dao);
        dao.init();
    }
    
    public DAO getDAO(Class daoclass) {
        return daos.get(daoclass);
    }
    
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        destroy();
    }
    
    public DataCache getCache() {
        return this.cache;
    }
    
    
}
