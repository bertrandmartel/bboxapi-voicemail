/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2017 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.bboxapi.voicemail;

import fr.bmartel.bboxapi.voicemail.model.*;
import fr.bmartel.bboxapi.voicemail.response.ApiResponse;
import fr.bmartel.bboxapi.voicemail.response.VoiceMailResponse;
import fr.bmartel.bboxapi.voicemail.utils.ApiUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Bytel Voicemail Api client
 *
 * @author Bertrand Martel
 */
public class VoiceMailApi {

    // hosts
    public final static String LOGIN_HOST = "www.mon-compte.bouyguestelecom.fr";
    public final static String VOICEMAIL_HOST = "www.messagerievocale.bbox.bouyguestelecom.fr";

    //login/session URL
    public final static String LOGIN_URI = "https://" + LOGIN_HOST + "/cas/login";
    public final static String SERVICE_URI = "https://www.aaa.bbox.bouyguestelecom.fr/servicesAAA/async/auth" +
            ".phtml?CI_servID=34";
    public final static String SERVICE_CALLBACK = "https://" + VOICEMAIL_HOST + "/flashvox.checkAccesAAA.phtml";
    public final static String SERVICE_CALLBACK_ERROR = "http://" + VOICEMAIL_HOST + "/index.phtml?pg=99";

    //API URL
    public final static String VOICEMAIL_SERVICE_URI = "http://" + VOICEMAIL_HOST + "?pg=6";
    public final static String WELCOME_MESSAGE_URI = "http:/" + VOICEMAIL_HOST + "?pg=27";
    public final static String WELCOME_MESSAGE_FILE_URI = "http://" + VOICEMAIL_HOST + "flashvox.playInviteMsg.phtml";
    public final static String DUAL_CALL_URI = "http://" + VOICEMAIL_HOST + "/index.phtml?pg=23";
    public final static String INCOGNITO_URI = "http://" + VOICEMAIL_HOST + "/index.phtml?pg=24";
    public final static String VOICEMAIL_NOTIFICATION_URI = "http://" + VOICEMAIL_HOST + "/index.phtml?pg=30";
    public final static String CALL_FORWARDING_URI = "http://" + VOICEMAIL_HOST + "?pg=25";
    public final static String VOICEMAIL_URI = "http://" + VOICEMAIL_HOST + "?pg=0";
    public final static String VOICEMAIL_FILE_URI = "http://" + VOICEMAIL_HOST + "/flashvox.dl.phtml";

    /**
     * Cookie store used to store cookies.
     */
    private CookieStore mCookieStore = new BasicCookieStore();

    /**
     * Http client.
     */
    private CloseableHttpClient mHttpClient = HttpClients.custom()
            .setRedirectStrategy(new LaxRedirectStrategy())
            .setDefaultCookieStore(mCookieStore)
            .build();

    /**
     * User info initialized on login.
     */
    private UserInfo mUserInfo;

    /**
     * Execute http request.
     *
     * @param request
     * @return
     */
    private HttpResponse executeRequest(HttpRequestBase request) {

        CloseableHttpResponse response;
        try {
            response = mHttpClient.execute(request);
            try {
                return response;
            } finally {
                response.close();
            }
        } catch (IOException e) {
            //ignored
        }
        return null;
    }

