/**
 * ProductionCalculator
 */	
public class ProductionCalculator implements Serializable {

	private Agent agent;
	private Main mainAgent;
	
	private int hyperbolicRate = 2;
	private int wellLifeCycle = 30;
	private double B = 1.8;
	private double D = 0.58;
	
    /**
     * Default constructor
     */
    public ProductionCalculator(Agent agent, Main mainAgent) {
    	this.agent = agent;
    	this.mainAgent = mainAgent;
    }

      
    
    
    public double calculateProductionCapacity(double IP,int month, boolean fixReserveSize, double B, double D){
    	double res = 0.0;
    	//B = mainAgent.HyperbolicParameterB;
    	//D = mainAgent.HyperbolicParameterD;
    	hyperbolicRate = mainAgent.HyperbolicToExponentialRatio;
    	wellLifeCycle = mainAgent.WellLifeCycle;
    	if(fixReserveSize){
    		res = IP;
    	}else{
    		if(month <= ((wellLifeCycle * 12) / hyperbolicRate)){
        		res =  IP / Math.pow( 1 + B * D * (double)month,1/B);
        	}else{
        		// The last month in hyperbolic period
        		int month60 = (((wellLifeCycle * 12) / hyperbolicRate));
        		// Producion of the last month in the hyperbolic period
        		double month60P = calculateProductionCapacity(IP,month60,false,B,D);
        		// The difference between last month in hyperbolic period and the first month in exponential period
        		// if we assume production in the first month in exponential period follows the hyperbolic formula
        		double Ds = month60P-(IP / Math.pow( 1 + B * D * (month60+1.0),1/B));//0.009;
        		Ds/=month60P;
        		res = calculateProductionCapacity(IP,month60,false,B,D);//IP / Math.pow( 1 + B * D * (wellLifeCycle * 12.0 / (double)hyperbolicRate),1/B);
        		res *= Math.exp(-Ds*(month-month60));
        	}
    	}
    	return res;
    }
    
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}