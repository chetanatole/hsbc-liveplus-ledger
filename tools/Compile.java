import javax.tools.*;
import java.io.*;
import java.util.*;

/** Own the file manager for a one-shot compiler process. Windows' restricted
 * token can deny ZipFS.toRealPath during close even after successful javac.
 * Let process teardown release the archives; compiler errors still fail build.
 */
public final class Compile {
 public static void main(String[] args){
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
  StandardJavaFileManager manager=compiler.getStandardFileManager(null,null,null);
  List<String> options=new ArrayList<>();List<File> sources=new ArrayList<>();
  for(String arg:args){if(arg.endsWith(".java"))sources.add(new File(arg));else options.add(arg);}
  boolean ok=compiler.getTask(null,manager,null,options,null,manager.getJavaFileObjectsFromFiles(sources)).call();
  System.exit(ok?0:1);
 }
}
