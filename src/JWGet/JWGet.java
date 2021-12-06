package JWGet;

import java.io.BufferedReader;
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
    
    private static final ArrayList<String> paths = new ArrayList<>(),
            gottenPaths = new ArrayList<String>();
    
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
        String path = getPath(tokenizer, arg);
        initWorkingDir();
        String contentType = Utilities.getResource(host, port, path);
        if(path.charAt(path.length() - 1) == '/') path += "index.html";
        if((recursive == 'y' || recursive == 'Y') && contentType.contains("htm"))
            getRefencesRecursion(host, port, path, 0);
        System.out.println("Resources now available at: " + Utilities.DOWNLOADSPATH);
        System.exit(0);
    }
    
    private static void getRefencesRecursion(String host, int port, String path, int count){
        if(count < Utilities.RECURSION_LIMIT){
            fillPathsList(path);
            String pth;
            for(int i = 0; i < paths.size(); i++) {
                pth = paths.get(i);
                if(!gottenPaths.contains(pth)){
                    String newPath = pth;
                    if(newPath.charAt(newPath.length() - 1) == '/') {
                        newPath += "index.html";
                    }
                    Utilities.getResource(host, port, pth);
                    gottenPaths.add(pth);
                    if(newPath.contains("htm")) {
                        getRefencesRecursion(host, port, newPath, count + 1);
                    }
                }
            }
        } else if(count == Utilities.RECURSION_LIMIT &&
                (path.charAt(path.length() - 1) == '/'
                || path.contains("htm"))) {
            fillPathsList(path);
        }
    }
    
    private static void fillPathsList(String path){
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    Utilities.DOWNLOADSDIR + path));
            StringBuffer fileContent = new StringBuffer();
            String[] srcSplit, hrefSplit;
            String line, aux, tmp;
            int i;
            while ((line = br.readLine()) != null) {
                srcSplit = line.split("src");
                hrefSplit = line.split("href");
                for(i = 1; i < srcSplit.length; i++) {
                    if(srcSplit[i].contains("?")) {
                        continue;
                    }
                    tmp = getHtmlPath(srcSplit[i]);
                    if(tmp != null) {
                        aux = replacePath(path, tmp);
                        if(!paths.contains(aux)) {
                            paths.add(aux);
                        }
                        if (tmp.startsWith("/")) {
                            line = line.replaceFirst(tmp,
                                    (Utilities.DOWNLOADSPATH + aux).replace(
                                            Utilities.SLASH, Utilities.ENCODING_SLASH));
                        }
                    }
                }
                for(i = 1; i < hrefSplit.length; i++) {
                    if(hrefSplit[i].contains("?")) {
                        continue;
                    }
                    tmp = getHtmlPath(hrefSplit[i]);
                    if(tmp != null && (tmp.contains(".") || (tmp.charAt(
                            tmp.length() - 1) == '/'))) {
                        aux = replacePath(path, tmp);
                        if(!paths.contains(aux)) {
                            paths.add(aux);
                        }
                        if (tmp.startsWith("/")){
                            aux = (Utilities.DOWNLOADSPATH + aux).replace(
                                    Utilities.SLASH, Utilities.ENCODING_SLASH);
                            if(tmp.endsWith("/")) {
                                aux += "index.html";
                            }
                            line = line.replaceFirst(tmp, aux);
                        } else if(tmp.endsWith("/")){
                            aux = tmp + "index.html";
                            line = line.replaceFirst(tmp, aux);
                        }
                    }
                }
                fileContent.append(line);
                fileContent.append("\n");
            }
            br.close();
            FileOutputStream fos = new FileOutputStream(Utilities.DOWNLOADSDIR +
                    path);
            fos.write(fileContent.toString().getBytes());
            fos.flush();
            fos.close();
        } catch (Exception ex) {
        }
    }
    
    private static String replacePath(String path, String pth){
        if (pth.startsWith("/")) {
            return pth;
        }
        String newPath = Utilities.getParentPath(path);
        int depth = 0;
        while(pth.startsWith("../")){
            depth++;
            pth = pth.substring(3);
        }
        while(depth-- > 0) newPath = Utilities.getParentPath(newPath);
        return newPath + pth;
    }
    
    private static String getHtmlPath(String line){
        String newPath;
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
    
    private static String getPath(StringTokenizer tokenizer, String url){
       String path;
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("/");
        while(tokenizer.hasMoreTokens()){
            pathBuilder.append(tokenizer.nextToken());
            if(tokenizer.hasMoreTokens() || url.charAt(url.length() - 1) == '/') {
                pathBuilder.append("/");
            } else {
                break;
            }
        }
        path = pathBuilder.toString();
        return path;
    }
    
}
