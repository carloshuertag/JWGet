package JWGet;

/**
 *
 * @author huert
 */
public class RscGet extends Thread {
    private final String host, path;
    private final int port;

    public RscGet(String host, int port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
    }

    @Override
    public void run() {
        Utilities.getResource(host, port, path);
    }
    
}
