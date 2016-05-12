import java.util.*;
import ist.meic.pa.GenericFunctions.*;

public class TestA {

	public static void main(String args[]) throws Throwable {

		final GenericFunction add = new GenericFunction("add");

		add.addMethod(new GFMethod() {
			Object call(Number a, Integer b) {
				return "NUMBER INTEGER";
			}});
		add.addMethod(new GFMethod() {
			Object call(Integer a, Number b) {
				return "INTEGER NUMBER";
			}});

		println(add.call(1, 1));

	}

	public static void println(Object obj) {

		if (obj instanceof Object[]) {
			System.err.println(Arrays.deepToString((Object[])obj));
		} else {
			System.err.println(obj);
		}
	}
}
