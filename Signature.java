/**
 * This class holds all relevandt information for a signature:
 * @author Rasmus Mølck Nilsson
 *
 */
public class Signature {

	public static int codeLength;
	public static byte brickLenght = 3;
	
	// The cell-numbering along the boundary line (signature)
	public int[] signatureBitCode;
	
	// partial generating function
	public long[] PGF;
	
	// The minimal number of bricks needed to construct a partial structure signature.
	public byte minimalBrickNumber;
	
	// information on whether or not the left side of the rectangle has been touched
	public boolean touchedLeft = false;
	
	//information about how many cells we still need to place in order to have placed an intire brick.
	//	actually this could just be computed from the bitCode above but this is faster.
	public byte partiallyPlacedBrickCellsLeft = 0;
	
	// information about the signature of the last cell that was overridden.
	public byte lastOneOverritten;
	
	// This is used to hold the signatures in a linked list.
	public Signature next;
	
	// this is used by the hash code.
	public static final int[] hashMultipliers = {7,17,3,11,2,1,13,19,15,9,15};
	
	public Signature(int length) {
		codeLength = (brickLenght-1)*length+1;
		signatureBitCode = new int[codeLength/16+1];
		PGF = new long[length+1];
	}
	public void reset(){
		java.util.Arrays.fill(signatureBitCode, (byte)0); // TODO
		java.util.Arrays.fill(PGF, 0);
		touchedLeft = false;
		partiallyPlacedBrickCellsLeft = 1;
		lastOneOverritten = 0;
		next = null;
		minimalBrickNumber = 0;
	}
	

	public byte get(int i){
		int arrayIndex = i>>4;
		int subIndex = i-(arrayIndex<<4);
		return (byte)((signatureBitCode[arrayIndex] << (30 - (subIndex * 2))) >>> 30);
	}
	public void set(int i, byte b){
		
		int arrayIndex = i>>4;
		int subIndex = i-(arrayIndex<<4);
		
		long bit = subIndex * 2;
		int mask = ((int)3 << bit);
		signatureBitCode[arrayIndex] &= ~mask;
		signatureBitCode[arrayIndex] |= ((long)b) << bit;
	
	}
	
	public boolean codeIs(int i, byte b){
		return get(i) == b;
	}
	
	public Signature copy(){
		Signature copy = TM.instance.makeNewSignature();
		copyCodeTo(this.signatureBitCode,copy.signatureBitCode);
		copy.touchedLeft = touchedLeft;
		copy.partiallyPlacedBrickCellsLeft = partiallyPlacedBrickCellsLeft;
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
	public void addPGFTo(Signature s){
		for(int i = 0; i < this.PGF.length; i++){
			s.PGF[i] += this.PGF[i];
		}
	}
	public void multiplyXtoPGF(){
		for(int i = PGF.length-1; i > minimalBrickNumber ; i--){
			PGF[i] = PGF[i-1];
		}
		PGF[minimalBrickNumber] = 0;
		
		minimalBrickNumber += 1;
	}
	
	public boolean sameCodeAs(Signature s2){
		if(touchedLeft != s2.touchedLeft){
			return false;
		}
		for(int i = 0; i < signatureBitCode.length; i++){
			if(signatureBitCode[i] != s2.signatureBitCode[i]){return false;};
		}
		return true;
	}
	
	public void write(int index, byte value, boolean hasJustPlaced){
		lastOneOverritten = get(index);
		set(index,value);
		if(hasJustPlaced){
			partiallyPlacedBrickCellsLeft= brickLenght;
			partiallyPlacedBrickCellsLeft--;
		}
		else if(partiallyPlacedBrickCellsLeft>0){
			partiallyPlacedBrickCellsLeft--;
		}
	}
	
	public void setNextToStart(int fromIndex){
		int startCount = 0;
		for(int i = fromIndex; i<codeLength; i++){
			if(startCount <= 0){
				if(get(i) == TM.MID || get(i) == TM.END){
					set(i,TM.START);
					return;
				}
			}
			if(get(i) == TM.START){
				startCount++;
			}
			else if(get(i) == TM.END){
				startCount--;
			}
			
		}
	}
	
	public void setPrevMidToEnd(int fromIndex){
		int endCount = 0;
		for(int i = fromIndex - 1; i >= 0; i--){
			if(endCount <= 0){
				if(get(i) == TM.MID){
					set(i,TM.END);
					return;
				}
			}
			if(get(i) == TM.END){
				endCount++;
			}
			else if(get(i) == TM.START){
				endCount--;
			}
			
		}
	}
	
	public boolean allFree(){
		for(int i = 0; i < signatureBitCode.length; i++){
			if(signatureBitCode[i] != 0){
				return false;
			}
		}
		return true;
	}
	
	public boolean tooManyEnds(int from){
		int endCount = 0;
		for(int i = from; i < codeLength; i++){
			if(get(i) == TM.START){
				endCount--;
			}
			else if(get(i) == TM.END){
				endCount++;
			}
		}
		return endCount > 0;
	}
	
	public boolean equalStartsAndEnds(){
		int startCount = 0;
		for(int i = 0; i < codeLength; i++){
			if(get(i) == TM.START){
				startCount++;
			}
			else if(get(i) == TM.END){
				startCount--;
			}
		}
		return startCount == 0;
	}
	
	public void setIntermediatePrevSTARTtoMID(int fromIndex){
		int endCount = 0;
		boolean toManyStarts = !equalStartsAndEnds();
		for(int i = fromIndex - 1; i >= 0; i--){
			if(endCount == 0 && get(i) == TM.START){
				if(toManyStarts){ // toManyStarts(0,i-1)
					set(i,TM.MID);
					return;
				}
			}
			if(get(i) == TM.END){
				endCount++;
			}
			else if(get(i) == TM.START){
				endCount--;
			}
		}
	}
	
	public void setIntermediateNextENDtoMID(int fromIndex){
		int startCount = 0;
		for(int i = fromIndex + 1; i < codeLength; i++){
			if(startCount == 0 && get(i) == TM.END){
				set(i,TM.MID);
				return;
			}
			if(get(i) == TM.START){
				startCount++;
			}
			else if(get(i) == TM.END){
				startCount--;
			}
		}
	}
	
	public boolean validLEGOstructure(){
		if(partiallyPlacedBrickCellsLeft>0){
			return false;
		}
		int startCount = 0;
		if(!touchedLeft){return false;}
		for(int i = 0; i < codeLength; i++){
			if(get(i) == TM.START){
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
			if(get(i) == TM.START){
				startCount++;
			}
		}
		return startCount;
	}
	
	public boolean prohibitedByStrict(int index){
		if(!Start.strict){return false;}
		if(get(index) == TM.FREE){return false;}
		int count = 0;
		for(int i = index; i < codeLength; i++){
			if(get(i) != TM.FREE){
				count++;
			}else{break;}
		}
		return count % brickLenght == 0;
		
	}
	
	
	@Override
	public int hashCode(){
		int hashCode = 0;
		for(int i = 0; i< signatureBitCode.length; i++){
			hashCode += signatureBitCode[i]*hashMultipliers[i%10];
		}
		hashCode += touchedLeft ? 21 : 0;
		return hashCode;
	}
	
	@Override
	public boolean equals(Object s){
		return sameCodeAs((Signature)s);
	}
	
}
