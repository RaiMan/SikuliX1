import org.sikuli.ide.ButtonCapture;
import org.sikuli.ide.SikulixIDE;
import static org.junit.Assert.*;

public class test extends SikulixIDE{

    @org.junit.Test
    public void SikuliValidate() {
        SikulixIDE IDE=  SikulixIDE.get();
        IDE.start();
        IDE.setWindow();
//        IDE.start();
//      validate that sikulixIDE is hidden by default baseline test
        assertEquals(IDE.notHidden(),false);
        IDE.doShow();

        assertEquals(IDE.notHidden(),true);

    }
}
