package trees.pcrstartree.util;

@FunctionalInterface
interface TriFunction<A,B,C,R> { //interface for implementing functions of the form: R functionName(A a, B b, C c)
    R apply(A a, B b, C c);
    default <V> TriFunction<A, B, C, V> andThen(
                                Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> after.apply(apply(a, b, c));
    }
}