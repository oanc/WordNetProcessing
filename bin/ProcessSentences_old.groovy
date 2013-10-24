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
import org.xces.graf.util.*

@Grab('org.tc37sc4.graf:graf-io:1.2.0')// was 1.2.2
import org.xces.graf.io.IRegionFilter
import org.xces.graf.io.GrafRenderer
import org.xces.graf.io.GrafLoader
import org.xces.graf.io.*
import org.xces.graf.io.dom.*


@Grab('org.tc37sc4.graf:graf-impl:1.2.0')// was 1.2.2
import org.xces.graf.impl.Factory
import org.xces.graf.impl.CharacterAnchor

@Grab('org.tc37sc4.graf:graf-api:1.2.0')// was 1.2.2
import org.xces.graf.api.*

import groovy.io.FileType

class ProcessSentences {
   IAnnotationSpace space = Factory.newAnnotationSpace("wn","http://wordnet.princeton.edu/");
   File root
   File outDir;
   SentenceListParser parser = new SentenceListParser()
   Logger logger = LoggerFactory.getLogger(ProcessSentences)
   String compiledText



   void run() {
      int regionCount = 0;
      int nodeCount = 0;
      int fileCount =0;
      int foundannotationCount = 0 ;
      def f = new File('noOffsets.txt');
      def noOffsetList = []
      // If the files name contains these annotators, do nothing with the files
      List<String> annotatorsToIgnore = Arrays.asList("a1", "a2", "a3", "a4", "ksuderman", "bpassonneau","tlippincott");
      //String upOneFileName;
      File headerFile = new File('/Users/frankcascio/anc/corpora/OANC-1.2b1/resource-header.xml');
      //File headerFile = new File('/Users/frankcascio/anc/corpora/masc/MASC-3.0.0/resource-header.xml');
      ResourceHeader header = new ResourceHeader(headerFile);
      GrafParser grafParser = new GrafParser(header);
      GrafLoader grafLoader = new GrafLoader(header);
      IGraph graphFromFile;

      //GrafRenderer grafRenderer = new GrafRenderer();
      //IGraph graph = parser.parse("testfile");


      //  building a list of all the file names in the masc
      def mascList = [:]
      def mascDir = new File(Masc.rootString)
      mascDir.eachFileRecurse (FileType.FILES)
      { file ->
         if(file.toString().contains(".hdr") ||
         file.toString().contains(".anc"))
         {
            if(mascList.containsKey(file.name.toString()))
            {

            }
            else
            {
               mascList[file.name.toString()] = file
            }
         }
      }

      // println mascList

      //  building a list of all the file names in the oanc
      def OancList = [:]
      def oancDir = new File(Oanc.rootString)
      oancDir.eachFileRecurse (FileType.FILES)
      { file ->
         if(file.toString().contains(".hdr") ||
         file.toString().contains(".anc"))
         {
            if(OancList.containsKey(file.name.toString()))
            {

            }
            else
            {
               OancList[file.name.toString()] = file
            }
         }
      }

      //println OancList

      logger.debug "Masc size on disk is ${mascList.size()}"
      logger.debug "Oanc size on disk is ${OancList.size()}"


      //Cache cache = new Cache()

      logger.info  "root is $root";
      logger.info "outDir is $outDir";

      //cycle through the subdirectories in root
      //      def directories = []
      //      root.eachDir { directories << it }
      //      def temp = []
      //      temp << directories[0]
      //      temp.each { subdir ->
      //outermost loop going through subdirectories; one subdir for each wordsense
      root.eachDir { subdir ->
         logger.info "subDir is $subdir.name"
         def bySentence = [:]
         compiledText = "";
         //IGraph graph = Factory.newGraph();
         def dirList = [];
         String parentDirName;
         //String outFileName;

         //cycle through the .xml files in this subDir
         int totalSentenceCount = 0
         subdir.eachFileMatch(~/.*\.xml/) { file ->
            regionCount = 0;
            nodeCount = 0;

            logger.debug"\n\n\n\n"
            logger.debug "Processing ${file.path}"
            //parts array gets populated from the file name...pretty neat...
            def parts = file.name.replace('.xml', '').split('_')
            String annotator = parts[0]
            String round = parts[1]
            String part = parts[2]

            if ( annotatorsToIgnore.contains(annotator))

            {
               logger.debug "skipping annotator: ${annotator}   round: ${round}   part:  ${part}"
            }
            else
            {
               //get all the sentences in a SentenceList and Sense Keys ( that area on the top of the file )
               SentenceList slist = parser.parse(file)
               SenseKeyIndex keyIndex = slist.getSenseKeyMap()
               // compiledText = "";

               //println ("compiled text is  ${compiledText}")

               //ok we are going to cycle through each sentence in the sentence list
               slist.each { s ->
                  //make an id using the path:start offset
                  String id = "${s.path}:${s.start}"
                  //use key, find the info, but wait might not be there
                  SentenceInfo sentenceInfo = bySentence[id]
                  //if not there, put it there using the key, if there, don't put it anywhere
                  if (sentenceInfo == null)
                  {
                     //if we are here, then we have a new sentence to work with...
                     //make new SentenceInfo
                     sentenceInfo = new SentenceInfo()
                     //put the text from the sentence from the SentenceList parse
                     sentenceInfo.text = s.text
                     //get the sentence and keep adding to all the previous sentences from this sentence list ( sentence file )

                     /*here figure out what the offsets of the words will be in the new compiled Sentence file  */

                     //start of word offset from sentence start only ( small number )
                     int offset = s.offset.toInteger()
                     if(offset == 0)
                     {
                        String spath = "${s.path}"
                        if (noOffsetList.contains(spath)== false)
                        {
                           logger.error "check ${s.path} the offset is missing"
                           f.append("round:${round} part:${part} annotator:${annotator} file:${file} original ${s.path}\n")
                           noOffsetList.add(spath)
                        }
                     }

                     def FilePathArray = s.path.split(File.separator);
                     //println "File Name is is ${FilePathArray.last()} "
                     String targetFileNameWithExtension = FilePathArray.last()
                     File targetFile
                     IGraph graph
                     // println "targetFile is is ${targetFile.name}"

                     def targetFileNameArray = targetFileNameWithExtension.tokenize('.')

                     //println "targetFileNameArray is ${targetFileNameArray}"
                     String targetFileName =  targetFileNameArray[0] + ".hdr"

                     //println "targetFileName is ${targetFileName}"

                     //is this file in the local masc corpus
                     targetFile = mascList[targetFileName]


                     if(targetFile != null)
                     {
                        //println "found in masc map ${targetFileName} "
                        // graph = grafParser.parse(targetFile)
                     }
                     else
                     {
                        //ok not found in masc map; look in the oanc map instead
                        targetFile = OancList[targetFileName]

                        if(targetFile != null)
                        {
                           //println "found in oanc map ${targetFileName}   "
                           // graph = grafParser.parse(targetFile)
                        }
                     }

                    // logger.debug"looking for ${targetFileName} and found ${targetFile.toString()}"
                     if(targetFile != null)
                     {
                        try
                        {
                           //ResourceHeader header = new ResourceHeader(Oanc.header)
                           //GrafParser parser = new GrafParser(header)

                           //graph = grafParser.parse(targetFile)
                           graphFromFile = grafLoader.load(targetFile);
                           println "graphFromFile is "
                           println graphFromFile.nodes().size();

                           // renderGraf(graphFromFile, new File("/Users/frankcascio/anc/sense-tagging/bin/grafOut.out"))

                           def sentences = []

                           graphFromFile.nodes().each { node ->

                              // println  node.getLinks().size();

                              if (node.annotation.label == 's')
                              {
                                 //println  "node annotation label starts with s: "  + node.annotation.label;

                                 IRegion span = GraphUtils.getSpan(node)
                                 Sentence originalSentence = new Sentence()
                                 originalSentence.id = node.id
                                 originalSentence.start = span.start.offset
                                 originalSentence.end = span.end.offset

                                 // println "${originalSentence.start} ... ${s.start.toInteger()} .... ${s.end.toInteger()} .. ${originalSentence.end}"

                                 if ((originalSentence.start <= s.start.toInteger() )  &&
                                 originalSentence.end >= s.end.toInteger() )
                                 {
                                    //remove this for now...I was trying to adjust the offset if &quot or &lt etc were found...the find is not working though...
                                    //int newOffset = getfakeOffset(s.text, offset)
                                    //println "found something here....${originalSentence.start} ... ${s.start.toInteger()} .... ${s.end.toInteger()} .. ${originalSentence.end}"
                                    //start of the word
                                    int wordStart = s.start.toInteger();
                                    //end of the word
                                    int wordEnd = s.end.toInteger();
                                    //the difference is the word length
                                    int wordLength = wordEnd - wordStart;
                                    //what is the last character in the compiledText document; make that the start of this sentence for the compiled text
                                    sentenceInfo.start = compiledText.length();

                                    //what is the length of the original sentence in the oanc or masc text file
                                    int originalSentenceLength = originalSentence.end - originalSentence.start;

                                    //now find the new end of the compiledText document using the length of this sentence..this does not seem reliable to me...
                                    //sentenceInfo.end = sentenceInfo.start + s.text.length();
                                    //instead find the new end of the compiledText document using the length of this sentence...use the oanc sentence length
                                    sentenceInfo.end = sentenceInfo.start + originalSentenceLength ;

                                    //start of the word in the compiled text, start of this sentence + the offset of the word in the sentence
                                    sentenceInfo.wordStart = sentenceInfo.start + offset
                                   // sentenceInfo.wordStart = sentenceInfo.start + newOffset
                                    //end of word in compiled text = start plus length of word
                                    sentenceInfo.wordEnd = sentenceInfo.wordStart + wordLength

                                    //is it possible to get the original sentence instead of the s.text ?
                                    compiledText = compiledText + s.text + '\n'

                                    sentenceInfo.path = s.path
                                    //store info in the bySentence Map using id as the key; now we wont enter this if statement again if
                                    //find this sentence again in the is file  { id = path.sentencestart
                                    bySentence[id] = sentenceInfo
                                 }
                                 sentences << originalSentence
                              }
                           }
                        }
                        catch (GrafException e)
                        {
                           logger.error "Unkown Graf Exception on file targetFile.toString()"
                           //e.printStackTrace()
                        }
                        catch(FileNotFoundException e)
                        {
                           logger.error  "FileNotFoundException on file targetFile.toString()"

                        }
                     }
                  }

                  //make new Assigned sense object,
                  AssignedSense sense = new AssignedSense()
                  //fill sense up; all these will go to features of the nodes; each sentence will have
                  sense.annotator = annotator
                  sense.round = round
                  sense.part = part
                  sense.sense = keyIndex.get(s.getWNSense())
                  sentenceInfo.senses << sense
               }  //end of this file's sentences



               logger.debug "Looping through this file's sentences is done )"

            }//end of if good annotator

         }    //End of cycling through each fileMatch (.xml ) of this subdir


         logger.debug"End of cycling through the subdirs of root..now to create the graf"

         //println ("compiled text is now ${compiledText}")
         IGraph graph = createGraph(bySentence)
         graph.setContent(compiledText);

         String outFileName = subdir.name + ".xml"

         //String suffix = fileCount + ".xml";
         //outFileName = outFileName.replaceAll(".xml", suffix)
         //fileCount++;

         File outFile = new File(outDir, outFileName);
         logger.debug "Creating....................... ${outFile.path}";


         FileOutputStream outStream = new FileOutputStream(outFile);
         //use stream to make a dream Weaver...dream Weaver ?  No, a streamWriter
         OutputStreamWriter grafWriter = new OutputStreamWriter(outStream, "UTF-8");

         GrafRenderer grafRenderer = new GrafRenderer(grafWriter);
         //logger.debug("output Graf in {}", outFile.getAbsolutePath());
         //render the graf
         grafRenderer.render(graph);
         grafRenderer.close();

         String textFileName = outFileName.replaceAll(".xml", ".txt")
         File textFile = new File(outDir, textFileName);
         textFile.setText(compiledText, 'UTF-8')

      }  //endo of cycling through the subdirs of root



   }  //end of run()

