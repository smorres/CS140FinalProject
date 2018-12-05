package projectview;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Consumer;
public class WindowListenerFactory {
	// Structure proposed by lead Java 8 developer Brian Goetz on
	// http://stackoverflow.com/questions/21833537/java-8-lambda-
	// expressions-what-about-multiple-methods-in-nested-class

    /**
     * Adapter to execute the specified behavior for a window closing event.
     * @param c expected lambda expression that will have access to the 
     * context where the code needs to execute. 
     * @return a WindowAdapter that has a specific behavior for closing
     * the Component that contains the lambda expression argument
     */
	static WindowListener windowClosingFactory(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                c.accept(e);
            }
        };
    }
}
