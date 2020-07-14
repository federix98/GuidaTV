/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller;

import com.mycompany.guidatv.controller.auth.Login;
import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Interesse;
import com.mycompany.guidatv.data.proxy.InteresseProxy;
import com.mycompany.guidatv.data.proxy.UtenteProxy;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Federico Di Menna
 */
public class Profile extends BaseController {

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
        request.setAttribute("area", 3);
        try {
            HttpSession s = SecurityLayer.checkSession(request);
            
            if(s != null) {
                // Se l'utente non ha verificato l'email non puo accedere al suo profilo
                if(UtilityMethods.getMe(request).getEmailVerifiedAt() == null) {
                    action_redirectConfirm(request, response);
                }
                else {
                    if(request.getParameter("submit") != null) {
                        //UtilityMethods.debugConsole(this.getClass(), "action_sendEmail", "email");
                        action_updatePreferences(request, response);
                    }
                    else {
                        //UtilityMethods.debugConsole(this.getClass(), "action_sendEmail", "default");
                        action_default(request, response);
                    }
                }
            }
            else {
                action_loginredirect(request, response);
            }
        }  catch (IOException | DataException | TemplateManagerException ex) {
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
        
        // Mi stampa la pagina di log in
        TemplateResult results = new TemplateResult(getServletContext());
        UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
        request.setAttribute("me", me);
        results.activate("profile.ftl.html", request, response);
        
    }
    
    private void action_loginredirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("login");
    }

    private void action_updatePreferences(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {
        
        boolean valid = true;
        String error = "";
        
        if(!SecurityLayer.checkBoolean(request.getParameter("send_email"))) {
            error += "Invalid input";
            valid = false;
        }
        else {
            boolean send = (request.getParameter("send_email").equals("1"));
            List<Integer> id_canali = null;
            List<Integer> id_fasce = null;
            
            if(request.getParameterValues("canali") != null && !request.getParameter("canali").isEmpty()) {
                    id_canali = new ArrayList<>();
                    for( String c : request.getParameterValues("canali")) {
                        if(c != null) id_canali.add(SecurityLayer.checkNumeric(c));
                    }
                    if(id_canali.contains(0)) id_canali = null;
            }
            
            if(request.getParameterValues("fasce") != null && !request.getParameter("fasce").isEmpty()) {
                id_fasce = new ArrayList<>();
                for( String f : request.getParameterValues("fasce")) {
                    if(f != null) id_fasce.add(SecurityLayer.checkNumeric(f));
                }
            }
            if(id_canali != null && id_fasce != null) {
                UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
                me.setSendEmail(send);

                me.cleanInteressi();
                List<Interesse> update_interessi = new ArrayList<>();
                for(Integer c_id : id_canali) {
                    if(id_fasce.contains(0)) {
                        
                        Interesse to_add = new InteresseProxy((DataLayer) request.getAttribute("datalayer"));
                        to_add.setCanale(((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getCanale(c_id));
                        to_add.setUtente(me);
                        to_add.setStartTime(UtilityMethods.getOrarioInizioFascia(0));
                        to_add.setEndTime(UtilityMethods.getOrarioFineFascia(0));
                        UtilityMethods.debugConsole(this.getClass(), "update", UtilityMethods.getOrarioInizioFascia(0).toString());
                        update_interessi.add(to_add);
                        ((GuidaTVDataLayer) request.getAttribute("datalayer")).getInteresseDAO().storeInteresse(to_add);
                    }
                    else {
                        for(Integer f_id : id_fasce) {
                            Interesse to_add = new InteresseProxy((DataLayer) request.getAttribute("datalayer"));
                            to_add.setCanale(((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getCanale(c_id));
                            to_add.setUtente(me);
                            to_add.setStartTime(UtilityMethods.getOrarioInizioFascia(f_id));
                            to_add.setEndTime(UtilityMethods.getOrarioFineFascia(f_id));
                            update_interessi.add(to_add);
                            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getInteresseDAO().storeInteresse(to_add);
                        }
                    }
                }
                me.setInteressi(update_interessi);
                ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);
            }
            else {
                if(send) {
                    // AGGIORNO SOLTANTO EMAIL
                    UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
                    me.setSendEmail(send);
                    ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);
                }
            }
            
        }
        
        
        if(!valid) request.setAttribute("error", error);
        action_default(request, response);
    }

    private void action_redirectConfirm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("confirmEmail");
    }
}
