package com.buglabs.osgi.concierge.templates;

import com.buglabs.osgi.concierge.ui.info.ProjectInfo;

 public class GeneratorActivator
 {
  protected static String nl;
  public static synchronized GeneratorActivator create(String lineSeparator)
  {
    nl = lineSeparator;
    GeneratorActivator result = new GeneratorActivator();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "package ";
  protected final String TEXT_2 = ";";
  protected final String TEXT_3 = NL + NL + "import org.osgi.framework.BundleActivator;" + NL + "import org.osgi.framework.BundleContext;" + NL + "" + NL + "public class ";
  protected final String TEXT_4 = " implements BundleActivator {" + NL + "" + NL + "\tpublic void start(BundleContext context) throws Exception {" + NL + "\t\t// TODO Auto-generated method stub" + NL + "\t\t" + NL + "\t}" + NL + "" + NL + "\tpublic void stop(BundleContext context) throws Exception {" + NL + "\t\t// TODO Auto-generated method stub" + NL + "\t\t" + NL + "\t}" + NL + "}";

   public String generate(ProjectInfo projInfo)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    //Author: Angel Roman - roman@mdesystems.com
    
if(projInfo.getActivatorPackage().length() > 0) {

    stringBuffer.append(TEXT_1);
    stringBuffer.append(projInfo.getActivatorPackage());
    stringBuffer.append(TEXT_2);
    
}

    stringBuffer.append(TEXT_3);
    stringBuffer.append(projInfo.getActivatorName());
    stringBuffer.append(TEXT_4);
    return stringBuffer.toString();
  }
}
