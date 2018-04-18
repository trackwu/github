package com.tiho.base.base.http.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author program
 * 
 */
public class JsonUtil {

	public static String toJson(Object o) throws Exception {
		return _toJson(o).toString();
	}

	@SuppressWarnings("rawtypes")
	private static Object _toJson(Object o) throws Exception {
		if (o == null)
			return null;

		Class c = o.getClass();

		List list = null;

		if (c.isArray()) {
			int length = Array.getLength(o);
			if (list == null)
				list = new LinkedList();

			for (int i = 0; i < length; i++) {
				Object arrObj = Array.get(o, i);
				Object arrJson = _toJson(arrObj);
				list.add(arrJson);
			}

			return new JSONArray(list);

		} else if (List.class.isAssignableFrom(c)) {
			if (list == null)
				list = new LinkedList();

			for (int i = 0; i < ((List) o).size(); i++) {
				Object listObj = ((List) o).get(i);
				Object arrJson = _toJson(listObj);
				list.add(arrJson);
			}
			return new JSONArray(list);
		} else if (c.isPrimitive() || canOutput(c))
			return o;

		else {
			return objectToJsonObject(o, c);
		}
	}

	/**
	 * 把非容器类转成jsonobject
	 * 
	 * @param o
	 * @param c
	 * @return
	 * @throws Exception
	 */
	private static JSONObject objectToJsonObject(Object o, Class c) throws Exception {
		JSONObject jo = new JSONObject();
		while (c != null && c != Object.class) {

			Field[] fields = c.getDeclaredFields();

			for (Field field : fields) {

				field.setAccessible(true);

				if (field.getType().isPrimitive()) {
					jo.put(field.getName(), field.get(o));
				} else if (canOutput(field.getType())) {
					jo.put(field.getName(), field.get(o));
				} else {

					Object s = _toJson(field.get(o));
					if (s != null)
						jo.put(field.getName(), s);
				}

			}
			c = c.getSuperclass();

		}

		return jo;
	}

	/**
	 * 是否可以直接输出值
	 * 
	 * @param c
	 * @return
	 */
	private static boolean canOutput(Class c) {

		return c == Byte.class || c == Short.class || c == Integer.class || Long.class == c || c == Float.class || c == Double.class || String.class == c || Timestamp.class == c || Date.class == c;

	}

	public static <T> T fromJson(String json, Class<T> c) throws InstantiationException, IllegalAccessException, JSONException {
		Object target = c.newInstance();
		JSONObject jo = new JSONObject(json);
		Iterator it = jo.keys();
		while (it.hasNext()) {
			map(it.next(), c, target, jo);
			// Object key = it.next();
			//
			// Field f = c.getDeclaredField((String) key);
			// Object value = subJsonBind(f, jo.get((String) key));
			//
			// String fType = f.getType().getName();
			// f.setAccessible(true);
			// if (fType.equals("java.lang.Long")) {
			// f.set(target, ((Integer) value).longValue());
			// } else if (fType.equals("java.lang.Short")) {
			// f.set(target, ((Integer) value).shortValue());
			// } else if (fType.equals("java.lang.Byte") ||
			// fType.equals("byte")) {
			// f.set(target, ((Integer) value).byteValue());
			// } else if (fType.equals("java.lang.Float") ||
			// fType.equals("float")) {
			// f.set(target, ((Double) value).floatValue());
			// } else {
			// f.set(target, value);
			// }

		}
		return (T) target;
	}

	private static void map(Object key, Class c, Object target, JSONObject jo) {

		Field f = null;
		Class clz = c;
		while (clz != Object.class) {
			try {
				f = clz.getDeclaredField((String) key);
				if (f != null)
					break;

			} catch (NoSuchFieldException e) {
				clz = clz.getSuperclass();
			}
		}

		if (f != null) {

			try {
				Object value = subJsonBind(f, jo.get((String) key));

				String fType = f.getType().getName();
				f.setAccessible(true);
				if (fType.equals("java.lang.Long")) {
					f.set(target, Long.parseLong(value.toString()));
				} else if (fType.equals("java.lang.Short")) {
					f.set(target, Short.parseShort(value.toString()));
				} else if (fType.equals("java.lang.Byte") || fType.equals("byte")) {
					f.set(target, Byte.parseByte(value.toString()));
				} else if (fType.equals("java.lang.Float") || fType.equals("float")) {
					f.set(target, Float.parseFloat(value.toString()));
				} else if (fType.equals("java.lang.Double") || fType.equals("double")) {
					f.set(target, Double.parseDouble(value.toString()));
				} else {
					f.set(target, value);
				}
			} catch (Exception e) {
				// IGNORE
			}
		}
	}

	private static Object subJsonBind(Field eleField, Object jo) throws Exception {
		if (jo instanceof JSONObject) {
			Class newCls = eleField.getType();
			Object newObj = newCls.newInstance();

			Iterator it = ((JSONObject) jo).keys();
			while (it.hasNext()) {
				Object key = it.next();
				Field f = newCls.getDeclaredField((String) key);
				Object v = subJsonBind(f, ((JSONObject) jo).get((String) key));
				f.setAccessible(true);
				f.set(newObj, v);
			}
			return newObj;

		} else if (jo instanceof JSONArray) {
			Class newCls = eleField.getType();
			Object newObj = null;
			Class elementType = null;

			if (newCls.isArray()) {
				elementType = newCls.getComponentType();
				newObj = Array.newInstance(elementType, ((JSONArray) jo).length());
			} else if (List.class.isAssignableFrom(newCls)) {
				newObj = new LinkedList();
				elementType = getListElementType(eleField);
			}

			if (newObj != null) {
				for (int i = 0; i < ((JSONArray) jo).length(); i++) {
					Object ele = ((JSONArray) jo).get(i);
					Object eleObj = null;
					if (ele instanceof JSONObject)
						eleObj = fromJson(ele.toString(), elementType);
					else
						eleObj = ele;

					addElement(newObj, newCls, i, eleObj);
				}
			}

			return newObj;

		} else {
			// if (eleField.getType().isPrimitive())
			return jo;

			// Object value = eleField.getType().newInstance();
			// eleField.set(value, jo);
			// return value;
		}
	}

	/**
	 * 往容器(数组和list)中添加对象
	 * 
	 * @param contain
	 * @param containCls
	 * @param index
	 * @param element
	 */
	private static void addElement(Object contain, Class containCls, int index, Object element) {
		if (containCls.isArray())
			Array.set(contain, index, element);
		else if (List.class.isAssignableFrom(containCls))
			((List) contain).add(element);
	}

	/**
	 * 获取某个字段的泛型
	 * 
	 * @param field
	 * @return
	 */
	private static Class getListElementType(Field field) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			Type[] actualTypes = paramType.getActualTypeArguments();
			for (Type aType : actualTypes) {
				if (aType instanceof Class) {
					Class clz = (Class) aType;
					return clz;
				}
			}
		}else{
			
		}
		return null;
	}

}
