/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client;

import java.util.BitSet;

/**
 *  Describes the msg waiting to be deliver
 * 
 */
public class ReceivedMsg implements Comparable<ReceivedMsg>{
    
    public static final long WAIT_TIME_MILIS = 2000;
    
    private long lcMessage;
    private BitSet ack;
    private byte[] data;
    private BitSet activeUsers; // Active users in the moment we receive the msg
    private long timeReception; 
    
    public ReceivedMsg(long lc, BitSet activeUsers, byte[] data){
        this.lcMessage = lc;
        this.ack = new BitSet(activeUsers.length());
        this.activeUsers = (BitSet) activeUsers.clone();
        this.data = data;
        this.timeReception = System.currentTimeMillis();
    }
    
    public long getLcMessage(){
        return this.lcMessage;
    }
    
    public byte[] getData(){
        return this.data;
    }
    
    public void setIncomingAck(int indexUser){
        this.ack.set(indexUser);
    }
    
    public BitSet getAck(){
        return this.ack;
    }
    
    public long getTimeReception(){
        return this.timeReception;
    }
    
    public boolean allAcksComplete(){
        BitSet tempAcks = (BitSet)this.ack.clone();
        tempAcks.and(this.activeUsers);
        return tempAcks.cardinality() == this.activeUsers.cardinality();
    }
    
    @Override
    public int compareTo(ReceivedMsg t) {
        if (this.lcMessage == t.getLcMessage()){
            return 0;
        }else{
            if(this.lcMessage < t.getLcMessage()){
                return -1;
            }else{
                return 1;
            }
        }
    }
    
}
