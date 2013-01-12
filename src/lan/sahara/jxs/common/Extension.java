package lan.sahara.jxs.common;

public class Extension {
    private final byte              majorOpcode;
    private final byte              firstEvent;
    private final byte              firstError;

    /**
     * Constructor.
     *
     * @param pmajorOpcode  Major opcode of the extension, or zero.
     * @param pfirstEvent   Base event type code, or zero.
     * @param pfirstError   Base error code, or zero.
     */
    public Extension (byte pmajorOpcode,byte pfirstEvent,byte pfirstError) {
            majorOpcode = pmajorOpcode;
            firstEvent = pfirstEvent;
            firstError = pfirstError;
    }
    public byte getMajorOpcode() {
    	return majorOpcode;
    }
    public byte getFirstEvent() {
    	return firstEvent;
    }
    public byte getFirstError() {
    	return firstError;
    }
    public String toString() {
    	return new String("[majorOpcode="+majorOpcode+",firstEvent="+firstEvent+",firstError="+firstError+"]");
    }
}
