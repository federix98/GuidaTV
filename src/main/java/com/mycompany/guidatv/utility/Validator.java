/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.guidatv.utility;

import com.mycompany.guidatv.data.DataException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author Federico Di Menna
 */
public class Validator {

    public static final int REQUIRED = 1;
    //public static final int STRING = 2;
    public static final int INTEGER = 3;
    public static final int DATE = 4;
    public static final int TIME = 5;
    public static final int STRING_NOT_EMPTY = 6;
    public static final int STRING_WITHOUT_SPECIAL = 7;
    public static final int STRING_EMAIL = 8;
    public static final int BOOLEAN = 9;
    public static final int PASSWORD = 10;
    public static final int DATETIME = 11;
    public static final int STRING_QUERY_PARAMETER = 12;

    public static Object validate(Object obj, List<Integer> parameters, String name) throws DataException {
        if (parameters.contains(REQUIRED)) {
            if (obj == null) {
                throw new DataException("required parameter missing: " + name);
            }
        }
        /*if(parameters.contains(STRING)) {
            return String.valueOf(obj);
        }*/
        if (((String) obj) != null) {
            if (parameters.contains(INTEGER)) {
                try{
                    String str = (String) obj;
                    if(str.contains(".")) str = str.replace(".", "");
                    if(str.contains(",")) str = str.replace(",", "");
                    return SecurityLayer.checkNumeric(str);
                } catch (Exception ex) {
                    throw new DataException("Invalid parameter: " + name + " must be a integer value");
                }
            }
            if (parameters.contains(DATETIME)) {
                try {
                    return LocalDateTime.parse((String) obj, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
                } catch (Exception ex) {
                    throw new DataException("Invalid parameter: " + name + " must be a valid Timestamp");
                }
            }
            if (parameters.contains(DATE)) {
                try {
                    return SecurityLayer.checkDate((String) obj);
                } catch (Exception ex) {
                    throw new DataException("Invalid parameter: " + name + " must be a valid Date");
                }
            }
            if (parameters.contains(TIME)) {
                try {
                    return SecurityLayer.checkTime((String) obj);
                } catch (Exception ex) {
                    throw new DataException("Invalid parameter: " + name + " must be a valid Time");
                }
            }
            if (parameters.contains(STRING_NOT_EMPTY)) {
                if (((String) obj).isBlank()) {
                    throw new DataException("Invalid parameter: " + name + " must be not empty");
                }
            }
            if (parameters.contains(STRING_QUERY_PARAMETER)) {
                return SecurityLayer.removeSpecialCharsQuery((String) obj);
            }
            if (parameters.contains(STRING_WITHOUT_SPECIAL)) {
                return SecurityLayer.removeSpecialChars((String) obj);
            }
            if (parameters.contains(STRING_EMAIL)) {
                if (!SecurityLayer.isEmailValid((String) obj)) {
                    throw new DataException("Invalid parameter: " + name + " must be a valid email");
                }
            }
            if (parameters.contains(BOOLEAN)) {
                if (!SecurityLayer.checkBoolean((String) obj)) {
                    throw new DataException("Invalid parameter: " + name + " must be boolean value");
                } else {
                    return (Integer.parseInt((String) obj) != 0);
                }
            }
            if (parameters.contains(PASSWORD)) {
                if (((String) obj).length() < 8) {
                    throw new DataException("Invalid parameter: " + name + " must be at least 8 characters long");
                }
            }
        }

        return obj;
    }

}
