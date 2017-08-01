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
package fr.bmartel.bboxapi.voicemail.examples.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utilities for example module.
 *
 * @author Bertrand Martel
 */
public class ExampleUtils {

    /**
     * logger.
     */
    private final static Logger LOGGER = LogManager.getLogger(ExampleUtils.class.getName());

    public static String getUsername() {
        LOGGER.debug("enter username : ");
        return userRequest();
    }

    public static String getPassword() {
        LOGGER.debug("enter password : ");
        return userRequest();
    }

    private static String userRequest() {
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        try {
            return bufferRead.readLine();
        } catch (IOException e) {
        }
        return "";
    }
}
