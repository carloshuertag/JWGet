package JWGet;

import java.io.File;
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
        initWorkingDir();
        String contentType = Utilities.getResource(host, port, path);
        if((recursive == 's' || recursive == 'S') && contentType.contains("htm")){ //html file stored analysis
                try {
                     BufferedReader br = new BufferedReader(new FileReader(Utilities.DOWNLOADSDIR + path));
                     String str;
                     while ((str = br.readLine()) != null) {
                        if(str.contains("src")){
                            String nlink;
                            //Obtain the string of the new link
                            //exlinks.add(nlink);
                        }
                        else
                            if(str.contains("href")){
                                String nlink; 
                                //Obtain the string of the new link
                                //exlinks.add(nlink);
                            }
                     }
                
      } catch (Exception ex2) {
            System.err.println("Fatal error: "+ex2.getMessage());
            }
        }
    }
    
    private static void initWorkingDir(){
        File file = new File(Utilities.DOWNLOADSDIR);
            file.mkdir();
            file.setWritable(true);
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
    
}
