package project;
import projectview.States;
public class Job {
	private int id;
	private int startcodeIndex;
	private int codeSize;
	private int startmemoryIndex;
	private int currentPC;
	private int currentAcc;
	private States currentState = States.NOTHING_LOADED;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public States getCurrentState() {
		return currentState;
	}
	public void setCurrentState(States currentState) {
		this.currentState = currentState;
	}
	public int getCurrentAcc() {
		return currentAcc;
	}
	public void setCurrentAcc(int currentAccumulator) {
		this.currentAcc = currentAccumulator;
	}
	public int getStartcodeIndex() {
		return startcodeIndex;
	}
	public void setStartcodeIndex(int startcodeIndex) {
		this.startcodeIndex = startcodeIndex;
	}
	public int getCodeSize() {
		return codeSize;
	}
	public void setCodeSize(int codeSize) {
		this.codeSize = codeSize;
	}
	public int getStartmemoryIndex() {
		return startmemoryIndex;
	}
	public void setStartmemoryIndex(int startmemoryIndex) {
		this.startmemoryIndex = startmemoryIndex;
	}
	public int getCurrentPC() {
		return currentPC;
	}
	public void setCurrentPC(int currentPC) {
		this.currentPC = currentPC;
	}
	public void reset() {
		codeSize = 0;
		currentState = States.NOTHING_LOADED;
		currentAcc = 0;
		currentPC = startcodeIndex;
	}
}
