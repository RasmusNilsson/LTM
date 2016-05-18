import java.util.Hashtable;

/**
 * This class does most of the work, 
 * iterating through the building space one cell at a time updating all the signatures.
 * It also holds all the references to the signatures in a hash table and in a linked list.
 * 
 * @author Rasmus Mølck Nilsson
 *
 */
public class TM {
	
	public static TM instance;
	
	public static int buildingBoxWidth;

	public static final byte FREE = 0;
	public static final byte START = 1;
	public static final byte END = 2;
	public static final byte MID = 3;
	
	
	
	private Signature currentSignatures;
	private Signature newSignatures;
	
	private Signature signaturePool;
	
	private Hashtable<Signature, Signature> signatureTable;
	
	private Signature finalPGF;
	public int maxSign;
	private int biggestLayer;
	
	private int numLEGOs;
	
	
	public TM(byte brickLength,int numLEGOs) {
		instance = this;
		this.numLEGOs = numLEGOs;
		Signature.brickLenght = brickLength;
		buildingBoxWidth = (brickLength-1)*numLEGOs +1;
		
		init();
		
		enumerate();
		
		printFinalPGF();
	}
	


	private void init(){
		currentSignatures = new Signature(numLEGOs);
		currentSignatures.PGF[0] = 1;
		signatureTable = new Hashtable<Signature, Signature>();
		finalPGF = new Signature(numLEGOs);
		
		Pruning.init(buildingBoxWidth);
		
		LoadingBar.init(numLEGOs,buildingBoxWidth); // cosmetic only
	}
	
	private void addToFinalPGFifValid(Signature s){
		if(s.validLEGOstructure()){
			s.addPGFTo(finalPGF);
		}
	}
	
	// Here we iterate the boundary-line one cell at a time, processing all signatures and adding new.
	private void enumerate(){		
		for(int j = 0; j<numLEGOs; j++){
			for(int i = 0; i < buildingBoxWidth ; i++){
				
				newSignatures = null;
				Signature next = currentSignatures; //Start with the first signature in the linked list.
				
				while(next!= null){
					Signature s =  next;
					next = s.next;
		
					if(s.partiallyPlacedBrickCellsLeft > 0 || i > buildingBoxWidth-Signature.brickLenght){
						
						if(s.partiallyPlacedBrickCellsLeft > 0){
							// Continue the placement of a half-placed brick.
							
							byte codeValue = END;
							
							if(s.codeIs(i-1,END)){
								s.set(i-1,MID);
							}
					
							if(s.codeIs(i,MID)){
								codeValue = MID;
								s.setIntermediatePrevSTARTtoMID(i);
							}
							else if (s.codeIs(i,START)){
								codeValue = MID;
								if(s.equalStartsAndEnds()){
									s.setIntermediateNextENDtoMID(i);
								}
							}
								
							else if(s.codeIs(i,END)){
								codeValue = END;
							}
							else if(s.codeIs(i,FREE)){
								
								if(s.equalStartsAndEnds()){
									codeValue = MID;
								}
								else{
									codeValue = END;
								}
							}
							s.write(i, codeValue, false);
							addToFinalPGFifValid(s);
						} 
						else{
							if(s.codeIs(i,START)){
								continue;
							}
							else if(s.codeIs(i,END) && s.equalStartsAndEnds()){
								s.setPrevMidToEnd(i);
							}
							s.write(i, FREE, false);
							if(s.allFree()){continue;}
						} 
						
						processNewSignature(s,s.partiallyPlacedBrickCellsLeft == 0,j,i);
						
					}
					else{
						// Place a new brick
						if(!s.prohibitedByStrict(i)){
							Signature s2 = s.copy();
							
							byte codeValue = START;
							
							if(s2.codeIs(i,START)){
								codeValue = START;
							}
							else if(s2.codeIs(i,FREE)){
								codeValue = START;
							}
							else if(s2.codeIs(i,MID)){
								codeValue = MID;
							}
							else if(s2.codeIs(i,END)){
									codeValue = END;
							}
							
							if(i == 0){s2.touchedLeft = true;}
							s2.write(i, codeValue, true);
							s2.multiplyXtoPGF();
							
							processNewSignature(s2,false,j,i);
						}
						
						// Leave a cell empty:
							
						if(s.codeIs(i,START)){
							if(!s.equalStartsAndEnds()){
								continue;
							}
							else{
								s.setNextToStart(i+1);
							}
							
						}
						else if(s.codeIs(i,END) && s.equalStartsAndEnds()){
							
							s.setPrevMidToEnd(i);
							
						}
						
						s.write(i, FREE,false);
						processNewSignature(s,false,j,i);
					}
				}
				currentSignatures = newSignatures;
				setMaxNumSignatures(j+1);
				signatureTable.clear();
				
				LoadingBar.update(j); // cosmetic only
			}
		}
	}
	
	private void processNewSignature(Signature s,boolean tryPrune,int layer,int row){
		
		Signature dupli = signatureTable.get(s);	
		if(dupli != null){
			s.addPGFTo(dupli);
			if(dupli.minimalBrickNumber > s.minimalBrickNumber){
				dupli.minimalBrickNumber = s.minimalBrickNumber;
			}
			discardSignature(s);
		}
		else{
			if(tryPrune && Pruning.canPrune(s,numLEGOs,layer,row)){
				discardSignature(s);
			}
			else{
				keepSignature(s);
			}
		}
	}
	
	public void discardSignature(Signature signature){
		signature.reset();
		signature.next = signaturePool;
		signaturePool = signature;
	}
	
	public Signature makeNewSignature(){
		Signature temp = null;
		if(signaturePool != null){
			temp = signaturePool;
			signaturePool = signaturePool.next;
		}
		else{
			temp = new Signature(numLEGOs);
		}
		return temp;
		
	}
	
	private void keepSignature(Signature s){
		s.next = newSignatures;
		newSignatures = s;
		signatureTable.put(s, s);
	}
	
	private void printFinalPGF(){
		for(int i = 0; i<finalPGF.PGF.length; i++){
			System.out.print(finalPGF.PGF[i] + ",");
		}
		System.out.print("\n");
		System.out.println("Maximum number of signatures in memory: " + maxSign + ", at layer " + biggestLayer);
	}
	
	private void setMaxNumSignatures(int layer){
		int num = signatureTable.size();
		if(num>maxSign){
			maxSign = num;
			biggestLayer = layer;
		}
	}
	
}
