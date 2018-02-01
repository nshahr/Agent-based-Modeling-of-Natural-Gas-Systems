/**
 * ReportDetail
 */	
public class ReportDetail implements Serializable {

	private final int day;
	
	
	public double totalQuantity;
//	public double totalIncome;
	
	public int totalDeal;
	public int totalTurn;
	public double weekAverageTurn;
	
	
	public int numOfSuppliers;
	public int numOfDemands;

	public double hubWeightedAverageMean;
	public double hubVariance;
	public double hubStandardDeviation;
	public double hubCoefOfVariation;
	
	public double minPriceHub;
	public double weightedAvgPriceHub;
	public double maxPriceHub;
	
	
    /**
     * Default constructor
     */
    public ReportDetail(int day, boolean setToZero) {
    	this.day = day;
    	
    	if(setToZero){
        	minPriceHub = 0.0;
        	maxPriceHub = 0.0;
    	}else{
        	minPriceHub = Double.MAX_VALUE;
        	maxPriceHub = Double.MIN_VALUE;
    	}
    	
    	weightedAvgPriceHub = 0.0;
    	totalQuantity = 0.0;
    	totalDeal = 0;
    	numOfSuppliers = 0;
    	numOfDemands = 0;
    	totalTurn = 0;
    	//totalIncome = 0.0;
    	weekAverageTurn = 0.0;
    	hubWeightedAverageMean = 0.0;
    	hubVariance = 0.0;
    	hubStandardDeviation = 0.0;
    	hubCoefOfVariation = 0.0;
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