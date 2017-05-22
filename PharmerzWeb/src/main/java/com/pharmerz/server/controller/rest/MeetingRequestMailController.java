package com.pharmerz.server.controller.rest;

import com.pharmerz.Appcations.Email;
import com.pharmerz.Appcations.IphexTemplate;
//import com.pharmerz.server.domain.model.IphexContactus;
import com.pharmerz.Appcations.MeetingRequestTemplate;
import com.pharmerz.server.domain.model.MeetingRequestMail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Amit on 08-05-2017.
 */
@RestController
@RequestMapping("/api/v1/")
public class MeetingRequestMailController {

    Email email = new Email();

    @PostMapping(value = "meetingrequest")
    public String sendmail(@RequestBody MeetingRequestMail meetingRequestMail){

     String from=meetingRequestMail.getFrom();
     String to=meetingRequestMail.getTo();
     String subject=meetingRequestMail.getSubject();
     String message=meetingRequestMail.getBody();


        MeetingRequestTemplate meetingRequestTemplate=new MeetingRequestTemplate();
        String body=meetingRequestTemplate.MeetingRequest(from,message);
        email.SendMail(to,subject,body);


        return "Mail send Successfully";
    }

}
