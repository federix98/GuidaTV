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
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import com.mycompany.guidatv.utility.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Federico Di Menna
 */
public class Login extends BaseController {

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
            
            if(request.getParameter("submit") != null ) {
                action_login(request, response);
            }
            else {
                action_default(request, response);
            }
        } catch (DataException | IOException | TemplateManagerException ex) {
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
    }
    
    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException {
        
        HttpSession s = SecurityLayer.checkSession(request);
        if(s != null) {
            response.sendRedirect("profile");
        }
        else {
            // Mi stampa la pagina di log in
            if(request.getParameter("send") != null) request.setAttribute("message", "Email inviata. Controlla la tua casella di posta elettronica.");
            else if(request.getParameter("newpass") != null) request.setAttribute("message", "Password ripristinata correttamente, accedi con le tue nuove credenziali.");
            else if(request.getParameter("error") != null) {
                String val = request.getParameter("error");
                String message = "Errore generico";
                if(val.equals("invalid")) message = "Errore: Invalid Data.";
                if(val.equals("exp")) message = "Errore: Token Expired.";
                request.setAttribute("error", message);
            }
            TemplateResult results = new TemplateResult(getServletContext());
            results.activate("login.ftl.html", request, response);
        }
        
        
    }
    
    // Effettua il login e mi redireziona sul profilo
    private void action_login(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException {
        
        String email = (String) Validator.validate(request.getParameter("email"), new ArrayList<>(Arrays.asList(Validator.STRING_NOT_EMPTY, Validator.STRING_EMAIL)), "email");
        String password = (String) request.getParameter("password");
        
        if (request.getParameter("email") != null && request.getParameter("password") != null && !email.isEmpty() && !password.isEmpty()) { 
            String my_pass = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getPassword(email);
            if(my_pass == null) {
                // Email non trovata
                request.setAttribute("error", "Credenziali non corrette.");
                action_default(request, response);
            }
            //UtilityMethods.debugConsole(this.getClass(), "action_login", "pass: " + my_pass + "  " + password);
            //UtilityMethods.debugConsole(this.getClass(), "action_login", "action: " + BCrypt.checkpw(password, my_pass));
            //UtilityMethods.debugConsole(this.getClass(), "action_login", "pass: " + BCrypt.hashpw("passpass", BCrypt.gensalt()));
            
            if(BCrypt.checkpw(password, my_pass)) {
                // Credenziali corrette
                Utente me = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(email);
                SecurityLayer.createSession(request, me.getUsername(), me.getKey());
                if(me.getEmailVerifiedAt() == null) {
                    response.sendRedirect("confirmEmail");
                }
                else if (request.getParameter("referrer") != null) {
                    response.sendRedirect(request.getParameter("referrer"));
                } 
                else {
                    response.sendRedirect("profile");
                }
            }
            else {
                request.setAttribute("error", "Credenziali non corrette.");
                action_default(request, response);
            }
        }
        else {
            request.setAttribute("error", "Compila tutti i campi");
            action_default(request, response);
        }
        
        
    }

}
