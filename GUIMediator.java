package projectview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import project.Model;
import project.CodeAccessException;
import project.Data;
import project.DivideByZeroException;
import project.MemoryAccessException;

public class GUIMediator {
	private Model model;
	private FilesMgr filesMgr;
	private TimerControl stepControl;
	private JFrame frame;
	private CodeViewPanel codeViewPanel;
	private DataViewPanel dataViewPanel1;
	private DataViewPanel dataViewPanel2;
	private DataViewPanel dataViewPanel3;
	private ControlPanel controlPanel;
	private ProcessorViewPanel processorPanel;
	private MenuBarBuilder menuBuilder;
	States s = model.getCurrentState(); 
	public Model getModel() {
		return this.model;
	}

	public void setModel(Model m) {
		this.model = m;
	}

	public JFrame getFrame() {
		return this.frame;
	}

	public void changeToJob(int i) {
		model.changeToJob(i);
		if (s != null) {
			s.enter();
			notifyObservers("");
		}
	}

	private void notifyObservers(String str) {
		codeViewPanel.update(str);
		dataViewPanel1.update(str);
		dataViewPanel2.update(str);
		dataViewPanel3.update(str);
		controlPanel.update(str);
		processorPanel.update(str);
	}

	public void clearJob() {
		int codeSize = model.getCurrentJob().getCodeSize();
		model.clearJob();
		model.setCurrentState(States.NOTHING_LOADED);
		model.getCurrentState().enter();
		notifyObservers("Clear " + codeSize);
	}

	public void makeReady(String s) {
		stepControl.setAutoStepOn(false);
		model.setCurrentState(States.PROGRAM_LOADED_NOT_AUTOSTEPPING);
		model.getCurrentState().enter();
		notifyObservers(s);
	}

	public void setCurrentState(States s) {
		if (s == States.PROGRAM_HALTED) {
			stepControl.setAutoStepOn(false);
		}
		model.setCurrentState(s);
		model.getCurrentState().enter();
		notifyObservers("");
	}

	public States getCurrentState() {
		return model.getCurrentState();
	}

	public void toggleAutoStep() {
		stepControl.toggleAutoStep();
		if (stepControl.isAutoStepOn()) {
			model.setCurrentState(States.AUTO_STEPPING);
		} else {
			model.setCurrentState(States.PROGRAM_LOADED_NOT_AUTOSTEPPING);
		}
	}

	public void reload() {
		stepControl.setAutoStepOn(false);
		clearJob();
		filesMgr.finalLoad_ReloadStep(model.getCurrentJob());
	}

	public void assembleFile() {
		filesMgr.assembleFile();
	}

	public void loadFile() {
		filesMgr.loadFile(model.getCurrentJob());
	}

	public void setPeriod(int value) {
		stepControl.setPeriod(value);
	}

	public void step() {
		if (model.getCurrentState() != States.PROGRAM_HALTED && model.getCurrentState() != States.NOTHING_LOADED) {
			try {
				model.step();
			} catch (MemoryAccessException e) {
				JOptionPane.showMessageDialog(frame, "Illegal access to data from line " + model.getInstrPtr() + "\n"
						+ "Exception message: " + e.getMessage(), "Run time error", JOptionPane.OK_OPTION);
			} catch (CodeAccessException e) {
				// copy the previous catch details but cahnge "data" to "code"
			} catch (NullPointerException e) {
				// copy the previous catch, changing "Illegal access to data" to
				// "NullPointerException"
			} catch (IllegalArgumentException e) {
				// copy the previous catch, changing "Illegal access to data" to "Program Error"
			} catch (DivideByZeroException e) {
				// copy the previous catch, changing "Illegal access to data" to "Divide by
				// zero"
			}
			notifyObservers("");
		}
	}

	public void execute() {
		while (model.getCurrentState() != States.PROGRAM_HALTED && model.getCurrentState() != States.NOTHING_LOADED) {
			try {
				model.step();
			} catch (MemoryAccessException e) {
				JOptionPane.showMessageDialog(frame, "Illegal access to data from line " + model.getInstrPtr() + "\n"
						+ "Exception message: " + e.getMessage(), "Run time error", JOptionPane.OK_OPTION);
			} catch (CodeAccessException e) {
				// copy the previous catch details but cahnge "data" to "code"
			} catch (NullPointerException e) {
				// copy the previous catch, changing "Illegal access to data" to
				// "NullPointerException"
			} catch (IllegalArgumentException e) {
				// copy the previous catch, changing "Illegal access to data" to "Program Error"
			} catch (DivideByZeroException e) {
				// copy the previous catch, changing "Illegal access to data" to "Divide by
				// zero"
			}
			
		}notifyObservers("");
	}

	// some complete methods:
	private void createAndShowGUI() {
		stepControl = new TimerControl(this);
		filesMgr = new FilesMgr(this);
		filesMgr.initialize();
		codeViewPanel = new CodeViewPanel(model);
		dataViewPanel1 = new DataViewPanel(model, 0, 240);
		dataViewPanel2 = new DataViewPanel(model, 240, Data.DATA_SIZE / 2);
		dataViewPanel3 = new DataViewPanel(model, Data.DATA_SIZE / 2, Data.DATA_SIZE);
		controlPanel = new ControlPanel(this);
		processorPanel = new ProcessorViewPanel(model);
		menuBuilder = new MenuBarBuilder(this);
		frame = new JFrame("Simulator");

		JMenuBar bar = new JMenuBar();
		frame.setJMenuBar(bar);
		bar.add(menuBuilder.createFileMenu());
		bar.add(menuBuilder.createExecuteMenu());
		bar.add(menuBuilder.createJobsMenu());

		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout(1, 1));
		content.setBackground(Color.BLACK);
		frame.setSize(1200, 600);
		frame.add(codeViewPanel.createCodeDisplay(), BorderLayout.LINE_START);
		frame.add(processorPanel.createProcessorDisplay(), BorderLayout.PAGE_START);
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(1, 3));
		center.add(dataViewPanel1.createMemoryDisplay());
		center.add(dataViewPanel2.createMemoryDisplay());
		center.add(dataViewPanel3.createMemoryDisplay());
		frame.add(center, BorderLayout.CENTER);
		frame.add(controlPanel.createControlDisplay(), BorderLayout.PAGE_END);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(WindowListenerFactory.windowClosingFactory(e -> exit()));
		frame.setLocationRelativeTo(null);
		model.setCurrentState(States.NOTHING_LOADED);
		model.getCurrentState().enter();
		stepControl.start();
		notifyObservers("");
		frame.setVisible(true);
	}

	public void exit() { // method executed when user exits the program
		int decision = JOptionPane.showConfirmDialog(frame, "Do you really wish to exit?", "Confirmation",
				JOptionPane.YES_NO_OPTION);
		if (decision == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIMediator organizer = new GUIMediator();
				Model model = new Model(() -> organizer.setCurrentState(States.PROGRAM_HALTED));
				organizer.setModel(model);
				organizer.createAndShowGUI();
			}
		});
	}
}
