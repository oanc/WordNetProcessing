@GrabResolver(name = 'anc-releases', root = 'http://www.anc.org:8080/nexus/content/repositories/releases')

@Grab('org.anc.wn:SentenceList:1.2.2-SNAPSHOT')
import org.anc.wn.*

@Grab('org.anc.osgi:wordnet:2.0.0-SNAPSHOT')
import org.anc.wordnet.api.*

@Grab('org.anc:common:3.0.0')
import org.anc.util.*

@Grab('ch.qos.logback:logback-classic:1.0.0')
import org.slf4j.*

import groovy.lang.GroovyClassLoader;

@GrabResolver(name = 'anc-snapshots', root = 'http://www.anc.org:8080/nexus/content/repositories/snapshots')
//@Grab('org.tc37sc4.graf:graf-util:1.2.3-SNAPSHOT')
@Grab('org.tc37sc4.graf:graf-util:1.2.2')
import org.xces.graf.util.GraphUtils
import org.xces.graf.util.*

@Grab('org.tc37sc4.graf:graf-io:1.2.0')
// was 1.2.2
import org.xces.graf.io.IRegionFilter
import org.xces.graf.io.GrafRenderer
import org.xces.graf.io.GrafLoader
import org.xces.graf.io.*
import org.xces.graf.io.dom.*


@Grab('org.tc37sc4.graf:graf-impl:1.2.0')
// was 1.2.2
import org.xces.graf.impl.Factory
import org.xces.graf.impl.CharacterAnchor

@Grab('org.tc37sc4.graf:graf-api:1.2.0')
// was 1.2.2
import org.xces.graf.api.*

import groovy.io.FileType

import generateWordnetSentenceCorpusHeaders;

class ProcessSentences
{
    IAnnotationSpace space = Factory.newAnnotationSpace("wn", "http://wordnet.princeton.edu/");
    File root
    File outDir;
    Boolean cv;

    SentenceListParser parser = new SentenceListParser()
    Logger logger = LoggerFactory.getLogger(ProcessSentences)
    String compiledText;

    def annotatorIdMap = ['brubin'      : '101',
                          'cgozo'       : '102',
                          'rstandig'    : '103',
                          'tmartin'     : '104',
                          'elstickles'  : '105',
                          'kaolla'      : '106',
                          'carichter'   : '107',
                          'comcgeetubb' : '108',
                          'tofox'       : '109',
                          'chisom'      : '110',
                          'veweser'     : '111',
                          'vebatchelder': '112',
                          'jeneale'     : '113',
                          'adelpriore'  : '114',
                          'anfahmy'     : '115',
                          'sophiav'     : '116',
                          'sophiavv'    : '116',
                          'jestuart'    : '117',

    ]


