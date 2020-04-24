package cn.org.cbim.ifc2rdf.model;

/**
 * Created by ginkgo on 6/22/17.
 */
public enum Status {
    Initial,//初始化
    Ready,//准备
    IfcNotFound,//ifc文件不存在
    IfcIOE,//ifc io异常
    Ifc2CNSuccess,//ifc汉化成功
    Ifc2RdfSuccess,//ifc 转rdf成功
}
