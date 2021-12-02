package JWGet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.ArrayList;
/**
 *
 * @author huert
 */
public class JWGet {
    
    private static final ArrayList<String> tmp = new ArrayList<>();
    private static final ArrayList<String> paths = new ArrayList<>();
    
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
        if(path.charAt(path.length() - 1) == '/') path += "index.html";
        if((recursive == 'y' || recursive == 'Y') && contentType.contains("htm"))
            getRefencesRecursion(host, port, path);
        else System.out.println("Resource now available at: " + path);
    }
    
    private static void getRefencesRecursion(String host, int port, String path){
        fillPathsList(path);
        paths.forEach((pth) -> {
            if(pth.endsWith(".htm") || pth.endsWith(".html")) {
                Utilities.getResource(host, port, pth);
                getRefencesRecursion(host, port, pth);
            }
            System.out.println(pth);
            Utilities.getResource(host, port, pth);
       });
    }
    
    private static void fillTmpList(String path){
        try {
            BufferedReader br = new BufferedReader(new FileReader(Utilities.DOWNLOADSDIR + path));
            String line;
            int begin;
            while ((line = br.readLine()) != null) {
                begin = line.indexOf("src");
                if(begin != -1){
                    line = getHtmlPath(line, begin);
                    if(line != null) tmp.add(line);
                } else {
                    begin = line.indexOf("href");
                    if(begin != -1){
                        line = getHtmlPath(line, begin);
                        if(line != null && line.indexOf(".") != -1) tmp.add(line);
                    }
                }
            }
            br.close();
        } catch (Exception ex) {
            System.err.println("Fatal error: "+ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void fillPathsList(String path){
        fillTmpList(path);
        String newPath = Utilities.getParentPath(path);
        int depth = 0;
        boolean flag = false;
        for(String pth: tmp){
            while(pth.startsWith("../")){
                depth++;
                pth = pth.substring(3);
            }
            while(depth-- > 0){
                newPath = Utilities.getParentPath(newPath);
                flag = true;
            }
            newPath = (flag) ? newPath: Utilities.getParentPath(path);
            if(newPath.charAt(newPath.length() - 1) != '/') newPath += "/";
            pth = pth.startsWith("/") ? pth : newPath + path;
            paths.add(pth);
        }
    }
    
    private static String getHtmlPath(String line, int begin){
        String newPath = null;
        try {
            line = line.substring(begin);
            line = line.replace(" ", "");
            begin = line.indexOf("\"") + 1;
            newPath = line.substring(begin);
            newPath = newPath.substring(0, newPath.indexOf("\""));
        } catch(Exception ex) {
            newPath = null;
        }
        return newPath;
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
        return path;
    }
    
}
