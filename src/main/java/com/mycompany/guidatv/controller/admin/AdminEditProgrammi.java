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
import com.mycompany.guidatv.data.model.impl.ProgrammaImpl;
import com.mycompany.guidatv.data.model.interfaces.Classificazione;
import com.mycompany.guidatv.data.model.interfaces.Genere;
import com.mycompany.guidatv.data.model.interfaces.Programma;
import com.mycompany.guidatv.result.JSONResult;
import com.mycompany.guidatv.utility.Validator;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@MultipartConfig
public class AdminEditProgrammi extends BaseController {

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
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AdminEditProgrammi.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                } else if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
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
        int total = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getNumeroProgrammi();

        List<Programma> programmi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgrammiPaginated(start, length);

        UtilityMethods.debugConsole(this.getClass(), "paginated", "total " + total);

        JSONResult results = new JSONResult(getServletContext());

        // JSON Response Templating Attributes
        request.setAttribute("draw", draw);
        request.setAttribute("total", String.valueOf(total));
        request.setAttribute("programmi", programmi);
        results.activate("/admin/json/dt_programmi.ftl.json", request, response);

    }

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {
        UtilityMethods.debugConsole(this.getClass(), "action_default", "me called");
        List<Programma> programmi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgrammiPaginated(0, 15);
        TemplateResult results = new TemplateResult(getServletContext());
        UtenteProxy me = (UtenteProxy) UtilityMethods.getMe(request);

        // Edit Programmi default Attributes
        request.setAttribute("me", me);
        request.setAttribute("programmi_admin", programmi);
        request.setAttribute("outline_tpl", request.getServletContext().getInitParameter("view.outline_admin_template"));

        results.activate("/admin/pages/edit_programmi.ftl.html", request, response);

    }

    private void action_edit(HttpServletRequest request, HttpServletResponse response) throws DataException, TemplateManagerException {
        int id_element = SecurityLayer.checkNumeric(request.getParameter("data_id"));
        Programma item = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(id_element);
        List<Genere> generi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGeneri();
        List<Classificazione> classificazioni = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getClassificazioneDAO().getAllClassificazioni();
        List<Programma> series = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getIdSerie();
        request.setAttribute("generi", generi);
        request.setAttribute("classificazioni", classificazioni);
        request.setAttribute("series", series);

        TemplateResult results = new TemplateResult(getServletContext());
        request.setAttribute("item", item);
        request.setAttribute("outline_tpl", "");
        results.activate("/admin/partials/programma_form.ftl.html", request, response);
    }

    private void action_loginredirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void action_store(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, UnsupportedEncodingException {
        
        JSONResult results = new JSONResult(getServletContext());
        UtilityMethods.debugConsole(this.getClass(), "store", "sono in store");
        // CHECK SU TUTTI I CAMPI
        try {

            boolean serie_update = false;
            Integer key = (Integer) Validator.validate(request.getParameter("key"), new ArrayList<>(Arrays.asList(Validator.INTEGER)), "ID");
            String nome = (String) Validator.validate(request.getParameter("nome"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.STRING_NOT_EMPTY, Validator.STRING_QUERY_PARAMETER)), "nome");
            //UtilityMethods.debugConsole(this.getClass(), "default", request.getCharacterEncoding() + " - " + URLDecoder.decode(request.getParameter("nome"), "UTF-8"));
            String descrizione = (String) Validator.validate(request.getParameter("descrizione"), new ArrayList<>(Arrays.asList(Validator.STRING_QUERY_PARAMETER)), "descrizione");
            String linkRefDetails = (String) Validator.validate(request.getParameter("linkExt"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.STRING_NOT_EMPTY)), "Link Details");
            Integer durata = (Integer) Validator.validate(request.getParameter("durata"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Durata");
            Integer id_serie = (Integer) Validator.validate(request.getParameter("idSerie"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "idSerie");
            Integer stagione = (Integer) Validator.validate(request.getParameter("stagione"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Stagione");
            Integer episodio = (Integer) Validator.validate(request.getParameter("episodio"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Episodio");
            Integer id_classificazione = (Integer) Validator.validate(request.getParameter("classificazione"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Classificazione");
            Integer id_genere = (Integer) Validator.validate(request.getParameter("genere"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "Genere");

            
            
            Programma target;
            if (key == null) {
                target = new ProgrammaImpl();
            } else {
                target = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(key);
            }
            target.setNome(nome);
            if(descrizione != null) target.setDescrizione(descrizione);
            if(id_serie != 0) {
                if(id_serie > 0) target.setIdSerie(id_serie);
                else serie_update = true;
            }
            else target.setIdSerie(null);
            target.setStagione(stagione);
            target.setEpisodio(episodio);
            target.setDurata(durata);
            target.setLinkRefDetails(linkRefDetails);
            target.setClassificazione(((GuidaTVDataLayer) request.getAttribute("datalayer")).getClassificazioneDAO().getClassificazione(id_classificazione));
            target.setGenere(((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGenere(id_genere));

            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().storeProgramma(target);
            
            if(serie_update) {
                target.setIdSerie(target.getKey());
                ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().storeProgramma(target);
            }
            
            // GESTIONE DELL'IMMAGINE
            if(request.getParameter("serie_image") != null && ((String) request.getParameter("serie_image")).equals("sel")) {
                // UTILIZZO L'immagine della serie
                if(target.getIdSerie() > 0) {
                    target.setLinkRefImg(((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(target.getIdSerie()).getLinkRefImg());
                    ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().storeProgramma(target);
                }
            }   
            else {
                Part image = request.getPart("image");
                if (image != null) {
                    String name = "prog_" + target.getKey() + ".jpg";
                    String path = getServletContext().getRealPath("/img/progs") + File.separatorChar + name;
                    String contentType = image.getContentType();
                    long size = image.getSize();
                    if (size > 0 && name != null && !name.isEmpty()) {
                        File new_file = new File(path);
                        Files.copy(image.getInputStream(), new_file.toPath(), StandardCopyOption.REPLACE_EXISTING); 
                        target.setLinkRefImg("/img/progs/" + name);
                        ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().storeProgramma(target);
                    }
                }
            }
            
            

            request.setAttribute("errors", "");
            request.setAttribute("success", "true");
            results.activate("/admin/json/store_response.ftl.json", request, response);
        } catch (IOException | ServletException | DataException ex) {
            // GESTISCO IN MODO DIVERSO L'ECCEZIONE
            request.setAttribute("errors", ex.getMessage());
            request.setAttribute("success", "false");
            results.activate("/admin/json/store_response.ftl.json", request, response);
        }
    }

    private void action_create(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, DataException {
        TemplateResult results = new TemplateResult(getServletContext());
        List<Genere> generi = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getGenereDAO().getGeneri();
        List<Classificazione> classificazioni = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getClassificazioneDAO().getAllClassificazioni();
        List<Programma> series = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getIdSerie();
        request.setAttribute("generi", generi);
        request.setAttribute("classificazioni", classificazioni);
        request.setAttribute("series", series);
        request.setAttribute("outline_tpl", "");
        results.activate("/admin/partials/programma_form.ftl.html", request, response);
    }

    private void action_delete(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        // Controllo se l'id è reale
        JSONResult results = new JSONResult(getServletContext());
        try {
            Integer key = (Integer) Validator.validate(request.getParameter("data_id"), new ArrayList<>(Arrays.asList(Validator.REQUIRED, Validator.INTEGER)), "ID");
            if (key == null) {
                throw new DataException("Invalid Key");
            }
            Programma p = ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().getProgramma(key);
            if (p == null) {
                throw new DataException("Invalid Key");
            }

            ((GuidaTVDataLayer) request.getAttribute("datalayer")).getProgrammaDAO().deleteProgramma(key);
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
