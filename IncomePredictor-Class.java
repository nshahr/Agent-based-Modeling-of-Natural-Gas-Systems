/**
 * PricePredictor
 */	
public class IncomePredictor implements Serializable {
	private Agent agent;
	private Main main;
	private double depletionAllowanceRate;
	private double severanceAndAdVoloremExpensesRate;
	private double totalNetOperatingExpenses;
	private double royalty;
	private double tax;

	/**
     * Default constructor
     */
    public IncomePredictor(Agent agent,Main main) {
    	this.agent = agent;
    	this.main = main;
    	this.depletionAllowanceRate = 0.201;
    	this.severanceAndAdVoloremExpensesRate = 0.055;
    	this.totalNetOperatingExpenses = 9747.0 / 12.0;
    	//this.royalty = main.Royalty;
    }

	@Override
	public String toString() {
		return super.toString();
	}

		public double calculateIncome(double price,double estimatedGrossGasProduction, double royalty){
    	this.tax = main.Tax;
        	double estimatedNetGasProduction = estimatedGrossGasProduction * (1- royalty);
		double estimatedTotalNetRevenue = price * estimatedNetGasProduction;
		double depletionAllowance = depletionAllowanceRate * estimatedGrossGasProduction;
		double severanceAndAdVoloremExpenses = severanceAndAdVoloremExpensesRate * estimatedGrossGasProduction;
		estimatedTotalNetRevenue -= (totalNetOperatingExpenses + severanceAndAdVoloremExpenses + depletionAllowance);
		estimatedTotalNetRevenue *= (1 - tax);
		estimatedTotalNetRevenue += depletionAllowance;
		return estimatedTotalNetRevenue;
	}
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}