/**
 * LinearRegressionCalculator
 */	
public class LinearRegressionCalculator implements Serializable {

    /**
     * Default constructor
     */
    public LinearRegressionCalculator() {
    }
 
    private  double intercept, slope;
    public double[] calculate(double[] x,double y[]){
    	int n = x.length;
		double[] result = new double[2];
		// first pass
		double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
		for (int i = 0; i < n; i++) {
			sumx  += x[i];
			sumx2 += x[i]*x[i];
			sumy  += y[i];
		}
		double xbar = sumx / n;
		double ybar = sumy / n;

		// second pass: compute summary statistics
		double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
		for (int i = 0; i < n; i++) {
			xxbar += (x[i] - xbar) * (x[i] - xbar);
			yybar += (y[i] - ybar) * (y[i] - ybar);
			xybar += (x[i] - xbar) * (y[i] - ybar);
		}
		slope  = xybar / xxbar;
		intercept = ybar - slope * xbar;

		
		result[0] = intercept;
		result[1] = slope;
		return result;
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