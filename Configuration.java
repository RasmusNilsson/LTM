public class Configuration {

	public static int codeLength;
	public static byte brickLenght = 3;
	
	//private byte[] code;
	public int[] bitCode;
	public boolean touchedRight = false;
	//public boolean touchedLeft = false;
	public byte placeNum = 1;
	public byte lastOneOverritten;
	public Configuration next;
	
	public static final int longIndex = 9;
	public static final int[] hashMultipliers = {7,17,3,11,2,1,13,19,15,9,15};
	
	public byte minimalBrickNumber;
	
	public int[] PGF; // partial generating function
	public long[] PGFext;
	
	public Configuration(int length) {
		codeLength = (brickLenght-1)*length+1;
		bitCode = new int[codeLength/16+1];
		//code = new byte[(length+1)]; // maybe +1
		PGF = new int[Math.min(length+1,longIndex)];
		PGFext = new long[Math.max(length + 1 - longIndex,1)];
	}
	/*
	 * 
	 * Layers processed , 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
FINAL: 00000000000000000 --- 0,1,3,11,44,186,814,3656,16731,77705,365095,1731797,8279363,39845689,192852359,937986507,4581678031,
N: 16 : 16
time: 1614

Layers processed , 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
FINAL: 000000000000000000 --- 0,1,3,11,44,186,814,3656,16731,77705,365095,1731797,8279363,39845689,192852359,937986507,4581678031,22464030959,
N: 17 : 17
time: 3895

Layers processed , 0, 1, 2
	 */
	public void reset(){
		/*for(int i = 0; i<codeLength; i++){
			code[i] = 0;
			
		}
		for(int i = 0; i<PGF.length; i++){
			PGF[i] = 0;
		}
		for(int i = 0; i<PGFext.length; i++){
			PGFext[i] = 0;
		}*/
		java.util.Arrays.fill(bitCode, (byte)0); // TODO
		//code2 = 0;
		java.util.Arrays.fill(PGF, 0);
		java.util.Arrays.fill(PGFext, 0L);
		touchedRight = false;
		//touchedLeft = false;
		placeNum = 1;
		lastOneOverritten = 0;
		next = null;
		minimalBrickNumber = 0;
	}
	

	public byte code(int i){
		//int bit = i << 2;
		int arrayIndex = i>>4;
		int subIndex = i-(arrayIndex<<4);
		
		return (byte)((bitCode[arrayIndex] << (30 - (subIndex * 2))) >>> 30);
		
		/*
		if((i & 1) == 0){
			return (byte)( code[(i)/2] & 15);
		}
		else{
			return (byte)(code[(i)/2] >>> 4);
			 // eller code[(i+1)/2] & 15
		}
		*/
		
	}
	public void codeSet(int i, byte b){
		
		int arrayIndex = i>>4;
		int subIndex = i-(arrayIndex<<4);
		
		long bit = subIndex * 2;
		int mask = ((int)3 << bit);
		bitCode[arrayIndex] &= ~mask;
		bitCode[arrayIndex] |= ((long)b) << bit;
	
	}
	
	public boolean codeIs(int i, byte b){
		return code(i) == b;
	}
	
	public Configuration copy(){
		Configuration copy = TransferMatrix.instance.getConfFromHeap();
		//copyCodeTo(this.code,copy.code);
		copyCodeTo(this.bitCode,copy.bitCode);
		copy.touchedRight = touchedRight;
		//copy.touchedLeft = touchedLeft;
		copy.placeNum = placeNum;
		copy.lastOneOverritten = lastOneOverritten;
		copy.minimalBrickNumber = minimalBrickNumber;
		addPGFTo(copy);
		return copy;
	}
	
	public void copyCodeTo(int[] codeOld,int[] codeNew){
		for(int i = 0; i < codeOld.length; i++){
			codeNew[i] = codeOld[i];
		}
	}
	public void addPGFTo(Configuration conf){
		for(int i = 0; i < this.PGF.length; i++){
			conf.PGF[i] += this.PGF[i];
		}
		for(int i = 0; i < this.PGFext.length; i++){
			conf.PGFext[i] += this.PGFext[i];
		}
	}
	public void multiplyXtoPGF(){
		for(int i = PGFext.length-1; i > 0 ; i--){
			PGFext[i] = PGFext[i-1];
		}
		PGFext[0] = PGF[PGF.length-1];
		if(minimalBrickNumber<longIndex){
			for(int i = PGF.length-1; i > minimalBrickNumber ; i--){
				PGF[i] = PGF[i-1];
			}
			PGF[minimalBrickNumber] = 0;
		}
		minimalBrickNumber += 1;
	}
	
	public boolean sameCodeAs(Configuration otherConf){
		//return otherConf.code2 == code2 && otherConf.touchedRight == touchedRight;
		if(touchedRight != otherConf.touchedRight){
			return false;
		}
		for(int i = 0; i < bitCode.length; i++){
			if(bitCode[i] != otherConf.bitCode[i]){return false;};
		}
		return true;
	}
	public void write(int index, byte value, boolean hasJustPlaced){
		lastOneOverritten = code(index);
		codeSet(index,value);
		if(hasJustPlaced){
			this.placeNum= brickLenght;
		}
		else if(placeNum>1){
			this.placeNum--;
		}
	}
	
	public void setNextToStart(int fromIndex){
		int startCount = 0;
		for(int i = fromIndex; i<codeLength; i++){
			if(startCount <= 0){
				if(code(i) == TransferMatrix.MID || code(i) == TransferMatrix.END){
					codeSet(i,TransferMatrix.START);
					return;
				}
			}
			if(code(i) == TransferMatrix.START){
				startCount++;
			}
			else if(code(i) == TransferMatrix.END){
				startCount--;
			}
			
		}
	}
	
	public void setPrevToEnd(int fromIndex){
		int endCount = 0;
		for(int i = fromIndex - 1; i >= 0; i--){
			if(endCount <= 0){
				if(code(i) == TransferMatrix.MID){
					codeSet(i,TransferMatrix.END);
					return;
				}
			}
			if(code(i) == TransferMatrix.END){
				endCount++;
			}
			else if(code(i) == TransferMatrix.START){
				endCount--;
			}
			
		}
	}
	
	public boolean allFree(){
		//return code2 == 0;
		for(int i = 0; i < bitCode.length; i++){
			if(bitCode[i] != 0){
				return false;
			}
		}
		return true;
	}
	
	public boolean toManyStarts(int from, int to){
		int startCount = 0;
		for(int i = from; i <= to; i++){
			if(code(i) == TransferMatrix.START){
				startCount++;
			}
			else if(code(i) == TransferMatrix.END){
				startCount--;
			}
		}
		return startCount > 0;
	}
	
	public boolean toManyEnds(int from){
		int endCount = 0;
		for(int i = from; i < codeLength; i++){
			if(code(i) == TransferMatrix.START){
				endCount--;
			}
			else if(code(i) == TransferMatrix.END){
				endCount++;
			}
		}
		return endCount > 0;
	}
	
	public boolean toManyStarts(){
		int startCount = 0;
		for(int i = 0; i < codeLength; i++){
			if(code(i) == TransferMatrix.START){
				startCount++;
			}
			else if(code(i) == TransferMatrix.END){
				startCount--;
			}
		}
		return startCount != 0;
	}
	
	public boolean equalStartsAndEnds(){
		int startCount = 0;
		for(int i = 0; i < codeLength; i++){
			if(code(i) == TransferMatrix.START){
				startCount++;
			}
			else if(code(i) == TransferMatrix.END){
				startCount--;
			}
		}
		return startCount == 0;
	}
	
	public void setIntermediatePrevSTARTtoMID(int fromIndex){
		int endCount = 0;
		boolean toManyStarts = toManyStarts();
		for(int i = fromIndex - 1; i >= 0; i--){
			if(endCount == 0 && code(i) == TransferMatrix.START){
				if(toManyStarts){ // toManyStarts(0,i-1)
					codeSet(i,TransferMatrix.MID);
					return;
				}
			}
			if(code(i) == TransferMatrix.END){
				endCount++;
			}
			else if(code(i) == TransferMatrix.START){
				endCount--;
			}
		}
	}
	
	public void setIntermediateNextENDtoMID(int fromIndex){
		int startCount = 0;
		for(int i = fromIndex + 1; i < codeLength; i++){
			if(startCount == 0 && code(i) == TransferMatrix.END){
				if(toManyEnds(i+1)){
					codeSet(i,TransferMatrix.MID);
					return;
				}
			}
			if(code(i) == TransferMatrix.START){
				startCount++;
			}
			else if(code(i) == TransferMatrix.END){
				startCount--;
			}
		}
	}
	
	public boolean validLEGOstructure(){
		if(placeNum>1){
			return false;
		}
		int startCount = 0;
		if(!touchedRight){return false;}
		for(int i = 0; i < codeLength; i++){
			if(code(i) == TransferMatrix.START){
				startCount++;
				if(startCount>1){
					return false;
				}
			}
		}
		return true;
	}
	
	public int numExtraComponents(){
		int startCount = -1;
		for(int i = 0; i < codeLength; i++){
			if(code(i) == TransferMatrix.START){
				startCount++;
			}
		}
		return startCount;
	}
	
	
	public boolean strictIndex(int index){
		if(code(index) == TransferMatrix.FREE){return true;}
		int count = 0;
		for(int i = index; i < codeLength; i++){
			if(code(i) != TransferMatrix.FREE){
				count++;
			}else{break;}
		}
		return count % brickLenght != 0;
		
	}
	
	/*
	 * public boolean strictIndex(int index){
		
		int count = 0;
		for(int i = index; i < codeLength; i++){
			if(code(i) != TransferMatrix.FREE){
				count++;
			}else{break;}
		}
		
		int nextCount = 0;
		for(int j=index;j < codeLength; j++){
			if(code(j) == TransferMatrix.FREE){
				nextCount++;
			}else{break;}
		}
		int n = 2;
		if(code(index) == TransferMatrix.FREE){return nextCount != brickLenght-n;}
		if(count > brickLenght){
			return count % brickLenght != brickLenght - n && count % brickLenght != n;
		}
		return count % brickLenght != n;
		
	}
	 */
	
	@Override
	public int hashCode(){
		int hashCode = 0;
		int base = 1;
		/*for(int i = 0; i<codeLength; i++){
			hashCode += code(i) * base;
			base *= 7; // 16.9
		}*/
		for(int i = 0; i< bitCode.length; i++){
			//hashCode += code2[i]*((i%2==0)?7:17);
			hashCode += bitCode[i]*hashMultipliers[i%10];
		}
		
		
		//hashCode += touchedRight ? base : 0;
		//hashCode += touchedLeft ? base*10 : 0;
		hashCode += touchedRight ? 21 : 0;
		return hashCode;
	}
	@Override
	public boolean equals(Object conf){
		return sameCodeAs((Configuration)conf);
	}
	
	public void printConf(Configuration conf){
		for(int i = 0; i<Configuration.codeLength; i++){
			System.out.print(conf.code(i));
		}
		System.out.print(" --- ");
		for(int i = 0; i<conf.PGF.length; i++){
			System.out.print(conf.PGF[i] + ",");
		}
		//System.out.print("Right: " + conf.touchedRight +  " , Left: " + conf.touchedLeft);
	}

}