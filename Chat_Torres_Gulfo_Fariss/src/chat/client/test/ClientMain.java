/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client.test;

import chat.client.Client;
import chat.client.ClientException;

/**
 *
 * @author aquilest
 */
public class ClientMain {

    public static void main(String args[]) throws InterruptedException {
        try {
            /**
             * New NioEngine and initializations
             */
            Client client = new Client(5959, 5970);
            /**
             * NioEngine.listen, init NioServer over server
             */
            client.initClient("localhost", 5000);
            System.out.println("Exit");
            while (true) {
                Thread.sleep(2000);
            }
        } catch (ClientException ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
