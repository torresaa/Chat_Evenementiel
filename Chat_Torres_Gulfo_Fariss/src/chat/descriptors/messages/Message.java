/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

/**
 *
 * @author aquilest
 */
public abstract class Message {
    private byte[] msg;
    
    public void setMessageBytes(byte[] msg){
        this.msg = msg;
    }
    
    public byte[] getMessage(){
        return this.msg;
    }
    
}
