/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.schedule;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.DataLayer;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Interesse;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.data.model.interfaces.Ricerca;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import com.mycompany.guidatv.utility.EmailTypes;
import com.mycompany.guidatv.utility.UtilityMethods;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Federico Di Menna
 */
public class EmailTask implements Runnable {
    
    /*public EmailTask(DataSource ds, String file) {
        this.ds = ds;
        this.path = file;
    }*/

    
    @Override
    public void run() {
        // Logica di invio email
        UtilityMethods.debugConsole(this.getClass(), "run", "Email task running");
        try { 
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/webeng_proj");
            
            try (GuidaTVDataLayer datalayer = new GuidaTVDataLayer(ds)) {

                datalayer.init();
                List<Utente> users = ((GuidaTVDataLayer) datalayer).getUtenteDAO().getUtentiSendEmail();

                for(Utente dest : users) {
                    String emailText = "Novità di Oggi (" + LocalDate.now().toString() + "):\n";
                    emailText += "\nCanali interessati\n";

                    List<Programmazione> interessati = new ArrayList<>();

                    for(Interesse intrs : dest.getInteressi()) {
                        interessati.addAll( ((GuidaTVDataLayer) datalayer).getProgrammazioneDAO().getProgrammazione(intrs.getCanale().getKey(), 
                                LocalDateTime.parse(LocalDate.now().format(DateTimeFormatter.ISO_DATE) + " " + intrs.getStartTime().format(DateTimeFormatter.ISO_TIME), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), LocalDateTime.parse(LocalDate.now() + " " + intrs.getEndTime().format(DateTimeFormatter.ISO_TIME), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) );
                    }
                    

                    for(Programmazione prog : interessati) {
                        emailText += prog.getCanale().getNome() + " |  dalle " + prog.getTime() + " alle " + prog.getEndTime() + " - " + prog.getProgramma().getNome()  + "\n";
                    }
                    
                    List<Ricerca> ricerche = dest.getRicerche();
                    if(ricerche != null) {
                        emailText += "\nAggiornamenti ricerche\n";
                        if(ricerche.isEmpty()) emailText += "Nessuna ricerca salvata\n";
                        for(Ricerca r : ricerche) {
                            Map<String, String> parametri = UtilityMethods.getQueryMap(r.getQueryString());
                            emailText += "Parametri di ricerca: \n";
                            for(String key : parametri.keySet()) {
                                String to_add;
                                emailText += key + " : " + parametri.get(key) + " | ";
                            }
                            emailText += "\nResults: Clicca qui <a href=\"...GuidaTV/cerca?" + r.getQueryString() + ">Visualizza Risultati</a>\n";
                        }
                    }
                    

                    UtilityMethods.sendEmailWithCodes("C:\\webengproj_files\\daily.txt", dest, emailText, EmailTypes.DAILY_EMAIL);
                }

            } catch (SQLException ex) {
                Logger.getLogger(EmailTask.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(EmailTask.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (NamingException ex) {
            Logger.getLogger(EmailTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
    }
    
}
