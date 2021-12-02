/*
 * To change this license header, choose License Headers in Project Utilities.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JWGet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author huert
 */
public class Utilities {
    public static final int N_THREADS = 4;
    public static final String SLASH = "\\"; //Windows, use / for UNIX based
    public static final String DOWNLOADSDIR = "Downloads";
    public static final String DOWNLOADSPATH = System.getProperty("user.dir") +
            SLASH + DOWNLOADSDIR;
    
    public static String getParentPath(String path){
        int end = path.lastIndexOf("/");
        return path.substring(0, end);
    }
    
    public static String getHttpRequest(String rsc, String host, int port){
        StringBuilder sb = new StringBuilder();
        sb.append("GET ");
        sb.append(rsc);
        sb.append(" HTTP/1.0\r\n");
        sb.append("Host: ");
        sb.append(host);
        if(port != 80){
            sb.append(":");
            sb.append(port);
        }
        sb.append("\r\nAccept: */*\r\nAccept-Encoding: gzip, deflate, br\r\n");
        sb.append("Connection: keep-alive\r\n\r\n");
        return sb.toString();
    }
    
    public static String getResource(String host, int port, String path){
        String request = Utilities.getHttpRequest(path, host, port);
        String contentType = "";
        try{
            InetAddress ipAddress = InetAddress.getByName(host);
            Socket client = new Socket(ipAddress, port);
            client.setReuseAddress(true);
            client.setKeepAlive(true);
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            dos.write(request.getBytes());
            dos.flush();
            DataInputStream dis = new DataInputStream(client.getInputStream());
            byte[] responseBuffer = new byte[1024], buffer;
            int read = dis.read(responseBuffer);
            String responseHeader = new String(responseBuffer);
            int offset = responseHeader.indexOf("\r\n\r\n") + 2;
            responseHeader = responseHeader.substring(0, offset);
            String lengthHeaderField = responseHeader.substring(
                    responseHeader.indexOf("Content-Length: ") + 16);
            lengthHeaderField = lengthHeaderField.substring(0,
                    lengthHeaderField.indexOf("\r\n"));
            int size = Integer.parseInt(lengthHeaderField.replace(" ", ""));
            contentType = responseHeader.substring(
                    responseHeader.indexOf("Content-Type: ") + 14);
            contentType = contentType.substring(0, contentType.indexOf("\r\n"));
            File file = new File(Utilities.DOWNLOADSPATH + Utilities.getParentPath(path));
            file.mkdirs();
            file.setWritable(true);
            System.out.println(request);
            System.out.println(responseHeader);
            DataOutputStream fileDos = new DataOutputStream(
                    new FileOutputStream(Utilities.DOWNLOADSDIR + path));
            int received = read - offset + 1;
            fileDos.write(responseBuffer, offset + 1, received);
            while(received < size){
                buffer = new byte[1024];
                read = dis.read(buffer);
                fileDos.write(buffer, 0, read);
                fileDos.flush();
                received += read;
            }
            fileDos.close();//file dos
            dis.close();//socket dis
            dos.close();//socket dos
        } catch(Exception ex){
            System.err.println("Fatal error: "+ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        return contentType; // MIME type on success
    }
}
