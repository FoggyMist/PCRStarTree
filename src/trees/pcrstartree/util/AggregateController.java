package trees.pcrstartree.util;

import trees.pcrstartree.util.DefaultHashMap;
import trees.pcrstartree.PCRStarNode;
import trees.pcrstartree.util.TriFunction;
import trees.pcrstartree.util.Aggregate;
import java.util.HashMap;
import java.util.function.Function;
import java.util.Map;
public class AggregateController {
	public HashMap<String, Aggregate> aggregateMap;

	public static AggregateController defaultController;
	static { //construction of the defaultController
		HashMap<String, Aggregate> defaultAggregateMap = new HashMap<>();

		defaultAggregateMap.put("MIN", new Aggregate(
			Double.POSITIVE_INFINITY,
			(node) -> {
				Aggregate aggregate = node.getAggregateFor("MIN");
				double newMin = Double.POSITIVE_INFINITY;
				for(PCRStarNode child : node.childrenNodes) {
					double childValue;
					if(child.aggregateController == null) {
						childValue = child.getValue();
					} else {
						childValue = child.checkValueFor("MIN");
					}

					if(childValue < newMin) {
						newMin = childValue;
					}
				}
				aggregate.value = newMin;
				return true;
			})
		);

		defaultAggregateMap.put("MAX", new Aggregate(
			Double.NEGATIVE_INFINITY,
			(node) -> {
				Aggregate aggregate = node.getAggregateFor("MAX");
				double newMax = Double.NEGATIVE_INFINITY;
				for(PCRStarNode child : node.childrenNodes) {
					double childValue;
					if(child.aggregateController == null) {
						childValue = child.getValue();
					} else {
						childValue = child.checkValueFor("MAX");
					}

					if(childValue > newMax) {
						newMax = childValue;
					}
				}
				aggregate.value = newMax;
				return true;
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
			aggregate.getValue().update.apply(node);
	}

	public double checkValueFor(String aggregateName) {
		return aggregateMap.get(aggregateName).value;
	}

	public Aggregate getAggregateFor(String aggregateName) {
		return aggregateMap.get(aggregateName);
	}
}
