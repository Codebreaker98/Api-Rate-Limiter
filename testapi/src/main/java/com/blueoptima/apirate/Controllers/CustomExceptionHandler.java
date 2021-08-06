package com.blueoptima.apirate.Controllers;

import java.util.ArrayList;
import java.util.List;

import com.blueoptima.apirate.Constants;
import com.blueoptima.apirate.Models.ApiResp.CheckLimitResp;
import com.blueoptima.apirate.Models.ApiResp.NewOrgResp;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.blueoptima.apirate.CM;
import com.blueoptima.apirate.Models.ApiError;
import com.blueoptima.apirate.Models.ApiResp.SetEndpointResp;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {


    public CustomExceptionHandler() {
        super();
    }


    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        errors.add(ex.getParameterName() + " : " + ex.getMessage());
        CheckLimitResp endpointResp = CM.getLimitResp(null, Constants.Status.NOT_FOUND, false, ex.getMessage());
        return super.handleExceptionInternal(ex, endpointResp, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatus status, WebRequest request) {
        //System.out.println("path="+request.getContextPath());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, status.value(), ex.getLocalizedMessage(), "Method not supported");
        return super.handleExceptionInternal(ex, apiError, headers, status, request);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        String[] path = request.getDescription(false).split("=");
        String[] msgs = ex.getMessage().split(":");
        String s = path[1];
        if (s.equals("/api/addOrUpdateEndpoint")) {
            SetEndpointResp resp = CM.getApiResp(null, msgs[0], false);
            return super.handleExceptionInternal(ex, resp, headers, status, request);
        }else if(s.equals("/api/addOrg")){
			NewOrgResp orgResp=CM.getOrgResp(null, msgs[0], false);
			return super.handleExceptionInternal(ex, orgResp, headers, status, request);
		} else if(s.equals("/api/deleteEndpoint")){
			SetEndpointResp resp=CM.getApiResp(null, msgs[0], false);
			return super.handleExceptionInternal(ex, resp, headers, status, request);
		}
        else{
			ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, status.value(), ex.getLocalizedMessage(), ex.getMessage());
        	return super.handleExceptionInternal(ex, apiError, headers, status, request);
		}
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, status.value(), ex.getLocalizedMessage(), ex.getMessage());
        return super.handleExceptionInternal(ex, apiError, headers, status, request);
    }


}
