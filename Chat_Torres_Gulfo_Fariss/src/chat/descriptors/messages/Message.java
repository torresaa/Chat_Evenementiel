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
public abstract class Message {
    
    //Msg client-server
    public static final int LISTENING_PORT_MSG = 11;
    public static final int GROUP_FINISH_MSG = 12;
    public static final int ACK_SERVER = 13;
    public static final int GROUP_MEMBER_MSG = 14;
    public static final int DELIVER_MSG = 15;
    
    //Msg client-client
    public static final int DATA_MSG = 21;
    public static final int DATA_ACK_MSG = 22;
    public static final int MY_INDEX_MSG = 23;
    
    private byte[] msg;
    
    private int length;
    private int type;
    private byte[] payLoad;
    
    public void setLength(int length){
        this.length = length;
    }
    
    public byte[] getMessage(){
        return this.msg;
    }
    
    public void setType(int type){
        this.type = type;
    } 
    
    public int getType(){
        return this.type;
    }
    
    public void setPayload(byte[] payload){
        this.payLoad = payload;
    }
    
    /**
     * Format byte chain to send
     * LENGTH | TYPE | PAYLOAD
     *   4B      4B       -- 
     * LENGTH = TYPE + PAYLOAD
     */
    public void formatMsg(){
        this.formatPayload();
        this.msg = Utils.concat(Utils.intToByteArray(this.payLoad.length + 4),
                Utils.concat(Utils.intToByteArray(this.type), this.payLoad));
    }
    
    /**
     * Format byte string payload of message to send
     */
    abstract public void formatPayload();
    
}
