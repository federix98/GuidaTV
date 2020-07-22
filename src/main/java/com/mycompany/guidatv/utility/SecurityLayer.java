/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.utility;

import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Calendar;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Federico Di Menna
 */
public class SecurityLayer {

    private static final String STATIC_KEY = "StaticKeyGuidaTV";
    
    
    //--------- SESSION SECURITY ------------    
    public static HttpSession checkSession(HttpServletRequest r) {
        boolean check = true;

        HttpSession s = r.getSession(false);
        //per prima cosa vediamo se la sessione è attiva
        if (s == null) {
            return null;
        }

        //check sulla validità  della sessione
        if (s.getAttribute("userid") == null) {
            check = false;
            //check sull'ip del client
        } else if ((s.getAttribute("ip") == null) || !((String) s.getAttribute("ip")).equals(r.getRemoteHost())) {
            check = false;
            //check sulle date
        } else {
            //inizio sessione
            Calendar begin = (Calendar) s.getAttribute("inizio-sessione");
            //ultima azione
            Calendar last = (Calendar) s.getAttribute("ultima-azione");
            //data/ora correnti
            Calendar now = Calendar.getInstance();
            if (begin == null) {
                check = false;
            } else {
                //secondi trascorsi dall'inizio della sessione
                long secondsfrombegin = (now.getTimeInMillis() - begin.getTimeInMillis()) / 1000;
                //dopo tre ore la sessione scade
                if (secondsfrombegin > 3 * 60 * 60) {
                    check = false;
                } else if (last != null) {
                    //secondi trascorsi dall'ultima azione
                    long secondsfromlast = (now.getTimeInMillis() - last.getTimeInMillis()) / 1000;
                    //dopo trenta minuti dall'ultima operazione la sessione è invalidata
                    if (secondsfromlast > 30 * 60) {
                        check = false;
                    }
                }
            }
        }
        if (!check) {
            s.invalidate();
            return null;
        } else {
            //reimpostiamo la data/ora dell'ultima azione
            s.setAttribute("ultima-azione", Calendar.getInstance());
            return s;
        }
    }
    
    public static boolean checkAdminSession(HttpServletRequest r) throws DataException {
        //return true;
        HttpSession s = checkSession(r);
        if(s == null) return false;
        else {
            return UtilityMethods.getMe(r).getRuolo().getKey() == 1; // RUOLO 1 = AMMINISTRATORE
        }
    }

    public static HttpSession createSession(HttpServletRequest request, String username, int userid) {
        HttpSession s = request.getSession(true);
        s.setAttribute("username", username);
        s.setAttribute("ip", request.getRemoteHost());
        s.setAttribute("inizio-sessione", Calendar.getInstance());
        s.setAttribute("userid", userid);
        return s;
    }

    public static void disposeSession(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) {
            s.invalidate();
        }
    }

    //--------- DATA SECURITY ------------
    public static String addSlashes(String s) {
        return s.replaceAll("(['\"\\\\])", "\\\\$1");
    }

    //questa funzione rimuove gli slash aggiunti da addSlashes
    public static String stripSlashes(String s) {
        return s.replaceAll("\\\\(['\"\\\\])", "$1");
    }

    public static int checkNumeric(String s) throws DataException {
        //convertiamo la stringa in numero, ma assicuriamoci prima che sia valida
        //convert the string to a number, ensuring its validity
        if (s != null) {
            //se la conversione fallisce, viene generata un'eccezione
            //if the conversion fails, an exception is raised
            try {
                return Integer.parseInt(s);
            }
            catch (NumberFormatException ex) {
                throw new DataException("Data not valid, Integer value expected: " + ex.getMessage());
            }
            
        } else {
            throw new DataException("Data not valid, Integer value expected");
        }
    }
    
    public static boolean checkBoolean(String s) throws NumberFormatException {
        if (s != null) {
            int n = Integer.parseInt(s);
            if(n != 0 && n != 1) return false;
            return true;
        } else {
            throw new NumberFormatException("String argument is null");
        }
    }
    
    public static boolean isEmailValid(String email) { 
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
                            "[a-zA-Z0-9_+&*-]+)*@" + 
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
                            "A-Z]{2,7}$"; 
                              
        Pattern pat = Pattern.compile(emailRegex); 
        if (email == null) 
            return false; 
        return pat.matcher(email).matches(); 
    } 

    //--------- CONNECTION SECURITY ------------
    //questa funzione verifica se il protocollo HTTPS è attivo
    public static boolean checkHttps(HttpServletRequest r) {
        return r.isSecure();
        //metodo "fatto a mano" che funziona solo se il server trasmette gli header corretti
    }

    //questa funzione ridirige il browser sullo stesso indirizzo
    //attuale, ma con protocollo https
    public static void redirectToHttps(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        //estraiamo le parti della request url
        String server = request.getServerName();
        //int port = request.getServerPort();
        String context = request.getContextPath();
        String path = request.getServletPath();
        String info = request.getPathInfo();
        String query = request.getQueryString();

        //ricostruiamo la url cambiando il protocollo e la porta COME SPECIFICATO NELLA CONFIGURAZIONE DI TOMCAT
        String newUrl = "https://" + server + ":8443" +  context + path + (info != null ? info : "") + (query != null ? "?" + query : "");
        try {
            //ridirigiamo il client
            response.sendRedirect(newUrl);
        } catch (IOException ex) {
            try {
                //in caso di problemi tentiamo prima di inviare un errore HTTP standard
                response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Cannot redirect to HTTPS, blocking request");
            } catch (IOException ex1) {
                //altrimenti generiamo un'eccezione
                throw new ServletException("Cannot redirect to https!");
            }
        }
    }

    public static String removeSpecialChars(String input) {
        return input.replaceAll("[^\\p{L}0-9']", "");  
    }
    
    public static String removeSpecialCharsQuery(String input) {
        return input.replaceAll("[^\\p{L}0-9'\\s\\+]", "");  
    }
    
    public static LocalDate checkDate(String input) {
        try {
            return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public static LocalTime checkTime(String input) {
        try {
            return LocalTime.parse(input, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public static String encrypt(String phrase, String key) throws Exception {
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
        byte[] dataBytes = phrase.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(dataBytes));
    }
    
    public static String decrypt(String enc_phrase, String key) throws Exception {
        byte[] dataBytes = Base64.getDecoder().decode(enc_phrase);
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] dataBytesDecrypted = (cipher.doFinal(dataBytes));
        return new String(dataBytesDecrypted);
    }
    
    public static String getStaticEncrypyionKey() {
        return SecurityLayer.STATIC_KEY;
    }
}
