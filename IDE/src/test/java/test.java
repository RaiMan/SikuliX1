import org.sikuli.ide.SikulixIDE;

import javax.swing.*;

import static org.junit.Assert.*;

public class test extends SikulixIDE{

    @org.junit.Test
    public void SikuliValidateHidden() {
        SikulixIDE IDE=  SikulixIDE.get();
        IDE.start();
        IDE.setWindow();
//        IDE.start();
//      validate that sikulixIDE is hidden by default baseline test
        assertEquals(IDE.notHidden(),false);
        IDE.doShow();

        assertEquals(IDE.notHidden(),true);
//        IDE.terminate();

    }
    @org.junit.Test
    public void SikuliValidateGetFileName() {
        SikulixIDE IDE=  SikulixIDE.get();
        IDE.start();
        IDE.setWindow();
        IDE.doShow();
        PaneContext context=IDE.getContext();
        context.getFileName();
        assertEquals(context.getFileName(),"sxtemp1.py");

    }
    @org.junit.Test
    public void SikuliValidateGetExt() {
        SikulixIDE IDE=  SikulixIDE.get();
        IDE.start();
        IDE.setWindow();
        IDE.doShow();
        PaneContext context=IDE.getContext();
        context.getExt();
        assertEquals(context.getExt(),"py");

    }

    @org.junit.Test
    public void SikuliValidateGetFile() {
        SikulixIDE IDE=  SikulixIDE.get();
        IDE.start();
        IDE.setWindow();
        IDE.doShow();

    }
    @org.junit.Test
    public void SikuliValidateSave() {
        SikulixIDE IDE=  SikulixIDE.get();
        IDE.start();
        IDE.setWindow();
        IDE.doShow();

    }
    public void SikuliValidateSaveAs() {
        SikulixIDE IDE=  SikulixIDE.get();
        IDE.start();
        IDE.setWindow();
        IDE.doShow();

    }


}
