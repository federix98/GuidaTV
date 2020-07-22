/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Federico Di Menna
 */
public class DettagliProgramma extends BaseController {

    
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
        
        
        try {
            response.setContentType("text/html;charset=UTF-8");
            if(request.getParameter("p_key") != null) {
                action_default(request, response);
            } else {
                action_without_parameter(request, response);
            }
        } catch (DataException | TemplateManagerException | IOException | NumberFormatException ex) {
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
        
        int id_programma = SecurityLayer.checkNumeric(request.getParameter("p_key"));
        
        try {
            TemplateResult results = new TemplateResult(getServletContext());
            
            Programma p = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(id_programma);
            
            if(p == null) throw new DataException("Programma non esistente");
            
            List<Programmazione> programmazioneSerie;
            
            List<Programma> related = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getRelatedPrograms(p);
            //UtilityMethods.debugConsole(this.getClass(), "action_default", "id serie: " + p.getIdSerie());
            
            if(p.getIdSerie() != null) {
                programmazioneSerie = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazioneSerie(p.getIdSerie());
            } else {
                programmazioneSerie = new ArrayList<>();
            }
            
            request.setAttribute("programma", p);
            request.setAttribute("related", related);
            request.setAttribute("programmazione_serie", programmazioneSerie);
            results.activate("programma.ftl.html", request, response);
        } catch (DataException ex) {
            request.setAttribute("message", "Data access exception: " + ex.getMessage());
            action_error(request, response);
        }
    }

    private void action_without_parameter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/palinsesto");
    }

}
