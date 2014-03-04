package powercraft.api.script.weasel;


public interface PC_WeaselEngine {

	public void run(int numInstructions, int numBlocks);
	
	public void callMain(String className, String methodName, Object...params) throws NoSuchMethodException;
	
	public byte[] save();
	
}