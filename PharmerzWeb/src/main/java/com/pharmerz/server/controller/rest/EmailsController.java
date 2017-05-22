package com.pharmerz.server.controller.rest;



import com.pharmerz.Appcations.Email;
import com.pharmerz.Appcations.Sms;
import com.pharmerz.server.domain.model.Emails;

import com.pharmerz.server.domain.repository.EmailFlagRepository;
import com.pharmerz.server.domain.repository.EmailsRepository;
import com.pharmerz.service.EmailService;
import com.pharmerz.service.OtpGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by Amit on 17-01-2017.
 */

@RestController
@RequestMapping("/api/v1")
public class EmailsController {

    private String  To=null;


    @Autowired
    private EmailService emailService;
    OtpGenerator otp=new OtpGenerator();
    @Autowired
    private EmailsRepository emailsRepository;
    @Autowired
    EmailFlagRepository emailFlagRepository;
    Email email=new Email();
    Date date = new Date();
    Timestamp timestamp1 = new Timestamp(date.getTime());
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


/****************Get Emails **************/
    /*************url :-Get - "/api/v1/emails"  **************/

    @RequestMapping(value = "/emails" ,method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Emails>> getEmailss(){
        Collection<Emails> emailss=emailService.findall();
        return new ResponseEntity<Collection<Emails>>(emailss, HttpStatus.OK);
    }

/****************Create Emails **************/
    /*************url :-Create(Post) - "/api/v1/emails"  **************/

    @PostMapping(value = "/emails",consumes = MediaType.APPLICATION_JSON_VALUE ,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Emails> createEmails(@RequestBody Emails emails){

        /**Initialising Parameters For Email*/

        int hashcode =otp.RandomOtp();

        this.To=emails.getEmail();
        String Subject="Verification OTP from Pharmerz";
        String HtmlBody=hashcode+" is your Pharmerz verification code .";


        //Console Logs
        System.out.println("************************************************+To "+To);
        System.out.println("************************************************+Subject "+Subject);
        System.out.println("************************************************+HtmlBody "+HtmlBody);
        System.out.println("************************************************+hashcode "+hashcode);

        emails.setHASHCODE(hashcode+"");


        email.SendMail(To,Subject,HtmlBody);

        if(emailsRepository.findByUserid(emails.getUserid()).isPresent()==true){
            emails.setId(emailsRepository.findByUserid(emails.getUserid()).get().getId());

        }

        emails.setCreated(emails.getUpdated());
        Emails saveEmails =emailService.create( emails );
        System.out.println("************************************************+savemobileobject"+saveEmails);

        return new ResponseEntity<Emails>(emails,HttpStatus.OK);


    }


/****************Verify Emails **************/
    /*************url :-Verift(Post ) - "/api/v1/verifyemails"  **************/

    @PostMapping(value = "/verifyemails",consumes = MediaType.APPLICATION_JSON_VALUE ,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Emails> verifyEmail(@RequestBody Emails emails){

        System.out.println("********************************from frontend otp"+emails.getHASHCODE());
        String fromotp=emails.getHASHCODE();
        System.out.println("*********************************from fromform"+fromotp);

        String Userid=emails.getUserid();
        ResponseEntity <Emails>emailData=getEmailsObjectUsingUesrId(Userid);

        System.out.println("//////////////////////////////////////////////////////");
        System.out.println(date.compareTo(emailData.getBody().getUpdated()));
        emailData.getBody().getUpdated();
        System.out.println("************************"+ emailData.getBody().getUpdated());
        System.out.print(date.getTime()+"      "+date.getDay()+"    "+date.getMonth()+"    "+date.getMinutes());
        String dateandTime=format.format(date);

        Date dateFromDatabase=emailData.getBody().getUpdated();

//        if((dateFromDatabase.getYear()-date.getYear())==0){
//
//            if((dateFromDatabase.getMonth()-date.getMonth())==0){
//
//                if((dateFromDatabase.getDay()-date.getDay())==0){
//                    if((dateFromDatabase.getHours()-date.getHours())<=4){
//                        System.out.print("May Be Right otp");

                        if( emailData.getBody().getHASHCODE().equals(fromotp)){
                            System.out.print("matched");
                            emails.setId(emailData.getBody().getId());
                            emails .setVERIFIED(1);
                            emails.setHASHCODE("");
                            emails.setEmail(To);
                            //updateEmails(emails);
                            String Subject="Pharmerz Thanknote";
                            String Acknowledge="Thank you for verifying on Pharmerz";
                            email.SendMail(To,Subject,Acknowledge);
                            Emails saveEmails =emailService.create( emails );
                            return new ResponseEntity<Emails>(emails,HttpStatus.OK);

                        }else{
                            System.out.print("miss matched");
                            emailService.delete(emailData.getBody().getId());
                            return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
                        }



//                    }else{
//                        System.out.print("Wrong or Old Otp");
//                        emailService.delete(emailData.getBody().getId());
//                        return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
//                    }
//                }else{
//                    System.out.print("Wrong or Old Otp");
//                    emailService.delete(emailData.getBody().getId());
//                    return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
//                }
//
//            }else{
//                System.out.print("Wrong or Old Otp");
//                emailService.delete(emailData.getBody().getId());
//                return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
//            }
//
//        }
//        else{
//            System.out.print("Wrong or Old Otp");
//            emailService.delete(emailData.getBody().getId());
//            return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
//        }

    }

    /******************************MEthod to get implementation of getEmailOTP MEthod */
    public ResponseEntity<Emails> getEmailsObjectUsingUesrId(String userId){
        Emails emails=emailsRepository.findByUserid(userId).get();
        return Optional.ofNullable(emails)
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }


    /*** Get mapping to get the verified Status from Email verification table ***/

    @GetMapping("/emailflag/{id}")
    public ResponseEntity<Emails> getflagId(@PathVariable String id) {

        String idflag = id;
        Emails verificationEmails = emailFlagRepository.findByUserid(id);
        if (verificationEmails != null) {
            return new ResponseEntity<Emails>(verificationEmails, HttpStatus.OK);
        } else
            return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/emailflagverification/{email}")
    public ResponseEntity<Emails> getflagId1(@PathVariable String email){
        System.out.println(email);
        Emails VerificationEmails=emailsRepository.findByEmailStartingWith(email);
        System.out.println(VerificationEmails);
        if(VerificationEmails == null){
            return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
        }else if(VerificationEmails.getVERIFIED() == 0){
            return new ResponseEntity<Emails>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Emails>(VerificationEmails,HttpStatus.OK);
    }


}
