package com.albertchow.lifecompass.security;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sends the SMS login verification code via Twilio.
 * <p>
 * If Twilio credentials aren't configured (e.g. local development without a
 * Twilio account), the code is logged instead of sent so the SMS login flow
 * can still be exercised end-to-end.
 */
@Slf4j
@Component
public class TwilioSmsSender {

    private final String fromNumber;
    private final boolean configured;

    public TwilioSmsSender(
            @Value("${lifecompass.twilio.account-sid:}") String accountSid,
            @Value("${lifecompass.twilio.auth-token:}") String authToken,
            @Value("${lifecompass.twilio.from-number:}") String fromNumber) {
        this.fromNumber = fromNumber;
        this.configured = !accountSid.isBlank() && !authToken.isBlank() && !fromNumber.isBlank();
        if (configured) {
            Twilio.init(accountSid, authToken);
        }
    }

    public void send(String toPhone, String code) {
        String body = "Your LifeCompass verification code is " + code + ". It expires in 5 minutes.";
        if (!configured) {
            log.warn("[DEV] Twilio not configured - verification code for {}: {}", toPhone, code);
            return;
        }
        Message.creator(new PhoneNumber(toPhone), new PhoneNumber(fromNumber), body).create();
    }
}
