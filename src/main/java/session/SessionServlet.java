package session;
 
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
 
public class SessionServlet extends SpeechletServlet {
 
  public SessionServlet() {
    this.setSpeechlet(new SessionSpeechlet());
  }
}
