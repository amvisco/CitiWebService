package citi;
 
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
 
public class CitiServlet extends SpeechletServlet {
 
  public CitiServlet() {
    this.setSpeechlet(new CitiSpeechlet());
  }
}