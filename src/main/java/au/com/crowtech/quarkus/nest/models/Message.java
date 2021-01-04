package au.com.crowtech.quarkus.nest.models;


import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Message<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private T[] items;
	private Long total;
	
	
	/**
	 * @param items
	 * @param total
	 */
	public Message(List<T> items, Long total,Class<T[]> clazz) {
		if ((items == null) || (items.isEmpty())) {
			items = new ArrayList<T>();
		}

		this.items = Message.list2Array(clazz,items);
		this.total = total;
	}
	/**
	 * @return the items
	 */
	public T[] getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(T[] items) {
		this.items = items;
	}
	/**
	 * @return the total
	 */
	public Long getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(Long total) {
		this.total = total;
	}
	
	public static <C, T extends C> C[] toArray(Class<C> componentType, List<T> list) {
	    @SuppressWarnings("unchecked")
	    C[] array = (C[])Array.newInstance(componentType, list.size());
	    return list.toArray(array);
	}
	
	public static <T> T[] list2Array(Class<T[]> clazz, List<T> elements)
	{
	    T[] array = clazz.cast(Array.newInstance(clazz.getComponentType(), elements.size()));
	    return elements.toArray(array);
	}
}
