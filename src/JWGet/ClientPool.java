/*
 * To change this license header, choose License Headers in Project Utilities.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JWGet;

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

    public ClientPool() {
    }

    @Override
    public void run() {
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
    }
    
    public static void main(String args[]){
        ClientPool clientPool = new ClientPool();
        new Thread(clientPool).start();
    }
    
}
