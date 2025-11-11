package pt.unl.fct.pds.utils;

public class GeneralUtils {

    // TODO: Add secure random instance

    // TODO: Maybe change function below to just take a subnet and add another function to extract the subnet to reduce redundancy when needing to check the same subent against more than one IP (like when getting middle node)
    /**
     *
     * @param ipAddress - IPv4 address in string format
     * @param otherIpAddress - IPv4 address in string format
     * @return true if both are in the same /16 subnet, false otherwise
     */
    public static boolean same16Subnet(String ipAddress, String otherIpAddress) {
        String[] splitIpGuard = ipAddress.split("\\.");
        String[] splitIpExit = otherIpAddress.split("\\.");
        assert splitIpGuard.length == splitIpExit.length && splitIpGuard.length == 4;
        return splitIpExit[0].equals(splitIpGuard[0]) && splitIpExit[1].equals(splitIpGuard[1]);
    }
}
