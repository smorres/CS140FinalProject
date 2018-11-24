package project;

public class Data {
public static final int DATA_SIZE = 2048;
private int[] data = new int[DATA_SIZE];
private int changedIndex = -1;

int getChangedIndex() {
	return changedIndex;
}
int getData(int index) {
	if(index < 0 || index>DATA_SIZE)throw new MemoryAccessException("Illegal access to data memory, index \" + index -- see the files above. ");
	return data[index];
}
void setData(int index, int value) {
	if(index < 0 || index>DATA_SIZE)throw new MemoryAccessException("Illegal access to data memory, index \" + index -- see the files above. ");
	this.data[index] = value;
}
void clearData(int start, int end) {
	for (int i = start; i < end; i++) {
		data[i] = 0;
	}
	changedIndex = -1;
}

}
