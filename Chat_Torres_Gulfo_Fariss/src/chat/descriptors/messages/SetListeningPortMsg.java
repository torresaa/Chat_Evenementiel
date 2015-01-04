/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 *
 * @author aquilest
 */
public class SetListeningPortMsg extends Message{
    private int listeningPort;
    
    public SetListeningPortMsg (int port){
        this.listeningPort = port;
        this.setMessageBytes(Utils.intToByteArray(this.listeningPort));
    }    
    
}
