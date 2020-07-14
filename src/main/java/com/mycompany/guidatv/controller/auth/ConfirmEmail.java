/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller.auth;

import com.mycompany.guidatv.utility.BCrypt;
import com.mycompany.guidatv.controller.BaseController;
import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.data.proxy.UtenteProxy;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.EmailTypes;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.io.IOException;
import java.time.LocalDate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Federico Di Menna
 */
public class ConfirmEmail extends BaseController {

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
        
        try {
            HttpSession s = SecurityLayer.checkSession(request);
            if(request.getParameter("verification_code") != null && request.getParameter("refer_code") != null){
                action_confirm_email(request, response);
            }
            else if(request.getParameter("resend") != null) {
                action_resend_email(request, response);
            }
            else {
                action_default(request, response);
            }
        } catch (Exception ex) {
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
    
    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException {
        
        HttpSession s = SecurityLayer.checkSession(request);
        if(s != null) {
            // Se l'utente è loggato gli mostro la pagina di conferma
            TemplateResult results = new TemplateResult(getServletContext());
            results.activate("confirmEmail.ftl.html", request, response);
        }
        else {
            // Altrimenti lo redireziono verso la pagina di login
            response.sendRedirect("login");
        }
        
    }
    
    private void action_confirm_email(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException, Exception {
        
        // Dentro verification_code ho l'hash del token
        String hashed_token = request.getParameter("verification_code");
        // Dentro refer_code ho l'hash dell'email
        String hashed_email = request.getParameter("refer_code");
        
        // Ottengo l'email dall'hash
        // Ho scelto una chiave statica per ottimizzare i tempi di ottenimento dell'email
        String real_email = SecurityLayer.decrypt(hashed_email, SecurityLayer.getStaticEncrypyionKey());
        Utente me = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(real_email);
        if(me == null) {
            response.sendRedirect("/GuidaTV/");
        }
        else {
            // Controllo se l'hash del token è valido per l'utente
            if(BCrypt.checkpw(me.getToken(), hashed_token)) {
                // Confermo l'email dell'utente
                me.setEmailVerifiedAt(LocalDate.now());
                ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);
                response.sendRedirect("profile");
            }
            else {
                response.sendRedirect("/GuidaTV/");
            }
        }
        
        
    }

    private void action_resend_email(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession s = SecurityLayer.checkSession(request);
        
        if(s != null) {
            UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
            me.setToken(UtilityMethods.generateNewToken(((GuidaTVDataLayer) request.getAttribute("datalayer"))));
            me.setExpirationDate(LocalDate.now().plusDays(1));
            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);
            UtilityMethods.sendEmailWithCodes(this.getServletContext().getInitParameter("files.directory") + "/links.txt", me, "Conferma la tua email cliccando sul link in basso", EmailTypes.CONFIRM_EMAIL);
            response.sendRedirect("confirmEmail");
        }
        else action_default(request, response);
        
    }


}
