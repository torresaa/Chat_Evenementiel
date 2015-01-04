/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors;

/**
 *
 * @author aquilest
 */
public class Utils {

    /**
     * Convert a byte array integer (4 bytes) to its int value
     *
     * @param b byte[]
     * @return int
     */
    public static int byteArrayToInt(byte[] b) {
        if (b.length == 4) {
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
                    | (b[3] & 0xff);
        } else if (b.length == 2) {
            return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);
        }

        return 0;
    }

    /**
     * Convert an integer value to its byte array representation
     *
     * @param value int
     * @return byte[]
     */
    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
}
