package ist.meic.pa.GenericFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.*;

public class GenericFunction {
	private String name;
	private ArrayList<GFMethod> befores = new ArrayList<GFMethod>();
	private ArrayList<GFMethod> afters = new ArrayList<GFMethod>();
	private ArrayList<GFMethod> primaries = new ArrayList<GFMethod>();

	public GenericFunction(String name) {
		this.name = name;
	}

	public void addMethod(GFMethod method) {
		primaries.add(method);
	}

	public void addBeforeMethod(GFMethod method) {
		befores.add(method);
	}

	public void addAfterMethod(GFMethod method) {
		afters.add(method);
	}

	public Object call(Object... args) throws Throwable {
		GFMethod primary = null;
		Method primaryCall = null;
		for (GFMethod gfm : primaries) {
			Method call = getCall(gfm);
			if (call == null)
				continue;

			for (int i = 0; i < args.length; i++) {
				Class<?> genericType = args[i].getClass();
				Class<?> specificType = call.getParameterTypes()[i];

				//check if applicable
				if (!(specificType.isAssignableFrom(genericType)))
					break;

				if (primary == null) {
					primary = gfm;
					primaryCall = getCall(primary);
					continue;
				}
				
				//TODO melhorar
				primaryCall = getCall(primary);

				Class<?> previousType = primaryCall.getClass();

				if (previousType.isAssignableFrom(specificType)) {
					primary = gfm;
				}
			}
		}
		
		return primaryCall.invoke(primary, args);
	}

	Method getCall(GFMethod gfm) {
		for (Method m : gfm.getClass().getDeclaredMethods()) {
			if (m.getName() == "call") {
				return m;
			}
		}
		return null;
	}

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
			void call(int entity) {
				System.out.printf("%s is int ", entity);
			}
		});
		explain.addMethod(new GFMethod() {
			Object call(Integer entity) {
				System.out.printf("%s is a integer", entity);
				return "";
			}
		});
		explain.addMethod(new GFMethod() {
			Object call(Double entity) {
				System.out.printf("%s is a double", entity);
				return "";
			}
		});
		explain.addMethod(new GFMethod() {
			Object call(String entity) {
				System.out.printf("%s is a string", entity);
				return "";
			}
		});

		explain.addMethod(new GFMethod() {
			void call(Double entity) {
				System.out.printf("The number ", entity);
			}
		});
		println(explain.call(15));
		println(explain.call("Hi"));
		println(explain.call(3.14159));

	}
}