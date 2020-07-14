/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller;

import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author Federico Di Menna
 */
public abstract class BaseController extends HttpServlet {
    
    // INITIALIZE THE DATASOURCE WITH CONNECTION POOLING
    @Resource(name = "jdbc/webeng_proj")
    private DataSource ds;
    
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException;

    private void processBaseRequest(HttpServletRequest request, HttpServletResponse response) {
        try (GuidaTVDataLayer datalayer = new GuidaTVDataLayer(ds)) {
            datalayer.init();
            request.setAttribute("datalayer", datalayer);
            
            /**
             * Link build utility
             */
            request.setAttribute("base_path", request.getContextPath());
            
            /**
             * Parametri per la ricerca
             */
            request.setAttribute("canali", datalayer.getCanaleDAO().getListaCanali());
            request.setAttribute("generi", datalayer.getGenereDAO().getGeneri());
            request.setAttribute("min_date", LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            request.setAttribute("max_date", LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            /**
             * Navbar Management
             */
            request.setAttribute("area", 1);
            boolean logged = ( SecurityLayer.checkSession(request) != null && request.isRequestedSessionIdValid() && !request.getSession(false).isNew());
            request.setAttribute("logged", logged);
            request.setAttribute("admin_session", SecurityLayer.checkAdminSession(request));
            if( logged ) {
                request.setAttribute("me", UtilityMethods.getMe(request));
            }
            
            /**
             * Other Utilities
             */
            // ULTIME PROGRAMMAZIONI INSERITE
            List<Programmazione> latest = datalayer.getProgrammazioneDAO().getLatest(4);
            request.setAttribute("latest", latest);
            
            // GIORNI DELLA SETTIMANA
            List<LocalDate> week = new ArrayList();
            for(int i = 0; i <= 7; i++) week.add(LocalDate.now().plusDays(i));
            request.setAttribute("week", week);
            
            
            //System.out.println("Inizializzo DataLayer: " + datalayer);
            processRequest(request, response);
        } catch (Exception ex) {
            ex.printStackTrace(); //for debugging only
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processBaseRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processBaseRequest(request, response);
    }

    
}