   IGraph createGraph(Map map)
   {
      IDGenerator id = new IDGenerator()
      IGraph graph = Factory.newGraph()
      graph.addAnnotationSpace(space)

      map.each { sid,info ->
         //makes some nodes, regions, an annotation just for the sentence label s , attribute the annotation to a space
         INode sentenceNode = Factory.newNode(id.generate('s-n'))
         IRegion sentenceRegion = Factory.newRegion(id.generate('s-r'), info.start, info.end)
         IAnnotation sentenceAnnotation = Factory.newAnnotation(id.generate('a'), "s")
         sentenceAnnotation.setAnnotationSpace(space)

         //now connect all the things we just made, set the node to the region,
         sentenceNode.addRegion(sentenceRegion)
         //add the sentence Annotation to the sentence node
         sentenceNode.addAnnotation(sentenceAnnotation)
         //add the node to the graph
         graph.addNode(sentenceNode)
         //add the region to the graph also...
         graph.addRegion(sentenceRegion)

         //make the words Node,
         INode wordNode = Factory.newNode(id.generate('w-n'))
         //add the node to the graph
         graph.addNode(wordNode)
         //make a new region just for the word
         IRegion wordRegion = Factory.newRegion(id.generate('w-r'), info.wordStart, info.wordEnd)
         //add this words region to the graph
         graph.addRegion(wordRegion)
         //connect the region to the word's node
         wordNode.addRegion(wordRegion)
         //now cycle through the senses of the info.senses list, each one becomes an annotation to the node
         info.senses.each { assigned ->
            //make the annotation
            IAnnotation wordAnnotation = assigned.createAnnotation(id.generate('a'))
            //connect it to the node
            wordNode.addAnnotation(wordAnnotation)
         }
      }
      return graph
   }



