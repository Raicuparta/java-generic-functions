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

	public ArrayList<GFMethod> sort(ArrayList<GFMethod> list, boolean specificFirst) {
		ArrayList<GFMethod> sorted = new ArrayList<GFMethod>();

		for (GFMethod listGfm : list) {
			if (sorted.isEmpty()) {
				sorted.add(listGfm);
				break;
			}
			boolean added = false;
			for (int i = 0; i < sorted.size(); i++) {
				GFMethod sortedGfm = sorted.get(i);

				Class<?> sortedType = getCall(sortedGfm).getClass();
				Class<?> listType = getCall(listGfm).getClass();

				boolean sortCondition = listType.isAssignableFrom(sortedType);
				if (specificFirst)
					sortCondition = !sortCondition;

				if (sortCondition) {
					sorted.add(i, listGfm);
					added = true;
					break;
				}
			}
			if (!added)
				sorted.add(listGfm);
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
			if (applicable) applicables.add(gfm);
		}
		return applicables;
	}

	public void addAfterMethod(GFMethod method) {
		afters.add(method);
	}

	public Object call(Object... args) throws Throwable {

		GFMethod primary = null;
		Method primaryCall = null;
		
		ArrayList<GFMethod> applicables = getApplicableMethods(primaries, args);
		for (GFMethod gfm : applicables) {
			Method call = getCall(gfm);
			if (call == null)
				continue;

			for (int i = 0; i < args.length; i++) {
				Class<?> genericType = args[i].getClass();
				Class<?> specificType = call.getParameterTypes()[i];

				// check if applicable
				if (!(specificType.isAssignableFrom(genericType)))
					break;

				if (primary == null) {
					primary = gfm;
					primaryCall = getCall(primary);
					continue;
				}

				// TODO melhorar
				primaryCall = getCall(primary);

				Class<?> previousType = primaryCall.getClass();

				if (previousType.isAssignableFrom(specificType)) {
					primary = gfm;
				}
			}
		}

		callBefores(args);
		if (primary != null) {
			Object ret = primaryCall.invoke(primary, args);
			callAfters(args);
			return ret;
		}
		callAfters(args);
		return null; //TODO cleanup
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
		final GenericFunction add = new GenericFunction("add");
		add.addMethod(new GFMethod() {
			Object call(Integer a, Integer b) {
				return a + b;
			}
		});
		add.addMethod(new GFMethod() {
			Object call(Object[] a, Object[] b) throws Throwable {
				Object[] r = new Object[a.length];
				for (int i = 0; i < a.length; i++) {
					r[i] = add.call(a[i], b[i]);
				}
				return r;
			}
		});

		add.addMethod(new GFMethod() {
			Object call(Object[] a, Object b) throws Throwable {
				Object[] ba = new Object[a.length];
				Arrays.fill(ba, b);
				return add.call(a, ba);
			}
		});
		add.addMethod(new GFMethod() {
			Object call(Object a, Object b[]) throws Throwable {
				Object[] aa = new Object[b.length];
				Arrays.fill(aa, a);
				return add.call(aa, b);
			}
		});
		add.addMethod(new GFMethod() {
			Object call(String a, Object b) throws NumberFormatException, Throwable {
				return add.call(Integer.decode(a), b);
			}
		});
		add.addMethod(new GFMethod() {
			Object call(Object a, String b) throws NumberFormatException, Throwable {
				return add.call(a, Integer.decode(b));
			}
		});
		add.addMethod(new GFMethod() {
			Object call(Object[] a, List b) throws Throwable {
				return add.call(a, b.toArray());
			}
		});

		println(add.call(new Object[] { 1, 2 }, 3));
		println(add.call(1, new Object[][] { { 1, 2 }, { 3, 4 } }));
		println(add.call("12", "34"));
		println(add.call(new Object[] { "123", "4" }, 5));
		println(add.call(new Object[] { 1, 2, 3 }, Arrays.asList(4, 5, 6)));


	}
}
