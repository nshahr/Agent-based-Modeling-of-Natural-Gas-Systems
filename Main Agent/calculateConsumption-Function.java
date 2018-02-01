Demand demand = findDemand(nodeId);
if( demand == null){
	return 0;
}
int stateindex = findStateIndex(nodeId);
double pricecoef = 0.0;
double price2coef = 0.0;
double priceavgcoef = 0.0;
double priceavg2coef = 0.0;
double temperaturecoef = 0.0;
double intercept = 0.0;
double consumption = 0.0;
double minimum_demand = 0.0;
List<Tuple> rows = new ArrayList<Tuple>();

//traceln("nodeId : " + nodeId + "  price : " + price + "  price average : " + priceavg + "   temperature : " + temperature + "   state index : " + stateindex);
switch(demand.DemandType){
	case COMMERCIAL:
		rows = selectFrom(commercial_consumption)
				.where(commercial_consumption.state_index.eq(stateindex))
				.list();
		if(rows.size() == 0) return 0;
		priceavg2coef = rows.get(0).get(commercial_consumption.price_avg2);
		temperaturecoef = rows.get(0).get(commercial_consumption.temperature);
		intercept = rows.get(0).get(commercial_consumption.intercept);
		minimum_demand = rows.get(0).get(commercial_consumption.minimum_demand);
		consumption = priceavg2coef * pow(priceavg, 2) +
				 temperaturecoef * temperature +
				 intercept;
		minimum_demand = rows.get(0).get(commercial_consumption.minimum_demand);
		//traceln("COMMERCIAL : price avg2 coef : " + priceavg2coef + "  temperature coef : " + temperaturecoef + "  intercept : " + intercept + "  consumption : " +consumption);
		//return consumption;
		break;
	case INDUSTRIAL:
		rows = selectFrom(industrial_consumption)
				.where(industrial_consumption.state_index.eq(stateindex))
				.list();
		if(rows.size() == 0) return 0;
		price2coef = rows.get(0).get(industrial_consumption.price2);
		pricecoef = rows.get(0).get(industrial_consumption.price);
		temperaturecoef = rows.get(0).get(industrial_consumption.temperature);
		intercept = rows.get(0).get(industrial_consumption.intercept);
		//traceln("node " + nodeId + "  state index " + stateindex + "  price 2 coef " + price2coef + "  price coef " + pricecoef + "  temp coef " + temperaturecoef + "  intercept " + intercept);
		consumption = price2coef * pow(price , 2) +
				 pricecoef * price +
				 temperaturecoef * temperature +
				 intercept;
		minimum_demand = rows.get(0).get(industrial_consumption.minimum_demand);
		//return consumption;
		//traceln("INDUSTRIAL : price2 coef : " + price2coef + "  price coef : " + pricecoef + "  temperature coef : " + temperaturecoef + "  intercept : " + intercept + "  consumption : " +consumption);
		break;
	case RESIDENTIAL:
		rows = selectFrom(residential_consumption)
			    .where(residential_consumption.state_index.eq(stateindex))
			    .list();
		if(rows.size() == 0) return 0;
		priceavg2coef = rows.get(0).get(residential_consumption.price_avg2);
		temperaturecoef = rows.get(0).get(residential_consumption.temperature);
		intercept = rows.get(0).get(residential_consumption.intercept);
//		traceln("price avg 2 coef " + priceavg2coef + "  temp coef " + temperaturecoef + "  intercept " + intercept);
		consumption = priceavg2coef * pow(priceavg , 2) +
				temperaturecoef * temperature +
				intercept;
		minimum_demand = rows.get(0).get(residential_consumption.minimum_demand);
		//return consumption;
		//traceln("RESIDENTIAL : price avg2 coef : " + priceavg2coef + "  temperature coef : " + temperaturecoef + "  intercept : " + intercept + "  consumption : " +consumption);
		break;
	case ELECTRIC_POWER:
		String monthName = currentMonthName.toLowerCase();
		int month = currentMonth % 12;
		String datas = selectFrom(utility_consumption)
						.where(utility_consumption.state_index.eq(stateindex))
						.firstResult(utility_consumption.consumption);
		datas = datas.substring(1, datas.length()-1);
		String[] values = datas.split(",");
		consumption = Double.parseDouble(values[month]);
		
		if(currentMonth < 120){
		
		}
		//traceln(stateindex + "  " + monthName  + "   " + consumption + "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		//return consumption;
		break;
	case LNG_EXPORT:
		//TODO
		break;
}
double percent = calculateShareInState(demand.nodeId);
if(consumption < minimum_demand){
	consumption = minimum_demand;
}
consumption *= 1000.0 * percent;
//traceln("consumption -----> " + consumption);
return consumption;