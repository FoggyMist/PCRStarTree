package trees.pcrstartree.util;

import trees.pcrstartree.util.TriFunction;
import java.util.function.BiFunction;
import trees.pcrstartree.PCRStarNode;

public class Aggregate {
	public double value;
	public TriFunction<Aggregate, PCRStarNode, Double, Double> onInsertUpdate;
	public TriFunction<Aggregate, PCRStarNode, Double, Double> onRemoveUpdate;
	public BiFunction<Aggregate, PCRStarNode, Double> globalUpdate;
	public Aggregate(double value,
					 TriFunction<Aggregate, PCRStarNode, Double, Double> onInsertUpdate,
					 TriFunction<Aggregate, PCRStarNode, Double, Double> onRemoveUpdate,
					 BiFunction<Aggregate, PCRStarNode, Double> globalUpdate) {
		this.value = value;
		this.onInsertUpdate = onInsertUpdate;
		this.onRemoveUpdate = onRemoveUpdate;
		this.globalUpdate = globalUpdate;
	}
	public Aggregate(Aggregate aggregate) {
		this(aggregate.value, aggregate.onInsertUpdate, aggregate.onRemoveUpdate, aggregate.globalUpdate);
	}
}