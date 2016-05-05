package ist.meic.pa.GenericFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

	public void addBeforeMethod(GFMethod newGfm) {
		befores.add(newGfm);
	}

	public ArrayList<GFMethod> sort(ArrayList<GFMethod> applicables, boolean specificFirst) {
		ArrayList<GFMethod> sorted = new ArrayList<GFMethod>();

		for (GFMethod appGfm : applicables) {
			if (sorted.isEmpty()) {
				sorted.add(appGfm);
				continue;
			}
			
			boolean added = false;
			for (int i = 0; i < sorted.size(); i++) {
				GFMethod sortedGfm = sorted.get(i);

				Class[] sortedArgs = getCall(sortedGfm).getParameterTypes();
				Class[] appArgs = getCall(appGfm).getParameterTypes();

				boolean sortCondition = true;
				for (int e = 0; e < sortedArgs.length; e++) {
					Class<?> sortedType = sortedArgs[e];
					Class<?> appType = appArgs[e];
					if (!appType.isAssignableFrom(sortedType))
						sortCondition = false;
				}

				if (specificFirst)
					sortCondition = !sortCondition;

				if (sortCondition) {
					sorted.add(i, appGfm);
					added = true;
					break;
				}
			}
			if (!added)
				sorted.add(appGfm);
		}
		return sorted;
	}

	// TODO tentar ordenar ao mesmo tempo se calhar
	public ArrayList<GFMethod> getApplicableMethods(ArrayList<GFMethod> methods, Object[] args) {
		ArrayList<GFMethod> applicables = new ArrayList<GFMethod>();
		for (GFMethod gfm : methods) {
			Method call = getCall(gfm);
			if (call == null)
				continue;

			boolean applicable = true;
			for (int i = 0; i < args.length; i++) {
				Class<?> genericType = args[i].getClass();
				Class<?> specificType = call.getParameterTypes()[i];

				// check if applicable
				if (!specificType.isAssignableFrom(genericType)) {
					applicable = false;
					break;
				}
			}
			if (applicable)
				applicables.add(gfm);
		}
		return applicables;
	}

	public void addAfterMethod(GFMethod method) {
		afters.add(method);
	}

	public Object call(Object... args) throws Throwable {

		ArrayList<GFMethod> applicables = getApplicableMethods(primaries, args);
		ArrayList<GFMethod> sorted = sort(applicables, true);
		GFMethod primary = sorted.get(0);
		Method primaryCall = getCall(primary);

		callBefores(args);
		if (primary != null) {
			Object ret = primaryCall.invoke(primary, args);
			callAfters(args);
			return ret;
		}
		callAfters(args);
		return null; // TODO cleanup
	}

	void callBefores(Object... args) throws Throwable {
		ArrayList<GFMethod> applicables = getApplicableMethods(befores, args);

		ArrayList<GFMethod> sorted = sort(applicables, true);
		for (GFMethod gfm : sorted) {
			getCall(gfm).invoke(gfm, args);
		}
	}

	void callAfters(Object... args) throws Throwable {
		ArrayList<GFMethod> applicables = getApplicableMethods(afters, args);

		ArrayList<GFMethod> sorted = sort(applicables, false);
		for (GFMethod gfm : sorted) {
			getCall(gfm).invoke(gfm, args);
		}
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
			Object call(Integer entity) {
				System.out.printf("%s is a integer", entity);
				return "";
			}
		});
		explain.addMethod(new GFMethod() {
			Object call(Number entity) {
				System.out.printf("%s is a number", entity);
				return "";
			}
		});
		explain.addMethod(new GFMethod() {
			Object call(String entity) {
				System.out.printf("%s is a string", entity);
				return "";
			}
		});
		explain.addAfterMethod(new GFMethod() {
			void call(Integer entity) {
				System.out.printf(" (in hexadecimal, is %x)", entity);
			}
		});
		explain.addBeforeMethod(new GFMethod() {
			void call(Number entity) {
				System.out.printf("The number ", entity);
			}
		});
		println(explain.call(123));
		println(explain.call("Hi"));
		println(explain.call(3.14159));

	}
}
