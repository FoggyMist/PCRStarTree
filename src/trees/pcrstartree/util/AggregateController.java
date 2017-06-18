package trees.pcrstartree.util;

import trees.pcrstartree.util.DefaultHashMap;
import trees.pcrstartree.PCRStarNode;
import trees.pcrstartree.util.TriFunction;
import java.util.HashMap;
import java.util.BiFunction;

public class AggregateController {
	public class Aggregate {
		public double value = 0.0;
		public TriFunction<Aggregate, PCRStarNode, double, double> onInsertUpdate;
		public TriFunction<Aggregate, PCRStarNode, double, double> onRemoveUpdate;
		public BiFunction<Aggregate, PCRStarNode, double> globalUpdate;
		public Aggregate(TriFunction<Aggregate, PCRStarNode, double, double> onInsertUpdate,
						 TriFunction<Aggregate, PCRStarNode, double, double> onRemoveUpdate,
						 BiFunction<Aggregate, PCRStarNode, double> globalUpdate) {
			this.onInsertUpdate = onInsertUpdate;
			this.onRemoveUpdate = onRemoveUpdate;
			this.globalUpdate = globalUpdate;
		}
	}
	public HashMap<String, Aggregate> aggregateMap;
	
	public static AggregateController defaultController;
	static { //construction of the defaultController
		HashMap<String, Aggregate> defaultAggregateMap = new HashMap<>();
		
		defaultAggregateMap.add("MIN", new Aggregate(
			(aggregate, node, value) -> { return (aggregate.value > value) ? (aggregate.value = value) : aggregate.value; },
			(aggregate, node, value) -> { return (aggregate.value == value) ? (aggregate.globalUpdate(aggregate, node)) ? aggregate.value; },
			(aggregate, node) -> {
				//to do
			}
		
		defaultAggregateMap.add("MAX", new Aggregate(
			(aggregate, node, value) -> { return (aggregate.value < value) ? (aggregate.value = value) : aggregate.value; },
			(aggregate, node, value) -> { return (aggregate.value == value) ? (aggregate.globalUpdate(aggregate, node)) ? aggregate.value; },
			(aggregate, node) -> {
				//to do
			}
			
		defaultAggregateMap.add("AVG", new Aggregate(
			(aggregate, node, value) -> { return aggregate.value = (aggregate.value * node.aggregateNumberOfLeafNodes + value) / (node.aggregateNumberOfLeafNodes + 1); },
			(aggregate, node, value) -> { return aggregate.value = (aggregate.value * node.aggregateNumberOfLeafNodes - value) / (node.aggregateNumberOfLeafNodes - 1); },
			(aggregate, node) -> {
				//likewise, to do
			}
		
		defaultController = new AggregateController(defaultAggregateMap);
	}
	
	public AggregateController(HashMap<String, Aggregate> aggregateMap) {
		aggregateMap = new HashMap<>();
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet()) {
			
		}
	}
	
	public AggregateController(AggregateController aggregateController) {
		
	}
	
	public void insertTrigger(PCRStarNode node, double value) {
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet())
			if (node.tree.activeAggregates.get(aggregate.getKey()) == true)
				aggregate.getValue().onInsertUpdate(aggregate.getValue(), node, value);
	}
	
	public void removeTrigger(PCRStarNode node, double value) {
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet())
			if (node.tree.activeAggregates.get(aggregate.getKey()) == true)
				aggregate.getValue().onRemoveUpdate(aggregate.getValue(), node, value);
	}
	
	public void updateEverything(PCRStarNode node) {
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet())
			aggregate.getValue().globalUpdate(aggregate.getValue(), node);
	}
}
