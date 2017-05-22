package com.pharmerz.server.controller.rest;


import com.pharmerz.Appcations.Sms;
import com.pharmerz.server.domain.model.Mobile;
import com.pharmerz.server.domain.repository.MobileFlagRepository;
import com.pharmerz.server.domain.repository.MobileRepository;
import com.pharmerz.service.MobilesService;
import com.pharmerz.service.OtpGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * Created by Amit on 1/25/2017.
 */

@RestController
@RequestMapping("/api/v1")
public class MobileController {

    private String To = null;
    @Autowired
    private MobilesService mobilesService;
    OtpGenerator otp = new OtpGenerator();
    @Autowired
    private MobileRepository mobileRepository;
    @Autowired
    private MobileFlagRepository mobileFlagRepository;
    Sms sms = new Sms();
    Date date = new Date();
    Timestamp timestamp1 = new Timestamp(date.getTime());
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


/***************  Get MObiles ********************/
    /************* url:-(Get):-"/api/v1/mobile"       *****************/

    @RequestMapping(value = "/mobile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Mobile>> getMobiles() {
        Collection<Mobile> mobiles = mobilesService.findall();
        return new ResponseEntity<Collection<Mobile>>(mobiles, HttpStatus.OK);
    }


/***************  Create MObiles ********************/
    /************* url:-(Post):-"/api/v1/mobile"       *****************/


    @PostMapping(value = "/mobile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mobile> createMobile(@RequestBody Mobile mobile) {

        System.out.println("************************************************+mobile" + mobile.getMob());
        int hashcode = otp.RandomOtp();
        System.out.println("************************************************+hashcode" + hashcode);
        mobile.setHASHCODE(hashcode + "");
        this.To = mobile.getMob();
        String Message = hashcode + " is your Pharmerz verification code ";
        sms.sms_generation(To, Message);


        System.out.println("**********************object" + mobileRepository.findByUserid(mobile.getUserid()));
        System.out.println("************************ is present " + mobileRepository.findByUserid(mobile.getUserid()).isPresent());
        System.out.println("**********************equals" + mobileRepository.findByUserid(mobile.getUserid()).equals(Optional.empty()));

        if (mobileRepository.findByUserid(mobile.getUserid()).isPresent() == true) {
            mobile.setId(mobileRepository.findByUserid(mobile.getUserid()).get().getId());
            mobile.setCreated(mobile.getUpdated());
        }

        mobile.setCreated(mobile.getUpdated());
        Mobile saveMobile = mobilesService.create(mobile);


        return new ResponseEntity<Mobile>(saveMobile, HttpStatus.OK);


    }


    /***************  Verify MObiles ********************/
    /************* url:-(Post):-"/api/v1/verifymobile"       *****************/

    @PostMapping(value = "/verifymobile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mobile> verifyMobile(@RequestBody Mobile mobile, HttpServletResponse http) {
        System.out.println("********************************from frontend otp" + mobile.getHASHCODE());
        String fromform = mobile.getHASHCODE();
        System.out.println("*********************************from fromform" + fromform);

        String Userid = mobile.getUserid();
        ResponseEntity<Mobile> mobileData = getMobileObjectUsingUesrId(Userid);
        Date date = new Date();
        System.out.println("//////////////////////////////////////////////////////");
        System.out.println(date.compareTo(mobileData.getBody().getUpdated()));
        mobileData.getBody().getUpdated();
        System.out.println("************************" + mobileData.getBody().getUpdated());
        System.out.print(date.getTime() + "      " + date.getDay() + "    " + date.getMonth() + "    " + date.getMinutes());
        String dateandTime = format.format(date);

        Date dateFromDatabase = mobileData.getBody().getUpdated();

//        if ((dateFromDatabase.getYear() - date.getYear()) == 0) {
//
//            if ((dateFromDatabase.getMonth() - date.getMonth()) == 0) {
//
//                if ((dateFromDatabase.getDay() - date.getDay()) == 0) {
//                    if ((dateFromDatabase.getHours() - date.getHours()) <= 4) {
//                        System.out.print("May Be Right otp");

                        if (mobileData.getBody().getHASHCODE().equals(fromform)) {
                            System.out.print("matched");
                            mobile.setId(mobileData.getBody().getId());
                            mobile.setVERIFIED(1);
                            mobile.setHASHCODE("");
                            mobile.setMob(To);

                            Mobile saveMobile = mobilesService.create(mobile);
                            String Acknowledge = "Thank you for verifying on Pharmerz";
                            sms.sms_generation(To, Acknowledge);
                            return new ResponseEntity<Mobile>(mobile, HttpStatus.OK);

                        } else {
                            System.out.print("miss matched");
                            mobileRepository.delete(mobileData.getBody().getId());
                            return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
                        }


//                    } else {
//                        System.out.print("Wrong or Old Otp 1");
//                        mobileRepository.delete(mobileData.getBody().getId());
//                        return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
//                    }
//                } else {
//                    System.out.print("Wrong or Old Otp 2");
//                    mobileRepository.delete(mobileData.getBody().getId());
//                    return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
//                }
//
//            } else {
//                System.out.print("Wrong or Old Otp 3");
//                mobileRepository.delete(mobileData.getBody().getId());
//                return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
//            }
//
//        } else {
//            System.out.print("Wrong or Old Otp 4");
//            mobileRepository.delete(mobileData.getBody().getId());
//            return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
//        }


    }


    public ResponseEntity<Mobile> getMobileObjectUsingUesrId(String userId) {
        Mobile mobile = mobileRepository.findByUserid(userId).get();
        return Optional.ofNullable(mobile)
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    @GetMapping(value = "api/mobile/userid/{Userid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mobile> getMobileOTP(@PathVariable("Userid") String Userid) {
        Mobile mobile = mobileRepository.findByUserid(Userid).get();


        return Optional.ofNullable(mobile)
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }


    /**********Get mapping to get the verified Status from Mobile verification table ********/
    /************* url:-(Get):-"/api/v2/mobileflag/{id}"       *****************/

    @GetMapping("/mobileflag/{id}")
    public ResponseEntity<Mobile> getflagId(@PathVariable String id) {

        String idflag = id;
        Mobile verificationMobile = mobileFlagRepository.findByUserid(id);
        if (verificationMobile != null) {
            return new ResponseEntity<Mobile>(verificationMobile, HttpStatus.OK);
        } else
            return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
    }


    @GetMapping("/mobileflagverify/{mobile}")
    public ResponseEntity<Mobile> getflagId1(@PathVariable String mobile){
        Mobile VerificationMobile=mobileRepository.findByMob(mobile);
        if(VerificationMobile == null){
            return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
        }else if(VerificationMobile.getVERIFIED() == 0){
            return new ResponseEntity<Mobile>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Mobile>(VerificationMobile,HttpStatus.OK);
    }


}
