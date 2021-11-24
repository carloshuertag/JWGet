package JWGet;

import java.net.InetAddress;
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
        InetAddress ipAddress;
        int p = host.indexOf(':'), port = 80;
        if(p != -1) {
            port = Integer.parseInt(host.substring(p + 1, host.length()));
            host = host.substring(0, p);
        }
        String path = "/";
        if(tokenizer.hasMoreTokens()) path += tokenizer.nextToken();
        //ipAddress = InetAddress.getByName(host);
        System.out.println("host: "+host);
        System.out.println("port: "+port);
        System.out.println("path: "+path);
        System.out.println("request: "+getHttpRequest(path));
    }
    
    private static String getHttpRequest(String rsc){
        StringBuilder sb = new StringBuilder();
        sb.append("GET ");
        sb.append(rsc);
        sb.append(" HTTP/1.0\n");
        return sb.toString();
    }
}
