/**
 * NPVCalculator
 */	
public class NPVCalculator implements Serializable {
    public NPVCalculator() {
    }

	@Override
	public String toString() {
		return super.toString();		
	}
	public double evaluate(double initialValue,double rate,FutureValue[] futureValues){
		double npv = -initialValue;
		for(int i = 0 ; i < futureValues.length ; i++){
			int n = futureValues[i].index;
			double fv = futureValues[i].value;
			double pv = fv / (Math.pow(1.0 + rate,n));
			npv += pv;
		}
		return npv;
	}
	
	public double getPresentValue(double n,double rate,double fv){
		return fv/(Math.pow(1.0 + rate,n));
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}