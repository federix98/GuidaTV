/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
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
public class Home extends BaseController {

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
            if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
                int page = SecurityLayer.checkNumeric(request.getParameter("page"));
                action_paginated(request, response, page);
            }
            else {
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
        
        action_paginated(request, response, 0);
        
    }

    private void action_paginated(HttpServletRequest request, HttpServletResponse response, int page) throws DataException, TemplateManagerException {
        
        int totale_canali, elements = 5;
        
        try {
            TemplateResult results = new TemplateResult(getServletContext());
            List<Canale> canali = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getListaCanali(page, elements);
            totale_canali = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getCanaleDAO().getNumeroCanali();
            request.setAttribute("canali_in_onda", canali);
            request.setAttribute("numero_pagine", (int)(Math.ceil(totale_canali / elements)));
            request.setAttribute("pagina", page);
            results.activate("current.ftl.html", request, response);
        } catch (DataException ex) {
            request.setAttribute("message", "Data access exception: " + ex.getMessage());
            action_error(request, response);
        }
    }

}
