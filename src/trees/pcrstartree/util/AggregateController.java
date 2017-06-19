package trees.pcrstartree.util;

import trees.pcrstartree.util.DefaultHashMap;
import trees.pcrstartree.PCRStarNode;
import trees.pcrstartree.util.TriFunction;
import trees.pcrstartree.util.Aggregate;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.Map;
public class AggregateController {
	public HashMap<String, Aggregate> aggregateMap;

	public static AggregateController defaultController;
	static { //construction of the defaultController
		HashMap<String, Aggregate> defaultAggregateMap = new HashMap<>();

		defaultAggregateMap.put("MIN", new Aggregate(
			Double.POSITIVE_INFINITY,
			(aggregate, node) -> {
				double newMin = Double.POSITIVE_INFINITY;
				for(PCRStarNode child : node.childrenNodes) {
					if(child.getValue() < newMin) {
						newMin = child.getValue();
					}
				}
				aggregate.value = newMin;
				return aggregate.value;
			})
		);

		defaultAggregateMap.put("MAX", new Aggregate(
			Double.NEGATIVE_INFINITY,
			(aggregate, node) -> {
				double newMax = Double.NEGATIVE_INFINITY;
				for(PCRStarNode child : node.childrenNodes) {
					if(child.getValue() > newMax) {
						newMax = child.getValue();
					}
				}
				aggregate.value = newMax;
				return aggregate.value;
			})
		);

		defaultController = new AggregateController(defaultAggregateMap);
	}

	public AggregateController(HashMap<String, Aggregate> aggregateMap) {
		this.aggregateMap = new HashMap<>();
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet()) {
			this.aggregateMap.put(aggregate.getKey(), new Aggregate(aggregate.getValue()));
		}
	}

	public AggregateController(AggregateController aggregateController) {
		this(aggregateController.aggregateMap);
	}

	public AggregateController() {
		this(defaultController);
	}

	public void update(PCRStarNode node) {
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet())
			aggregate.getValue().globalUpdate.apply(aggregate.getValue(), node);
	}

	public double checkValueFor(String aggregateName) {
		return aggregateMap.get(aggregateName).value;
	}
}
