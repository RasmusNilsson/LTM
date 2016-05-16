

public class Pruning {
	
	public static byte[] finishSceme;

	
	public Pruning() {
		// TODO Auto-generated constructor stub
	}
	
	public static boolean prune(Configuration conf,int maxLEGO,int layer,int row){
		if(conf.minimalBrickNumber >= maxLEGO){
			return true;
		}
		if(!conf.touchedRight){
			int numLEGOsNeededRight = numLEGOsToRight(conf);
			/*if(layer == maxLEGO-1){
				//System.out.println("GET WRECKED!");
				return true;
			}
			else if(layer == maxLEGO-2){
				if(numLEGOsNeededRight > 1){
					return true;
				}
			}*/
			if(numLEGOsNeededRight + conf.minimalBrickNumber + computeNumberOfLEGOsToComplete(conf,row) > maxLEGO){
				//System.out.println("YO, minLEGOnum: " + conf.minimalBrickNumber);
				return true;
			}
		}
		else if(conf.minimalBrickNumber + computeNumberOfLEGOsToComplete(conf,row) >maxLEGO){
			return true;
		}
		
		
		return false;
	}
    
	public static int numLEGOsToRight(Configuration conf){
		for(int i = 0; i<Configuration.codeLength; i++){
		
			if(conf.code(i) == TransferMatrix.START){return (i+Configuration.brickLenght-2)/(Configuration.brickLenght-1);}
		}
		return -1;
	}
	
	public static int firstENDorMID(Configuration conf){
		for(int i = 0; i<Configuration.codeLength; i++){
			if(conf.code(i) == TransferMatrix.END || conf.code(i) == TransferMatrix.MID){return i;}
		}
		return -1;
	}
	
	private static int connectionNum(int i, int ti, int row){
		int di = i-ti;
		int returnVal = (di+Configuration.brickLenght-2)/(Configuration.brickLenght-1);
		/*if( (di & 1) == 0 ) { // even
			if(row <ti || row >= i){
				//returnVal+= 1;
			}
		}
		//odd
		else if(row >= ti && row < i){
			//returnVal += 2;
		}*/
		
		return returnVal;
	}
	
	private static int computeNumberOfLEGOsToComplete(Configuration conf,int row){
		
		int firstEND = firstENDorMID(conf);
		int ti = firstEND;
		for(int i = firstEND+1; i<Configuration.codeLength; i++){
			if(conf.code(i) == TransferMatrix.END || conf.code(i) == TransferMatrix.MID){
				if(conf.code(ti) == TransferMatrix.END){
					finishSceme[connectionNum(i,ti,row)] += 1; // i-ti
					//System.out.println("i: " + (i-ti));
				}
					ti = i;
			}
			else if(conf.code(i) == TransferMatrix.START){
				finishSceme[connectionNum(i,ti,row)] += 1;
				ti = i;
			}
		}
		int extraComponents = conf.numExtraComponents();
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

}
