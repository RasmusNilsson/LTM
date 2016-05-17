/**
 *  The purpose of this class is purely cosmetic. 
 * It will print out a loading bar as the algorithm progresses.
 * 
 * Please ignore.
 * @author Rasmus Mølck Nilsson
 *
 */
public class LoadingBar {
	private static final int[] timeWeights = {50,1000,3500,2000,1000,300,200,50,30,20,15,10,6,5,4,3,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}; //{50,150,1300,2600,2000,750,350,200,100,50,25,12,6,5,4,3,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
	private static final String numbers = "0       10        20        30        40        50        60        70        80        90       100\n|";
	private static final int totalChars = 100;
	private static int accChars;
	private static long totalTime;
	private static long accumulatedTime;
	private static int boxWidth;
	
	public static void init(int numLegos,int buildingBoxWidth){
		System.out.print(numbers);
		boxWidth = buildingBoxWidth;
		setValues(numLegos);
	}
	
	public static void update(int layer){
		accumulatedTime += layer >= timeWeights.length ? 1 : timeWeights[layer];
		while(totalTime * accChars  < totalChars * accumulatedTime){
			if(accChars % 10 != 9){
				System.out.print("-");
			}
			else{
				System.out.print("|");
				if(accChars == 99){
					System.out.print(" : \n");
				}
			}
			accChars++;
		}
	}
	
	private static void setValues(int numLegos){
		accChars = 1;
		totalTime = 0;
		accumulatedTime = 0;
		for(int i = 0; i<numLegos;i++){
			totalTime += timeWeights[i]*boxWidth;
		}
	}
}
