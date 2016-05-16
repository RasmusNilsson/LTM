import java.util.Hashtable;


public class TransferMatrix {
	
	public static TransferMatrix instance;
	
	public static int buildingBoxWidth;

	public static final byte FREE = 0;
	public static final byte START = 1;
	public static final byte END = 2;
	public static final byte MID = 3;
	
	
	
	private Configuration configs;
	private Configuration newConfigs;
	
	private Configuration heap;
	
	private Hashtable<Configuration, Configuration> configTable;
	
	private Configuration finalPGF;
	public int maxNumConfigs;
	private int biggestLayer;
	
	private int numLEGOs;
	
	
	public TransferMatrix(int N) {
		instance = this;
		this.numLEGOs = N;
		buildingBoxWidth = (Configuration.brickLenght-1)*numLEGOs +1;
		Pruning.finishSceme = new byte[buildingBoxWidth * 2];
		init();
	}
	


	private void init(){
		configs = new Configuration(numLEGOs);
		
		configs.PGF[0] = 1;
		configTable = new Hashtable<Configuration, Configuration>();
		
		
		finalPGF = new Configuration(numLEGOs);
		
		LoadingBar.init(numLEGOs,buildingBoxWidth);
		
		iterate();
		
		
		
		countConfs();
		//validateConfigs();
	}
	
	private void addToFinalPGFifValid(Configuration conf){
		if(conf.validLEGOstructure()){
			//printConf(conf);
			//System.out.println();
			conf.addPGFTo(finalPGF);
		}
	}
	
	public void returnConfToHeap(Configuration conf){
		conf.reset();
		conf.next = heap;
		heap = conf;
	}
	
	public Configuration getConfFromHeap(){
		Configuration temp = null;
		if(heap != null){
			temp = heap;
			heap = heap.next;
		}
		else{
			temp = new Configuration(numLEGOs);
		}
		return temp;
		
	}
	
	
	private void iterate(){		
		for(int j = 0; j<numLEGOs; j++){
			for(int i = 0; i < buildingBoxWidth ; i++){
				
				newConfigs = null;
				Configuration next;
				for(Configuration conf = configs; conf != null; conf = next){
					next = conf.next;
					if(i==6 && j == 4){
						if(conf.codeIs(i, MID) && conf.codeIs(i-1, FREE) && conf.codeIs(2, START) && conf.codeIs(8, START)&& conf.codeIs(15, END)){
							this.printConf(conf);
						}
					}
					if(conf.placeNum > 1 || i > buildingBoxWidth-Configuration.brickLenght){
						
						if(conf.placeNum > 1){ // placer 2. halvdel af LEGO-klods
							
							/*if(i == numLEGOs){
								conf.touchedLeft = true;
							}*/
							
							byte codeValue = END;
							
							if(conf.codeIs(i-1,END)){
								conf.codeSet(i-1,MID);
								//conf.code[i-1] = MID;
							}
					
							if(conf.codeIs(i,MID)){
								codeValue = MID;
								conf.setIntermediatePrevSTARTtoMID(i);
							}
							else if (conf.codeIs(i,START)){
								codeValue = MID;
								if(conf.equalStartsAndEnds()){
									conf.setIntermediateNextENDtoMID(i);
								}
							}
								
							else if(conf.codeIs(i,END)){
								codeValue = END;
							}
							else if(conf.codeIs(i,FREE)){
								
								if(conf.equalStartsAndEnds()){
									codeValue = MID;
								}
								else{
									codeValue = END;
								}
							}
							conf.write(i, codeValue, false);
							addToFinalPGFifValid(conf);
						} 
						else{// eller gør ingenting
							
							if(conf.codeIs(i,START)){
								continue;
							}
							else if(conf.codeIs(i,END) && conf.equalStartsAndEnds()){
								conf.setPrevToEnd(i);
							}
							conf.write(i, FREE, false);
							if(conf.allFree()){continue;}
						} 
						
						processNewConfiguration(conf,conf.placeNum == 1,j,i); // conf.placeNum == 1
						
					}
					else{
						// indsæt 2 kopier.
						//#1
						if(conf.strictIndex(i)){
							Configuration newConf = conf.copy();
							
							byte codeValue = START;
							
							if(newConf.codeIs(i,START)){
								codeValue = START;
							}
							else if(newConf.codeIs(i,FREE)){
								codeValue = START;
							}
							else if(newConf.codeIs(i,MID)){
								codeValue = MID;
							}
							else if(newConf.codeIs(i,END)){
									codeValue = END;
							}
							
							if(i == 0){newConf.touchedRight = true;}
							newConf.write(i, codeValue, true);
							newConf.multiplyXtoPGF();
							
							processNewConfiguration(newConf,false,j,i);
						}
						
						
						//#2
						if(conf.codeIs(i,START)){
							if(conf.lastOneOverritten == START && conf.toManyStarts()){
								// FAIL
								continue;
							}
							else{
								conf.setNextToStart(i+1);
							}
							
						}
						else if(conf.codeIs(i,END) && conf.equalStartsAndEnds()){
							
							conf.setPrevToEnd(i);
							
						}
						
						conf.write(i, FREE,false);
						processNewConfiguration(conf,false,j,i);
					}
				}
				configs = newConfigs;
				setMaxNumConfigs(j);
				configTable.clear();
				
				LoadingBar.update(j);
			}
			//countConfs();
		}
	}
	
