/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.utility;

import com.mycompany.guidatv.utility.BCrypt;
import com.mycompany.guidatv.data.DataException;
import com.mycompany.guidatv.data.dao.GuidaTVDataLayer;
import com.mycompany.guidatv.data.model.interfaces.Programmazione;
import com.mycompany.guidatv.data.model.interfaces.Utente;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Federico Di Menna
 */
public class UtilityMethods {

    /**
     * Funzione customizzata di debugging utile a ricordare dove vengono
     * inserite le diverse print di debug.
     *
     * @param c
     * @param method
     * @param message
     */
    public static void debugConsole(Class c, String method, String message) {
        System.out.println(ConsoleColors.RED + "[Debugging] " + c.getName() + "\t\t->" + method + ConsoleColors.RESET + "\t" + message);
    }

    /**
     * Restituisce l'utente loggato in sessione. Controllare che la sessione
     * esista prima della chiamata.
     *
     * @param request
     * @return
     * @throws DataException
     */
    public static Utente getMe(HttpServletRequest request) throws DataException {
        return ((GuidaTVDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente((int) request.getSession().getAttribute("userid"));
    }

    /**
     * Restituisce una stringa alfanumerica della dimensione desiderata
     *
     * @param size
     * @return
     */
    public static String getRandomString(int size) {
        String base = "";
        base += "abcdefghijklmnopqrstuvwxyz";
        base += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        base += "0123456789";
        Random random = new Random();
        String to_return = "";
        for (int i = 0; i < size; i++) {
            to_return += base.charAt(random.nextInt(base.length()));
        }
        return to_return;
    }
    
    public static String generateNewToken(GuidaTVDataLayer dl) throws DataException {
        String token;
        do {
            token = getRandomString(16);
        } while(dl.getUtenteDAO().tokenExists(token));
        return token;
    }

    /**
     * Simula l'invio di una email all'utente in base ai parametri inseriti
     *
     * @param file
     * @param me
     * @param emailText
     * @param type
     * @throws IOException
     * @throws Exception
     */
    public static void sendEmailWithCodes(String file, Utente me, String emailText, EmailTypes type) throws IOException, Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String direct_to = null;
            boolean link = true;
            switch (type) {
                case CONFIRM_EMAIL:
                    direct_to = "confirmEmail";
                    break;
                case PASSWORD_RECOVERY_EMAIL:
                    direct_to = "forgot";
                    break;
                case DAILY_EMAIL:
                    link = false;
                    break;
                default:
                    return;
            }

            writer.write("[ " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " ] Sended to " + me.getEmail() + ":");
            writer.newLine();
            writer.write("--------------------------------- GuidaTv.net Staff ----------------------------------");
            writer.newLine();
            writer.write("Gentile " + me.getUsername() + ",");
            writer.newLine();
            writer.write(emailText);

            if (link) {
                String verification_code = BCrypt.hashpw(me.getToken(), BCrypt.gensalt());
                String refer_code = SecurityLayer.encrypt(me.getEmail(), SecurityLayer.getStaticEncrypyionKey());
                writer.newLine();
                writer.write("/GuidaTV/" + direct_to + "?verification_code=" + verification_code + "&refer_code=" + refer_code);
            }

            writer.newLine();
            writer.write("--------------------------------- GuidaTv.net Staff ----------------------------------");
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();
        }
    }

    public static boolean equalsIntegerLists(List<Integer> list1, List<Integer> list2) {
        Collections.sort(list1);
        Collections.sort(list2);
        if (list1.size() <= 0 || list2.size() <= 0) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!Objects.equals(list2.get(i), list1.get(i))) {
                return false;
            }
        }
        return true;

    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] p = param.split("=");
            String name = p[0];
            if (p.length > 1) {
                String value = p[1];
                map.put(name, value);
            }
        }
        return map;
    }

    public static String getQueryList(String name, List<Integer> values) {
        String ret = "";
        if (values != null) {
            for (int i : values) {
                ret += name + "=" + i + "&";
            }
            // Tolgo l'ultimo &
            if (ret.charAt(ret.length() - 1) == '&') {
                ret = ret.substring(0, ret.length() - 1);
            }
        }
        return ret;
    }

    public static void clearRequestAttributes(HttpServletRequest request) {
        while(request.getAttributeNames().hasMoreElements()) {
            request.removeAttribute(request.getAttributeNames().nextElement());
        }
    }
    
    /*
     * Metodi per gestione Fasce Orarie
     * 1 : Mattina
     * 2 : Pomeriggio
     * 3 : Sera
     * 4 : Notte
     */
    public static String getNomeFascia(int fascia_id) {
        switch (fascia_id) {
            case 0:
                return "Tutte le fasce";
            case 1:
                return "Mattina";
            case 2:
                return "Pomeriggio";
            case 3:
                return "Sera";
            case 4:
                return "Notte";
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Restituisce l'orario di inizio della fascia indicata
     * @param fascia_id
     * @return 
     */
    public static LocalTime getOrarioInizioFascia(int fascia_id) {
        switch (fascia_id) {
            case 0:
                return LocalTime.parse("00:00:00");
            case 1:
                return LocalTime.parse("06:00:00");
            case 2:
                return LocalTime.parse("12:00:00");
            case 3:
                return LocalTime.parse("18:00:00");
            case 4:
                return LocalTime.parse("00:00:00");
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Restituisce l'rario di fine della fascia indicata
     * @param fascia_id
     * @return 
     */
    public static LocalTime getOrarioFineFascia(int fascia_id) {
        switch (fascia_id) {
            case 0:
                return LocalTime.parse("23:59:00");
            case 1:
                return LocalTime.parse("11:59:00");
            case 2:
                return LocalTime.parse("17:59:00");
            case 3:
                return LocalTime.parse("23:59:00");
            case 4:
                return LocalTime.parse("05:59:00");
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Restituisce una stringa con il body della richiesta effettuata
     * @param request
     * @return
     * @throws IOException 
     */
    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }

    /**
     * Effettua il resize di un'immagine
     * @param inputImagePath
     * @param outputImagePath
     * @param scaledWidth
     * @param scaledHeight
     * @throws IOException 
     */
    public static void resize(String inputImagePath,
            String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {
        // reads input image
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
 
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());
 
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
 
        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);
 
        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
    
    /**
     * Effettua il resize di un'immagine
     * @param inputImagePath
     * @param outputImagePath
     * @param percent
     * @throws IOException 
     */
    public static void resize(String inputImagePath,
            String outputImagePath, double percent) throws IOException {
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
    }
    
    /**
     * Restituisce la URL di base della richiesta
     * @param request
     * @return 
     */
    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme() + "://";
        String serverName = request.getServerName();
        String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
        String contextPath = request.getContextPath();
        return scheme + serverName + serverPort + contextPath;
     }
    
    /**
     * Filtra le programmazioni in base all'utente
     * @param results
     * @param user 
     */
    public static void filterResults(List<Programmazione> results, Utente user) {
        List<Programmazione> toRemove = new ArrayList();
        for(Programmazione p : results) {
            if(p.getProgramma().getClassificazione().getMinAge() > user.getAge()) toRemove.add(p);
        }
        results.removeAll(toRemove);
    }
    
    /**
     * Filtra le programmazioni in base all'utente
     * @param result
     * @param user 
     * @return Programmazione 
     */
    public static Programmazione filterResults(Programmazione result, Utente user) {
        if(result.getProgramma().getClassificazione().getMinAge() > user.getAge()) return null;
        return result;
    }
}
