/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

/**
 *
 * @author aquilest
 */
public class SocketChannelCloseException extends Exception {

    private static final long serialVersionUID = 1L;

    String message;

    public SocketChannelCloseException() {
    }

    public SocketChannelCloseException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
