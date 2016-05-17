
/**
 * The class responsible for starting the enumeration.
 * Set the values below as you please and the program will enumerate flat LEGO structures,
 * either strict or not with n bricks of length w for all n less than maxNumberOfBricks
 * and for all w between minBrickLenght and maxBrickLenght.
 * @author Rasmus Mølck nilsson
 *
 */
public class Start {

	public Start() {
		// TODO Auto-generated constructor stub
	}
	public final static boolean strict = false;
	public final static int maxNumberOfBricks = 14;
	public final static byte minBrickLenght = 3;
	public final static byte maxBrickLenght = 3;
	
	public static void main(String[] args) {
		
		System.out.println("RUNNING LTR:");
		
		for(byte w = minBrickLenght; w<= maxBrickLenght;w++){
			System.out.println("Building with " + maxNumberOfBricks + " 1x" + w + " LEGOS");
			long startTime = System.currentTimeMillis();
			new TM(w,maxNumberOfBricks);
			long time = System.currentTimeMillis() - startTime; 
			System.out.println( "\ntime: " + time + "\n");
		}	
	}
	
}
