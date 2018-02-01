//at the begining of simulation, we assume a number of days are past which are startsimulationoffset and we did this to use sample report data of price for the last year
int currentDay = main.currentDay + main.StartSimulationOffset;
//In the financial calculations for planning for a new well, we assume this well starts after 180 days and we do our calculations based on that
int startingDeliveryDay = currentDay + 180;

int currentMonth = (currentDay) / 30;
int startingDeliveryMonth =  (startingDeliveryDay) / 30;



// Hyperbolic decline period is 1 / 6 of wellLifeCycle
ProductionCalculator pc = new ProductionCalculator(this, main);

double initialProduction; //= pc.calculateInitialProduction(getCompanySize()); // mcf / d

//traceln(getName() + " " + initialProduction);
///1/traceln("Initial Production for " + getName() + " is " + initialProduction);
//double initialProduction = 10000; // mcf / d
double productionMonthly;
double initialInvestment; //= calculateInitialInvestment();
//double initialInvestment = 15000;


// TODO: Must be related to Company size and type
double r  = main.InterestRate / 12.0; //For changing Annual interest rate to monthly (what we have in main is annual)

NPVCalculator npv = new NPVCalculator();
IncomePredictor ip = new IncomePredictor(this,main);


int wellLifeCycle = main.WellLifeCycle;

double MIN_VALUE = -200000;

Pipeline_Node maxNode = null;
double maxNPV = MIN_VALUE;

double maxNPVCounty,maxNPVShalePlay,maxNPVOutside;
maxNPVCounty = maxNPVShalePlay = maxNPVOutside = maxNPV;
String logMaxNPVCounty,logMaxNPVShalePlay,logMaxNPVOutside;
logMaxNPVCounty = logMaxNPVShalePlay = logMaxNPVOutside = "";


Pipeline_Node maxNodeCounty=null,maxNodeShalePlay=null,maxNodeOutside=null;

//-------------------------------------------------------------------------------------------------------
//finding 3 maxnpv locations of focus county, focus shale play without the focus county, and outside focused shale play
addLog("Seasonal Planning");

String counties = "";
for(County county : FocusedCounties){
  counties += (county.id) + " , ";
}
addLog("Focused Counties: " + counties);

