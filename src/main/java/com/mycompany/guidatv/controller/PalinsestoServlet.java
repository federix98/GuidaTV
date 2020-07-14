/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import com.mycompany.guidatv.utility.Validator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Federico Di Menna
 */
public class PalinsestoServlet extends BaseController {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     */
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException{
        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("area", 2);
        int fascia = 1;
        
        try {
            if (request.getParameter("fascia") != null) {
                fascia = SecurityLayer.checkNumeric(request.getParameter("fascia"));
                if (fascia >= 1 && fascia <= 4) {
                    // Se la fascia non è valida la lascio a 1 (Mattina) altrimenti eseguo la action get_by_fascia
                    action_get_by_fascia(request, response, fascia);
                }
            } else {
                action_default(request, response);
            }
        } catch (TemplateManagerException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);

        } catch (DataException ex) {
            Logger.getLogger(PalinsestoServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void action_error(HttpServletRequest request, HttpServletResponse response) {
        if (request.getAttribute("exception") != null) {
            (new FailureResult(getServletContext())).activate((Exception) request.getAttribute("exception"), request, response);
        } else {
            (new FailureResult(getServletContext())).activate((String) request.getAttribute("message"), request, response);
        }
        return;
    }
    
    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {
        
        action_get_by_fascia(request, response, 1);
        
    }


    private void action_get_by_fascia(HttpServletRequest request, HttpServletResponse response, int fascia) throws TemplateManagerException, DataException {
        //UtilityMethods.debugConsole(this.getClass(), "action_get_by_fascia", "Sono in palinsesto post fascia");
        
        int elements = 5, page = 0;
        if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
            page = SecurityLayer.checkNumeric(request.getParameter("page"));
        }
        
        try {
            TemplateResult results = new TemplateResult(getServletContext());
            
            LocalDate day = null;
            if(request.getParameter("day") != null) day = (LocalDate) Validator.validate(request.getParameter("day"), new ArrayList<>(Arrays.asList(Validator.DATE)), "Selected day");
            if(day == null) {
                day = LocalDate.now();
            }
            
            
            String day_target = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String start_str, end_str;
            
            switch(fascia) {
                case 1: {
                    start_str = day_target + " 06:00";
                    end_str = day_target + " 12:00";
                    break;
                }
                case 2: {
                    start_str = day_target + " 12:00";
                    end_str = day_target + " 18:00";
                    break;
                }
                case 3: {
                    start_str = day_target + " 18:00";
                    end_str = day_target + " 23:59";
                    break;
                }
                case 4: {
                    start_str = day_target + " 00:00";
                    end_str = day_target + " 06:00";
                    break;
                }
                default: {
                    // anche se non dovrebbe mai verificarsi lo metto per sicurezza. Setto mattina
                    start_str = day_target + " 06:00";
                    end_str = day_target + " 12:00";
                    break;
                }
            }
            
            // Converto le stinghe in LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); 
            LocalDateTime start = LocalDateTime.parse(start_str, formatter);
            LocalDateTime end = LocalDateTime.parse(end_str, formatter);;

            
            Map<Canale, List<Programmazione>> palinsesto = new TreeMap<>();
            
            for(Canale c : ((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getListaCanali(page, elements)) {
                List<Programmazione> programmazione = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazione(c.getKey(), start, end);
                if((boolean) request.getAttribute("logged")) UtilityMethods.filterResults(programmazione, UtilityMethods.getMe(request));
                palinsesto.put(c, programmazione);
            }
            
            
            
            
            // PAGINATION INFO
            request.setAttribute("numero_pagine", (int)(Math.ceil(((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getNumeroCanali() / elements)));
            request.setAttribute("pagina", page);
            
            // PALINSESTO INFO
            request.setAttribute("palinsesto", palinsesto);
            request.setAttribute("fascia", fascia);
            request.setAttribute("nome_fascia", UtilityMethods.getNomeFascia(fascia));
            request.setAttribute("day", day_target);
            request.setAttribute("start", start.format(DateTimeFormatter.ofPattern("HH:mm")));
            request.setAttribute("end", end.format(DateTimeFormatter.ofPattern("HH:mm")));
            results.activate("palinsesto.ftl.html", request, response);
        } catch (DataException ex) {
            request.setAttribute("message", "Data access exception: " + ex.getMessage());
            action_error(request, response);
        }
    }

}
