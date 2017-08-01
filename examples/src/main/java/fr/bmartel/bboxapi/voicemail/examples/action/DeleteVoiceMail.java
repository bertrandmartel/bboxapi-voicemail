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
package fr.bmartel.bboxapi.voicemail.examples.action;

import fr.bmartel.bboxapi.voicemail.examples.utils.ExampleUtils;
import fr.bmartel.bboxapi.voicemail.VoiceMailApi;
import fr.bmartel.bboxapi.voicemail.model.HttpStatus;
import fr.bmartel.bboxapi.voicemail.model.VoiceMail;
import fr.bmartel.bboxapi.voicemail.response.ApiResponse;
import fr.bmartel.bboxapi.voicemail.response.VoiceMailResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Delete voicemail example.
 *
 * @author Bertrand Martel
 */
public class DeleteVoiceMail {

    private final static Logger LOGGER = LogManager.getLogger(DeleteVoiceMail.class.getName());

    public static void main(String[] args) {

        VoiceMailApi api = new VoiceMailApi();

        String username = ExampleUtils.getUsername();
        String password = ExampleUtils.getPassword();

        ApiResponse loginResponse = api.login(username, password);

        if (loginResponse.getStatus() != HttpStatus.OK) {
            LOGGER.error("http error  : " + loginResponse.getStatus());
            return;
        }

        VoiceMailResponse voiceMailListResponse = api.getVoiceMailList();

        if (voiceMailListResponse.getStatus() == HttpStatus.OK) {

            List<VoiceMail> voiceMailList = voiceMailListResponse.getVoiceMailList();

            if (voiceMailList.size() > 0) {
                ApiResponse response = api.deleteVoiceMail(voiceMailList.get(0).getId());

                if (response.getStatus() == HttpStatus.OK) {
                    LOGGER.debug("delete voicemail successful");
                } else {
                    LOGGER.error("http error  : " + response.getStatus());
                }
            }
        } else {
            LOGGER.error("http error  : " + voiceMailListResponse.getStatus());
        }
    }
}