String log = ""; //we buid logs for any NPV and at the end we just present the maxNPV
for(Pipeline_Node node : main.pipeline_Nodes){
	double maxNPVCurrentNode = MIN_VALUE;
	if(node.isCounty && node.countyId < 1000 && !node.isStorageGateway){
		if(!node.county.resourceAvailable())continue;
		List<Hub> hubs = new ArrayList<Hub>();
		for(Hub hub : main.hubs){
			Route route = main.getRoute(node.nodeId, hub.nodeId);
			if(route != null)hubs.add(hub);
		}
		if(hubs.size() == 0)continue;
				for(Hub hub : hubs){
			Route route = main.getRoute(node.nodeId, hub.nodeId);
			HubMonthlyReport report; //HubMonthlyreport is a class but it is being used as a data type (not similar to NPVCalculator that we use its methods). Since we just want to 
			                         //read it from hub, we don't need to new this class (the object already exist)
			FutureValue[] futureValues = new FutureValue[wellLifeCycle * 12];
			
			int numOfLastMonth = main.LRForPriceForecasting;
			int startingMonth = (currentMonth - numOfLastMonth ) > 0 ? (currentMonth - numOfLastMonth ) : 0;
			double[] x = new double[currentMonth - startingMonth];
			double[] y = new double[currentMonth - startingMonth];
			
			for(int i = startingMonth, cntLinear = 0; i < currentMonth ; i++, cntLinear++){
				int sameMonth = i;
				x[cntLinear] = (i-startingMonth) + 0.0; //this zero makes it double
				if(sameMonth < 12){
					report = main.getHubSampleMonthlyReport(hub.nodeId, sameMonth);
				}else{
					report = hub.MonthlyReports.get(sameMonth - 12);
				}
				//traceln(node.nodeId + " "  + hub.nodeId);
				y[cntLinear] = report.averageHubPrice - route.getRealCheapestRoute().routeCost - main.SupplyMarketingCost;
				//traceln("Input of regression:" + y[cntLinear]);
			} 
			LinearRegressionCalculator regCalc = new LinearRegressionCalculator();
			double[] reg = regCalc.calculate(x, y);
			
			//traceln("regression : " + this.getName() + " " + reg[1] + " , " + reg[0]);
			
			//getEngine().pause();
			
			double diffPrice = 0.0;
			/*if(startingDeliveryMonth > 12){
				int prevMonth = startingDeliveryMonth - 1;
				int prevYear = startingDeliveryDay - 12;
				while(prevMonth >= currentMonth) prevMonth -= 12;
				while(prevYear >= currentMonth) prevYear -= 12;
				double prevMonthPrice = getPriceOfMonth(hub, prevMonth);
				double prevYearPrice = getPriceOfMonth(hub, prevYear);
				diffPrice = (prevMonthPrice - prevYearPrice) / prevMonthPrice;
			}*/
			
			diffPrice = reg[1];
			//traceln("Diff price for "+ hub.getName() + " is " + diffPrice);
			//getEngine().pause();
			
			this.EUR = 0;
			int fvMonth = 6;
			log = "";
			initialProduction = node.county.initialProduction * productivityRatio; 
			
			for(int j = 0 ; j < wellLifeCycle ; j++){ //for instance if the wellLifeCycle is 30, we repeat the last year 30 times
				for(int i = startingDeliveryMonth ; i < startingDeliveryMonth + 12 ; i++){
					double averageMonthlyPrice = 0;
					int sameMonth = i - 12;
					if(sameMonth >= currentMonth) sameMonth -= 12;
					averageMonthlyPrice = getPriceOfMonth(hub, sameMonth);
					averageMonthlyPrice *= (1 + diffPrice);
					averageMonthlyPrice -= route.getRealCheapestRoute().routeCost - main.SupplyMarketingCost;
					//traceln("Price in month " + (fvMonth-1) + " is " + averageMonthlyPrice);
					//averageMonthlyPrice = (i-startingMonth)*reg[1]+reg[0]; //
					//averageMonthlyPrice = 4.5;
					productionMonthly = pc.calculateProductionCapacity(initialProduction, fvMonth - 6, false, node.county.hyperbolicParameterB, node.county.hyperbolicParameterD) * 30.0;
					//the amount of gas in data base is based on the production of gas per day per Mcf but our calculations here is monthly so we have to multiply by 30
					productionMonthly *= productivityRatio;
					this.EUR += productionMonthly;
					double income = ip.calculateIncome(averageMonthlyPrice, productionMonthly, node.county.royalty);
					FutureValue fv = new FutureValue();
					fv.index = fvMonth;
					fv.value = income;
					futureValues[fvMonth-6] = fv;
					
					log += ("Month " + (fvMonth) + ": \n");
					log += ("Production:  " + productionMonthly + "\n");
					//traceln((fvMonth - 5) + " Production :  " + productionMonthly );
					log += ("Price: " +averageMonthlyPrice + "\n");
					//traceln((fvMonth-5)+" Price : " +averageMonthlyPrice);
					log += ("Income: " + income + "\n");
					//traceln((fvMonth-5)+" Income : " +income);
					log += ("Present Value : " + npv.getPresentValue(fvMonth,r,income) + "\n");
					//traceln((fvMonth-5)+" Present Value : " + npv.getPresentValue(fvMonth,r,income));
					fvMonth++;
					log += ("\n");
				}
				
			}
			EUR /= (1000000.0); // to change Mcf to Bcf
			initialInvestment = calculateInitialInvestment(node.county.initialInvestment);
			initialInvestment = npv.getPresentValue(6,r,initialInvestment);
			double npvValue = npv.evaluate(initialInvestment, r, futureValues);
	
			//traceln("npv : " + npvValue);
			double minR=0.0,maxR=1.0/12.0,midR = 0; //binery search algorith is used to calculate IRR
			
			for(int i = 0 ; i < 20 && minR < maxR; i++){ //20 iterations gives a vergy good estimation
				midR = (minR + maxR) / 2.0;
				double currentNPV = npv.evaluate(initialInvestment, midR, futureValues);
				//if(Math.abs(currentNPV)<10.0)break;
				if(currentNPV < 0)minR = midR;
				else minR = midR;
			}
			//traceln("IRR is " + midR + " for " + node.getName() + " and " + hub.getName() + " is " + npv.evaluate(initialInvestment, midR, futureValues)); 
			maxNPVCurrentNode = max(maxNPVCurrentNode,npvValue);
			this.IRR = min(this.IRR,midR);
		}
		//traceln("Hi " + maxNPVCurrentNode + " " + maxNPV + " " + node.county.remainingReserve + " " + newWellReserveSize);
		if(maxNPVCurrentNode > maxNPV){
			this.InitialInvestment = calculateInitialInvestment(node.county.initialInvestment); 
			this.InitialProduction = node.county.initialProduction * productivityRatio;
			maxNode = node;
			maxNPV = maxNPVCurrentNode;
			//traceln("Hi");
		}
		if(FocusedCounties.contains(node.county) && maxNPVCurrentNode > maxNPVCounty){
			maxNodeCounty = node;
			maxNPVCounty = maxNPVCurrentNode;
			logMaxNPVCounty = log;
		}else{
			ArrayList<Integer> shalePlays = new ArrayList<Integer>();
			for(County county : FocusedCounties)
				shalePlays.add(county.shalePlay);	
			if(shalePlays.contains(node.county.shalePlay) && maxNPVCurrentNode > maxNPVShalePlay){
				maxNodeShalePlay = node;
				maxNPVShalePlay = maxNPVCurrentNode;
				logMaxNPVShalePlay = log;
			}else if(maxNPVCurrentNode > maxNPVOutside){
				maxNodeOutside = node;
				maxNPVOutside = maxNPVCurrentNode;
				logMaxNPVOutside = log;
			}
		}
	}
}

