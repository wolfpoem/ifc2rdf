package cn.org.cbim.ifc2rdf.schedule;

import be.ugent.IfcSpfReader;
import cn.org.cbim.ifc2rdf.dao.TaskDao;
import cn.org.cbim.ifc2rdf.model.Status;
import cn.org.cbim.ifc2rdf.model.TaskDO;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskSchedule {

    private static final Logger logger = LoggerFactory.getLogger(TaskSchedule.class);
    // 加锁防止任务并行执行
    private static boolean pollLock = false;

    @Value("${app.ifc-path}")
    private String ifcPath;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private RDFConnection rdfConnection;


    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    @Transactional
    public synchronized void pollModel() {
        if (pollLock) {
            return;
        }
        try {
            pollLock = true;
            TaskDO taskDO = taskDao.getOldestTask();
            if (taskDO != null){
                try {
                    taskDao.changeStatus(taskDO.getId(),Status.Ready,"Ready");
                    String ifcName = taskDO.getFile();
                    File ifcFile = new File(ifcPath + ifcName);
                    if (!ifcFile.exists()){
                        taskDao.changeStatus(taskDO.getId(),Status.IfcNotFound,"IfcNotFound");
                        return;
                    }

                    //ifc 汉化
                    String ifcCNName =ifcName.substring(0, ifcName.lastIndexOf(".")) + "-CN" + ifcName.substring(ifcName.lastIndexOf("."));
                    FileReader fr = new FileReader(ifcFile);
                    BufferedReader reader = new BufferedReader(fr);
                    String line;
                    // 一行一行的读,减少内存占用
                    File ifcCNFile = new File(ifcPath + ifcCNName);
                    FileWriter fw = new FileWriter(ifcCNFile);

                    while ((line = reader.readLine()) != null) {
                        fw.write(Unicode2Chinese(line));
                        fw.write("\r\n");
                    }
                    reader.close();
                    fr.close();
                    fw.flush();
                    fw.close();
                    taskDao.changeStatus(taskDO.getId(),Status.Ifc2CNSuccess,"Ifc2CNSuccess");

                    //ifc 转 rdf
                    IfcSpfReader r = new IfcSpfReader();
                    try {
                        r.convert(ifcPath + ifcCNName, ifcPath+ifcName.substring(0, ifcName.lastIndexOf(".")) + "-CN.ttl" , ifcPath);
                    } catch (IOException e) {
                        logger.error("ifc转rdf异常",e);
                    }
                    taskDao.changeStatus(taskDO.getId(),Status.Ifc2RdfSuccess,"Ifc2RdfSuccess");

                    //导入到数据库
//                    DatasetGraphTDB datasetGraphTDB = new DatasetGraphTDB();

                } catch (IOException e) {
                    logger.error("异常",e);
                    taskDao.changeStatus(taskDO.getId(),Status.IfcIOE,null);
                }
            }

        } finally {
            // 释放锁
            pollLock = false;
        }
    }

    public static String Unicode2Chinese(String str) {
        int length = str.length();
        int count = 0;
        StringBuilder res = new StringBuilder();
        String regEx = "\\\\X2\\\\[0-9a-zA-Z]*\\\\X0\\\\";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String matchedUnicodeStr = matcher.group();
            StringBuilder convertedChineseStr = new StringBuilder();
            String matchedUnicodeStrAfterTrim = matchedUnicodeStr.substring(4, matchedUnicodeStr.length() - 4);
            for (int i = 0; i < matchedUnicodeStrAfterTrim.length(); i = i + 4) {
                String singleUnicode = matchedUnicodeStrAfterTrim.substring(i, i + 4);
                convertedChineseStr.append((char) Integer.parseInt(singleUnicode, 16));
            }
            int index = matcher.start();
            res.append(str, count, index);//添加前面不是unicode的字符
            res.append(convertedChineseStr);//添加转换后的字符
            count = index + matchedUnicodeStr.length();//统计下标移动的位置

        }
        res.append(str, count, length);
        return res.toString();
    }

    public static void main(String[] args) {
        String str = "05= IFCPROJECT('2vs$6EoRvDt9aVJ1qjP15n',#41,'0001',$,$,'\\X2\\987976EE540D79F0\\X0\\','\\X2\\987976EE72B66001\\X0\\',(#97),#92);";
        String s = Unicode2Chinese(str);
    }
}
