package au.com.crowtech.quarkus.nest.utils.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RawStringMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] items = new String[0];
	private Long total = 0L;
	
	
	/**
	 * @param items
	 * @param total
	 */
	public RawStringMessage() {
	}	
	
	/**
	 * @param items
	 * @param total
	 */
	public RawStringMessage(List<? extends String> items, Long total) {
		if ((items == null) || (items.isEmpty())) {
			items = new ArrayList<String>();
		}

		this.items = items.toArray(new String[0]);
		this.total = total;
	}
	
	/**
	 * @param items
	 * @param total
	 */
	public RawStringMessage(List<? extends String> items) {
		if ((items == null) || (items.isEmpty())) {
			items = new ArrayList<String>();
		}

		this.items = items.toArray(new String[0]);
		this.total = (long)items.size();
	}
	/**
	 * @return the items
	 */
	public String[] getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(String[] items) {
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
