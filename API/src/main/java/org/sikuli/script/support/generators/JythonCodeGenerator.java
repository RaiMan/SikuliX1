package org.sikuli.script.support.generators;

import java.io.File;
import java.util.Locale;

import org.sikuli.script.Image;
import org.sikuli.script.Location;
import org.sikuli.script.Pattern;
import org.sikuli.script.support.recorder.actions.IRecordedAction;

public class JythonCodeGenerator implements ICodeGenerator {

  @Override
  public String pattern(Pattern pattern, String mask) {
    String imageFile = pattern.getFilename();
    Image image = pattern.getImage();
    float resizeFactor = pattern.getResize();
    double similarity = pattern.getSimilar();
    Location offset = pattern.getTargetOffset();

    String imgName = new File(imageFile).getName();
    if (image != null) {
      imgName = new File(image.getName()).getName();
    }
    String pat = "Pattern(\"" + imgName + "\")";
    String patternString = "";
    if (resizeFactor > 0 && resizeFactor != 1) {
      patternString += String.format(".resize(%.2f)", resizeFactor).replace(",", ".");
    }
    if (similarity > 0) {
      if (similarity >= 0.99) {
        patternString += ".exact()";
      } else if (similarity != 0.7) {
        patternString += String.format(Locale.ENGLISH, ".similar(%.2f)", similarity);
      }
    }
    if (offset != null && (offset.x != 0 || offset.y != 0)) {
      patternString += ".targetOffset(" + offset.x + "," + offset.y + ")";
    }
    if (null != mask && !mask.isEmpty()) {
      patternString += "." + mask;
    }
    if (!patternString.isEmpty()) {
      patternString = pat + patternString;
    } else {
      patternString = "\"" + imgName + "\"";
    }
    return patternString;
  }
  
  private String click(String type, Pattern pattern, String[] modifiers) {
    String code = type + "(";
    
    if (pattern != null) {
      code += pattern(pattern, null);
    }
    
    if(modifiers.length > 0) {
      if (pattern != null) {
        code += ", ";
      }      
      
      code += String.join(" + ", modifiers);        
    }
    code += ")";
    return code;    
  }

  @Override
  public String click(Pattern pattern, String[] modifiers) {
    return click("click", pattern, modifiers);
  }

  @Override
  public String doubleClick(Pattern pattern, String[] modifiers) {
    return click("doubleClick", pattern, modifiers);
  }
  
  @Override
  public String rightClick(Pattern pattern, String[] modifiers) {
    return click("rightClick", pattern, modifiers);
  }

  @Override
  public String typeText(String text, String[] modifiers) {
    if(modifiers.length > 0) {
      return "type(u\"" + text + "\", " + String.join(" + ", modifiers) + ")";
    }
    return "type(u\"" + text + "\")";
  }

  @Override
  public String typeKey(String key, String[] modifiers) {
    if(modifiers.length > 0) {
      return "type(" + key + ", " + String.join(" + ", modifiers) + ")";
    }
    return "type(" + key + ")";
  }

  @Override
  public String wait(Pattern pattern, int seconds, IRecordedAction matchAction) {
    String code = "wait(" + pattern(pattern, null) + ", " + seconds + ")";
            
    if (matchAction != null) {
      code += "." + matchAction.generate(this);      
    }
    
    return code;
  }   
}
