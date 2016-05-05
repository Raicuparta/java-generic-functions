package ist.meic.pa.GenericFunctions;

import java.util.Arrays;

public class Main {

	public static void println(Object obj) {
		if (obj instanceof Object[]) {
			System.out.println(Arrays.deepToString((Object[]) obj));
		} else {
			System.out.println(obj);
		}
	}

	public static void main(String[] args) throws Throwable {
		final GenericFunction explain = new GenericFunction("explain");
		explain.addMethod(new GFMethod() {
			Object call(Integer asd, Number entity) {
				System.out.printf("", entity);
				return "";
			}
		});
		/*explain.addMethod(new GFMethod() {
			Object call(Integer entity, Integer i) {
				System.out.printf("%s %s is a integer", entity, i);
				return "";
			}
		});
		explain.addMethod(new GFMethod() {
			Object call(String entity, Integer ert) {
				System.out.printf("%s is a string", entity);
				return "";
			}
		});*/
		explain.addBeforeMethod(new GFMethod() {
			void call(Number numb, Integer entity) {
				System.out.printf(" (in hexadecimal, is %x %s)", entity, numb);
			}
		});
		explain.addBeforeMethod(new GFMethod() {
			void call(Object entity, Integer pois) {
				System.out.printf("The number ", entity);
			}
		});
		
		explain.addBeforeMethod(new GFMethod() {
			void call(Integer entity, Number pois) {
				System.out.printf("The double %s", pois);
			}
		});
		
		explain.addMethod(new GFMethod() {
			void call(Object entity, Float pois) {
				System.out.printf("The float %s", pois);
			}
		});
		
		explain.addMethod(new GFMethod() {
			void call(Object entity, Float pois) {
				System.out.printf("The float popo %s", pois);
			}
		});
		
		println(explain.call(123, 456));
		println(explain.call(741, 3.1415));
		println(explain.call(3, 3));
		
		Class c1 = "pilinha".getClass();
		Class c2 = "7456456".getClass();
		System.out.println(c1.equals(c2));

	}
}
