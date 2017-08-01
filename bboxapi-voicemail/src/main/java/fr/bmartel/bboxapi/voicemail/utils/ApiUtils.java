/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017 Bertrand Martel
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.bboxapi.voicemail.utils;

import fr.bmartel.bboxapi.voicemail.VoiceMailApi;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Api utility functions.
 *
 * @author Bertrand Martel
 */
public class ApiUtils {

    /**
     * Build the raw login URI used to retrieve dynamic parameters.
     *
     * @return full login uri with correct redirection params
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    public static String buildRawLoginUri() throws URISyntaxException, UnsupportedEncodingException {
        URIBuilder serviceUri = new URIBuilder(VoiceMailApi.LOGIN_URI);
        serviceUri.addParameter("service", VoiceMailApi.SERVICE_URI + "&callback=" + VoiceMailApi.SERVICE_CALLBACK +
                "&callback_error=" + VoiceMailApi.SERVICE_CALLBACK_ERROR);
        return serviceUri.build().toString();
    }

    /**
     * Write response to output file.
     *
     * @param response
     * @param fileDest
     * @return true if OK
     */
    public static boolean writeToFile(HttpResponse response, String fileDest) {
        InputStream input = null;
        OutputStream output = null;
        byte[] buffer = new byte[1024];

        try {
            input = response.getEntity().getContent();
            output = new FileOutputStream(fileDest);
            for (int length; (length = input.read(buffer)) > 0; ) {
                output.write(buffer, 0, length);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) try {
                output.close();
            } catch (IOException logOrIgnore) {
            }
            if (input != null) try {
                input.close();
            } catch (IOException logOrIgnore) {
            }
        }
        return false;
    }

    /**
     * Find if input is checked
     *
     * @param option
     * @return true if option is checked.
     */
    public static boolean matchOption(Elements option) {
        Pattern pattern = Pattern.compile("checked");
        Matcher matcher = pattern.matcher(option.get(0).toString());
        return matcher.find();
    }

    /**
     * Get voicemail id in href attribute.
     *
     * @param element
     * @return voicemail id string
     */
    public static String getVoiceMailId(Element element) {
        Pattern pattern = Pattern.compile("idmsg=([0-9]+)");
        Matcher matcher = pattern.matcher(element.attr("href"));
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Check if PHPSESSID cookie is set.
     *
     * @param cookieStore
     * @return true if session cookie is set.
     */
    public static boolean hasSessionCookie(CookieStore cookieStore) {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equalsIgnoreCase("PHPSESSID")) {
                return true;
            }
        }
        return false;
    }
}
