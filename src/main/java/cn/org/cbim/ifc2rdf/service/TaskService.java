package cn.org.cbim.ifc2rdf.service;

import cn.org.cbim.ifc2rdf.dao.TaskDao;
import cn.org.cbim.ifc2rdf.model.TaskDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskDao taskDao;


    public Long createTask(String file) {
        TaskDO taskDO = new TaskDO();
        taskDO.setFile(file);
        taskDao.createTask(taskDO);
        return taskDO.getId();
    }

    public TaskDO getTaskById(Long id) {
        return taskDao.getTaskById(id);
    }
}
