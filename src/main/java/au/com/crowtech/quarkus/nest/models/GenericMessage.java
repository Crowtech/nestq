package au.com.crowtech.quarkus.nest.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class GenericMessage<T extends Object> implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Object[] items;
    private Long total;
    
    /**
     * @param items
     * @param total
     */
        
    public GenericMessage(List<T> items, Long total) {
        if ((items == null) || (items.isEmpty())) {
            items = new ArrayList<>();
        }
        
        this.items = items.toArray();
        this.total = total;
    }

    public GenericMessage(List<T> items) {
        this(items, items.size());
    }
    
    private GenericMessage(List<T> items, Integer total) {
        if ((items == null) || (items.isEmpty())) {
            items = new ArrayList<>();
        }
        
        this.items = items.toArray();
        this.total = total.longValue();
    }
    
    public GenericMessage(Set<T> itemSet) {
    	if ((itemSet == null) || (itemSet.isEmpty())) {
    		itemSet = new HashSet<T>();
    	}
    	
    	this.items = itemSet.toArray();
    	Integer size = items.length;
    	this.total = size.longValue();
    }
    
    /**
     * @return the items
     */
    public Object[] getItems() {
        return items;
    }
    /**
     * @param items the items to set
     */
    public void setItems(T[] items) {
        this.items = items;
        this.total = (long)items.length;
    }
    /**
     * @return the total
     */
    public Long getTotal() {
        return (long)total;
    }
    
    public boolean isEmpty() {
    	return (items.length == 0);
    }
    
}