//-------------------------------------------------------------------------------------------------
//In This section, we compare the 3 locations of previous section (focus county, focus shale play without the focus county, and outside focused shale play ) 


double FocusCountyLeavingCost = main.FocusCountyLeavingCost, FocusShalePlayLeavingCost = main.FocusShalePlayLeavingCost;
if(maxNPVShalePlay != MIN_VALUE)
	maxNPVShalePlay -= FocusCountyLeavingCost;
if(maxNPVOutside != MIN_VALUE)
	maxNPVOutside -= FocusShalePlayLeavingCost;
	
this.maxNPVCalculated = maxNPV;
if(maxNPV <= MIN_VALUE){ //sometimes when NPV is negative, suppliers continue their business 
	this.locationNode = null;
}else{
	if(maxNPVCounty > maxNPVShalePlay){
		if(maxNPVCounty > maxNPVOutside){
			this.locationNode = maxNodeCounty;
			this.maxNPVCalculated = maxNPVCounty;
			addLog("Location is in focuesd counties : " + locationNode.nodeId + "  Conty of Location: " + locationNode.countyId);
			addLog(logMaxNPVCounty);
		}else{
			this.locationNode = maxNodeOutside;
			this.maxNPVCalculated = maxNPVOutside;
			addLog("Location is outside of the shaleplay : " + locationNode.nodeId + "  Conty of Location: " + locationNode.countyId);
			addLog(logMaxNPVOutside);
		}
	}else{
		if(maxNPVShalePlay > maxNPVOutside){
			this.locationNode = maxNodeShalePlay;
			this.maxNPVCalculated = maxNPVShalePlay;
			addLog("Location is in the shaleplay : " + locationNode.nodeId + "  Conty of Location: " + locationNode.countyId);
			addLog(logMaxNPVShalePlay);
		}else{
			this.locationNode = maxNodeOutside;
			this.maxNPVCalculated = maxNPVOutside;
			addLog("Location is outside of the shaleplay : " + locationNode.nodeId + "  Conty of Location: " + locationNode.countyId);
			addLog(logMaxNPVOutside);
		}
	}
}