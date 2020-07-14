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
import com.mycompany.guidatv.utility.EmailTypes;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import com.mycompany.guidatv.utility.Validator;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Federico Di Menna
 */
public class ForgotPassword extends BaseController {

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
            if (request.getParameter("verification_code") != null && request.getParameter("refer_code") != null) {
                if (request.getParameter("submit") != null && request.getParameter("password") != null) {
                    action_restorePassword(request, response);
                } else {
                    action_showRestorePassword(request, response);
                }
            } else if (request.getParameter("submit") != null && request.getParameter("email") != null) {
                action_send_email(request, response);
            } else {
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

        TemplateResult results = new TemplateResult(getServletContext());
        request.setAttribute("req_email", true);
        results.activate("forgot_password.ftl.html", request, response);

    }

    private void action_showRestorePassword(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException, Exception {
        TemplateResult results = new TemplateResult(getServletContext());
        // Dentro verification_code ho l'hash del token
        String hashed_token = request.getParameter("verification_code");
        // Dentro refer_code ho l'hash dell'email
        String hashed_email = request.getParameter("refer_code");

        // Ottengo l'email dall'hash
        // Ho scelto una chiave statica per ottimizzare i tempi di ottenimento dell'email
        String real_email = SecurityLayer.decrypt(hashed_email, SecurityLayer.getStaticEncrypyionKey());
        Utente me = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(real_email);
        if (me == null) {
            response.sendRedirect("login?error=invalid");
        } else {
            // Controllo se l'hash del token è valido per l'utente
            if (BCrypt.checkpw(me.getToken(), hashed_token)) {
                // Confermo l'email dell'utente
                if (me.getExpirationDate() != null && !me.getExpirationDate().isBefore(LocalDate.now())) {
                    me.setToken(UtilityMethods.generateNewToken(((GuidaTVDataLayer) request.getAttribute("datalayer"))));
                    me.setExpirationDate(LocalDate.now().plusDays(1));
                    ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);
                    request.setAttribute("refer_code", SecurityLayer.encrypt(me.getEmail(), SecurityLayer.getStaticEncrypyionKey()));
                    request.setAttribute("verification_code", BCrypt.hashpw(me.getToken(), BCrypt.gensalt()));
                    request.setAttribute("token", me.getToken());
                    request.setAttribute("req_pass", true);
                    results.activate("forgot_password.ftl.html", request, response);
                } else {
                    response.sendRedirect("login?error=exp");
                }

            } else {
                response.sendRedirect("login?error=exp");
            }
        }

    }

    private void action_restorePassword(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException, Exception {

        String password = (String) Validator.validate(request.getParameter("password"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.PASSWORD)), "password");
        String confirm = (String) Validator.validate(request.getParameter("confirm"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.PASSWORD)), "confirm");
        String token = (String) Validator.validate(request.getParameter("token"), new ArrayList<>(Arrays.asList(Validator.REQUIRED)), "token");
        String hashed_email = (String) Validator.validate(request.getParameter("refer_code"), new ArrayList<>(Arrays.asList(Validator.REQUIRED)), "refer_code");
        String hashed_token = (String) Validator.validate(request.getParameter("verification_code"), new ArrayList<>(Arrays.asList(Validator.REQUIRED)), "verification_code");

        String real_email = SecurityLayer.decrypt(hashed_email, SecurityLayer.getStaticEncrypyionKey());
        Utente me = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(real_email);
        if (me == null) {
            request.setAttribute("error", "Invalid token.");
            action_default(request, response);
        } else {
            // Controllo se l'hash del token è valido per l'utente
            if (BCrypt.checkpw(me.getToken(), hashed_token) && token.equals(me.getToken())) {
                if (me.getExpirationDate() != null && !me.getExpirationDate().isBefore(LocalDate.now())) {
                    if (password.equals(confirm)) {
                        me.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                        ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);
                        response.sendRedirect("login?newpass=true");
                    } else {
                        request.setAttribute("error", "Le password inserite non sono valide");
                        action_showRestorePassword(request, response);
                    }
                } else {
                    request.setAttribute("error", "Token expired");
                    action_default(request, response);
                }
            } else {
                request.setAttribute("error", "Invalid token");
                action_default(request, response);
            }
        }
    }

    private void action_send_email(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // VALIDO EMAIL
        String email = (String) Validator.validate(request.getParameter("email"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.STRING_EMAIL)), "email");
        // Controllo se esiste l'email
        Utente me = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(email);
        me.setToken(UtilityMethods.generateNewToken(((GuidaTVDataLayer) request.getAttribute("datalayer"))));
        me.setExpirationDate(LocalDate.now().plusDays(1));
        ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(me);
        UtilityMethods.sendEmailWithCodes(this.getServletContext().getInitParameter("files.directory") + "/links.txt", me, "Ripristina la tua password cliccando sul link in basso", EmailTypes.PASSWORD_RECOVERY_EMAIL);
        response.sendRedirect("login?send=true");

    }

}
