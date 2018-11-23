package project;
public class MemoryAccessException extends RuntimeException {
	private static final long serialVersionUID = -7710997064849420900L;
	/**
     * No-argument constructor
     */
    public MemoryAccessException() {
        super();
    }
    /**
     * Preferred constructor that sets the inherited message field
     * of the exception object
     * @param arg0 message passed by the exception that was thrown
     */
    public MemoryAccessException(String arg0) {
        super(arg0);
    }
}