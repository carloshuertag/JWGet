package JWGet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author huert
 */
public class ClientPool extends Thread {
    
    protected Thread runningThread= null;
    protected final ExecutorService pool = Executors.newFixedThreadPool(
            Utilities.N_THREADS);
    private final ArrayList<String> paths = new ArrayList<>(),
            gottenPaths = new ArrayList<String>();

    public ClientPool() {
    }

    @Override
    public void run() {
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
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
        System.out.println(Utilities.DOWNLOADSPATH);
        if((recursive == 'y' || recursive == 'Y') && contentType.contains("htm")){
            getRefencesRecursion(host, port, path, 0);
            System.exit(0);
        }
        else System.out.println("Resource now available at: " +
                Utilities.DOWNLOADSPATH + path);
    }
    
    private void getRefencesRecursion(String host, int port, String path,
            int count){
        if(count < Utilities.RECURSION_LIMIT){
            fillPathsList(path);
            String pth;
            for(int i = 0; i < paths.size(); i++) {
                pth = paths.get(i);
                if(!gottenPaths.contains(pth)){
                    String newPath = pth;
                    if(newPath.charAt(newPath.length() - 1) == '/')
                        newPath += "index.html";
                    if(newPath.contains("htm")){
                        Utilities.getResource(host, port, pth);
                        gottenPaths.add(pth);
                        getRefencesRecursion(host, port, newPath, count + 1);
                    } else pool.execute(new RscGet(host, port, pth));
                }
            }
        }
    }
    
    private void fillPathsList(String path){
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
                    if(srcSplit[i].contains("?")) continue;
                    tmp = getHtmlPath(srcSplit[i]);
                    if(tmp != null) {
                        aux = replacePath(path, tmp);
                        if(!paths.contains(aux)) paths.add(aux);
                        if (tmp.startsWith("/"))
                            line = line.replaceFirst(tmp,
                                    Utilities.DOWNLOADSPATH + aux);
                    }
                }
                for(i = 1; i < hrefSplit.length; i++) {
                    if(hrefSplit[i].contains("?")) continue;
                    tmp = getHtmlPath(hrefSplit[i]);
                    if(tmp != null && (tmp.contains(".") || (tmp.charAt(
                            tmp.length() - 1) == '/'))) {
                        aux = replacePath(path, tmp);
                        if(!paths.contains(aux)) paths.add(aux);
                        if (tmp.startsWith("/"))
                            line = line.replaceFirst(tmp,
                                    Utilities.DOWNLOADSPATH + aux);
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
            System.err.println("Fatal error: "+ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    private String replacePath(String path, String pth){
        String newPath = Utilities.getParentPath(path);
        if (pth.startsWith("/")) return pth;
        int depth = 0;
        boolean flag = false;
        while(pth.startsWith("../")){
            depth++;
            pth = pth.substring(3);
            flag = true;
        }
        while(depth-- > 0) newPath = Utilities.getParentPath(newPath);
        return newPath + pth;
    }
    
    private String getHtmlPath(String line){
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
    
    private void initWorkingDir(){
        File file = new File(Utilities.DOWNLOADSDIR);
        file.mkdir();
        file.setWritable(true);
    }
    
    private String getPath(StringTokenizer tokenizer, String url){
        String path;
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("/");
        while(tokenizer.hasMoreTokens()){
            pathBuilder.append(tokenizer.nextToken());
            if(tokenizer.hasMoreTokens() || url.charAt(url.length() - 1) == '/')
                pathBuilder.append("/");
            else break;
        }
        path = pathBuilder.toString();
        return path;
    }
    
    public static void main(String args[]){
        ClientPool clientPool = new ClientPool();
        new Thread(clientPool).start();
    }
    
}
