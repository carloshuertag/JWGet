/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JWGet;

/**
 *
 * @author huert
 */
public class Properties {
    public static final String SLASH = "\\"; //Windows, use / for UNIX based
    public static final String DOWNLOADSDIR = "Downloads";
    public static final String DOWNLOADSPATH = System.getProperty("user.dir") +
            SLASH + DOWNLOADSDIR;
}
