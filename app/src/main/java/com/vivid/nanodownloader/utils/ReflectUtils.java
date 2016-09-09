package com.vivid.nanodownloader.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {
	private static final String TAG = "ReflectUtils";

	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object object, String methodName,
			Object[] params, Class<?>[] paramTypes) {
		if (object == null) {
			return null;
		}

		Class<?>[] types = null;

		if (paramTypes != null) {
			types = paramTypes;
		} else if (params != null) {
			types = new Class<?>[params.length];
			for (int i = 0; i < params.length; ++i) {
				types[i] = (params[i] == null) ? null : params[i].getClass();
			}
		}

		Method method = getMethodEx(object, methodName, types);
		// invoke
		if (method != null) {
			method.setAccessible(true);
			try {
				return object instanceof Class ? (T) method
						.invoke(null, params) : (T) method.invoke(object,
						params);
			} catch (IllegalAccessException iae) {
				LogUtils.i(TAG, "IllegalAccessException: " + "invokeMethod "
						+ methodName);
			} catch (IllegalArgumentException iage) {
				LogUtils.i(TAG, "IllegalArgumentException: " + "invokeMethod "
						+ methodName);
			} catch (InvocationTargetException ite) {
				LogUtils.i(TAG, "InvocationTargetException: " + "invokeMethod "
						+ methodName);
			} catch (ExceptionInInitializerError eiie) {
				LogUtils.i(TAG, "ExceptionInInitializerError: " + "invokeMethod "
						+ methodName);
			} catch (ClassCastException cce) {
				LogUtils.i(TAG, "ClassCastException: " + "invokeMethod "
						+ methodName);
				throw cce;
			}
		}

		return null;
	}

	private static Method getMethodEx(Object object, String methodName,
			Class<?>[] paramTypes) {
		Method method = null;
		Class<?> theClass = object instanceof Class ? (Class<?>) object
				: object.getClass();
		for (; theClass != Object.class; theClass = theClass.getSuperclass()) {
			try {
				method = theClass.getDeclaredMethod(methodName, paramTypes);
				return method;
			} catch (NoSuchMethodException e) {
				LogUtils.i(TAG, "NoSuchMethodException: " + theClass.getName() + "."
						+ methodName);
			} catch (SecurityException e) {
				LogUtils.i(TAG, "SecurityException: " + theClass.getName() + "."
						+ methodName);
			}
		}

		LogUtils.i(TAG, "getMethodEx: " + object.getClass().getName() + "."
				+ methodName + " not found");
		return null;
	}

}
