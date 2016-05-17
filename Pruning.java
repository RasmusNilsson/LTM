
/**
 * This class checks if a signature can be discarded or must be kept in memory.
 * @author Rasmus Mølck Nilsson
 *
 */
public class Pruning {
	
	
	public static byte[] finishSceme;
	
	public static void init(int buildingBoxWidth){
		finishSceme = new byte[buildingBoxWidth * 2];
	}
	
	/**
	 * Checks if a signature can be discarded.
	 * @param signature
	 * @param maxLEGO
	 * @param layer
	 * @param row
	 * @return true if the provided signature can never contribute to the final count.
	 */
	public static boolean canPrune(Signature signature,int maxLEGO,int layer,int row){
		if(signature.minimalBrickNumber >= maxLEGO){
			return true;
		}
		if(!signature.touchedLeft){
			int numLEGOsNeededLeft = computeNumLegosNeededLeft(signature);
			
			if(numLEGOsNeededLeft + signature.minimalBrickNumber + computeNumberOfLEGOsToComplete(signature,row) > maxLEGO){
				return true;
			}
		}
		else if(signature.minimalBrickNumber + computeNumberOfLEGOsToComplete(signature,row) >maxLEGO){
			return true;
		}
		
		
		return false;
	}
    
	public static int computeNumLegosNeededLeft(Signature s){
		for(int i = 0; i<Signature.codeLength; i++){
			if(s.get(i) == TM.START){return (i+Signature.brickLenght-2)/(Signature.brickLenght-1);}
		}
		return -1;
	}
	
	public static int firstENDorMID(Signature s){
		for(int i = 0; i<Signature.codeLength; i++){
			if(s.get(i) == TM.END || s.get(i) == TM.MID){return i;}
		}
		return -1;
	}
	
	private static int computeNumberOfLEGOsToComplete(Signature s,int row){
		
		int firstEND = firstENDorMID(s);
		int ti = firstEND;
		for(int i = firstEND+1; i<Signature.codeLength; i++){
			if(s.get(i) == TM.END || s.get(i) == TM.MID){
				if(s.get(ti) == TM.END){
					finishSceme[connectionNum(i,ti,row)] += 1;
				}
					ti = i;
			}
			else if(s.get(i) == TM.START){
				finishSceme[connectionNum(i,ti,row)] += 1;
				ti = i;
			}
		}
		int extraComponents = s.numExtraComponents();
		int returnVal = 0;
		for(int i = 0; i<finishSceme.length; i++){
			if(extraComponents == 0){
				break;
			}
			if(finishSceme[i] != 0){
				returnVal += i;
				finishSceme[i]--;
				
				extraComponents--;
				i--;
			}
		}
		for(int i = 0; i<finishSceme.length; i++){
			finishSceme[i] = 0;
		}
		return returnVal;
	}
	
	private static int connectionNum(int i, int ti, int row){
		int di = i-ti;
		return (di+Signature.brickLenght-2)/(Signature.brickLenght-1);
	}
	
	

}
