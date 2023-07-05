package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.commons.contants.Contants;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.settings.domain.DicValue;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.DivValueService;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.workbench.domain.Tran;
import com.bjpowernode.crm.workbench.domain.TranHistory;
import com.bjpowernode.crm.workbench.domain.TranRemark;
import com.bjpowernode.crm.workbench.service.CustomerService;
import com.bjpowernode.crm.workbench.service.TranHistoryService;
import com.bjpowernode.crm.workbench.service.TranRemarkService;
import com.bjpowernode.crm.workbench.service.TranService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class TranController {

    @Autowired
    private DivValueService divValueService;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TranService tranService;

    @Autowired
    private TranRemarkService tranRemarkService;

    @Autowired
    TranHistoryService tranHistoryService;

    @RequestMapping("/workbench/transaction/index.do")
    public String index(HttpServletRequest request){

        List<DicValue> stageList=divValueService.queryDicValueByTypeCode("stage");
        List<DicValue> transactionTypeList=divValueService.queryDicValueByTypeCode("transactionType");
        List<DicValue> sourceList=divValueService.queryDicValueByTypeCode("source");

        request.setAttribute("stageList",stageList);
        request.setAttribute("transactionTypeList",transactionTypeList);
        request.setAttribute("sourceList",sourceList);

        return "workbench/transaction/index";
    }

    @RequestMapping("/workbench/transaction/toSave.do")
     public String toSave(HttpServletRequest request){
        //调用service层方法
        List<User>userList=userService.queryAllUsers();
        List<DicValue> stageList=divValueService.queryDicValueByTypeCode("stage");
        List<DicValue>transactionTypeList=divValueService.queryDicValueByTypeCode("transactionType");
        List<DicValue>sourceList=divValueService.queryDicValueByTypeCode("source");
        //保存到request中
        request.setAttribute("userList",userList);
        request.setAttribute("stageList",stageList);
        request.setAttribute("transactionTypeList",transactionTypeList);
        request.setAttribute("sourceList",sourceList);
        return "workbench/transaction/save";
     }

     @RequestMapping("/workbench/transaction/queryAllCustomerName.do")
     public @ResponseBody Object queryAllCustomerName(){
        //调用service层方法，查询客户名称
         List<String> customerNameList=customerService.queryAllCustomerName();
         return customerNameList;
     }

     @RequestMapping("/workbench/transaction/saveCreateTran.do")
     public @ResponseBody Object saveCreateTran(@RequestParam Map<String,Object> map, HttpSession session){
        //封装参数
         map.put(Contants.SESSION_USER,session.getAttribute(Contants.SESSION_USER));
         //调用service层方法
         ReturnObject returnObject=new ReturnObject();
         try {
             tranService.saveCreateTran(map);
             returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
         }catch (Exception e){
             returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
             returnObject.setMessage("系统忙，请稍后重试...");
         }
         return returnObject;
     }

     @RequestMapping("/workbench/transaction/detailTran.do")
     public String detailTran(String id,HttpServletRequest request){
        //调用service层方法
         Tran tran=tranService.queryTranForDetailById(id);
         List<TranRemark> remarkList = tranRemarkService.queryTranRemarkForDetailByTranId(id);
         List<TranHistory> historyList = tranHistoryService.queryTranHistoryForDetailByTranId(id);
         //根据tran所处阶段名称查询可能性
         //ResourceBundle bundle=ResourceBundle.getBundle("possibility");
         //String possibility=bundle.getString(tran.getStage());
         //保存到request中
         request.setAttribute("tran",tran);
         request.setAttribute("remarkList",remarkList);
         request.setAttribute("historyList",historyList);
         //request.setAttribute("possibility",possibility);
         //将结果返回
         return "workbench/transaction/detail";
     }
}