   File findFile(File root, String targetFile)
   {
      // def pathStringArray = []
      // def fileStringArray = []
      // def targetFileString = []
      //root.eachFileMatch(~/.*\.hdr/)
      root.eachFile
      { file ->
         if(file.isFile())
         {
            //logger.debug("checking ${file} vs target file is ${targetFile}")
            //do they match ? at least the first part of the file name...without the extension
            //split up the path of this iterations file
            def pathStringArray = file.toString().split(File.separator)
            //get the size of the resulting array
            int pathStringArraySize = pathStringArray.size()
            //get the last item in the array; which is just the file name. and split that up to drop the extension
            // println "pathStringArray is ${pathStringArray} and pathStringArraySize is ${pathStringArraySize}"
            def fileStringArray = (pathStringArray[pathStringArraySize-1]).split("\\.")
            //get the file name only, the first elemetn in the array, throw away the extension
            // println "fileStringArray is ${fileStringArray}"
            def fileString = fileStringArray[0]
            // print "file is ${file} but fileString is "
            // println "$fileString"
            def targetFileString = targetFile.toString().split("\\.")
            // print "targetFileString is ${targetFile} but targetFileString is "
            // println "$targetFileString"
            if ( (fileString[0]).equals(targetFileString[0]) &&
            (fileString[1].equals(".hdr")))
            {
               return file
               //logger.debug("file found is ${file} and target file is ${targetFile}")
            }
            else
            {
               //this aint the file..
               logger.debug("nope")
            }
         }
         else
         {
            if(file.isDirectory())
            {
               findFile(file,targetFile)
            }
         }

      }

      return null;

      //list directories, if not empty list
      //cycle through directories ( recurse on each one )

      // list is empty, then list the files..
      //search the files for the target file.
      //if found, return, if not found retun -1;


   }

