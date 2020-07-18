/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.result;

import freemarker.core.HTMLOutputFormat;
import freemarker.core.JSONOutputFormat;
import freemarker.core.XMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Federico Di Menna
 */
public class TemplateResult {
    
    private ServletContext context = null;
    private Configuration cfg = null;
    
    public TemplateResult(ServletContext context) {
        this.context = context;
        init();
    }

    private void init() {
        cfg = new Configuration(Configuration.VERSION_2_3_26);
        
        // encoding di default per l'input e l'output
        if (context.getInitParameter("view.encoding") != null) {
            cfg.setOutputEncoding(context.getInitParameter("view.encoding"));
            cfg.setDefaultEncoding(context.getInitParameter("view.encoding"));
        }
        
        //impostiamo la directory (relativa al contesto) da cui caricare i templates
        if (context.getInitParameter("view.template_directory") != null) {
            cfg.setServletContextForTemplateLoading(context, context.getInitParameter("view.template_directory"));
        } else {
            cfg.setServletContextForTemplateLoading(context, "templates");
        }
        
        if (context.getInitParameter("view.debug") != null && context.getInitParameter("view.debug").equals("true")) {
            //impostiamo un handler per gli errori nei template - utile per il debug
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        } else {
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        }
        
        //date/time default formatting
        if (context.getInitParameter("view.date_format") != null) {
            cfg.setDateTimeFormat(context.getInitParameter("view.date_format"));
        }
        
        
        DefaultObjectWrapperBuilder owb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_26);
        owb.setForceLegacyNonListCollections(false);
        owb.setDefaultDateType(TemplateDateModel.DATETIME);
        cfg.setObjectWrapper(owb.build());
    }
    
    protected Map getDefaultDataModel() {
        
        Map defaultDataModel = new HashMap();
        
        defaultDataModel.put("compiled_on", Calendar.getInstance().getTime()); //data di compilazione del template
        defaultDataModel.put("outline_tpl", context.getInitParameter("view.outline_template")); //eventuale template di outline
        
        
        
        //aggiungiamo altri dati di inizializzazione presi dal web.xml
        Map initTplData = new HashMap();
        defaultDataModel.put("defaults", initTplData);
        Enumeration parms = context.getInitParameterNames();
        while (parms.hasMoreElements()) {
            String name = (String) parms.nextElement();
            if (name.startsWith("view.data.")) {
                initTplData.put(name.substring(10), context.getInitParameter(name));
            }
        }
        
        return defaultDataModel;

        
    }
    
    protected void process(String tplname, Map datamodel, Writer out) throws TemplateManagerException {
        Template t;
        
        // MI PRENDO I DATI DI DEFAULT
        Map<String, Object> localDataModel = getDefaultDataModel();
        
        // SE I DATI IN INPUT SONO NON NULLI LI AGGIUNGO AL DATAMODEL
        if (datamodel != null) {
            localDataModel.putAll(datamodel);
        }
        
        // PRENDO IL NOME DEL TEMPLATE DI OUTLINE
        String outline_name = (String) localDataModel.get("outline_tpl");
        
        try {
            if (outline_name == null || outline_name.isEmpty()) {
                //se non c'è un outline, carichiamo semplicemente il template specificato
                t = cfg.getTemplate(tplname);
                System.out.println("No template outline name");
            } else {
                //un template di outline è stato specificato: il template da caricare è quindi sempre l'outline...
                t = cfg.getTemplate(outline_name);
                //...e il template specifico per questa pagina viene indicato all'outline tramite una variabile content_tpl
                localDataModel.put("content_tpl", tplname);
                //si suppone che l'outline includa questo secondo template
                System.out.println("Template outline name " + tplname);
            }
            //associamo i dati al template e lo mandiamo in output
            t.process(localDataModel, out);
            
            
            try ( // PER VALIDAZIONE HTML
                    Writer fileWriter = new FileWriter(new File("to_validate.html"))) {
                t.process(localDataModel, fileWriter);
            }
            
            
        } catch (IOException e) {
            throw new TemplateManagerException("Template error: " + e.getMessage(), e);
        } catch (TemplateException e) {
            throw new TemplateManagerException("Template error: " + e.getMessage(), e);
        }
    }
    
    //questa versione di activate accetta un modello dati esplicito
    public void activate(String tplname, Map datamodel, HttpServletResponse response) throws TemplateManagerException {
        //impostiamo il content type, se specificato dall'utente, o usiamo il default
        String contentType = (String) datamodel.get("contentType");
        if (contentType == null) {
            contentType = "text/html";
        }
        response.setContentType(contentType);

        //impostiamo il tipo di output: in questo modo freemarker abiliter� il necessario escaping
        //set the output format, so that freemarker will enable the correspondoing escaping
        switch (contentType) {
            case "text/html":
                cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
                break;
            case "text/xml":
            case "application/xml":
                cfg.setOutputFormat(XMLOutputFormat.INSTANCE);
                break;
            case "application/json":
                cfg.setOutputFormat(JSONOutputFormat.INSTANCE);
                break;
            default:
                break;
        }

        //impostiamo l'encoding, se specificato dall'utente, o usiamo il default
        String encoding = (String) datamodel.get("encoding");
        if (encoding == null) {
            encoding = cfg.getOutputEncoding();
        }
        response.setCharacterEncoding(encoding);

        try {
            process(tplname, datamodel, response.getWriter());
        } catch (IOException ex) {
            throw new TemplateManagerException("Template error: " + ex.getMessage(), ex);
        }
    }
    
    //questo metodo restituisce un data model estratto dagli attributi della request
    protected Map getRequestDataModel(HttpServletRequest request) {
        Map datamodel = new HashMap();
        Enumeration attrs = request.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attrname = (String) attrs.nextElement();
            datamodel.put(attrname, request.getAttribute(attrname));
        }
        return datamodel;
    }

    //questa versione di activate estrae un modello dati dagli attributi della request
    public void activate(String tplname, HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
        Map datamodel = getRequestDataModel(request);
        activate(tplname, datamodel, response);
    }

    //questa versione di activate pu� essere usata per generare output non diretto verso il browser, ad esempio
    //su un file
    public void activate(String tplname, Map datamodel, OutputStream out) throws TemplateManagerException {
        //impostiamo l'encoding, se specificato dall'utente, o usiamo il default
        String encoding = (String) datamodel.get("encoding");
        if (encoding == null) {
            encoding = cfg.getOutputEncoding();
        }
        try {
            //notare la gestione dell'encoding, che viene invece eseguita implicitamente tramite il setContentType nel contesto servlet
            process(tplname, datamodel, new OutputStreamWriter(out, encoding));
        } catch (UnsupportedEncodingException ex) {
            throw new TemplateManagerException("Template error: " + ex.getMessage(), ex);
        }
    }
    
}
