package pt.unl.fct.pds.utils;

public class HexUtils {

    private static final String digits = "0123456789abcdef";

    /**
     * Return string hexadecimal from byte array of certain size
     *
     * @param data : bytes to convert
     * @param length : nr of bytes in data block to be converted
     * @return  hex : hexadecimal representation of data
     */
    public static String toHex(byte[] data, int length)
    {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i != length; i++)
        {
            int	v = data[i] & 0xff;

            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0xf));
        }

        return buf.toString();
    }

    /**
     * Return data in byte array from string hexadecimal
     *
     * @param data : bytes to be converted
     * @return : hexadecimal representation of data
     */
    public static String toHex(byte[] data)
    {
        return toHex(data, data.length);
    }
}
