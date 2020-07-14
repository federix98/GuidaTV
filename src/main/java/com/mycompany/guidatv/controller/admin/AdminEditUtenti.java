/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller.admin;

import com.mycompany.guidatv.utility.BCrypt;
import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.proxy.UtenteProxy;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import com.mycompany.guidatv.controller.BaseController;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.impl.UtenteImpl;
import com.mycompany.guidatv.data.model.interfaces.Ruolo;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.result.JSONResult;
import com.mycompany.guidatv.utility.Validator;

import java.io.IOException;
import java.time.LocalDate;
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
public class AdminEditUtenti extends BaseController {

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

        UtilityMethods.debugConsole(this.getClass(), "processRequest", "Sono in PR");

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
        // Sanitizzazione parametri vs XSS
        int draw = SecurityLayer.checkNumeric(request.getParameter("draw"));
        int start = SecurityLayer.checkNumeric(request.getParameter("start"));
        int length = SecurityLayer.checkNumeric(request.getParameter("length"));
        int total = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getNumeroUtenti();
        
        List<Utente> utenti = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtentiPaginated(start, length);
     
        UtilityMethods.debugConsole(this.getClass(), "paginated", "total " + total);
               
        JSONResult results = new JSONResult(getServletContext());
        request.setAttribute("draw", draw);
        request.setAttribute("total", String.valueOf(total));
        request.setAttribute("utenti", utenti);
        results.activate("/admin/json/dt_utenti.ftl.json", request, response);
        
    }

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {

        List<Utente> utenti = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtentiPaginated(0, 15);
        TemplateResult results = new TemplateResult(getServletContext());
        UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
        request.setAttribute("me", me);
        request.setAttribute("utenti_admin", utenti);
        request.setAttribute("outline_tpl", request.getServletContext().getInitParameter("view.outline_admin_template"));
        results.activate("/admin/pages/edit_utenti.ftl.html", request, response);

    }

    private void action_loginredirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void action_create(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, DataException {
        List<Ruolo> ruoli = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getRuoloDAO().getRuoli();
        
        TemplateResult results = new TemplateResult(getServletContext());
        request.setAttribute("outline_tpl", "");
        request.setAttribute("ruoli", ruoli);
        results.activate("/admin/partials/utente_form.ftl.html", request, response);
    }

    private void action_edit(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, DataException {
        int id_element = SecurityLayer.checkNumeric(request.getParameter("data_id"));
        Utente item = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(id_element);
        List<Ruolo> ruoli = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getRuoloDAO().getRuoli();
        
        
        TemplateResult results = new TemplateResult(getServletContext());
        request.setAttribute("item", item);
        request.setAttribute("ruoli", ruoli);
        request.setAttribute("outline_tpl", "");
        results.activate("/admin/partials/utente_form.ftl.html", request, response);
    }

    private void action_delete(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        // Controllo se l'id è reale
        JSONResult results = new JSONResult(getServletContext());
        try {
            Integer key = (Integer) Validator.validate(request.getParameter("data_id"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "ID");
            if (key == null) {
                throw new DataException("Invalid Key");
            }
            Utente u = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(key);
            if (u == null) {
                throw new DataException("Invalid Key");
            }

            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().deleteUtente(key);
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

    private void action_store(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        JSONResult results = new JSONResult(getServletContext());
        // CHECK SU TUTTI I CAMPI
        try {
            Integer key = (Integer) Validator.validate(request.getParameter("key"), new ArrayList<>(Arrays.asList(Validator.INTEGER)), "ID");
            String username = (String) Validator.validate(request.getParameter("username"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.STRING_NOT_EMPTY, Validator.STRING_WITHOUT_SPECIAL)), "nome");
            String email = (String) Validator.validate(request.getParameter("email"), new ArrayList<>(Arrays.asList(Validator.STRING_NOT_EMPTY, Validator.STRING_EMAIL)), "email");
            LocalDate data_nascita = (LocalDate) Validator.validate(request.getParameter("dataNascita"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.DATE)) , "data di nascita");
            Boolean sendEmail = (Boolean) Validator.validate(request.getParameter("sendEmail"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.BOOLEAN)), "send email" );
            String password = (String) Validator.validate(request.getParameter("password"), new ArrayList<>(Arrays.asList(Validator.PASSWORD)), "password" );
            LocalDate emailVerifiedAt = (LocalDate) Validator.validate(request.getParameter("verified"), new ArrayList<>(Arrays.asList(Validator.DATE)) , "data di nascita");
            Integer id_ruolo = (Integer) Validator.validate(request.getParameter("ruolo"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Ruolo");
            Utente target;
            
            if (key == null) {
                target = new UtenteImpl();
                if(password == null) throw new DataException("Password required on insert");
                if(email == null) throw new DataException("Email required on insert");
                target.setEmail(email);
                target.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            } else {
                target = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(key);
                if(target == null) throw new DataException("INVALID ID");
            }
            
            Ruolo user_role = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getRuoloDAO().getRuolo(id_ruolo);
            if(user_role == null) throw new DataException("Invalid role");
            
            target.setUsername(username);
            target.setEmailVerifiedAt(emailVerifiedAt);
            target.setDataNascita(data_nascita);
            target.setSendEmail(sendEmail);
            target.setRuolo(user_role);
            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(target);
            

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
