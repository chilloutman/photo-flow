package ch.zhaw.photoflow.core.util;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;

public class GuavaCollectors {
	
	public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
		Supplier<ImmutableList.Builder<T>> supplier = ImmutableList.Builder::new;
		BiConsumer<ImmutableList.Builder<T>, T> accumulator = (b, v) -> b.add(v);
		BinaryOperator<ImmutableList.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
		Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher = ImmutableList.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}
	
}
