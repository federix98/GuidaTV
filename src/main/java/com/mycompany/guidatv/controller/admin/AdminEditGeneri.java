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
import com.mycompany.guidatv.data.model.impl.CanaleImpl;
import com.mycompany.guidatv.data.model.impl.GenereImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Genere;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.result.JSONResult;
import com.mycompany.guidatv.utility.Validator;
import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author Federico Di Menna
 */
public class AdminEditGeneri extends BaseController {

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

        

        try {
            boolean is_admin = SecurityLayer.checkAdminSession(request);
            if (is_admin) {
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
        int total = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getNumeroGeneri();
        List<Genere> generi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGeneriPaginated(start, length);

        JSONResult results = new JSONResult(getServletContext());
        request.setAttribute("draw", draw);
        request.setAttribute("total", String.valueOf(total));
        request.setAttribute("generi", generi);
        results.activate("/admin/json/dt_generi.ftl.json", request, response);

    }

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {

        List<Genere> generi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGeneriPaginated(0, 15);
        TemplateResult results = new TemplateResult(getServletContext());
        UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
        request.setAttribute("me", me);
        request.setAttribute("generi_admin", generi);
        request.setAttribute("outline_tpl", request.getServletContext().getInitParameter("view.outline_admin_template"));
        results.activate("/admin/pages/edit_generi.ftl.html", request, response);

    }

    private void action_loginredirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void action_create(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        TemplateResult results = new TemplateResult(getServletContext());
        request.setAttribute("outline_tpl", "");
        results.activate("/admin/partials/genere_form.ftl.html", request, response);
    }

    private void action_edit(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, DataException {
        int id_element = SecurityLayer.checkNumeric(request.getParameter("data_id"));
        Genere item = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGenere(id_element);
        
        TemplateResult results = new TemplateResult(getServletContext());
        request.setAttribute("item", item);
        request.setAttribute("outline_tpl", "");
        results.activate("/admin/partials/genere_form.ftl.html", request, response);
    }

    private void action_delete(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        // Controllo se l'id è reale
        JSONResult results = new JSONResult(getServletContext());
        try {
            Integer key = (Integer) Validator.validate(request.getParameter("data_id"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "ID");
            if (key == null) {
                throw new DataException("Invalid Key");
            }
            Genere g = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGenere(key);
            if (g == null) {
                throw new DataException("Invalid Key");
            }

            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().deleteGenere(key);
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
        request.getParameter("nome");
        UtilityMethods.debugConsole(this.getClass(), "action_store", request.getParameter("nome"));
        try {
            Integer key = (Integer) Validator.validate(request.getParameter("key"), new ArrayList<>(Arrays.asList(Validator.INTEGER)), "ID");
            String nome = (String) Validator.validate(request.getParameter("nome"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.STRING_NOT_EMPTY, Validator.STRING_WITHOUT_SPECIAL)), "Nome");
            String descrizione = (String) Validator.validate(request.getParameter("descrizione"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.STRING_NOT_EMPTY, Validator.STRING_WITHOUT_SPECIAL)), "Descrizione");
            Genere target;
            if (key == null) {
                target = new GenereImpl();
            } else {
                target = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGenere(key);
            }
            
            target.setNome(nome);
            target.setDescrizione(descrizione);
            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().storeGenere(target);
            

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
