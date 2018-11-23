package projectview;

public enum States {
	AUTO_STEPPING, 
	NOTHING_LOADED,
	PROGRAM_HALTED,
	PROGRAM_LOADED_NOT_AUTOSTEPPING;
	
	public void enter() {}
}