	private void setMaxNumConfigs(int layer){
		int num = configTable.size();
		if(num>maxNumConfigs){
			maxNumConfigs = num;
			biggestLayer = layer;
		}
	}
	
	
	private void processNewConfiguration(Configuration conf,boolean prune,int layer,int row){
		
		Configuration dupli = configTable.get(conf);
		
			if(dupli != null){
				
				conf.addPGFTo(dupli);
				if(dupli.minimalBrickNumber > conf.minimalBrickNumber){
					dupli.minimalBrickNumber = conf.minimalBrickNumber;
				}
				
				returnConfToHeap(conf);
			}
			else{
				if(!prune || !Pruning.prune(conf,numLEGOs,layer,row)){
					conf.next = newConfigs;
					newConfigs = conf;
					configTable.put(conf, conf);
				}
				else{
					returnConfToHeap(conf);
				}
			}
	}
	
	private void countConfs(){
		long count = 0;
		for(Configuration conf = configs; conf != null; conf = conf.next){
			count++;
			//printConf(conf);
			//System.out.println("\n");
		}
		//System.out.print("\nFINAL: ");
		printConf(finalPGF);
		System.out.print("\n");
		System.out.println("max number of configurations: " + maxNumConfigs + ", at layer " + biggestLayer);
		
		//System.out.print("N: " + numLEGOs + " : " + count + "\n");
		//System.out.print(finalPGF.PGF[this.numLEGOs]+",");
		//System.out.print(finalPGF.PGFext[this.numLEGOs-4]+",");
	}
	
	private void printConf(Configuration conf){
		for(int i = 0; i<Configuration.codeLength; i++){
			System.out.print(conf.code(i));
		}
		System.out.print(" --- ");
		for(int i = 0; i<conf.PGF.length; i++){
			System.out.print(conf.PGF[i] + ",");
		}
		if(numLEGOs >= Configuration.longIndex){
			for(int i = 0; i<conf.PGFext.length; i++){
				System.out.print(conf.PGFext[i] + ",");
			}
		}
		//System.out.print("Right: " + conf.touchedRight +  " , Left: " + conf.touchedLeft);
	}
	
	/*private boolean validateConfigs(){
		for(Configuration conf = configs; conf != null; conf = conf.next){
			if(!validateConfig(conf)){
				System.out.println("FAAAAAAAAAAAAAAAAAAAAAAAAIL ! ! ! ! ! ! ! !");
				return false;
			}
		}
		return true;
	}*/
	private boolean validateConfig(Configuration conf){
		int startCount = 0;
		int endCount = 0;
		int midCount = 0;
		
		for(int i = 0; i< Configuration.codeLength; i++){
			if(conf.codeIs(i,START)){
				startCount++;
			}
			if(conf.codeIs(i,END)){
				endCount++;
			}
			if(conf.codeIs(i,MID)){
				midCount++;
			}
			if(endCount-startCount > 1){
				return false;
			}
		}
		if(midCount % 2 != 0){
			System.out.println("BLUBLIB");
			printConf(conf);
			return false;
		}
		if(startCount != endCount){
			
			return false;
		}
		return true;
	}

}
