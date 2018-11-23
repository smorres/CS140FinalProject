package project;
public class CodeAccessException extends RuntimeException {
	private static final long serialVersionUID = 2663580265549951229L;
	/**
     * No-argument constructor
     */
    public CodeAccessException() {
        super();
    }
    /**
     * Preferred constructor that sets the inherited message field
     * of the exception object
     * @param arg0 message passed by the exception that was thrown
     */
    public CodeAccessException(String arg0) {
        super(arg0);
    }
}