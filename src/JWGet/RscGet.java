package JWGet;

/**
 *
 * @author huert
 */
public class RscGet extends Thread {
    private final String host, path;
    private final int port;

    public RscGet(String host, String path, int port) {
        this.host = host;
        this.path = path;
        this.port = port;
    }

    @Override
    public void run() {
        Utilities.getResource(host, port, path);
    }
    
}