   File    findLocalGraf(String targetFileName)
   {
   }


   class Sentence implements Comparable<Sentence> {
      String id
      int start
      int end

      String toString()
      {
         return "${id}\t${start}\t${end}"
      }
      int compareTo(Sentence sen)
      {
         return start - sen.start
      }
   }

   void renderGraf(IGraph graf, File outFile)
   {



      //create an out File
      //outFile = new File(outDir, outFileName);

      //make an output stream
      //      FileOutputStream outStream = new FileOutputStream(outFile);

      //use stream to make a streamWriter
      //      OutputStreamWriter writer = new OutputStreamWriter(outStream, "UTF-8");

      //make the renderer
      //GrafRenderer grafRenderer = new GrafRenderer(writer);

      //or as an alternative, render to the console
      GrafRenderer grafRenderer = new GrafRenderer(System.out);

      //render the graf
      grafRenderer.render(graf);

      //close the thing...
      grafRenderer.close();
   }

   int getfakeOffset(String sentenceText, int originalOffset)
   {



      try
      {
          int newOffset;

          newOffset = originalOffset;

         if ( (originalOffset > 0 )&&
         ( sentenceText.length() > 0))
         {
            //println " originalSentence is ${sentenceText}\nand originalOffset is ${originalOffset}"
            String choppedSentence = sentenceText[0..originalOffset-1]
           // println " originalSentence is ${sentenceText}\nand choppedSentence is ${choppedSentence}"
            if(choppedSentence.contains("/&quot") )
          {
             newOffset = originalOffset - 2;
             logger.debug "found something: &quote"
          }
          if(choppedSentence.contains("/&lt"))
          {
              newOffset = originalOffset - 2;
              logger.debug "found something: &lt"
          }
          if(choppedSentence.contains("/&gt"))
          {
              newOffset = originalOffset - 2;
              logger.debug "found something: &gt"
          }
//          if(choppedSentence.contains("\&quot")
//          {
//              newOffset = originalOffset - 4;
//              println "found something: &quote"
//           }
          
       } 
      
      }
      catch (Exception e)
      {
         //do nothing smarty pants
      }
      
      
      
      
      
      
      return newOffset;
   }

