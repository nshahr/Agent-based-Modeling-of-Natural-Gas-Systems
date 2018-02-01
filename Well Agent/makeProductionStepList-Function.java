double res = 0.0;
double B = main.HyperbolicParameterB;
double D = main.HyperbolicParameterD;
int hyperbolicRate = main.HyperbolicToExponentialRatio;
int wellLifeCycle = main.WellLifeCycle;

// The last month in hyperbolic period
int lastMonthInH = (((wellLifeCycle * 12) / hyperbolicRate));
// Producion of the last month in the hyperbolic period
double lastMonthInHProduction = initialProduction / Math.pow(1.0 + B * D * (double)lastMonthInH , 1.0/B);
// The difference between last month in hyperbolic period and the first month in exponential period
// if we assume production in the first month in exponential period follows the hyperbolic formula
double Ds = lastMonthInHProduction - (initialProduction / Math.pow( 1.0 + B * D * (lastMonthInH + 1.0),1.0/B));
Ds /= lastMonthInHProduction;

int totalAge = wellLifeCycle * 12;
for(int step = age_in_month; step <= totalAge; step++){
	if(main.FixReserveSize){
		res = initialProduction;
	}else{
		if(step <= ((wellLifeCycle * 12) / hyperbolicRate)){
			res =  initialProduction / Math.pow( 1.0 + B * D * (double)step , 1.0/B);
		}else{
			res = lastMonthInHProduction * Math.exp(-Ds*(step - lastMonthInH));
		}
	}
	StepDailyProduction.add(res);
	StepProduction.add(res * 30.0);
}
StepCounter = 0;
StepSoldQuantity = 0.0;