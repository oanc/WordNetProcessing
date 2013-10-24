@GrabResolver(name='anc-releases', root='http://www.anc.org:8080/nexus/content/repositories/releases')


@Grab('org.anc.wn:SentenceList:1.2.2-SNAPSHOT')
import org.anc.wn.*

@Grab('org.anc.osgi:wordnet:2.0.0-SNAPSHOT')
import org.anc.wordnet.api.*

@Grab('org.anc:common:3.0.0')
import org.anc.util.*

@Grab('ch.qos.logback:logback-classic:1.0.0')
import org.slf4j.*

@GrabResolver(name='anc-snapshots', root='http://www.anc.org:8080/nexus/content/repositories/snapshots')
//@Grab('org.tc37sc4.graf:graf-util:1.2.3-SNAPSHOT')
@Grab('org.tc37sc4.graf:graf-util:1.2.2')
import org.xces.graf.util.GraphUtils

@Grab('org.tc37sc4.graf:graf-io:1.2.2')
import org.xces.graf.io.IRegionFilter
import org.xces.graf.io.GrafRenderer
import org.xces.graf.io.GrafParser
import org.xces.graf.io.dom.ResourceHeader


@Grab('org.tc37sc4.graf:graf-impl:1.2.2')
import org.xces.graf.impl.Factory
import org.xces.graf.impl.CharacterAnchor

@Grab('org.tc37sc4.graf:graf-api:1.2.2')
import org.xces.graf.api.*



class VerifyWordOffsets {

     File root;
     File outDir;
     
    
     
   
   void run()
   {
   
      int length
      Integer wordStart
      Integer wordEnd
      println "root from run is ${root}"
      File resourceHeaderFile = new File("/Users/frankcascio/anc/sense-tagging/resource-header.xml")
      ResourceHeader resourceHeader = new ResourceHeader (resourceHeaderFile)
      GrafParser parser = new GrafParser(resourceHeader)
      root.eachFileMatch(~/.*\.xml/){ file ->
      //GrafParser parser = new GrafParser(/Users/frankcascio/anc/sense-tagging)
      IGraph graph = parser.parse(file)
      println "file is ${file}"
      def fileNameString = file.toString().replaceAll(".xml",".txt")
     // println "fileNameString is ${fileNameString}"
      def textString = new File(fileNameString).getText()
      StringBuffer stringBuffer = new StringBuffer(textString);
      
      graph.getNodes().each {node ->
      
       // println " node id is ${node.getId()}"
      node.links().each { link ->
           link.regions().each { region ->
             if (region.getId().getAt(0).equals("w"))
             {
                // println ( " region is ${region.getId()} starts at ${region.getStart()} and ends at ${region.getEnd()}")
                 wordStart = region.getStart().toString().toInteger()
                 wordEnd = region.getEnd().toString().toInteger()
                 println "word at ${wordStart} - ${wordEnd} offset is ${stringBuffer.substring(wordStart,wordEnd)}"
             }
           }    
         }
       }       
      }  //end of cycling through xml files
      
      
      
      
 }//end of run()
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   

   
   
   
   
   
   
   
   
   
   
  static void usage()
  {
     println()
     println "USAGE"
     println "       groovy VerifyWordOffsets /path/to/files  /pathtooutput"
  }

  static void main(args)
  {
      if (args.size() != 2)
      {
         usage()
         return;
      }
      
      def verify = new VerifyWordOffsets()
      verify.root = new File(args[0])
      verify.outDir = new File(args[1])
           
      if (!verify.outDir.exists())
      {
         new File(verify.outDir.toString()).mkdir()
        if (!verify.root.exists())
        {
          println "Input directory does not exist: ${verify.root.path}"
          return
        }
       
     }
      verify.run()
  }



}