    /**
     * Store cookies in cookiestore
     *
     * @param cookies
     * @param domain
     */
    private void storeCookies(Map<String, String> cookies, String domain) {
        mCookieStore.clear();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
            cookie.setDomain(domain);
            mCookieStore.addCookie(cookie);
        }
    }

    /**
     * Login to website.
     *
     * @param username
     * @param password
     */
    public ApiResponse login(String username, String password) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        try {
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));

            Connection.Response resRawPage = Jsoup.connect(ApiUtils.buildRawLoginUri()).execute();

            Document doc = resRawPage.parse();

            Elements lt = doc.select("#log_cta input[name=lt]");
            Elements execution = doc.select("#log_cta input[name=execution]");

            if (lt.size() > 0) {
                params.add(new BasicNameValuePair("lt", lt.get(0).attr("value")));
            }
            if (execution.size() > 0) {
                params.add(new BasicNameValuePair("execution", execution.get(0).attr("value")));
            }
            params.add(new BasicNameValuePair("rememberMe", "true"));
            params.add(new BasicNameValuePair("_rememberMe", "on"));
            params.add(new BasicNameValuePair("_eventId", "submit"));

            HttpPost loginRequest = new HttpPost(LOGIN_URI);

            loginRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            storeCookies(resRawPage.cookies(), LOGIN_HOST);

            HttpResponse response = executeRequest(loginRequest);

            boolean hasSession = ApiUtils.hasSessionCookie(mCookieStore);

            if (hasSession && response.getStatusLine().getStatusCode() == 200) {
                mUserInfo = getUserInfo();
            } else if (!hasSession) {
                return new ApiResponse(HttpStatus.UNAUTHORIZED);
            }
            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    /***
     * lood cookies into map for further usage with JSoup
     *
     * @return
     */
    private Map<String, String> loadCookies() {
        Map<String, String> cookies = new HashMap<String, String>();
        for (Cookie cookie : mCookieStore.getCookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }

    public UserInfo getCustomerInfo() {
        return mUserInfo;
    }

    private UserInfo getUserInfo() {

        UserInfo userInfo = new UserInfo();

        try {
            Connection.Response customerInfo = Jsoup.connect(VOICEMAIL_SERVICE_URI).cookies(loadCookies()).execute();

            Document doc = customerInfo.parse();

            Elements pseudo = doc.select("input[name=pseudo]");
            Elements phoneNumber = doc.select("input[name=voip_num]");
            Elements login = doc.select("input[name=login]");
            Elements email = doc.select("input[name=email]");
            Elements uid = doc.select("input[name=uid]");

            userInfo.setPseudo((pseudo.size() > 0) ? pseudo.get(0).attr("value") : "");
            userInfo.setPhoneNumber((phoneNumber.size() > 0) ? phoneNumber.get(0).attr("value") : "");
            userInfo.setLogin((login.size() > 0) ? login.get(0).attr("value") : "");
            userInfo.setEmail((email.size() > 0) ? email.get(0).attr("value") : "");
            userInfo.setUid((uid.size() > 0) ? uid.get(0).attr("value") : "");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    public ApiResponse setVoiceMailState(VoiceMailState state, int ringsNumber) {

        try {
            HttpPost stateRequest = new HttpPost(VOICEMAIL_URI);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("command", "modifier_mevo"));
            params.add(new BasicNameValuePair("pseudo", mUserInfo.getPseudo()));
            params.add(new BasicNameValuePair("voip_num", mUserInfo.getPhoneNumber()));
            params.add(new BasicNameValuePair("ref_Sip", ""));
            params.add(new BasicNameValuePair("login", mUserInfo.getLogin()));
            params.add(new BasicNameValuePair("email", mUserInfo.getEmail()));
            params.add(new BasicNameValuePair("uid", mUserInfo.getUid()));
            params.add(new BasicNameValuePair("rang_tel", "1"));
            params.add(new BasicNameValuePair("mevo", state.getValue()));
            params.add(new BasicNameValuePair("nb_sonnerie_select", String.valueOf(ringsNumber)));

            stateRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = executeRequest(stateRequest);

            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (UnsupportedEncodingException e) {
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public ApiResponse setWelcomeMessageState(WelcomeMessageState state) {

        try {
            HttpPost stateRequest = new HttpPost(WELCOME_MESSAGE_URI);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("inviteMsg", state.getValue()));

            stateRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = executeRequest(stateRequest);

            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (UnsupportedEncodingException e) {
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public ApiResponse getWelcomeMessage(int id, String fileDest) {

        try {
            HttpGet stateRequest = new HttpGet(WELCOME_MESSAGE_FILE_URI +
                    "?uid=" + mUserInfo.getUid() +
                    "&id_message=" + id);

            HttpResponse response = mHttpClient.execute(stateRequest);

            if (response.getStatusLine().getStatusCode() == 200) {
                ApiUtils.writeToFile(response, fileDest);
            }
            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public ApiResponse uploadWelcomeMessage(String filePath, int messageId, int selectedMessageId) {

        File file = new File(filePath);
        HttpPost uploadRequest = new HttpPost(WELCOME_MESSAGE_URI);

        StringBody commandeBody = new StringBody("annonce_mess", ContentType.MULTIPART_FORM_DATA);
        StringBody messageIdBody = new StringBody(String.valueOf(messageId), ContentType.MULTIPART_FORM_DATA);
        StringBody maxFileSizeBody = new StringBody("5242880", ContentType.MULTIPART_FORM_DATA);
        StringBody selectMessageBody = new StringBody(String.valueOf(selectedMessageId),
                ContentType.MULTIPART_FORM_DATA);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        builder.addBinaryBody("FILE", file, ContentType.create("audio/mp3"), "message.mp3");
        builder.addPart("commande", commandeBody);
        builder.addPart("id_message", messageIdBody);
        builder.addPart("id_message_select", selectMessageBody);
        builder.addPart("MAX_FILE_SIZE", maxFileSizeBody);
        HttpEntity entity = builder.build();

        uploadRequest.setEntity(entity);

        HttpResponse response = executeRequest(uploadRequest);

        return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));
    }

    public ApiResponse setDualCallState(DualCallState state) {

        try {
            HttpPost stateRequest = new HttpPost(DUAL_CALL_URI);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("doubleCall", state.getValue()));
            params.add(new BasicNameValuePair("Valider", "VALIDER"));

            stateRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = executeRequest(stateRequest);

            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (UnsupportedEncodingException e) {
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public ApiResponse setIncognitoMode(IncognitoState state) {

        try {
            HttpPost stateRequest = new HttpPost(INCOGNITO_URI);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("outGoingCallIdentity", state.getValue()));
            params.add(new BasicNameValuePair("Valider", "VALIDER"));

            stateRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = executeRequest(stateRequest);

            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (UnsupportedEncodingException e) {
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public ApiResponse setVoiceMailSMS(NotificationState voicemail, NotificationState missedCall, String phoneNumber) {

        try {
            HttpPost stateRequest = new HttpPost(VOICEMAIL_NOTIFICATION_URI);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("command", "Activer_Mode_SMS"));
            params.add(new BasicNameValuePair("uid", mUserInfo.getUid()));
            params.add(new BasicNameValuePair("buttonSms", voicemail.getValue()));
            params.add(new BasicNameValuePair("buttonSmsAbs", missedCall.getValue()));
            params.add(new BasicNameValuePair("numSms", phoneNumber));
            params.add(new BasicNameValuePair("Valider", "VALIDER"));

            stateRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = executeRequest(stateRequest);

            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (UnsupportedEncodingException e) {
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public ApiResponse setCallForwarding(CallForwardType type, String phoneNumber) {

        try {

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("command", "modifier_transfert"));
            params.add(new BasicNameValuePair("uid", mUserInfo.getUid()));
            params.add(new BasicNameValuePair("uid_mevo", mUserInfo.getUid()));
            params.add(new BasicNameValuePair("voip_num", mUserInfo.getPhoneNumber()));
            params.add(new BasicNameValuePair("sip", "0"));
            params.add(new BasicNameValuePair("rang_tel", "1"));

            Map<String, String> parameters = new HashMap<String, String>();

            parameters.put("option", "Desactiver");
            parameters.put("opt_0_tel", "");
            parameters.put("opt_2_tel", "");
            parameters.put("opt_3_tel", "");
            parameters.put("opt_4_tel", "");

            Connection.Response resRawPage = Jsoup.connect(CALL_FORWARDING_URI).cookies(loadCookies()).execute();

            Document doc = resRawPage.parse();

            Elements idbox = doc.select("#form_transfert input[name=idbox]");

            Elements opt0tel = doc.select("#opt_0_tel");
            Elements opt2tel = doc.select("#opt_2_tel");
            Elements opt3tel = doc.select("#opt_3_tel");
            Elements opt4tel = doc.select("#opt_4_tel");

            Elements opt0 = doc.select("#opt_0");
            Elements opt2 = doc.select("#opt_2");
            Elements opt3 = doc.select("#opt_3");
            Elements opt4 = doc.select("#opt_4");
            Elements opt5 = doc.select("#opt_5");

            if (idbox.size() > 0) {
                params.add(new BasicNameValuePair("idbox", idbox.get(0).attr("value")));
            }
            if (opt0.size() > 0 && ApiUtils.matchOption(opt0)) {
                parameters.put("opt_0", "1");
                parameters.put("opt_0_tel", opt0tel.get(0).attr("value"));
            }
            if (opt2.size() > 0 && ApiUtils.matchOption(opt2)) {
                parameters.put("opt_1", "6");
                parameters.put("opt_2", "3");
                parameters.put("opt_2_tel", opt2tel.get(0).attr("value"));
            }
            if (opt3.size() > 0 && ApiUtils.matchOption(opt3)) {
                parameters.put("opt_1", "6");
                parameters.put("opt_3", "2");
                parameters.put("opt_3_tel", opt3tel.get(0).attr("value"));
            }
            if (opt4.size() > 0 && ApiUtils.matchOption(opt4)) {
                parameters.put("opt_4", "4");
                parameters.put("opt_4_tel", opt4tel.get(0).attr("value"));
            }
            if (opt5.size() > 0 && ApiUtils.matchOption(opt5)) {
                parameters.put("opt_5", "1");
            }

            HttpPost stateRequest = new HttpPost(CALL_FORWARDING_URI);

            switch (type) {
                case DISABLE:
                    parameters.put("option", "Desactiver");
                    parameters.put("opt_0_tel", "");
                    parameters.put("opt_2_tel", "");
                    parameters.put("opt_3_tel", "");
                    parameters.put("opt_4_tel", "");
                    parameters.put("opt_0", "");
                    parameters.put("opt_1", "");
                    parameters.put("opt_2", "");
                    parameters.put("opt_3", "");
                    parameters.put("opt_4", "");
                    parameters.put("opt_5", "");
                    break;
                case ALL_TO_VOICEMAIL:
                    parameters.put("option", "Activer");
                    parameters.put("opt_5", "1");
                    break;
                case ALL_TO_NUMBER:
                    parameters.put("option", "Activer");
                    parameters.put("opt_0", "1");
                    parameters.put("opt_0_tel", phoneNumber);
                    break;
                case UNANSWERED_CALL_TO_NUMBER:
                    parameters.put("option", "Activer");
                    parameters.put("opt_1", "6");
                    parameters.put("opt_2", "3");
                    parameters.put("opt_2_tel", phoneNumber);
                    break;
                case LINE_OCCUPIED_TO_NUMBER:
                    parameters.put("option", "Activer");
                    parameters.put("opt_1", "6");
                    parameters.put("opt_3", "2");
                    parameters.put("opt_3_tel", phoneNumber);
                    break;
                case LINE_UNAVAILABLE_TO_NUMBER:
                    parameters.put("option", "Activer");
                    parameters.put("opt_1", "6");
                    parameters.put("opt_4", "4");
                    parameters.put("opt_4_tel", phoneNumber);
                    break;
            }

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            stateRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = executeRequest(stateRequest);

            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public VoiceMailResponse getVoiceMailList() {

        HashMap<String, VoiceMail> voiceMailMap = new HashMap<String, VoiceMail>();

        try {
            Connection.Response resRawPage = Jsoup.connect(VOICEMAIL_URI).cookies(loadCookies()).execute();

            if (resRawPage.statusCode() == 200) {
                Document doc = resRawPage.parse();
                Elements nodes = doc.select(".trOut a");

                VoiceMailParseState state = VoiceMailParseState.PHONE_NUMBER;

                for (Element element : nodes) {
                    String rawText = element.text().trim();

                    String id = ApiUtils.getVoiceMailId(element);

                    if (id != null && !id.equals("")) {
                        if (!voiceMailMap.containsKey(id)) {
                            voiceMailMap.put(id, new VoiceMail(id));
                        }

                        VoiceMail voiceMail = voiceMailMap.get(id);

                        if (!rawText.equals("")) {
                            switch (state) {
                                case PHONE_NUMBER:
                                    voiceMail.setPhoneNumber(element.text());
                                    state = VoiceMailParseState.DATE;
                                    break;
                                case DATE:
                                    voiceMail.setDate(element.text());
                                    state = VoiceMailParseState.DURATION;
                                    break;
                                case DURATION:
                                    voiceMail.setDuration(element.text());
                                    state = VoiceMailParseState.PHONE_NUMBER;
                                    break;
                            }
                        }
                    }
                }

                List<VoiceMail> voiceMailList = new ArrayList<VoiceMail>(voiceMailMap.values());
                voiceMailList.sort(new Comparator<VoiceMail>() {
                    @Override
                    public int compare(VoiceMail o1, VoiceMail o2) {
                        return o2.getId().compareTo(o1.getId());
                    }
                });
                return new VoiceMailResponse(HttpStatus.gethttpStatus(resRawPage.statusCode()), voiceMailList);
            }
            return new VoiceMailResponse(HttpStatus.gethttpStatus(resRawPage.statusCode()), new ArrayList<VoiceMail>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new VoiceMailResponse(HttpStatus.UNKNOWN, new ArrayList<VoiceMail>());
    }

    public ApiResponse downloadVoiceMail(String voiceMailId, String fileDest) {

        try {
            URIBuilder fileUri = new URIBuilder(VOICEMAIL_FILE_URI);
            fileUri.addParameter("idmsg", voiceMailId);
            fileUri.addParameter("rang_tel", "1");

            HttpGet stateRequest = new HttpGet(fileUri.build().toString());

            HttpResponse response = mHttpClient.execute(stateRequest);

            if (response.getStatusLine().getStatusCode() == 200) {
                ApiUtils.writeToFile(response, fileDest);
            }
            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }

    public ApiResponse deleteVoiceMail(String voiceMailId) {

        try {
            HttpPost stateRequest = new HttpPost(VOICEMAIL_URI);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("voicelist", "Supprimer"));
            params.add(new BasicNameValuePair("acl", "Ajouter"));
            params.add(new BasicNameValuePair("id[" + voiceMailId + "]", voiceMailId));

            stateRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = executeRequest(stateRequest);

            return new ApiResponse(HttpStatus.gethttpStatus(response.getStatusLine().getStatusCode()));

        } catch (UnsupportedEncodingException e) {
        }
        return new ApiResponse(HttpStatus.UNKNOWN);
    }
}