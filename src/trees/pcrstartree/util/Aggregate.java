package trees.pcrstartree.util;

import trees.pcrstartree.util.TriFunction;
import java.util.function.Function;
import trees.pcrstartree.PCRStarNode;

public class Aggregate {
	public double value;
	public int readCount = 0;
	public int writeCount = 0;
	public int readByte = 0;
	public int writeByte = 0;

	public Function<PCRStarNode, Boolean> update;
	public Aggregate(double value,
					 Function<PCRStarNode, Boolean> update) {
		this.value = value;
		this.update = update;
	}
	public Aggregate(Aggregate aggregate) {
		this(aggregate.value, aggregate.update);
	}
}
