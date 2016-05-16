import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class Start {

	public Start() {
		// TODO Auto-generated constructor stub
	}
	
	public final static int maxLEGO = 12;
	public final static byte minBrickLenght = 3;
	public final static byte maxBrickLenght = 3;
	
	private static long lastTime = 1;
	private static long lastLastTime = 1;
	private static int[] times = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private static int[] maxconfignums = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	public static void main(String[] args) {
		
		System.out.println(System.getProperty("java.runtime.version")+" lolfisk "+3/2);
		for(int j = minBrickLenght; j<= maxBrickLenght;j++){
			times = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			maxconfignums = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			for(int i = maxLEGO; i<=maxLEGO; i++){
				System.out.println("Building with " + i + " 1x" + j + " LEGOS");
				Configuration.brickLenght = (byte)j;
				long startTime = System.currentTimeMillis();
				TransferMatrix TM = new TransferMatrix(i);
				long time = System.currentTimeMillis() - startTime; 
				lastLastTime = Math.max(1, lastTime);
				lastTime = time;
				maxconfignums[i] = TM.maxNumConfigs;
				times[i] = (int)time;
				System.out.println( "\ntime: " + time + "\n");
				if(lastTime*lastTime/lastLastTime > 1800000){
					System.out.print( "TIMES: ");printArray(times);
					System.out.print( "MAX CONFIGS: ");printArray(maxconfignums);
					System.out.print("\n");
					break;
				}
				
			}
		}
		
		
	}
	private static void printArray(int[] array){
		System.out.print("[");
		for(int i = 0; i< array.length; i++){
			System.out.print(array[i] + ",");
		}
		System.out.print("]\n");
	}
	
}
