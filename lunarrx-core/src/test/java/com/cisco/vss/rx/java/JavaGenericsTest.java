package com.cisco.vss.rx.java;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class JavaGenericsTest {
	private class Dada<T> {

//	    public Class<T> typeOfT;

	    @SuppressWarnings("unchecked")
	    public Dada() {
//	    	Class<?> clazz = getClass().getSuperclass();
//	    	ParameterizedType genericSuperclass =  ((ParameterizedType) clazz.getGenericSuperclass());
//	    	Type[] args = genericSuperclass.getActualTypeArguments();
//	    	this.typeOfT = (Class<T>)args[0];
	    }
	    
//	    @SuppressWarnings("unchecked")
//	    public Class<?> getGenericParameter()
//	    {
//	    	Class<?> clazz = getClass();
//	    	Type type = clazz.;
//	    	ParameterizedType genericSuperclass = ((ParameterizedType) clazz.getGenericSuperclass());
//	        Type type = genericSuperclass.getActualTypeArguments()[0];
//	        if (type instanceof Class) {
//	          return (Class<T>) type;
//	        } else if (type instanceof ParameterizedType) {
//	          return (Class<T>) ((ParameterizedType)type).getRawType();
//	        }
//	        return null;
//	    }
	}
	
	@Ignore
	@Test
	public void testTypeOfT() {
		final Dada<String> dada = new Dada<String>();
		Class<?> clazz = dada.getClass();
		System.out.println(clazz.getTypeParameters().length);
		TypeVariable<?>[] args = clazz.getTypeParameters();
		for(TypeVariable<?> t : args)
			System.out.println(t.getName());		
    	ParameterizedType genericSuperclass =  ((ParameterizedType) clazz.getGenericSuperclass());
//    	Type[] args = genericSuperclass.getActualTypeArguments();
//    	Class<?> typeOfT = (Class<?>)args[0];
//    	assertEquals(String.class, typeOfT);
	}
}


