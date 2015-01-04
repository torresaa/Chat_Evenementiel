/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server.test;

import chat.server.ManagementServer;
import chat.server.ServerException;

/**
 *
 * @author aquilest
 */
public class ServerMain {

    public static void main(String args[]) {
        try {
            /**
             * New NioEngine and initializations
             */
            ManagementServer server = new ManagementServer();
            /**
             * NioEngine.listen, init NioServer over server
             */
            server.initServer();
            System.out.println("Exit");
            while(true){
            }
        } catch (ServerException ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
