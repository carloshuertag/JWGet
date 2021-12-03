package JWGet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.ArrayList;
/**
 *
 * @author huert
 */
public class JWGet {
    
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
            getRefencesRecursion(host, port, path, 0);
        else System.out.println("Resource now available at: " + path);
    }
    
    private static void getRefencesRecursion(String host, int port, String path, int count){
        if(count < Utilities.RECURSION_LIMIT){
            fillPathsList(path);
            paths.forEach((pth) -> {
                System.out.println(pth);
                String contentType = Utilities.getResource(host, port, pth);
                if(contentType.contains("htm")) {
                    getRefencesRecursion(host, port, pth + "index.html", count + 1);
                }
            });
        }
    }
    
    private static void fillPathsList(String path){
        try {
            BufferedReader br = new BufferedReader(new FileReader(Utilities.DOWNLOADSDIR + path));
            //DataOutputStream dos = new DataOutputStream(new FileOutputStream(Utilities.DOWNLOADSDIR + path));
            String[] srcSplit, hrefSplit;
            String line, aux, tmp;
            int i;
            while ((line = br.readLine()) != null) {
                aux = line;
                srcSplit = line.split("src");
                hrefSplit = line.split("href");
                for(i = 1; i < srcSplit.length; i++) {
                    if(srcSplit[i].contains("?")) continue;
                    tmp = getHtmlPath(srcSplit[i]);
                    if(tmp != null) {
                        aux = replacePath(path, tmp);
                        if(!paths.contains(aux)) paths.add(aux);
                        line = (aux.startsWith("/")) ? 
                                    line.replaceFirst(tmp, Utilities.DOWNLOADSPATH + aux):
                                    line.replaceFirst(tmp, Utilities.getParentPath(path) + aux);
                    }
                }
                for(i = 1; i < hrefSplit.length; i++) {
                    if(hrefSplit[i].contains("?")) continue;
                    tmp = getHtmlPath(hrefSplit[i]);
                    if(tmp != null && (tmp.contains(".") || (tmp.charAt(tmp.length() - 1) == '/'))) {
                        aux = replacePath(path, tmp);
                        if(!paths.contains(aux)) paths.add(aux);
                        line = (aux.startsWith("/")) ? 
                                line.replaceFirst(tmp, Utilities.DOWNLOADSPATH + aux):
                                line.replaceFirst(tmp, Utilities.getParentPath(path) + aux);
                    }
                }
                //dos.writeUTF(line);
            }
            br.close();
            //dos.flush();
            //dos.close();
        } catch (Exception ex) {
            System.err.println("Fatal error: "+ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    private static String replacePath(String path, String pth){
        String newPath = Utilities.getParentPath(path);
        int depth = 0;
        while(pth.startsWith("../")){
            depth++;
            pth = pth.substring(3);
        }
        while(depth-- > 0) newPath = Utilities.getParentPath(newPath);
        if(newPath.charAt(newPath.length() - 1) != '/') newPath += "/";
        pth = pth.startsWith("/") ? pth : newPath + pth;
        return pth;
    }
    
    private static String getHtmlPath(String line){
        String newPath = null;
        int begin;
        try {
            line = line.replace(" ", "");
            begin = line.indexOf("\"") + 1;
            boolean sc = false;
            if(begin == 0){
                begin = line.indexOf("'") + 1;
                sc = true;
            }
            newPath = line.substring(begin);
            newPath = (sc) ? newPath.substring(0, newPath.indexOf("'")):
                    newPath.substring(0, newPath.indexOf("\""));
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
