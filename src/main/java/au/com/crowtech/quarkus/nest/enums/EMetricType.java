package au.com.crowtech.quarkus.nest.enums;

public enum EMetricType {

	CM(0, 0, 100),
	M(1, 100, 1000),
	KM(2, 1000, 0);
	
	
	private final int index;
	public final int prevScale, nextScale;
	
	private EMetricType(int index, int prevScale, int nextScale) {
		this.index = index;
		this.prevScale = prevScale;
		this.nextScale = nextScale;
	}
	
	// TODO: Finish this idea off later
	
	private static EMetricType convert(Double value, EMetricType from, EMetricType to) {
		int currentIndex = from.index;
		int numConversions = Math.abs(from.index - to.index);
		
		while(numConversions > 0) {
			from = ofIndex(currentIndex + 1);
			numConversions--;
		}
		
		return null;
	}
	
	private static EMetricType ofIndex(int index) {
		for(EMetricType metric : EMetricType.values()) {
			if(metric.index == index) 
				return metric;
		}
		
		return null;
	}
}
