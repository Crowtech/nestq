package au.com.crowtech.quarkus.nest.models;


import java.io.Serializable;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DataTable<T> implements Serializable {

    private static final long serialVersionUID = -7304814269819778382L;
    public long draw;
    public long recordsTotal;
    public long recordsFiltered;
    public List<T> data;
    public String error;

    public DataTable() {

    }

    public void setDraw(int draw) {
        this.draw = draw;
    }
    public void setRecordsTotal(long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }
    public void setRecordsFiltered(long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }
    public void setData(List<T> data) {
        this.data = data;
    }
    public void setError(String error) {
        this.error = error;
    }

    public long getDraw() {
        return this.draw;
    }
    public long getRecordsTotal() {
        return this.recordsTotal;
    }
    public long getRecordsFiltered() {
        return this.recordsFiltered;
    }
    public List<T> getData() {
        return this.data;
    }
    public String getError() {
        return this.error;
    }


}