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
import com.mycompany.guidatv.data.model.impl.UtenteImpl;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.EmailTypes;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.io.IOException;
import java.time.LocalDate;
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
public class Register extends BaseController {

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
            if(request.getParameter("submit") != null){
                action_register(request, response);
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
            response.sendRedirect("profile");
        }
        else {
            TemplateResult results = new TemplateResult(getServletContext());
            results.activate("register.ftl.html", request, response);
        }
        
    }
    
    private void action_register(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException, Exception {
        
        String username = ( request.getParameter("username") != null) ? request.getParameter("username") : "";
        String email = ( request.getParameter("email") != null) ? request.getParameter("email") : "";
        String password = ( request.getParameter("password") != null) ? request.getParameter("password") : "";
        String confirm = ( request.getParameter("confirm_password") != null) ? request.getParameter("confirm_password") : "";
        String accepted = ( request.getParameter("agree") != null) ? request.getParameter("agree") : "";
        LocalDate data_nascita = LocalDate.parse(request.getParameter("data_nascita"));
        boolean valid = true;
        String error_msg = "";
        
        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirm.isEmpty() && data_nascita != null && accepted.equals("1")) {
            
            
            // Sanitizzo tutti i campi e controllo se i campi sono validi
            username = SecurityLayer.removeSpecialChars(username);
            if(!SecurityLayer.isEmailValid(email)) {
                error_msg += "Email non valida\n";
                valid = false;
            }
            if(!password.equals(confirm)) {
                error_msg += "Le password non combaciano\n";
                valid = false;
            }
            if(!data_nascita.isBefore(LocalDate.now())) {
                error_msg += "Data di nascita non valida\n";
                valid = false;
            }
            
            // Controllo se l'email e l'username sono presenti nel DB
            Utente exists_email = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(email);
            Utente exists_username = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtenteByUsername(username);
            if(exists_email != null || exists_username != null) {
                error_msg += "Email o username non disponibili\n";
                valid = false;
            }
            
            
            //UtilityMethods.debugConsole(this.getClass(), "action_login", "pass: " + my_pass + "  " + password);
            //UtilityMethods.debugConsole(this.getClass(), "action_login", "pass: " + BCrypt.hashpw("passpass", BCrypt.gensalt()));
            
            if(valid) {
                Utente me = new UtenteImpl();
                me.setUsername(username);
                me.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                me.setEmail(email);
                me.setDataNascita(data_nascita);
                me.setRuolo(((GuidaTVDataLayer) request.getAttribute("datalayer")).getRuoloDAO().getRuolo(2));
                me.setToken(UtilityMethods.generateNewToken(((GuidaTVDataLayer) request.getAttribute("datalayer"))));
                me.setExpirationDate(LocalDate.now().plusDays(1));
                ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);

                if(me.getKey() == 0) {
                    error_msg += "Errore nell'inserimento dell'utente\n";
                    valid = false;
                }
                else {
                    // Invio email e faccio redirect
                    
                    // Logica di invio email mancante, stampo su file. Try with resource sul buffer
                    
                    UtilityMethods.sendEmailWithCodes(this.getServletContext().getInitParameter("files.directory") + "/links.txt", me, "Conferma la tua email cliccando sul link in basso", EmailTypes.CONFIRM_EMAIL);
                    // redirect
                    if (request.getParameter("referrer") != null) {
                        response.sendRedirect(request.getParameter("referrer"));
                    } else {
                        response.sendRedirect("login");
                    }
                }
                
            }
            
        }
        else {
            error_msg = "Compila correttamente tutti i campi e accetta i termini e condizioni.";
            valid = false;
        }
        
        if(!valid) {
            request.setAttribute("error", error_msg);
            action_default(request, response);                
        }
        
    }


}
