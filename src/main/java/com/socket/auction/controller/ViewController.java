package com.socket.auction.controller;

import java.net.Socket;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {
    
    @RequestMapping("/livechat")
    public ModelAndView livechat(@RequestParam String actSno, @RequestParam String mmbrId, @RequestParam String service, HttpServletRequest request){
        String serviceUrl = request.getServerName();

        ModelAndView mv = new ModelAndView();
        mv.addObject("actSno", actSno);
        mv.addObject("service", service);
        mv.addObject("mmbrId", mmbrId);
        mv.addObject("serviceUrl", serviceUrl);
        mv.setViewName("livechat");

        return mv;
    } 
    
    @RequestMapping("/chat")
    public ModelAndView index(@RequestParam String actSno, @RequestParam String mmbrId, @RequestParam String service, @RequestParam String gameSeq, HttpServletRequest request){
        String serviceUrl = request.getServerName();

        ModelAndView mv = new ModelAndView();
        mv.addObject("actSno", actSno);
        mv.addObject("service", service);
        mv.addObject("mmbrId", mmbrId);
        mv.addObject("serviceUrl", serviceUrl);
        mv.addObject("gameSeq", gameSeq);
        mv.setViewName("index");

        return mv;
    } 

    @RequestMapping("/check")
    public ModelAndView healthCheck() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("check");

        return mv;
    } 

    @RequestMapping("/checkSocket")
    public ModelAndView healthSocket() {
        ModelAndView mv = new ModelAndView();
        
        try{
            Socket socket1 = new Socket("localhost", 9091);
            Socket socket2 = new Socket("localhost", 9092);
            Socket socket3 = new Socket("localhost", 9093);
            System.out.println(socket1);
            System.out.println(socket2);
            System.out.println(socket3);

            mv.setViewName("check");
        } catch (Exception e) {
            e.printStackTrace();
            mv.setViewName("fail");
        }

        return mv;
    }  
    
    @RequestMapping("/bid")
    public ModelAndView bid(@RequestParam String actSno, @RequestParam String service){
        ModelAndView mv = new ModelAndView();
        mv.addObject("actSno", actSno);
        mv.addObject("serviceUrl", service);
        mv.setViewName("bid");

        return mv;
    }    
    
    @RequestMapping("/bid1")
    public ModelAndView bid1(@RequestParam String actSno, @RequestParam String mmbrId,  @RequestParam String service){
        ModelAndView mv = new ModelAndView();
        mv.addObject("actSno", actSno);
        mv.addObject("mmbrId", mmbrId);
        mv.addObject("serviceUrl", service);
        mv.setViewName("bid1");

        return mv;
    }    
    
    @RequestMapping("/bid2")
    public ModelAndView bid2(@RequestParam String actSno, @RequestParam String mmbrId,  @RequestParam String service){
        ModelAndView mv = new ModelAndView();
        mv.addObject("actSno", actSno);
        mv.addObject("mmbrId", mmbrId);
        mv.addObject("serviceUrl", service);
        mv.setViewName("bid2");

        return mv;
    }  
}
