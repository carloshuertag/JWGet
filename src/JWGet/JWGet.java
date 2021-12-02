package JWGet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
        System.out.println("Follow references recursively? (y/n)");
        char recursive = scanner.nextLine().charAt(0);
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
            initWorkingDir(); // if it is the first time that the program is executed
            File file = new File(Properties.DOWNLOADSPATH + getParentPath(path));
            file.mkdirs();
            file.setWritable(true);
            System.out.println(request);
            System.out.println(responseHeader);
            DataOutputStream fileDos = new DataOutputStream(
                    new FileOutputStream(Properties.DOWNLOADSDIR + path));
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
    }
    
    private static void initWorkingDir(){
        File file = new File(Properties.DOWNLOADSDIR);
            file.mkdir();
            file.setWritable(true);
    }
    
    private static String getParentPath(String path){
        int end = path.lastIndexOf("/");
        return path.substring(0, end);
    }
    
    private static String getPath(StringTokenizer tokenizer){
        String path;
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("/");
        while(tokenizer.hasMoreTokens()){
            pathBuilder.append(tokenizer.nextToken());
            pathBuilder.append("/");
        }
        path = pathBuilder.toString();
        if(path.charAt(path.length() - 1) == '/') path += "index.html";
        return path;
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
