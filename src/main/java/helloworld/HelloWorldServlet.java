package helloworld;
 
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
 
public class HelloWorldServlet extends SpeechletServlet {
 
  public HelloWorldServlet() {
    this.setSpeechlet(new HelloWorldSpeechlet());
  }
}