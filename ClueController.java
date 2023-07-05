package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.commons.contants.Contants;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.commons.utils.DateUtils;
import com.bjpowernode.crm.commons.utils.UUIDUtils;
import com.bjpowernode.crm.settings.domain.DicValue;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.DivValueService;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.Clue;
import com.bjpowernode.crm.workbench.domain.ClueActivityRelation;
import com.bjpowernode.crm.workbench.domain.ClueRemark;
import com.bjpowernode.crm.workbench.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ClueController {
    private final UserService userService;
    private final DivValueService divValueService;
    private final ClueService clueService;
    private final ClueRemarkService clueRemarkService;
    private final ActivityService activityService;
    private final ClueActivityRelationService clueActivityRelationService;

    public ClueController(UserService userService, DivValueService divValueService, ClueService clueService,
                          ClueRemarkService clueRemarkService, ActivityService activityService,
                          ClueActivityRelationService clueActivityRelationService) {
        this.userService = userService;
        this.divValueService = divValueService;
        this.clueService=clueService;
        this.clueRemarkService = clueRemarkService;
        this.activityService=activityService;
        this.clueActivityRelationService=clueActivityRelationService;
    }

    @RequestMapping("/workbench/clue/index.do")
    public String index(HttpServletRequest request){
        //调用service层方法，查询动态数据
        List<User> userList=userService.queryAllUsers();
        List<DicValue> appellationList=divValueService.queryDicValueByTypeCode("appellation");
        List<DicValue> clueStateList= divValueService.queryDicValueByTypeCode("clueState");
        List<DicValue> sourceList=divValueService.queryDicValueByTypeCode("source");
        //把数据保存到request中
        request.setAttribute("userList",userList);
        request.setAttribute("appellationList",appellationList);
        request.setAttribute("clueStateList",clueStateList);
        request.setAttribute("sourceList",sourceList);
        return "workbench/clue/index";
    }
    @RequestMapping("/workbench/clue/saveCreateClue.do")
    public @ResponseBody Object saveCreateClue(Clue clue, HttpSession session){
        User user=(User)session.getAttribute(Contants.SESSION_USER);
        //封装参数
        clue.setId(UUIDUtils.getUUID());
        clue.setCreateTime(DateUtils.formateDateTime(new Date()));
        clue.setCreateBy(user.getId());
        ReturnObject returnObject = new ReturnObject();
        //调用service层方法
        try {
            int ret = clueService.saveCreateClue(clue);
            if (ret == 0) {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            } else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试。。。");
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试。。。");
        }
        return  returnObject;
    }
    @RequestMapping("/workbench/clue/detailClue.do")
     public String detailClue(String id,HttpServletRequest request){
        //调用service层方法，查询数据
        Clue clue=clueService.queryClueForDetailById(id);
        List<ClueRemark> remarkList=clueRemarkService.queryClueRemarkForDetailByClueId(id);
        List<Activity> activityList=activityService.queryActivityForDetailByClueId(id);
        //将参数保存到request中
        request.setAttribute("clue",clue);
        request.setAttribute("remarkList",remarkList);
        request.setAttribute("activityList",activityList);
        return "workbench/clue/detail";
     }
     @RequestMapping("/workbench/clue/queryActivityForDetailByNameClueId.do")
     public @ResponseBody Object queryActivityForDetailByNameClueId(String activityName,String clueId){
        //封装参数
        Map<String,Object>map=new HashMap<>();
        map.put("activityName",activityName);
        map.put("clueId",clueId);
        //调用service层方法,查询市场活动
        List<Activity> activityList =activityService.queryActivityForDetailByNameClueId(map);
        //返回结果
        return activityList;
     }
     @RequestMapping("/workbench/clue/saveBd.do")
     public @ResponseBody Object saveBd(String[] activityId,String clueId){
        //封装参数
         ClueActivityRelation car=null;
         List<ClueActivityRelation> relationList = new ArrayList<>();
         for (String ai:activityId){
             car= new ClueActivityRelation();
             car.setActivityId(ai);
             car.setClueId(clueId);
             car.setId(UUIDUtils.getUUID());
             relationList.add(car);
         }
         ReturnObject returnObject = new ReturnObject();
         try {
             int ret=clueActivityRelationService.saveCreateClueActivityRelationByClueIdActivityId(relationList);
             if (ret>0){
                 returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
                 List<Activity>activityList=activityService.queryActivityForDetailByIds(activityId);
                 returnObject.setRetDate(activityList);
             }else {
                 returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                 returnObject.setMessage("数据错误");
             }
         }catch (Exception e){
             e.printStackTrace();
             returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
             returnObject.setCode("系统忙，请稍后重试...");
         }
         return returnObject;
     }
     @RequestMapping("/workbench/clue/deleteBd.do")
     public @ResponseBody Object deleteBd(ClueActivityRelation relation){
        ReturnObject returnObject = new ReturnObject();
        try {
            //调用service层方法
            int ret = clueActivityRelationService.deleteClueActivityRelationByClueIdActivityId(relation);
            if (ret>0){
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试");
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试");
        }
        return returnObject;
     }
     @RequestMapping("/workbench/clue/toConvert.do")
     public String toConvert(String id,HttpServletRequest request){
        //调用service层方法
        Clue clue=clueService.queryClueForDetailById(id);
        List<DicValue> stageList=divValueService.queryDicValueByTypeCode("stage");
        //把信息保存到request中
        request.setAttribute("clue",clue);
        request.setAttribute("stageList",stageList);
        return "workbench/clue/convert";
     }
     @RequestMapping("/workbench/clue/queryActivityForConvertBuNameClueId.do")
     public @ResponseBody Object queryActivityForConvertBuNameClueId(String activityName,String clueId){
        //封装参数
        Map<String,Object>map=new HashMap<>();
        map.put("activityName",activityName);
        map.put("clueId",clueId);
        //调用service层方法
        List<Activity> activityList=activityService.queryActivityForConvertByNameClueId(map);
        return activityList;
     }
     @RequestMapping("/workbench/clue/convertClue.do")
     public @ResponseBody Object convertClue(String clueId,String money,String name,String expectedDate,
                               String stage,String activityId,String isCreateTran,HttpSession session){
        //封装参数
        Map<String,Object> map = new HashMap<>();
        map.put("clueId",clueId);
        map.put("money",money);
        map.put("name",name);
        map.put("expectedDate",expectedDate);
        map.put("stage",stage);
        map.put("activityId",activityId);
        map.put("isCreateTran",isCreateTran);
        map.put(Contants.SESSION_USER,session.getAttribute(Contants.SESSION_USER));
         //调用service层方法
         ReturnObject returnObject = new ReturnObject();
         try {
             clueService.saveConvertClue(map);
             returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
         }catch (Exception e){
             returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
             returnObject.setMessage("系统忙，请稍后重试...");
         }
         return returnObject;
     }
}
