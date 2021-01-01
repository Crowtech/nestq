package au.com.crowtech.quarkus.nest.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StringMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StringClass[] items = new StringClass[0];
	private Long total = 0L;
	
	
	/**
	 * @param items
	 * @param total
	 */
	public StringMessage() {
	}	
	
	/**
	 * @param items
	 * @param total
	 */
	public StringMessage(List<? extends StringClass> items, Long total) {
		if ((items == null) || (items.isEmpty())) {
			items = new ArrayList<StringClass>();
		}

		this.items = items.toArray(new StringClass[0]);
		this.total = total;
	}
	
	/**
	 * @param items
	 * @param total
	 */
	public StringMessage(List<? extends StringClass> items) {
		if ((items == null) || (items.isEmpty())) {
			items = new ArrayList<StringClass>();
		}

		this.items = items.toArray(new StringClass[0]);
		this.total = (long)items.size();
	}
	/**
	 * @return the items
	 */
	public StringClass[] getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(StringClass[] items) {
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
	
	
}
