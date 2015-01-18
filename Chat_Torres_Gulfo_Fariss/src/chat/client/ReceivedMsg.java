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
    private long lcMessage;
    private BitSet ack;
    private byte[] data;
    private int numberOfAck;
    
    public ReceivedMsg(long lc, int numberOfMembers, byte[] data){
        this.lcMessage = lc;
        this.ack = new BitSet(numberOfMembers);
        this.data = data;
        this.numberOfAck = numberOfMembers;
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
    
    public boolean allAcksComplete(){
        if(this.ack.cardinality() == this.numberOfAck){
            return true;
        }
        return false;
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
