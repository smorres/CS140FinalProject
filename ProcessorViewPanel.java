package projectview;

import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import project.Model;

public class ProcessorViewPanel {
	private Model model;
	private JTextField acc = new JTextField();
	private JTextField ip = new JTextField();
	private JTextField base = new JTextField();

	public ProcessorViewPanel(Model mdl) {
		model = mdl;
	}

	public JComponent createProcessorDisplay() {
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new GridLayout(1, 0));
		jpanel.add(new JLabel("Accumulator: ", JLabel.RIGHT));
		jpanel.add(acc);
		jpanel.add(new JLabel("Instruction Pointer: ", JLabel.RIGHT));
		jpanel.add(ip);
		jpanel.add(new JLabel("Memory Base: ", JLabel.RIGHT));
		jpanel.add(base);
		return jpanel;
	}

	public void update(String arg1) {
		if (model != null) {
			acc.setText("" + model.getAccum());
			ip.setText("" + model.getInstrPtr());
			base.setText("" + model.getMemBase());
		}
		
	}

	public static void main(String[] args) {
		Model model = new Model();
		ProcessorViewPanel panel = new ProcessorViewPanel(model);
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700, 60);
		frame.setLocationRelativeTo(null);
		frame.add(panel.createProcessorDisplay());
		frame.setVisible(true);
	}
}
