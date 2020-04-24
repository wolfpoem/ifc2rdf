package cn.org.cbim.ifc2rdf.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class TaskDO {
    private Long id;
    private String file;
    private String result;
    private Status status;
    private Date updateTime;

    public TaskDO(){
        this.updateTime = new Date();
        this.status = Status.Initial;
    }
}