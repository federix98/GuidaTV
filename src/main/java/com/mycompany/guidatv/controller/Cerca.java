/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.controller;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.impl.RicercaImpl;
import com.mycompany.guidatv.data.model.interfaces.Canale;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;
import com.mycompany.guidatv.data.proxy.UtenteProxy;
import com.mycompany.guidatv.result.FailureResult;
import com.mycompany.guidatv.result.StreamResult;
import com.mycompany.guidatv.result.TemplateManagerException;
import com.mycompany.guidatv.result.TemplateResult;
import com.mycompany.guidatv.utility.SecurityLayer;
import com.mycompany.guidatv.utility.UtilityMethods;
import com.mycompany.guidatv.utility.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Federico Di Menna
 */
public class Cerca extends BaseController {

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
            //if(request.getParameter("resize") != null) action_resize(request, response);
            //else 
            action_default(request, response);
            
        } catch (DataException | TemplateManagerException| IOException ex) {
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

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException, IOException{
        
        String titolo = "";
        List<Integer> canali = null, generi = null;
        LocalDate date_min = null, date_max = null;
        LocalTime start_min = null, start_max = null;
        
        // VALIDAZIONE CAMPI
        titolo = (String) Validator.validate(request.getParameter("titolo"), new ArrayList<>(Arrays.asList(Validator.STRING_QUERY_PARAMETER)), "titolo");
        // UtilityMethods.debugConsole(this.getClass(), "action_default", "titolo: " + titolo);
        if(request.getParameter("titolo") != null && !request.getParameter("titolo").isEmpty()) titolo = SecurityLayer.removeSpecialChars(request.getParameter("titolo"));
        if(request.getParameterValues("generi") != null && !request.getParameter("generi").isEmpty()) {
            generi = new ArrayList<>();
            for( String g : request.getParameterValues("generi")) {
                if(g != null) generi.add(SecurityLayer.checkNumeric(g));
            }
        }
        if(request.getParameterValues("canali") != null && !request.getParameter("canali").isEmpty()) {
            canali = new ArrayList<>();
            for( String c : request.getParameterValues("canali")) {
                if(c != null) canali.add(SecurityLayer.checkNumeric(c));
            }
        }
        if(request.getParameter("start_time_min") != null && !request.getParameter("start_time_min").isEmpty()) start_min = (LocalTime) Validator.validate(request.getParameter("start_time_min"), new ArrayList<>(Arrays.asList(Validator.TIME)), "Start Time Min");
        if(request.getParameter("start_time_max") != null && !request.getParameter("start_time_max").isEmpty()) start_max = (LocalTime) Validator.validate(request.getParameter("start_time_max"), new ArrayList<>(Arrays.asList(Validator.TIME)), "Start Time Max");
        if(request.getParameter("date_min") != null && !request.getParameter("date_min").isEmpty()) date_min = (LocalDate) Validator.validate(request.getParameter("date_min"), new ArrayList<>(Arrays.asList(Validator.DATE)), "Start Date Min");
        if(request.getParameter("date_max") != null && !request.getParameter("date_max").isEmpty()) date_max = (LocalDate) Validator.validate(request.getParameter("date_max"), new ArrayList<>(Arrays.asList(Validator.DATE)), "Start Date Max");
        
        
        if(request.getParameter("save") != null && SecurityLayer.checkSession(request) != null) {
            // DEVO SALVARE LA RICERCA
            UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);
            boolean exists = false;
            
            // CREO UNA QUERY STRING DAI PARAMETRI ANALIZZATI
            // Per motivi di sicurezza non salvo direttamente l'input dell'utente (querystring) nel DB
            String titolo_query = "titolo=" + URLEncoder.encode(titolo, "UTF-8");
            String genere_query = UtilityMethods.getQueryList("generi", generi);
            String canale_query = UtilityMethods.getQueryList("canale", canali);
            String start_min_query = (start_min == null) ? "start_min=" : "start_min=" + URLEncoder.encode(start_min.toString(), "UTF-8");
            String start_max_query = (start_max == null) ? "start_max=" : "start_max=" + URLEncoder.encode(start_max.toString(), "UTF-8");
            String date_min_query = (date_min == null) ? "date_min=" : "date_min=" + URLEncoder.encode(date_min.toString(), "UTF-8");
            String date_max_query = (date_max == null) ? "date_max=" : "date_max=" + URLEncoder.encode(date_max.toString(), "UTF-8");
            String safe_queryString = titolo_query + "&" + genere_query + "&" + canale_query + "&" + start_min_query + "&" + start_max_query + "&" + date_min_query + "&" + date_max_query;
            
            
            // Controllo se la ricerca è già presente
            for( Ricerca prec : ((GuidaTVDataLayer) request.getAttribute("datalayer")).getRicercaDAO().getRicercheUtente(me) ) {
                if(prec.getQueryString().equals(safe_queryString)) {
                    // La ricerca è presente
                    exists = true;
                }
            }
            
            if(!exists) {
                // La ricerca è nuova e la salvo
                Ricerca da_salvare = new RicercaImpl();
                da_salvare.setQueryString(safe_queryString);
                ((GuidaTVDataLayer) request.getAttribute("datalayer")).getRicercaDAO().storeRicerca(da_salvare, me.getKey());
            }
        }

        try {
            TemplateResult results = new TemplateResult(getServletContext());
            if(titolo == null && generi == null && canali == null) response.sendRedirect(request.getContextPath() + "/palinsesto");

            List<Programma> interessati = new ArrayList<>();

            if(titolo != null && generi.contains(0)) {
                // Se ho solo il titolo cerco solo per titolo
                interessati.addAll( ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().cercaProgrammi(titolo, 0) );
            }

            else if(titolo != null && !generi.contains(0)) {
                // Se ho anche il genere cerco per titolo e per genere stando
                // attento a non inserire duplicati
                for( Integer id_genere : generi) {
                    for(Programma p : ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().cercaProgrammi(titolo, id_genere) ) {
                        if(!interessati.contains(p)) interessati.add(p);
                    }
                } 
            } 
            else if(titolo == null && !generi.contains(0)){
                // Significa che ho solo il genere
                for (Integer id_genere : generi) {
                    for(Programma p : ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().cercaProgrammi(null, id_genere) ) {
                        if(!interessati.contains(p)) interessati.add(p);
                    }
                } 
            }

            // a questo punto ho i programmi che mi interessano e devo filtrare le programmazioni
            LocalDate start, end;

            if(date_min == null) {
                start = LocalDate.now().minusMonths(1);
            }
            else {
                start = date_min;
            }

            if(date_max == null) {
                end = LocalDate.now().plusMonths(1);
            }
            else {
                end = date_max;
            }

            Map<Canale, List<Programmazione>> programmazioni_per_canale = new TreeMap<>();

            if(canali == null || canali.contains(0)) {
                // se non sto filtrando per canale
                for(Programma p : interessati) {
                    for(Programmazione prog : ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazioneSpecifica(p.getKey(), start, end, start_min, start_max)) {

                        if(programmazioni_per_canale.containsKey(prog.getCanale())) {
                            if(!programmazioni_per_canale.get(prog.getCanale()).contains(prog)) programmazioni_per_canale.get(prog.getCanale()).add(prog);
                        }
                        else {
                            List<Programmazione> toInsert = new ArrayList<>();
                            toInsert.add(prog);
                            programmazioni_per_canale.put(prog.getCanale(), toInsert);
                        }
                    }
                }
            }
            else {
                // Se sto filtrando per canale
                for(Programma p : interessati) {
                    for(Programmazione prog : ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammazioneDAO().getProgrammazioneSpecifica(p.getKey(), start, end, start_min, start_max)) {
                        if((boolean) request.getAttribute("logged")) {
                            if(UtilityMethods.filterResults(prog, UtilityMethods.getMe(request)) != null) {
                                if(canali.contains(prog.getCanale().getKey())) {
                                    if(programmazioni_per_canale.containsKey(prog.getCanale())) {
                                        if(!programmazioni_per_canale.get(prog.getCanale()).contains(prog)) programmazioni_per_canale.get(prog.getCanale()).add(prog);
                                    }
                                    else {
                                        List<Programmazione> toInsert = new ArrayList<>();
                                        toInsert.add(prog);
                                        programmazioni_per_canale.put(prog.getCanale(), toInsert);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Ordino le programmazioni per start_time
            for(Map.Entry<Canale, List<Programmazione>> entry : programmazioni_per_canale.entrySet()) {
                Collections.sort( entry.getValue(), Collections.reverseOrder());
            }

            request.setAttribute("risultati", programmazioni_per_canale);
            
            request.setAttribute("titoloSearchValue", titolo);
            request.setAttribute("generiSearchValue", generi);
            request.setAttribute("canaliSearchValue", canali);
            request.setAttribute("startMinSearchValue", start_min);
            request.setAttribute("startMaxSearchValue", start_max);
            request.setAttribute("dateMinSearchValue", date_min);
            request.setAttribute("dateMaxSearchValue", date_max);
            
            results.activate("risultati.ftl.html", request, response);
        } catch (DataException ex) {
            request.setAttribute("message", "Data access exception: " + ex.getMessage());
            action_error(request, response);
        }
        //response.sendRedirect(request.getContextPath() + "/palinsesto");
    }

    /*
    Metodo di prova per la restituzione di immagini rimpicciolite
    private void action_resize(HttpServletRequest request, HttpServletResponse response) throws DataException, IOException {
        int id = SecurityLayer.checkNumeric(request.getParameter("id"));
        String img_path = request.getServletContext().getRealPath(((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(id).getLinkRefImg());// + '/' + ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(id).getLinkRefImg();
        String outpath = this.getServletContext().getInitParameter("thumbs.directory") + "\\\\" + id + ".jpg";
        UtilityMethods.debugConsole(this.getClass(), "resize", img_path);
        UtilityMethods.resize(img_path, outpath, 0.2);
        StreamResult result = new StreamResult(getServletContext()); 
        request.setAttribute("contentDisposition", "inline");
        result.setResource(new File(outpath));
        result.activate(request, response);
    }*/
    
}
