package net.shyshkin.study.grpc.protobuf.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OneOfDemoTest {

    public static final EmailCredentials EMPTY_EMAIL_CREDENTIALS = EmailCredentials.newBuilder().build();
    public static final PhoneOTP EMPTY_PHONE_OTP = PhoneOTP.newBuilder().build();
    private EmailCredentials emailCredentials;
    private PhoneOTP phoneOTP;

    @BeforeEach
    void setUp() {

        emailCredentials = EmailCredentials.newBuilder()
                .setEmail("d.art.shishkin@gmail.com")
                .setPassword("foo")
                .build();

        phoneOTP = PhoneOTP.newBuilder()
                .setNumber(321321321)
                .setCode(123)
                .build();
    }

    @Test
    void otpIsLast_overridesEmail() {
        //when
        Credentials credentials = Credentials.newBuilder()
                .setEmailMode(emailCredentials)
                .setPhoneMode(phoneOTP)
                .build();

        //then
        assertAll(
                () -> assertEquals(phoneOTP, credentials.getPhoneMode()),
                () -> assertEquals(EMPTY_EMAIL_CREDENTIALS, credentials.getEmailMode()),
                () -> assertEquals(phoneOTP, loginStupid(credentials)),
                () -> assertEquals(phoneOTP, login(credentials))
        );
    }

    @Test
    void emailIsLast_overridesOTP() {
        //when
        Credentials credentials = Credentials.newBuilder()
                .setPhoneMode(phoneOTP)
                .setEmailMode(emailCredentials)
                .build();

        //then
        assertAll(
                () -> assertEquals(EMPTY_PHONE_OTP, credentials.getPhoneMode()),
                () -> assertEquals(emailCredentials, credentials.getEmailMode()),
                () -> assertEquals(emailCredentials, loginStupid(credentials)),
                () -> assertEquals(emailCredentials, login(credentials))
        );
    }

    private Object loginStupid(Credentials credentials) {
        return credentials.hasEmailMode() ? credentials.getEmailMode() :
                credentials.hasPhoneMode() ? credentials.getPhoneMode() : null;
    }

    private Object login(Credentials credentials) {
        switch (credentials.getModeCase()) {
            case PHONE_MODE:
                return credentials.getPhoneMode();
            case EMAIL_MODE:
                return credentials.getEmailMode();
        }
        throw new RuntimeException("Bad Credentials");
    }
}