   static void usage()
   {
      println()
      println "USAGE"
      println "    groovy ProcessSentences.groovy /path/to/data/"
      println ()
   }

   static void main(args)
   {
      if (args.size() != 2)
      {
         usage()
         return
      }
      def proc = new ProcessSentences()
      //set input dir
      proc.root = new File(args[0])
      //set output dir
      proc.outDir = new File(args[1])

      //      def root = new File('/Users/suderman/Projects/sense-tagging')
      //      def proc = new ProcessSentences()
      //     proc.root = new File(root, 'words')
      //     proc.outDir = new File(root, 'output')
      if (!proc.root.exists())
      {
         println "Input directory not found: ${proc.root.path}"
         return
      }
      if (!proc.outDir.exists())
      {
         new File(proc.outDir.toString()).mkdir()

         if (!proc.outDir.exists())
         {
            println "Output directory could not be : ${proc.out.path}"
            return
         }
      }

      proc.run()
   }
}







class Masc {
   static final String rootString = '/Users/frankcascio/anc/corpora/masc/MASC-3.0.0';
   static final File root = new File(rootString)
   static final File header = new File(root, 'resource-header.xml')
   static final File data = new File(root, 'data')
}

class Oanc {
   static final String rootString = '/Users/frankcascio/anc/corpora/OANC-1.2b1'
   //static final String rootString = '/Users/frankcascio/anc/corpora/OANC-GrAF'
   static final File root = new File(rootString)
   static final File header = new File(root, 'resource-header.xml')
   static final File data = new File(root, 'data')
}




//
//
//class Counter
//{
//   int count = 0
//
//   void add(int n)
//   {
//      count += n
//   }
//
//   void next()
//   {
//      ++count
//   }
//
//   String toString() { return count.toString() }
//}
//
