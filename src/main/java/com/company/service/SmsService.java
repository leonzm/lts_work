package com.company.service;

import java.io.UnsupportedEncodingException;

/**
 * Created by Leon on 2016/12/27.
 */
public interface SmsService {

    boolean sendOneSmsToOnePhone(String message, String phone) throws UnsupportedEncodingException;

    boolean sendOneSmsToMorePhone(String message, String[] phones) throws UnsupportedEncodingException;

    boolean sendMoreSmsToOnePhone(String[] messages, String phone) throws UnsupportedEncodingException;

    boolean sendMoreSmsToMorePhone(String[] messages, String[] phones) throws UnsupportedEncodingException;

}
