package trees.pcrstartree.util;

import trees.pcrstartree.util.TriFunction;
import java.util.function.BiFunction;
import trees.pcrstartree.PCRStarNode;

public class Aggregate {
	public double value;
	public BiFunction<Aggregate, PCRStarNode, Double> globalUpdate;
	public Aggregate(double value,
					 BiFunction<Aggregate, PCRStarNode, Double> globalUpdate) {
		this.value = value;
		this.globalUpdate = globalUpdate;
	}
	public Aggregate(Aggregate aggregate) {
		this(aggregate.value, aggregate.globalUpdate);
	}
}
