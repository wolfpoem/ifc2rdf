package cn.org.cbim.ifc2rdf.ctrl;

import cn.org.cbim.ifc2rdf.model.TaskDO;
import cn.org.cbim.ifc2rdf.service.SparqlService;
import cn.org.cbim.ifc2rdf.service.TaskService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Date;
import java.util.Iterator;

@Api(value = "上传接口")
@RestController
@RequestMapping("/v1")
public class UploadController {
    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);

    @Value("${app.ifc-path}")
    private String ifcPath;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SparqlService sparqlService;

    /**
     * 经实际测试  第一个上传方法在50M 和230M文件的测试中都是时间最快的
     * 且方法而会因为一次写1024个 多写入一些字符 导致文件大小改变
     * 三个方法文件所占空间一样
     */

    @PostMapping(value="/upload")
    public String  fileUpload2(@RequestParam("file") CommonsMultipartFile file) throws IOException {
        long  startTime=System.currentTimeMillis();
        LOG.info("fileName："+file.getOriginalFilename());
        if (!file.getOriginalFilename().endsWith(".ifc")){
            return "只支持ifc格式文件转换";
        }
        String fileName = new Date().getTime()+"-"+file.getOriginalFilename();
        String path=ifcPath+fileName;

        File newFile=new File(path);
        //通过CommonsMultipartFile的方法直接写文件
        file.transferTo(newFile);
        long  endTime=System.currentTimeMillis();
        LOG.info("方法1的运行时间："+ (endTime - startTime) +"ms");

        Long id = taskService.createTask(fileName);

        if (id > 0){
            return "上传成功! 你可根据任务id " + id + " 查询状态";
        }

        return "上传异常" + id;
    }

    @GetMapping("task")
    public String getTaskResult(@RequestParam("id")Long id){
        TaskDO taskDO = taskService.getTaskById(id);
        return taskDO == null ?  "任务不存在" : taskDO.getResult();
    }

    @GetMapping("search")
    public Object getQueryResult(@RequestParam("sql") String sql,@RequestParam("dataset") String dateSet){
        return sparqlService.query(dateSet,sql);
    }


    @PostMapping("/upload2")
    public String  fileUpload(@RequestParam("file") CommonsMultipartFile file) throws IOException {

        //用来检测程序运行时间
        long  startTime=System.currentTimeMillis();
//        LOG.info("fileName："+file.getOriginalFilename());

        try {
            //获取输出流
            OutputStream os=new FileOutputStream("E:\\Temp\\1-"+new Date().getTime()+"-"+file.getOriginalFilename());
            //获取输入流 CommonsMultipartFile 中可以直接得到文件的流
            InputStream is=file.getInputStream();
            byte[] bts = new byte[1024];
            //一个一个字节的读取并写入
            while(is.read(bts)!=-1)
            {
                os.write(bts);
            }
            os.flush();
            os.close();
            is.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long  endTime=System.currentTimeMillis();
        LOG.info("方法2的运行时间："+String.valueOf(endTime-startTime)+"ms");
        return "success";
    }

    /*
     *采用spring提供的上传文件的方法
     */
    @PostMapping(value="/upload3")
    public String  springUpload(HttpServletRequest request) throws IllegalStateException, IOException
    {
        long  startTime=System.currentTimeMillis();
        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(
                request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if(multipartResolver.isMultipart(request))
        {
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
            //获取multiRequest 中所有的文件名
            Iterator iter=multiRequest.getFileNames();

            while(iter.hasNext())
            {
                //一次遍历所有文件
                MultipartFile file=multiRequest.getFile(iter.next().toString());
                if(file!=null)
                {
                    String path="E:\\Temp\\3-"+new Date().getTime()+"-"+file.getOriginalFilename();
                    //上传
                    file.transferTo(new File(path));
                }

            }

        }
        long  endTime=System.currentTimeMillis();
        LOG.info("方法3的运行时间："+String.valueOf(endTime-startTime)+"ms");
        return "/success";
    }
}
