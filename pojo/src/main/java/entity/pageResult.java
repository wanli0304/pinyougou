package entity;

import java.io.Serializable;
import java.util.List;

public class pageResult implements Serializable {

    private Long total;
    private List <?> rows;

    public Long getTotal () {
        return total;
    }

    public void setTotal (Long total) {
        this.total = total;
    }

    public List <?> getRows () {
        return rows;
    }

    public void setRows (List <?> rows) {
        this.rows = rows;
    }

    public pageResult (Long total, List <?> rows) {

        this.total = total;
        this.rows = rows;
    }
}
