package com.tiho.dlplugin.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 对象工具，包含一些对象操作的方法。
 * 
 * @author Joey.Dai
 * 
 */
public final class ObjectUtil {

	/**
	 * 把原对象中的属性拷贝到目标对象中。 原对象中如果某个属性值为null，则不复制该属性。
	 * 拷贝方式是按照属性名来进行。即：如果原对象中的某个属性不存在于目标对象中，则该属性 将不会被复制过去，只有具有相同属性名的属性才会被复制过去。
	 * 
	 * @param dest
	 *            目标对象
	 * @param origin
	 *            原对象
	 * @throws IllegalArgumentException
	 *             参数为空
	 */
	public final static void copyProperties(Object dest, Object origin)
			throws IllegalArgumentException {
		if (dest == null || origin == null) {
			throw new NullPointerException("参数不能为空。");
		}

		Class<?> destC = dest.getClass();
		Class<?> originC = origin.getClass();

		while (originC != null) {
			Field[] fields = originC.getDeclaredFields();
			for (Field field : fields) {
				String fname = field.getName();

				if (!fname.equals("serialVersionUID")) {
					Object value = getValue(origin, field);

					if (value != null) {
						Field destF = null;
						try {
							destF = destC.getDeclaredField(fname);
							destF.setAccessible(true);// 强行访问
							destF.set(dest, value);
						} catch (NoSuchFieldException e) {
							continue;
						} catch (IllegalAccessException e) {
							// nothing is gonna happen
						}
					}
				}
			}
			originC = originC.getSuperclass();
		}
	}
	
	private final static Object getValue(Object instance,Field f){
		f.setAccessible(true);
		try {
			return f.get(instance);
		}  catch (IllegalAccessException e) {
			//nothing happens.
		}
		return null;
	}
	
	public final static <T> T generateByObject(Class<T> c, Object origin)
			throws Exception {
		T target = c.newInstance();
		copyProperties(target, origin);
		return target;
	}
	
	public static byte[] toByte(Object o) throws IOException{
		byte[] d = null;
		if(o instanceof Serializable){
			ByteArrayOutputStream baos  = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
			
			d = baos.toByteArray();
			
			oos.close();
			baos.close();
		}
		
		return d;
	}

	
	
	public static Object toObject(byte[] bb){
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bb);
			ObjectInputStream os = new ObjectInputStream(bais);
			return os.readObject();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
