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
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import com.mycompany.guidatv.utility.Validator;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Federico Di Menna
 */
public class PalinsestoCanale extends BaseController {

    
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
        try {
            if(request.getParameter("c_key") != null) {
                action_default(request, response);
            } else {
                action_without_parameter(request, response);
            }
        } catch (TemplateManagerException | DataException |IOException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);

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
        Integer id_canale = (Integer) Validator.validate(request.getParameter("c_key"), new ArrayList<>(Arrays.asList(Validator.INTEGER)), "Canale");
        Integer fascia = (Integer) Validator.validate(request.getParameter("fascia"), new ArrayList<>(Arrays.asList(Validator.INTEGER)), "Fascia");
        if(fascia == null || fascia < 0 || fascia > 4) fascia = 0;
        LocalTime start_time = UtilityMethods.getOrarioInizioFascia(fascia);
        LocalTime end_time = UtilityMethods.getOrarioFineFascia(fascia);
        //UtilityMethods.debugConsole(this.getClass(), "action_get_by_fascia", "Sono in palinsesto post fascia");
        
        try {
            TemplateResult results = new TemplateResult(getServletContext());
            
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            LocalDate day = null;
            if(request.getParameter("day") != null) day = (LocalDate) Validator.validate(request.getParameter("day"), new ArrayList<>(Arrays.asList(Validator.DATE)), "Selected day");
            if(day == null) {
                day = LocalDate.now();
            }
            
            start = day.atTime(start_time);
            end = day.atTime(end_time);
            
            
            
            Canale c = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getCanale(id_canale);
            List<Programmazione> palinsesto = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazione(id_canale, start, end);
            
            if((boolean) request.getAttribute("logged")) UtilityMethods.filterResults(palinsesto, UtilityMethods.getMe(request));
            
            request.setAttribute("day", day);
            request.setAttribute("programmazioni", palinsesto);
            request.setAttribute("canale", c);
            request.setAttribute("fascia", fascia);
            request.setAttribute("start", start.format(DateTimeFormatter.ofPattern("HH:mm")));
            request.setAttribute("end", end.format(DateTimeFormatter.ofPattern("HH:mm")));
            results.activate("palinsesto_canale.ftl.html", request, response);
        } catch (DataException ex) {
            request.setAttribute("message", "Data access exception: " + ex.getMessage());
            action_error(request, response);
        }
    }

    private void action_without_parameter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/palinsesto");
    }

}
