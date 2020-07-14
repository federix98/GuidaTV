/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller.admin;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.proxy.UtenteProxy;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import com.mycompany.guidatv.controller.BaseController;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.impl.ProgrammazioneImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.result.JSONResult;
import com.mycompany.guidatv.utility.Validator;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Federico Di Menna
 */
public class AdminEditProgrammazioni extends BaseController {

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
            throws ServletException {
        response.setContentType("text/html;charset=UTF-8");

        //UtilityMethods.debugConsole(this.getClass(), "processRequest", "Sono in PR");
        UtilityMethods.debugConsole(this.getClass(), "PR", "day: " + request.getParameter("day"));
        try {
            boolean is_admin = SecurityLayer.checkAdminSession(request);

            if (is_admin) {
                //UtilityMethods.debugConsole(this.getClass(), "action_sendEmail", "default");
                if (request.getParameter("draw") != null) {
                    action_paginate_results(request, response);
                } else if (request.getParameter("insert") != null) {
                    action_create(request, response);
                } else if (request.getParameter("edit") != null) {
                    action_edit(request, response);
                } else if (request.getParameter("delete") != null) {
                    action_delete(request, response);
                } else if (request.getParameter("store") != null) {
                    action_store(request, response);
                } else {
                    action_default(request, response);
                }

            } else {
                action_loginredirect(request, response);
            }
        } catch (IOException | DataException | TemplateManagerException ex) {
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

    private void action_paginate_results(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {
        //UtilityMethods.debugConsole(this.getClass(), "paginated", "draw content: " + request.getParameter("draw"));
        //UtilityMethods.debugConsole(this.getClass(), "paginated", "start content: " + request.getParameter("start"));
        //UtilityMethods.debugConsole(this.getClass(), "paginated", "length content: " + request.getParameter("length"));
        UtilityMethods.debugConsole(this.getClass(), "paginated", "day: " + request.getParameter("day"));
        
        // Sanitizzazione parametri vs XSS
        int draw = SecurityLayer.checkNumeric(request.getParameter("draw"));
        int start = SecurityLayer.checkNumeric(request.getParameter("start"));
        int length = SecurityLayer.checkNumeric(request.getParameter("length"));
        int total = 0;
        List<Programmazione> programmazioni = null;
        
        LocalDate day = null;
        if(request.getParameter("day") != null) day = (LocalDate) Validator.validate(request.getParameter("day"), new ArrayList<>(Arrays.asList(Validator.DATE)), "Selected day");
        if(day == null) {
            programmazioni = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazioniPaginated(start, length);
            total = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getNumeroProgrammazioni();
        } 
        else {
            programmazioni = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazioniPaginated(day, day, start, length);
            total = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getNumeroProgrammazioni(day, day);
        }
        
        if(day != null) UtilityMethods.debugConsole(this.getClass(), "paginated", "target day " + day.toString());
        //UtilityMethods.debugConsole(this.getClass(), "paginated", "total " + total);

        JSONResult results = new JSONResult(getServletContext());
        request.setAttribute("draw", draw);
        request.setAttribute("total", String.valueOf(total));
        request.setAttribute("programmazioni", programmazioni);
        request.setAttribute("day", day);
        results.activate("/admin/json/dt_programmazioni.ftl.json", request, response);

    }

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {

        List<Programmazione> programmazioni = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazioniPaginated(0, 3);
        TemplateResult results = new TemplateResult(getServletContext());
        UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
        request.setAttribute("me", me);
        request.setAttribute("programmazioni_admin", programmazioni);
        request.setAttribute("outline_tpl", request.getServletContext().getInitParameter("view.outline_admin_template"));
        results.activate("/admin/pages/edit_programmazioni.ftl.html", request, response);

    }

    private void action_loginredirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void action_create(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, DataException {
        TemplateResult results = new TemplateResult(getServletContext());
        List<Programma> programmi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgrammi();
        
        // Canali ce l'ho in BaseController;
        request.setAttribute("programmi", programmi);
        request.setAttribute("outline_tpl", "");
        results.activate("/admin/partials/programmazione_form.ftl.html", request, response);
    }

    private void action_edit(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {
        Integer key = (Integer) Validator.validate(request.getParameter("data_id"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "ID");
        if (key == null) {
            throw new DataException("Invalid Key");
        }
        Programmazione item = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazione(key);
        List<Programma> programmi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgrammi();
        
        request.setAttribute("programmi", programmi);
        TemplateResult results = new TemplateResult(getServletContext());
        request.setAttribute("item", item);
        request.setAttribute("outline_tpl", "");
        results.activate("/admin/partials/programmazione_form.ftl.html", request, response);
    }
    
    private void action_store(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        JSONResult results = new JSONResult(getServletContext());
        // CHECK SU TUTTI I CAMPI
        try {

            Integer key = (Integer) Validator.validate(request.getParameter("key"), new ArrayList<>(Arrays.asList(Validator.INTEGER)), "ID");
            Integer id_canale = (Integer) Validator.validate(request.getParameter("canale"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Canale");
            Integer id_programma = (Integer) Validator.validate(request.getParameter("programma"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Programma");
            LocalDateTime start = (LocalDateTime) Validator.validate(request.getParameter("start"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.DATETIME)), "Start Time");
            Integer durata = (Integer) Validator.validate(request.getParameter("durata"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Durata");
            
            Canale c = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getCanale(id_canale);
            Programma p = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(id_programma);
            
            Programmazione target;
            if (key == null) {
                // INSERT NEW PROGRAMMAZIONE
                target = new ProgrammazioneImpl();
            } else {
                // UPDATE PROGRAMMAZIONE
                target = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazione(key);
            }
            
            target.setProgramma(p);
            target.setCanale(c);
            target.setStartTime(start);
            target.setDurata(durata);
            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().storeProgrammazione(target);
            
            request.setAttribute("errors", "");
            request.setAttribute("success", "true");
            results.activate("/admin/json/store_response.ftl.json", request, response);
        } catch (DataException ex) {
            // GESTISCO IN MODO DIVERSO L'ECCEZIONE
            request.setAttribute("errors", ex.getMessage());
            request.setAttribute("success", "false");
            results.activate("/admin/json/store_response.ftl.json", request, response);
        }
    }

    private void action_delete(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        // Controllo se l'id è reale
        JSONResult results = new JSONResult(getServletContext());
        try {
            Integer key = (Integer) Validator.validate(request.getParameter("data_id"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "ID");
            if (key == null) {
                throw new DataException("Invalid Key");
            }
            Programmazione p = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazione(key);
            if (p == null) {
                throw new DataException("Invalid Key");
            }

            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().deleteProgrammazione(key);
            request.setAttribute("errors", "");
            request.setAttribute("success", "true");
            results.activate("/admin/json/store_response.ftl.json", request, response);
        } catch (DataException ex) {
            // GESTISCO IN MODO DIVERSO L'ECCEZIONE
            request.setAttribute("errors", ex.getMessage());
            request.setAttribute("success", "false");
            results.activate("/admin/json/store_response.ftl.json", request, response);
        }

    }


}
