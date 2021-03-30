package org.upgrad.upstac.testrequests.consultation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);




    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;


    @Autowired
    TestRequestFlowService  testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;



    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {
    	//Get API to list all the test requests which are ready for the doctor consultation.
    	return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {
    	
    	//The GET API for the test requests on which the doctor has worked.
    	//Only the logged in valid doctor shall be accessing the list of requests assigned to her. 

    	User aUser = userLoggedInService.getLoggedInUser();
    	
    	if (aUser.doesRoleIsDoctor()) {
    		log.info("ConsultationController:getForDoctor - Get the test requests assigned to doctor as the logged in user is the doctor");
    		return testRequestQueryService.findByDoctor(aUser);
    	}else {
    		log.info("ConsultationController:getForDoctor -  The logged in user is not a doctor");
    		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
    	}
    }



    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {
    	
    	//Assign API request for doctor to assign the requests to herself
    	 try {
    		 User aUser = userLoggedInService.getLoggedInUser();
    		 if(aUser.doesRoleIsDoctor()) {
    			 log.info("ConsultationController:assignForConsultation - Assign the test request for consultation.");
    			 return   testRequestUpdateService.assignForConsultation(id, aUser);
    		 } else {
    			 log.info("ConsultationController:assignForConsultation - The logged in user is not a doctor");
    			 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
    		 }
         }catch (AppException e) {
             throw asBadRequest(e.getMessage());
         }
        	
    }



    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest testResult) {

    	//Update the test result with doctor inputs.
        try {
        	User aUser = userLoggedInService.getLoggedInUser();
        	
        	if ( aUser.doesRoleIsDoctor()) {
        		log.info("ConsultationController:updateConsultation - Update the consultation.");
        		return testRequestUpdateService.updateConsultation(id, testResult, aUser);
        	} else {
        		log.info("ConsultationController:updateConsultation - The logged in user is not a doctor");
        		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
        	}
        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }



}
