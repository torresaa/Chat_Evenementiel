/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client.test;

import chat.client.Client;
import chat.client.ClientException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aquilest
 */
public class Client1 {

    public static void main(String args[]) throws InterruptedException {
        try {
            Client client = new Client("Jorge",5966, 5970);
            /**
             * NioEngine.listen, init NioServer over server
             */
            client.initClient("localhost", 5000);
            while (true) {
                //Thread.sleep(2000);
            }
        } catch (ClientException ex) {
            Logger.getLogger(Client1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
