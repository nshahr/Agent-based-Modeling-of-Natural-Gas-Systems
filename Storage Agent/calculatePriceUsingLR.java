int numOfLastDays = main.LRNumOfLastDays;
int startDay = ((currentDay + main.StartSimulationOffset - numOfLastDays));
startDay = (startDay < 0) ? 0 : startDay;
ArrayList<Double> x = new ArrayList<Double>();
ArrayList<Double> yMin = new ArrayList<Double>();
ArrayList<Double> yMax = new ArrayList<Double>();
ArrayList<Double> yAve = new ArrayList<Double>();
int count = 1;
for(int i = startDay ; i < currentDay + main.StartSimulationOffset ; i++){
	int day = i;
	if(day <= main.StartSimulationOffset){
		HubReport hb = main.getHubSampleReport(hub.nodeId , day);
		//if(hb.averageDemand == 0.0 || hb.averageSupply == 0.0)
			//continue;
		if(hb.avgHub == 0.0)
			continue;
		yMin.add(hb.minHub);
		yMax.add(hb.maxHub);
		yAve.add(hb.avgHub);
		/*if(StorageType == "Demand"){
			yMin.add(hb.minDemand);
			yMax.add(hb.maxDemand);
			yAve.add(hb.averageDemand);
		}else{
			yMin.add(hb.minSupply);
			yMax.add(hb.maxSupply);
			yAve.add(hb.averageSupply);	
		}*/	
		x.add(count + 0.0);
		count++;
	}else{
		ReportDetail report = hub.Reports.get(day - main.StartSimulationOffset);
		if(report.totalDeal == 0)
			continue;
		yMin.add(report.minPriceHub);
		yMax.add(report.maxPriceHub);
		yAve.add(report.weightedAvgPriceHub);
		/*if(StorageType == "Demand"){
			yMin.add(report.minPriceDemand);
			yMax.add(report.maxPriceDemand);
			yAve.add(report.weightedAverageDemand);
		}else{
			yMin.add(report.minPriceSupply);
			yMax.add(report.maxPriceSupply);
			yAve.add(report.weightedAverageSupply);
		}*/
		x.add(count + 0.0);
		count++;
	}
}
LinearRegressionCalculator regCalc = new LinearRegressionCalculator();
double[] xx = new double[x.size()];
double[] yyMin = new double[x.size()];
double[] yyMax = new double[x.size()];
double[] yyAve = new double[x.size()];
averageLR.reset();
daysLR.reset();
for(int i = 0 ; i<x.size() ; i++){
	xx[i] = x.get(i);
	yyMin[i] = yMin.get(i);
	yyMax[i] = yMax.get(i);
	yyAve[i] = yAve.get(i);
	if(hub.nodeId == 31){
		averageLR.add(yAve.get(i));	
		daysLR.add(x.get(i));
	}
}
double[] regMin = regCalc.calculate(xx, yyMin);
double[] regMax = regCalc.calculate(xx, yyMax);
double[] regAve = regCalc.calculate(xx, yyAve);
//traceln("LL Result on Average : hubid = " +hub.nodeId + "   "  + regAve[0] + "   " + regAve[1]);
int forecastDay = x.size() + 1;
//========================================================================================================================================
if(x.size() > 1){
	double minReg = 0.0;
	double maxReg = 0.0;
	double avgReg = 0.0;
	
	// add LR difference with last priced day to range calculated by similar days.
	// otherwise use LR result in combination of calculated range.
	if(main.AddLRDifference){
		// find last day with purchase 
		startDay = currentDay - 1;
		double lastDayMin = 0.0;
		double lastDayMax = 0.0;
		double lastDayAvg = 0.0;
		while(true){
			if(startDay < 0){ // use sample reports
				startDay += main.StartSimulationOffset;
				HubReport hb = main.getHubSampleReport(hub.nodeId, startDay);
				//if(hb.averageDemand == 0.0 || hb.averageSupply == 0.0){ 
				if(hb.avgHub == 0.0){ // go one day back if report is empty
					startDay -= 1;
					continue;
				}
				// find min max avg from report
				lastDayMin = hb.minHub;
				lastDayMax = hb.maxHub;
				lastDayAvg = hb.avgHub;
				/*if(StorageType == "Demand"){ 
					lastDayMin = hb.minDemand;
					lastDayMax = hb.maxDemand;
					lastDayAvg = hb.averageDemand;
				}else{
					lastDayMin = hb.minSupply;
					lastDayMax = hb.maxSupply;
					lastDayAvg = hb.averageSupply;
				}*/
				break;
			}else{ // use hub reports
				ReportDetail report = hub.Reports.get(startDay);
				if(report.totalDeal == 0){ // go one day back if report is empty
					startDay -= 1;
					continue;
				}
				// find min max avg from report
				lastDayMin = report.minPriceHub;
				lastDayMax = report.maxPriceHub;
				lastDayAvg = report.weightedAvgPriceHub;
				/*if(StorageType == "Demand"){ 
					lastDayMin = report.minPriceDemand;
					lastDayMax = report.maxPriceDemand;
					lastDayAvg = report.weightedAverageDemand;
				}else{
					lastDayMin = report.minPriceSupply;
					lastDayMax = report.maxPriceSupply;
					lastDayAvg = report.weightedAverageSupply;
				}*/
				break;
			}
		}
		
		//traceln("Find last day : " + startDay + "  " + lastDayMin + "  " + lastDayAvg + "  " + lastDayMax);
		minReg = regMin[1] * forecastDay + regMin[0];
		maxReg = regMax[1] * forecastDay + regMax[0] ;
		avgReg = regAve[1] * forecastDay + regAve[0] ;
		//traceln("Regression result ======== " + minReg + "  " + avgReg + "  " + maxReg);
		double deltaMin = minReg - lastDayMin;
		double deltaMax = maxReg - lastDayMax;
		double deltaAvg = avgReg - lastDayAvg;
		//traceln("Regression Delta ---------------------         " + deltaMin + "    " + deltaAvg + "    " + deltaMax);
		pr.min += deltaMin ;
		pr.max += deltaMax ;
		pr.avg += deltaAvg ;
	}else{
		//minReg = regMin[1] * forecastDay + regMin[0];
		//maxReg = regMax[1] * forecastDay + regMax[0] ;
		avgReg = regAve[1] * forecastDay + regAve[0] ;
		//addLog("Linear Regression Values -->   Min Reg : " + minReg + " Avg Reg : " + avgReg + " Max Reg : " + maxReg);
		addLog("Linear Regression Values -->   Avg Reg : " + avgReg );
		//traceln("Regression result ======== " + minReg + "  " + avgReg + "  " + maxReg);
		double newAvg = (avgReg) * (1.0 - main.ShareOfLastYearPriceWithLR) + 
				 (pr.avg * main.ShareOfLastYearPriceWithLR);
		double delta = newAvg - pr.avg;
		pr.min += delta;
		pr.max += delta;
		pr.avg += delta;
		/* 
		pr.min = (minReg) * (1.0 - main.ShareOfLastYearPriceWithLR) + 
				(pr.min * main.ShareOfLastYearPriceWithLR);
		pr.max = (maxReg) * (1.0 - main.ShareOfLastYearPriceWithLR) + 
				(pr.max * main.ShareOfLastYearPriceWithLR);
		pr.avg = (avgReg) * (1.0 - main.ShareOfLastYearPriceWithLR) + 
				(pr.avg * main.ShareOfLastYearPriceWithLR);
		*/
		addLog("After Linear Regression -->   Min : " + pr.min + " Avg : " + pr.avg + " Max : " + pr.max );
	}
	
	if(pr.min > pr.max){
		double tmp = pr.min;
		pr.min = pr.max;
		pr.max = tmp;
	}
	addLog("After Linear Regression (FIXED) -->   Min : " + pr.min + " Avg : " + pr.avg + " Max : " + pr.max );
}
//traceStr = String.format("D--> Min : %s , Ave : %s ,Max : %s",minP,aveP,maxP);
//traceln(traceStr);
//traceln("##########################################################################################    LINEAR REGRESSION ");
return pr;