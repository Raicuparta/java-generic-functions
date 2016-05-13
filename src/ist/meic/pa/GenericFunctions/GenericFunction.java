package ist.meic.pa.GenericFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.lang.reflect.*;

public class GenericFunction {
	private String name;
	private ArrayList<GFMethod> befores = new ArrayList<GFMethod>();
	private ArrayList<GFMethod> afters = new ArrayList<GFMethod>();
	private ArrayList<GFMethod> primaries = new ArrayList<GFMethod>();
	
	private HashMap<String, ArrayList<GFMethod>> cache = new HashMap<String, ArrayList<GFMethod>>();

	public GenericFunction(String name) {
		this.name = name;
	}

	public void addMethod(GFMethod method) {
		replace(primaries, method);
	}

	public void addBeforeMethod(GFMethod method) {
		replace(befores, method);
	}

	public void addAfterMethod(GFMethod method) {
		replace(afters, method);
	}

	public Object call(Object... args) {
		ArrayList<GFMethod> effective = getEffective(args);
		Object primaryRet = null;
		for (GFMethod gfm : effective) {
			Method m = getCall(gfm);
			Object ret = callMethod(m, gfm, args);
			if (ret != null) primaryRet = ret;
		}
		return primaryRet;
	}

	// checks if method with same arguments types already exists
	// replaces if true, adds otherwise
	public void replace(ArrayList<GFMethod> list, GFMethod method) {
		Class<?>[] newTypes = getCall(method).getParameterTypes();

		for (int e = 0; e < list.size(); e++) {
			GFMethod gfm = list.get(e);
			boolean exists = true;
			for (int i = 0; i < newTypes.length; i++) {
				Class<?> type = getCall(gfm).getParameterTypes()[i];
				Class<?> newType = newTypes[i];
				if (type != newType) {
					exists = false;
					break;
				}
			}
			if (exists) {
				list.set(e, method);
				return;
			}
		}
		list.add(method);
	}

	// sorts a GFMethod list
	// if specificFirst is true, the methods with the most specific arguments
	// come first
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
					if (appType != sortedType) {
						sortCondition = appType.isAssignableFrom(sortedType);
						break;
					}
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

	public ArrayList<GFMethod> getEffective(Object... args) {
		ArrayList<GFMethod> effective = getFromCache(args);
		if (effective != null) return effective;
		
		effective = getBefores(args);
		effective.add(getPrimary(args));
		effective.addAll(getAfters(args));
		putOnCache(effective, args);

		return effective;
	}
	
	public ArrayList<GFMethod> getFromCache(Object...args) {
		String key = printArgTypes(args);
		return cache.get(key);
	}
	
	public void putOnCache(ArrayList<GFMethod> value, Object...args) {
		String key = printArgTypes(args);
		cache.put(key,  value);
	}

	public GFMethod getPrimary(Object... args) {
		ArrayList<GFMethod> applicables = getApplicableMethods(primaries, args);
		if (applicables.isEmpty())
			throw new IllegalArgumentException("No methods for generic function " + name + " with args "
					+ printArgs(args) + " of classes " + printArgTypes(args));
		ArrayList<GFMethod> sorted = sort(applicables, true);
		return sorted.get(0);
	}

	public ArrayList<GFMethod> getBefores(Object... args) {
		ArrayList<GFMethod> applicables = getApplicableMethods(befores, args);
		return sort(applicables, true);
	}

	public ArrayList<GFMethod> getAfters(Object... args) {
		ArrayList<GFMethod> applicables = getApplicableMethods(afters, args);
		return sort(applicables, false);
	}

	Object callMethod(Method m, GFMethod gfm, Object... args) {
		Object ret = null;
		m.setAccessible(true);
		try {
			ret = m.invoke(gfm, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			System.err.println("GFMethod needs a call method");
			System.exit(-1);
		}
		return ret;
	}

	Method getCall(GFMethod gfm) {
		for (Method m : gfm.getClass().getDeclaredMethods()) {
			if (m.getName() == "call") {
				return m;
			}
		}
		return null;
	}

	private String printArgs(Object[] args) {
		String result = "[";
		for (Object arg : args) {
			result += printObj(arg);
			if (arg != args[args.length - 1])
				result += " ";
		}
		return result + "]";
	}

	public static String printObj(Object obj) {
		String result = "";
		if (obj instanceof Object[]) {
			result += Arrays.deepToString((Object[]) obj);
		} else {
			result += obj;
		}
		return result;
	}

	private String printArgTypes(Object[] args) {
		String result = "[";
		for (int i = 0; i < args.length; i++) {
			result += "class ";
			if (i == args.length - 1) {
				result += args[i].getClass().getName();

			} else
				result += args[i].getClass().getName() + ", ";
		}
		result += "]\n";
		return result;
	}
}
