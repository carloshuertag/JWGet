package JWGet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author huert
 */
public class JWGet {
    
    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter URL: ");
        String arg = scanner.nextLine();
        if(!arg.toUpperCase().startsWith("HTTP")) {
            System.err.println("Unsupported protocol, http only.");
            System.exit(1);
        }
        StringTokenizer tokenizer = new StringTokenizer(arg, "/");
        tokenizer.nextToken();
        String host = tokenizer.nextToken();
        int p = host.indexOf(':'), port = 80;
        if(p != -1) {
            port = Integer.parseInt(host.substring(p + 1, host.length()));
            host = host.substring(0, p);
        }
        String path = getPath(tokenizer);
        String request = getHttpRequest(path, host, port);
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
            String contentType = responseHeader.substring(
                    responseHeader.indexOf("Content-Type: ") + 14);
            contentType = contentType.substring(0, contentType.indexOf("\r\n"));
            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer, offset, read);
            buffer = new byte[read - offset + 1];
            bais.read(buffer);
            
        } catch(Exception ex){
            System.err.println("Fatal error: "+ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    private static String getPath(StringTokenizer tokenizer){
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("/");
        while(tokenizer.hasMoreTokens()){
            pathBuilder.append(tokenizer.nextToken());
            pathBuilder.append("/");
        }
        return pathBuilder.toString();
    }
    
    private static String getHttpRequest(String rsc, String host, int port){
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
}
