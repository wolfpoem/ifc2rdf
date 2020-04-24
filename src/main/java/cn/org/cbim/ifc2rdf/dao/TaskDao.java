package cn.org.cbim.ifc2rdf.dao;

import cn.org.cbim.ifc2rdf.model.Status;
import cn.org.cbim.ifc2rdf.model.TaskDO;
import org.apache.ibatis.annotations.Param;

public interface TaskDao {

    int createTask(TaskDO taskDO);

    TaskDO getOldestTask();

    void changeStatus(@Param("id") Long id, @Param("status") Status ready, @Param("result") String result);

    TaskDO getTaskById(Long id);
}
