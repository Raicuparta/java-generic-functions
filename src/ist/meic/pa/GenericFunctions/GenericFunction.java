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
		replace(primaries, method);
	}

	public void addBeforeMethod(GFMethod method) {
		replace(befores, method);
	}
	
	public void addAfterMethod(GFMethod method) {
		replace(afters, method);
	}
	
	// checks if method with same argument types already exists
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

	public Object call(Object... args) throws Throwable {
		callBefores(args);
		Object ret = callPrimary(args);
		callAfters(args);
		return ret;
	}
	
	public Object callPrimary(Object... args) throws Throwable {
		ArrayList<GFMethod> applicables = getApplicableMethods(primaries, args);
		if(applicables.isEmpty()) 
				throw new IllegalArgumentException("No methods for generic function " + name + " with args " + printArgs(args)
						+ " of classes " + printArgTypes(args));
		ArrayList<GFMethod> sorted = sort(applicables, true);
		GFMethod primary = sorted.get(0);
		Method primaryCall = getCall(primary);
		if (primary != null) {
			Object ret = primaryCall.invoke(primary, args);
			callAfters(args);
			return ret;
		}
		return null;
	}

	private String printArgs(Object[] args) {
		String result = "";
		for(Object arg : args){
			result += printcenas(arg);
		}
		return result;
	}
	
	public static String printcenas(Object obj) {
		String result = "";
		if (obj instanceof Object[]) {
			result += Arrays.deepToString((Object[]) obj);
		} else {
			result += obj +" ";
		}
		return result;
	}

	private String printArgTypes(Object[] args) {
		String result = "";
		for(int i = 0; i < args.length; i++){
			System.out.println("merda");
			if(i==args.length-1){
				result += args[i].getClass().getName();
				
			}
			else
				result += args[i].getClass().getName() + ";, ";
		}
		result += "\n";
		return result;
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
}
