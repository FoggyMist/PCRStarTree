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
			(aggregate, node, value) -> {
				if (aggregate.value > value) aggregate.value = value;
				if (node.parent != null) node.parent.aggregateController.aggregateMap.get("MIN").onInsertUpdate.apply(node.parent.aggregateController.aggregateMap.get("MIN"), node.parent, value);
				return aggregate.value; },
			(aggregate, node, value) -> { return (aggregate.value == value) ? (aggregate.globalUpdate.apply(aggregate, node)) : aggregate.value; },
			(aggregate, node) -> {
				TriFunction<TriFunction, Aggregate, PCRStarNode, Double> branchClimber = (bc, aggr, n) -> {
					if (n.isLeafNode())
						return aggr.value = n.getValue();
					for (PCRStarNode child : n.childrenNodes) {
						double x = (double)bc.apply(bc, child.aggregateController, child);
						if (aggr.value > x) aggr.value = x;
					}	
					return aggr.value;
				};
				branchClimber.apply(branchClimber, aggregate, node);
				for (PCRStarNode ancestor = node; ancestor.parent != null;) {
					ancestor = ancestor.parent;
					Aggregate ancestorsAggregate = ancestor.aggregateController.aggregateMap.get("MIN");
					if (ancestorsAggregate.value <= aggregate.value)
						break;
					else
						ancestorsAggregate.value = aggregate.value;
				}
				return aggregate.value;
			}));
		
		defaultAggregateMap.put("MAX", new Aggregate(
			Double.NEGATIVE_INFINITY,
			(aggregate, node, value) -> { 
				if (aggregate.value < value) aggregate.value = value; 
				if (node.parent != null) node.parent.aggregateController.aggregateMap.get("MAX").onInsertUpdate.apply(node.parent.aggregateController.aggregateMap.get("MIN"), node.parent, value);
				return aggregate.value; },
			(aggregate, node, value) -> { return (aggregate.value == value) ? (aggregate.globalUpdate.apply(aggregate, node)) : aggregate.value; },
			(aggregate, node) -> {
				TriFunction<TriFunction, Aggregate, PCRStarNode, Double> branchClimber = (bc, aggr, n) -> {
					if (n.isLeafNode())
						return aggr.value = n.getValue();
					for (PCRStarNode child : n.childrenNodes) {
						double x = (double)bc.apply(bc, child.aggregateController, child);
						if (aggr.value < x) aggr.value = x;
					}	
					return aggr.value;
				};
				branchClimber.apply(branchClimber, aggregate, node);
				for (PCRStarNode ancestor = node; ancestor.parent != null;) {
					ancestor = ancestor.parent;
					Aggregate ancestorsAggregate = ancestor.aggregateController.aggregateMap.get("MAX");
					if (ancestorsAggregate.value >= aggregate.value)
						break;
					else
						ancestorsAggregate.value = aggregate.value;
				}
				return aggregate.value;
			}));
			
		/*defaultAggregateMap.put("AVG", new Aggregate(
			0,
			(aggregate, node, value) -> { return (node.isLeafNode()) ? aggregate.value = value : aggregate.value = (aggregate.value * node.aggregateNumberOfLeafNodes + value) / (node.aggregateNumberOfLeafNodes + 1); },
			(aggregate, node, value) -> { return (node.isLeafNode()) ? aggregate.value = value : aggregate.value = (aggregate.value * node.aggregateNumberOfLeafNodes - value) / (node.aggregateNumberOfLeafNodes - 1); },
			(aggregate, node) -> {
				TriFunction<TriFunction, Aggregate, PCRStarNode, Double> branchClimber = (branchClimber, aggregate, node) -> {
					if (node.isLeafNode())
						return aggregate.value = node.getValue();
					for (PCRStarNode child : node.childrenNodes) {
						return aggregate.onInsertUpdate(branchClimber.apply(branchClimber, child.aggregateController, child));
					}	
				};
				branchClimber.apply(branchClimber, aggregate, node);
				for (PCRStarNode ancestor = node; ancestor.parent != null;) {
					ancestor.parent.aggregateController.aggregateMap.get("AVG");
				}
				return aggregate.value;
			}));
		*/
		defaultController = new AggregateController(defaultAggregateMap);
	}
	
	public AggregateController(HashMap<String, Aggregate> aggregateMap) {
		aggregateMap = new HashMap<>();
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet()) {
			aggregateMap.put(aggregate.getKey(), new Aggregate(aggregate.getValue()));
		}
	}
	
	public AggregateController(AggregateController aggregateController) {
		this(aggregateController.aggregateMap);
	}
	
	public AggregateController() {
		this(defaultController);
	}
	
	public void insertTrigger(PCRStarNode node, double value) {
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet())
			if (node.tree.activeAggregates.getk(aggregate.getKey()) == true)
				aggregate.getValue().onInsertUpdate.apply(aggregate.getValue(), node, value);
	}
	
	public void removeTrigger(PCRStarNode node, double value) {
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet())
			if (node.tree.activeAggregates.getk(aggregate.getKey()) == true)
				aggregate.getValue().onRemoveUpdate.apply(aggregate.getValue(), node, value);
	}
	
	public void updateEverything(PCRStarNode node) {
		for (Map.Entry<String, Aggregate> aggregate : aggregateMap.entrySet())
			aggregate.getValue().globalUpdate.apply(aggregate.getValue(), node);
	}
	
	public double checkValueFor(String aggregateName) {
		return aggregateMap.get(aggregateName).value;
	}
}
