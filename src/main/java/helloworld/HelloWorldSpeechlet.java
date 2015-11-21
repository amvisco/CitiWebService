/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package helloworld;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

/**
 * This sample shows how to create a simple speechlet for handling intent requests and managing
 * session interactions.
 */
public class HelloWorldSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldSpeechlet.class);

    private static final String ACCOUNT_KEY = "ACCOUNT";
    private static final String ACCOUNT_SLOT = "Account";
    private static final String URL_PREFIX = "https://citiapi.bluemix.net/";

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        //if  ("MyBalanceIntent".equals(intentName)) {
            //return getBalanceResponse(intent, session);
        if ("MyAccountIntent".equals(intentName)) {
            return setAccountResponse(intent, session);
        }else if ("HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText =
                "Welcome to the Citi. To check your balance, please tell me the account you are interested in by "
                        + "saying, what's my savings account balance?";
        String repromptText =
                "Please tell me the account you are interested by saying, what's my savings account balance?";

        return getSpeechletResponse(speechText, repromptText, true);
    }
    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted color in the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse setAccountResponse(final Intent intent, final Session session) {
        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the account slot from the list of slots.
        Slot listOfAccountsSlot = slots.get(ACCOUNT_SLOT);
        String speechText, repromptText;

        // Check for account and create output to user.
        if (listOfAccountsSlot != null) {
            // Store the user's account in the Session and create response.
            String theAccount = listOfAccountsSlot.getValue();
            session.setAttribute(ACCOUNT_KEY, theAccount);
            speechText =
                    String.format("Your %s account balance is $65,345.45. You can ask me for your "
                            + "balance by saying, what's my %s account balalnce", theAccount, theAccount);
            repromptText =
                    String.format("You can ask me your balace by saying, what's my %s account balance?", theAccount);

        } else {
            // Render an error since we don't know what the users desired account is.
            speechText = "I'm not sure which account you are talking about, please try again";
            repromptText =
                    "I'm not sure which account you are talking about. You can tell me the account you want information about by "
                            + "saying something like, what's my savings account balance?";
        }
        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's favorite color from the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
    @SuppressWarnings("unused") //maybe we don't need this class
	private SpeechletResponse getBalanceResponse(final Intent intent, final Session session) {
        String speechText;
        boolean isAskResponse = false;

        // Get the user's requested account from the session.
        String theAccount = (String) session.getAttribute(ACCOUNT_KEY);

        // Check to make sure user's requested account is in the session
        if (StringUtils.isNotEmpty(theAccount)) {
            speechText = String.format("Your %s account balance is $65,345.45. Thank you for using Citi, Goodbye.", theAccount);
        } else {
            // Since the user's account is not set render an error message.
            speechText =
                    "I'm not sure what account you are talking about. You can say, give me the balance for my savings, checking or credit card account.";
            isAskResponse = true;
        }

        return getSpeechletResponse(speechText, speechText, isAskResponse);
    }


    /**
     * Returns a response for the help intent.
     */
    private SpeechletResponse getHelpResponse() {
        String speechText =
                "You can ask me for one of your account balances by saying, "
                        + "what is my savings, checking or credit card account balance?";
        String repromptText =
                    "You can tell me the account you want information about by "
                            + "saying, what's my savings account balance?";

          return getSpeechletResponse(speechText, repromptText, true);
    }
    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
            boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Snapshot");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    /**
     * Download JSON-formatted balance from Citi, for a defined account, and return a
     * String array of the balance, with each balance representing an element in the array.
     * 
     * @param account
     *            the account to get balance for, example: Savings
     * @return String array of balance for that account, 1 balance per element of the array
     */
    @SuppressWarnings("unused")//delete after code is complete
	private ArrayList<String> getJsonBalanceFromCiti(String account) {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text = "";
        try {
            String line;
            URL url = new URL(URL_PREFIX + account);
            inputStream = new InputStreamReader(url.openStream(), Charset.forName("US-ASCII"));
            bufferedReader = new BufferedReader(inputStream);
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            text = builder.toString();
        } catch (IOException e) {
            // reset text variable to a blank string
            text = "";
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }
        return parseJson(text);
    }

    /**
     * Parse JSON-formatted list of balance from Citi, extract list of balances and
     * split the events into a String array of individual balances. Run Regex matchers to make the
     * list pretty by adding a comma after the year to add a pause, and by removing a unicode char.
     * 
     * @param text
     *            the JSON formatted list of balance for a certain account
     * @return String array of events for that date, 1 event per element of the array
     */
    private ArrayList<String> parseJson(String text) {
        text =
                text.substring(text.indexOf("\\Balance\\n"));
        ArrayList<String> balance = new ArrayList<String>();
        if (text.isEmpty()) {
            return balance;
        }
        int startIndex = 0, endIndex = 0;
        while (endIndex != -1) {
            endIndex = text.indexOf("\\n", startIndex);
            String eventText =
                    (endIndex == -1 ? text.substring(startIndex) : text.substring(startIndex,
                            endIndex));
            // replace dashes returned in text from Citi's API
            //Pattern pattern = Pattern.compile("\\\\u2013\\s*");
            //Matcher matcher = pattern.matcher(eventText);
            //eventText = matcher.replaceAll("");
            // add comma after year so Alexa pauses before continuing with the sentence
           // pattern = Pattern.compile("(^\\d+)");
            //matcher = pattern.matcher(eventText);
            //if (matcher.find()) {
                //eventText = matcher.replaceFirst(matcher.group(1) + ",");
            //}
            eventText = "In " + eventText;
            startIndex = endIndex + 2;
            balance.add(eventText);
        }
        Collections.reverse(balance);
        return balance;
    }
    
        /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    @SuppressWarnings("unused") //delete after code fix.
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);
        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }
}