    void run()
    {
        int regionCount = 0;
        int nodeCount = 0;
        int fileCount = 0;
        int foundannotationCount = 0;

        def noOffsetList = [];
        def noSenseList = [];
        // If the files name contains these annotators, do nothing with the files
        List<String> annotatorsToIgnore = Arrays.asList("a1", "a2", "a3", "a4", "ksuderman", "bpassonneau", "tlippincott");
        List<String> partsToIgnore = Arrays.asList("sample-50");
        //String upOneFileName;
        File headerFile = new File('/Users/frankcascio/anc/corpora/OANC-1.2b1/resource-header.xml');
        //File headerFile = new File('/Users/frankcascio/anc/corpora/masc/MASC-3.0.0/resource-header.xml');
        File problemFile = new File(outDir, 'problemFile.log');
        ResourceHeader header = new ResourceHeader(headerFile);
        GrafParser grafParser = new GrafParser(header);
        GrafLoader grafLoader = new GrafLoader(header);
        IGraph graphFromFile;
        DocumentHeader docHeader;


        //GrafRenderer grafRenderer = new GrafRenderer();
        //IGraph graph = parser.parse("testfile");

        //  building a list of all the file names in the masc
        def mascList = [:]
        def mascDir = new File(Masc.rootString)
        mascDir.eachFileRecurse(FileType.FILES)
                { file ->

                    if (file.toString().contains(".hdr") ||
                            file.toString().contains(".anc"))
                    {
                        if (mascList.containsKey(file.name.toString()))
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
        oancDir.eachFileRecurse(FileType.FILES)
                { file ->
                    if (file.toString().contains(".hdr") ||
                            file.toString().contains(".anc"))
                    {
                        if (OancList.containsKey(file.name.toString()))
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

        logger.info "root is $root";
        logger.info "outDir is $outDir";

        //cycle through the subdirectories in root
        //      def directories = []
        //      root.eachDir { directories << it }
        //      def temp = []
        //      temp << directories[0]
        //      temp.each { subdir ->
        //outermost loop going through subdirectories; one subdir for each wordsense
        root.eachDir { subdir ->

            if (!subdir.getName().equals('.svn'))
            {


                logger.info "subDir is $subdir.name";

                String word = " ";         //leave this here, a better programmer would not need this line...
                String pos;
                String crossvalidated = "wn";

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

                    logger.debug "\n\n\n\n"
                    logger.debug "Processing ${file.path}"
                    parentDirName = getParentDir(file.path.toString());

                    //parts array gets populated from the file name...pretty neat...
                    def parts = file.name.replace('.xml', '').split('_');
                    String annotator = parts[0];
                    String round = parts[1];
                    String part = parts[2];
                    if  ((annotatorsToIgnore.contains(annotator))     ||
                         (partsToIgnore.contains(part))   )//            ||
//                         (  cv && !part.contains('cv') )           ||
//                         ( !cv &&  part.contains('cv')) )
                    {
                        logger.info "skipping: annotator: ${annotator}   round: ${round}   part:  ${part}";
                    }
                    else
                    {
                        logger.debug "using annotator: ${annotator}   round: ${round}   part:  ${part}";
                        if (cv)
                        {
                            crossvalidated = "wn-fn";
                            logger.info "                                                              part:  ${part}";
                        }
                        else
                        {
                            crossvalidated = "wn";
                        }
                        //get all the sentences in a SentenceList and Sense Keys ( that area on the top of the file )
                        SentenceList slist = parser.parse(file)
                        SenseKeyIndex keyIndex = slist.getSenseKeyMap();
                        word = slist.getWord();
                        slist.getPos();
                        logger.debug("working on ${word} and pos ${pos}");
                        // compiledText = "";
                        logger.debug("keyIndex size is ${keyIndex.size()}");
                        //println ("compiled text is  ${compiledText}")
                        if (keyIndex.size() > 0)
                        {
                            //ok we are going to cycle through each sentence in the sentence list
                            slist.each
                                    { s ->
                                        int newOffset;
                                        int offset;
                                        if (!validSense(s))
                                        {
                                            problemFile.append("invalid sense: ${word}: ${round} ${part} ${annotator} Offset:${s.offset} wnkey:${s.getWNSense()} text:${s.text} \n");
                                        }
                                        else
                                        {
//                                            if (!validOffset(s, word))
//                                            {
//                                                s.text = cleanSentence(s.text);
//                                                newOffset = getNewOffset(s.text, s.offset.toInteger(), word);
//                                                problemFile.append("invalid offset corrected:${word}: ${round} ${part} ${annotator} oldOffset:${s.offset} newOffset:${newOffset} wnkey:${s.getWNSense()} text:${s.text} \n");
//                                                offset = newOffset;
//                                            }
//                                            else
//                                            {
//                                                //start of word offset from sentence start only ( small number )
//                                                offset = s.offset.toInteger();
//                                            }
//
//                                            if (offset <= 0)
//                                            {
//                                                problemFile.append("invalid offset: ${word}: ${round} ${part} ${annotator} Offset:${s.offset} wnkey:${s.getWNSense()} text:${s.text} \n");
//                                            }

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
                                               // offset = s.offset.toInteger();

                                                //if the word is not found at the offset, find the word and change the offset
                                                if (!validOffset(s, word))
                                                {
                                                    s.text = cleanSentence(s.text);
                                                    newOffset = getNewOffset(s.text, s.offset.toInteger(), word);
                                                    problemFile.append("invalid offset corrected:${word}: ${round} ${part} ${annotator} oldOffset:${s.offset} newOffset:${newOffset} wnkey:${s.getWNSense()} text:${s.text} \n");
                                                    offset = newOffset;
                                                }
                                                else
                                                {
                                                    //ok we found the word where it was supposed to be, just use the original offset...
                                                    //start of word offset from sentence start only ( small number )
                                                    offset = s.offset.toInteger();
                                                }

                                                if (offset <= 0)
                                                {
                                                    problemFile.append("invalid offset: ${word}: ${round} ${part} ${annotator} Offset:${s.offset} wnkey:${s.getWNSense()} text:${s.text} \n");
                                                }


                                                def FilePathArray = s.path.split(File.separator);
                                                //println "File Name is is ${FilePathArray.last()} "
                                                String targetFileNameWithExtension = FilePathArray.last();
                                                File targetFile;
                                                IGraph graph;
                                                // println "targetFile is is ${targetFile.name}"

                                                def targetFileNameArray = targetFileNameWithExtension.tokenize('.');

                                                //println "targetFileNameArray is ${targetFileNameArray}"
                                                String targetFileName = targetFileNameArray[0] + ".hdr";

                                                //println "targetFileName is ${targetFileName}"

                                                //is this file in the local masc corpus
                                                targetFile = mascList[targetFileName];


                                                if (targetFile != null)
                                                {
                                                    //println "found in masc map ${targetFileName} "
                                                    // graph = grafParser.parse(targetFile)
                                                }
                                                else
                                                {
                                                    //ok not found in masc map; look in the oanc map instead
                                                    targetFile = OancList[targetFileName];

                                                    if (targetFile != null)
                                                    {
                                                        //println "found in oanc map ${targetFileName}   "
                                                        // graph = grafParser.parse(targetFile)
                                                    }
                                                }

                                                // logger.debug"looking for ${targetFileName} and found ${targetFile.toString()}"
                                                if (targetFile != null)
                                                {
                                                    try
                                                    {
                                                        //ResourceHeader header = new ResourceHeader(Oanc.header)
                                                        //GrafParser parser = new GrafParser(header)

                                                        String docId = findDocId(targetFile);
                                                        //println "found docId ${docId}";



                                                       // docHeader = new DocumentHeader(targetFile) ;

                                                       // println "docHeader gives content sLocation of ${docHeader.getContentLocation()}";
                                                       // println "docHeader get Annotation Types ${docHeader.getAnnotationTypes()}";
                                                       // println "docHeader get Properties ${docHeader.getProperties()}";
                                                       // println "docHeader get Properties ${docHeader.toString()}";

                                                     //   println "docHeader getAt documentHeader ${docHeader.getAt("documentHeader")}";

                                                      //  println "docHeader getAt doc id of ${docHeader.getAt("docId")}";


                                                       // println "docHeader gives doc id of ${docHeader.getDocId()}";
                                                        //throws FileNotFoundException

                                                        //graph = grafParser.parse(targetFile)
                                                        graphFromFile = grafLoader.load(targetFile);
                                                        //logger.debug( "graphFromFile size is ${graphFromFile.nodes().size()}" );

                                                        // renderGraf(graphFromFile, new File("/Users/frankcascio/anc/sense-tagging/bin/grafOut.out"))

                                                        def sentences = [];

                                                        graphFromFile.nodes().each { node ->

                                                            // println  node.getLinks().size();

                                                            if (node.annotation.label == 's')
                                                            {
                                                                //println  "node annotation label starts with s: "  + node.annotation.label;

                                                                IRegion span = GraphUtils.getSpan(node);
                                                                Sentence originalSentence = new Sentence();
                                                                originalSentence.id = node.id;
                                                                originalSentence.start = span.start.offset;
                                                                originalSentence.end = span.end.offset;

                                                                // println "${originalSentence.start} ... ${s.start.toInteger()} .... ${s.end.toInteger()} .. ${originalSentence.end}"

                                                                if ((originalSentence.start <= s.start.toInteger()) &&
                                                                        originalSentence.end >= s.end.toInteger())
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
                                                                    sentenceInfo.end = sentenceInfo.start + originalSentenceLength;

                                                                    //start of the word in the compiled text, start of this sentence + the offset of the word in the sentence
                                                                    sentenceInfo.wordStart = sentenceInfo.start + offset;
                                                                    // sentenceInfo.wordStart = sentenceInfo.start + newOffset
                                                                    //end of word in compiled text = start plus length of word
                                                                    sentenceInfo.wordEnd = sentenceInfo.wordStart + wordLength;

                                                                    //is it possible to get the original sentence instead of the s.text ?
                                                                    compiledText = compiledText + s.text + '\n';

                                                                    sentenceInfo.path = s.path;
                                                                    sentenceInfo.originalSentenceStart = originalSentence.start;
                                                                    sentenceInfo.originalSentenceEnd   = originalSentence.end;
                                                                    sentenceInfo.originalDocId = docId;

                                                                    sentenceInfo.annotator = annotator;
                                                                    //store info in the bySentence Map using id as the key; now we wont enter this if statement again if
                                                                    //find this sentence again in this file  { id = path.sentencestart
                                                                    bySentence[id] = sentenceInfo
                                                                }
                                                                sentences << originalSentence
                                                            }
                                                        }
                                                    }
                                                    catch (GrafException e)
                                                    {
                                                        logger.error "Unkown Graf Exception on file ${targetFile.toString()}";
                                                        //e.printStackTrace()
                                                    }
                                                    catch (FileNotFoundException e)
                                                    {
                                                        logger.error "FileNotFoundException on file ${targetFile.toString()}";
                                                    }
                                                }
                                                //make new Assigned sense object, put it in SentenceInfo
                                                AssignedSense sense = new AssignedSense()

                                                //fill sense up; all these will go to features of the nodes; each sentence will have
                                                if (annotatorIdMap.containsKey(annotator))
                                                {
                                                    //sense.annotator = annotator
                                                    sense.annotator = annotatorIdMap.get(annotator);
                                                }
                                                else
                                                {
                                                    problemFile.append("annotator:${annotator} does not have an id  \n");

                                                }
                                                sense.round = round
                                                sense.part = part
                                                sense.set = "wn";  //TODO what to do with the cv set here ?
                                                sense.sense = keyIndex.get(s.getWNSense())
                                                sentenceInfo.senses << sense
                                            }
                                        }
                                    }  //end of this file's sentences


                            logger.debug "Looping through this file's sentences is done )"
                        } //end of if keyIndex.size() > 0

                    }//end of if good annotator

                }    //End of cycling through each fileMatch (.xml ) of this subdir


                logger.debug "End of cycling through the subdirs of root..now to create the graf"

                //println ("compiled text is now ${compiledText}")
                def IGraph[] graphList = createGraph(bySentence)
                graphList[0].setContent(compiledText);
                graphList[1].setContent(compiledText);

                String outFileName0 = subdir.name + "-s-wn.xml";
                String outFileName1 = subdir.name + "-wn.xml";

                //String suffix = fileCount + ".xml";
                //outFileName = outFileName.replaceAll(".xml", suffix)
                //fileCount++;

                String outSubDir;
                //creating a new directory for each word
                if(parentDirName.contains('-'))
                {
                    outSubDir = parentDirName
                }
                else
                {
                    outSubDir =   word ;
                }

                //String outSubDir =   word + File.separator + crossvalidated + File.separator;
                logger.info "outSubDir is ${outSubDir}";
                File tempOutDir = new File(outDir, outSubDir);
                logger.info "tempOutDir is ${tempOutDir}";
                if (!tempOutDir.exists())
                {
                    new File(tempOutDir.toString()).mkdir();
                    if (!tempOutDir.exists())
                    {
                        println "Output directory could not be made: ${tempOutDir.path}"
                        // return
                    }
                }
                File wordOutDir = new File(tempOutDir, crossvalidated);
                logger.info "wordOutDir is ${wordOutDir}";
                if (!wordOutDir.exists())
                {
                    new File(wordOutDir.toString()).mkdir();
                    if (!wordOutDir.exists())
                    {
                        println "Output directory could not be made: ${wordOutDir.path}"
                        // return
                    }
                }


                //render the sentence graph first
                File outFile = new File(wordOutDir, outFileName0);
                logger.debug "Creating....................... ${outFile.path}";

                FileOutputStream outStream0= new FileOutputStream(outFile);
                //use stream to make a dream Weaver...dream Weaver ?  No, a streamWriter
                OutputStreamWriter grafWriter0 = new OutputStreamWriter(outStream0, "UTF-8");

                GrafRenderer grafRenderer0 = new GrafRenderer(grafWriter0);
                //logger.debug("output Graf in {}", outFile.getAbsolutePath());
                //render the graf
                grafRenderer0.render(graphList[0]);
                grafRenderer0.close();


                //render the word graph next
                outFile = new File(wordOutDir, outFileName1);
                logger.debug "Creating....................... ${outFile.path}";

                FileOutputStream outStream1 = new FileOutputStream(outFile);
                //use stream to make a dream Weaver...dream Weaver ?  No, a streamWriter
                OutputStreamWriter grafWriter1 = new OutputStreamWriter(outStream1, "UTF-8");

                GrafRenderer grafRenderer1 = new GrafRenderer(grafWriter1);
                //logger.debug("output Graf in {}", outFile.getAbsolutePath());
                //render the graf
                grafRenderer1.render(graphList[1]);
                grafRenderer1.close();

                String textFileName = outFileName1.replaceAll("-wn.xml", ".txt");
                File textFile = new File(wordOutDir, textFileName);
                textFile.setText(compiledText, 'UTF-8')

            }//end of if not .svn directory

        }  //endo of cycling through the subdirs of root


    }  //end of run()

    ArrayList<IGraph> createGraph(Map map)
    {
        IDGenerator id = new IDGenerator();
        IGraph sentenceGraph = Factory.newGraph();
        IGraph wordGraph     = Factory.newGraph();
        sentenceGraph.addAnnotationSpace(space);
        wordGraph.addAnnotationSpace(space);
        def IGraphList = [];


        map.each { sid, info ->
            //makes some nodes, regions, an annotation just for the sentence label s , attribute the annotation to a space
            INode sentenceNode = Factory.newNode(id.generate('s-n'));
            IRegion sentenceRegion = Factory.newRegion(id.generate('s-r'), info.start, info.end);
            IAnnotation sentenceAnnotation = Factory.newAnnotation(id.generate('a'), "s");
            sentenceAnnotation.setAnnotationSpace(space);
            sentenceAnnotation.addFeature(Factory.newFeature("path", info.path)) ;
            sentenceAnnotation.addFeature(Factory.newFeature("orig-start", info.originalSentenceStart) );
            sentenceAnnotation.addFeature(Factory.newFeature("orig-end", info.originalSentenceEnd)) ;
            sentenceAnnotation.addFeature(Factory.newFeature("orig-docId", info.originalDocId)) ;

            //now connect all the things we just made, set the node to the region,
            sentenceNode.addRegion(sentenceRegion);
            //add the sentence Annotation to the sentence node
            sentenceNode.addAnnotation(sentenceAnnotation);
            //add the node to the graph
            sentenceGraph.addNode(sentenceNode);
            //add the region to the graph also...
            sentenceGraph.addRegion(sentenceRegion);

            //make the words Node,
            INode wordNode = Factory.newNode(id.generate('w-n'));
            //add the node to the graph
            wordGraph.addNode(wordNode);
            //make a new region just for the word
            IRegion wordRegion = Factory.newRegion(id.generate('w-r'), info.wordStart, info.wordEnd);
            //add this words region to the graph
            wordGraph.addRegion(wordRegion);
            //connect the region to the word's node
            wordNode.addRegion(wordRegion);
            //now cycle through the senses of the info.senses list, each one becomes an annotation to the node
            info.senses.each { assigned ->
                //make the annotation
                IAnnotation wordAnnotation = assigned.createAnnotation(id.generate('a'));
                //connect it to the node
                wordNode.addAnnotation(wordAnnotation);
            }
        }

        IGraphList[0] = sentenceGraph;
        IGraphList[1] = wordGraph;
        return IGraphList;
    }

    /**
     * validates certain things about the Sentence info object, right now just offsets and the WN sense
     *
     * @param sentence
     * @return
     */
    Boolean validSense(org.anc.wn.Sentence sentence)
    {
        Boolean result = true;


        if (sentence.getWNSense() == "")
        {
            result = false;
        }
        else if (sentence.getWNSense().contains("2147"))
        {
            result = false;
        }


        return result;

    }


    String findDocId(File headerFile)
    {
        def firstLine;
        String docId = "";
        headerFile.withReader { firstLine = it.readLine() }
       // println " firstLine is ${firstLine}";

        //  if (line.contains("docId"))
        // {
        String headerString = firstLine.toString();
        def headerStringArray = headerString.tokenize() ;
        headerStringArray.each{ item->
          //  println "item is ${item.toString()}"


            if (item.toString().toLowerCase().contains("docid") )
            {
                docId = item.toString();

                docId = docId.replaceAll("\"","");
                docId = docId.replaceAll("docId=","");
               // docId = docId.replaceAll("docId");

            }




        }
       // println "headString is ${headerString.tokenize()}";

        //  }
        return docId;


    }

    /**
     * validates certain things about the Sentence info object, right now just offsets and the WN sense
     *
     * @param sentence
     * @return
     */
    Boolean validOffset(org.anc.wn.Sentence sentence, String word)
    {
        Boolean result = true;
        int wordLength = 0;
        int offset;
        String text = sentence.text;
        String first2ofWord;
        String first2ofText;


        if (sentence.offset.toInteger() == 0)
        {
            result = false;
        }
        else if (sentence.text.length() <= sentence.offset.toInteger())
        {
            result = false;
            println("${sentence.start.toInteger()} to ${sentence.end.toInteger()} offset is ${sentence.offset.toInteger()} text is ${sentence.text}");
        }
        else if ((sentence.start.toInteger() != null) && (sentence.start.toInteger() != null))
        {
            wordLength = sentence.end.toInteger() - sentence.start.toInteger();

            if (wordLength > 0)
            {
                offset = sentence.offset.toInteger();

                if (offset + 1 <= sentence.text.length())
                {
                    //println ("${sentence.start.toInteger()} to ${sentence.end.toInteger()} offset is ${sentence.offset.toInteger()} text is ${sentence.text}") ;
                    //println ("${text.substring(offset,offset+2)}   first two characters of word are ${word.substring(0, 2)}") ;
                    first2ofText = text.substring(offset, offset + 1).toUpperCase();
                    first2ofWord = word.substring(0, 1).toUpperCase();

                    if (first2ofText.equals(first2ofWord))
                    {

                    }
                    else
                    {
                       // println("${sentence.start.toInteger()} to ${sentence.end.toInteger()} offset is ${sentence.offset.toInteger()} text is ${sentence.text}");
                       // println("text in sentence at offset is ${text.substring(offset, offset + 1)}, but the first character of the word: ${word} is ${word.substring(0, 1)}");
                        result = false;
                    }
                }
                else
                {
                   // println("${sentence.start.toInteger()} to ${sentence.end.toInteger()} offset is ${sentence.offset.toInteger()} text is ${sentence.text}");
                   // println("offsets greater than sentence length, the word is ${word}");


                    result = false;
                }

            }
            else    //wordlength <= 0
            {
                result = false;
            }

        }

        return result;

    }

    /**
     * if offsets are wrong tries to correct them or just returns the same sentence unchanged
     * @param sentence
     * @return
     */
    org.anc.wn.Sentence correctOffsets(org.anc.wn.Sentence sentence, String word)
    {

        return sentence;

    }

    String cleanSentence(String sentence)
    {
        String newSentence;
        newSentence = sentence;

        newSentence = newSentence.minus('<b>');
        newSentence = newSentence.minus('</b>');



        return newSentence;

    }


    String getParentDir(String filePathString)
    {
        String upOneDir = "";
        def pathArray = filePathString.split(File.separator);

        println pathArray;
        if (pathArray.size() > 1)
        {
           upOneDir = pathArray[pathArray.size() -2 ];
        }
        println " upOneDir is ${upOneDir}";
       return upOneDir;





    }

    File findFile(File root, String targetFile)
    {
        // def pathStringArray = []
        // def fileStringArray = []
        // def targetFileString = []
        //root.eachFileMatch(~/.*\.hdr/)
        root.eachFile
                { file ->
                    if (file.isFile())
                    {
                        //logger.debug("checking ${file} vs target file is ${targetFile}")
                        //do they match ? at least the first part of the file name...without the extension
                        //split up the path of this iterations file
                        def pathStringArray = file.toString().split(File.separator)
                        //get the size of the resulting array
                        int pathStringArraySize = pathStringArray.size()
                        //get the last item in the array; which is just the file name. and split that up to drop the extension
                        // println "pathStringArray is ${pathStringArray} and pathStringArraySize is ${pathStringArraySize}"
                        def fileStringArray = (pathStringArray[pathStringArraySize - 1]).split("\\.")
                        //get the file name only, the first elemetn in the array, throw away the extension
                        // println "fileStringArray is ${fileStringArray}"
                        def fileString = fileStringArray[0]
                        // print "file is ${file} but fileString is "
                        // println "$fileString"
                        def targetFileString = targetFile.toString().split("\\.")
                        // print "targetFileString is ${targetFile} but targetFileString is "
                        // println "$targetFileString"
                        if ((fileString[0]).equals(targetFileString[0]) &&
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
                        if (file.isDirectory())
                        {
                            findFile(file, targetFile)
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

    File findLocalGraf(String targetFileName)
    {
    }


    class Sentence implements Comparable<Sentence>
    {
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

    int getNewOffset(String sentenceText, int originalOffset, String word)
    {
        int newOffset;

        try
        {
            newOffset = originalOffset;

            if ((sentenceText.length() > 0) && (sentenceText.contains(word)))
            {

                newOffset = sentenceText.indexOf(word);

                //println "${word} not found at ${originalOffset} new offset is changed to ${newOffset}";
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
        println "    groovy ProcessSentences.original_groovy /path/to/data/ output/path cv|v  "
        println "     cv = do only cross validation set, output dir is /'wn-fn/' in word directory";
        println "     v = do NOT do cross validation set, output dir is /'wn/' in word directory"
        println()
    }

    static void main(args)
    {
        if (args.size() < 3)
        {
            usage()
            return
        }
        def proc = new ProcessSentences()
        //set input dir
        proc.root = new File(args[0])
        //set output dir
        proc.outDir = new File(args[1])
        proc.cv = false;

        if (args.size() >=3)
        {
            if(args[2].equals('cv'))
            {
                proc.cv = true;
            }



        }




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
                println "Output directory could not be : ${proc.outDir.path}"
                return
            }
        }

        proc.run()

       // final GroovyClassLoader classLoader = new GroovyClassLoader();
       // classLoader.parseClass("generateWordnetSentenceCorpusHeaders.groovy") ;

       // generateWordnetSentenceCorpusHeaders.run();

        //def headerMaker = new generateWordnetCorpusHeaders();
        //headerMaker.run();
    }
}


class Masc
{
    static final String rootString = '/Users/frankcascio/anc/corpora/masc/MASC-3.0.0';
    static final File root = new File(rootString)
    static final File header = new File(root, 'resource-header.xml')
    static final File data = new File(root, 'data')
}

class Oanc
{
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

