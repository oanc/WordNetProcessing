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
import org.xces.graf.impl.Factory;
import org.xces.graf.impl.CharacterAnchor

@Grab('org.tc37sc4.graf:graf-api:1.2.0')// was 1.2.2
import org.xces.graf.api.*;

import groovy.io.FileType;

import org.xml.sax.SAXException;
import groovy.xml.MarkupBuilder;

//first import the right stuff
import org.xces.graf.io.GrafRenderer;





class CreateWordNetStandOffFiles {
    File root;
    File outDir;

    IAnnotationSpace space = Factory.newAnnotationSpace("wn","http://wordnet.princeton.edu/");

    SentenceListParser parser = new SentenceListParser();

    IAnnotationSpace annotationSpace = Factory.newAnnotationSpace("wn", "http://wordnet.princton.edu/");
    Logger logger = LoggerFactory.getLogger(CreateWordNetStandOffFiles);



    void run()
    {


        //find the local masc files using the Masc.rootString below, make a map of the full paths
        def mascList = [:];
        def mascDir = new File(Masc.rootString);
        mascDir.eachFileRecurse(FileType.FILES)
                {   file ->
                     if(file.toString().contains(".hdr")  ||
                         file.toString().contains(".anc"))
                     {
                         if(mascList.containsKey(file.name.toString()))
                         {
                             //do nothing
                         }
                         else
                         {
                             mascList[file.name.toString()] = file;
                         }
                     }

                }

        //now find all the Oanc Files  make a second map for that
        def  OancList = [:]
        def OancDir = new File(Oanc.rootString);
        OancDir.eachFileRecurse(FileType.FILES)
        {file ->
            if(file.toString().contains(".hdr") ||
              file.toString().contains(".anc"))
            {
                if(OancList.containsKey(file.name.toString()))
                {

                }
                else
                {
                    OancList[file.name.toString()] = file ;
                }
            }

        }


        logger.debug "Masc size on disk is ${mascList.size()} \nOanc size on disk is ${OancList.size()}";

        //starting in root cycle through all files

        root.eachFileMatch(~/.*\.xml/) {file->
            logger.info( "Processing ${file.path}" );

            //SentenceList sentenceList = parser.parse(file.path) ;

            try{

                def list = new XmlParser().parse(file);
                def sentenceDescMap = [:];

                list.sentences.s.each {s->

                    SentenceDesc sen = new SentenceDesc();

                    sen.path = "${s.'@path'}";
                    sen.start = "${s.'@start'}";
                    sen.end = "${s.'@end'}";
                    sen.offset = "${s.'@offset'}";
                    sen.sid = "${s.'@sid'}";
                    sen.wn = "${s.'@win'}";
                    sen.wnkey = "${s.'@wnkey'}";
                    sen.text = "${s.'@text'}";

                    println  "     ${sen.path} ${sen.start}   ${sen.end}    ${sen.wnkey}";

                    //sentenceDescList.add(sen);

                    //println "need to find ${sen.path} and ${sen.start}";
                    def FilePathArray =  sen.path.toString().split(File.separator);
                    String targetFileNameWithExtenstion = FilePathArray.last();
                    File targetFile = null;

                    def FileNameArray = targetFileNameWithExtenstion.tokenize('.');

                    String targetFileName = FileNameArray[0] + ".hdr";


                    //look in the masc map first
                    targetFile = mascList[targetFileName];

                    if(targetFile != null)
                    {
                        //move along nothing to see here
                    }
                    else
                    {
                        //not in masc map, look in Oanc map next
                        targetFile = OancList[targetFileName];
                        if (targetFile != null)
                        {
                            //not found in either map; so target File remains null
                        }

                    }

                   // println "targetFile Name is ${targetFileName.toString()}";

                    if(targetFile != null)
                    {

                        IGraph graphFromFile = getLocalGraph(targetFile) ;
                        def sentenceNumber = 1;

                        //lets go through all the regions in this local graf file, and find the region
                        //that matches the start and stop of the word that we are using now...
                        graphFromFile.regions().each{region->

                            sentenceNumber++;
                            def regionStart = Integer.parseInt("${region.anchors[0]}");
                            def regionEnd   = Integer.parseInt("${region.anchors[1]}");
                            def sentenceStart = Integer.parseInt("${sen.start}");
                            def sentenceEnd = Integer.parseInt("${sen.end}");


                            //if we find something...cool, now through into our map
                            if ((regionStart == sentenceStart) &&
                                (regionEnd   == sentenceEnd  ))
                            {

                                println ();
                                println "found this region ${region.id} with anchors of ${region.anchors[0]} and ${region.anchors[1]} and the sentence states it starts at ${sen.start} and ends at ${sen.end}";
 //   loger not working ??      logger.info("found this region ${region.id} with anchors of ${region.anchors[0]} and ${region.anchors[1]} and the sentence states it starts at ${sen.start} and ends at ${sen.end}");


                                 //here is where the magic happens
                             //   sentenceDescMap[sen.sid] = sen    ;

 //                               region.nodes().each {node ->
                                    //println "  ${node.getId()}";


 //                               }
                                //make a useless key, well not completely useless, just to keep things unique
                                def senId = "s" + sentenceNumber.toString();

                                //now put the sentence Object thingy in the map with the useless key
                                sentenceDescMap[senId] = sen;



                                //println();
                            }
                        }
                    }
                }   //end of this xml file's sentences

                //ok we now have a filled map, send it off to make a graph
        //        println "sentenceDescMap size is ${sentenceDescMap.size()}";
                IGraph graph = createGraph(sentenceDescMap);
                //graph.setContent(compiledText);    //this step is needed ?


                //ok...now to render the graph
                //create an out File
//                print "outDir is ${outDir.toString()}   ";
//                print "file is ${file.toString()}   ";
//                print "file Name is ${file.getName()}   ";



                File outFile = new File(outDir, file.getName());
                print "outFile is ${outFile.toString()}";
                rendertheGraph(outFile,graph);






            }
            catch(SAXException e)
            {
                println "SAXException in file ${file}";

            }
            catch (FileNotFoundException e)
            {
                println "FileNotFoundException for ${file}" ;
            }
        }    //end of this dir's files
    }


    void rendertheGraph(File outputFile, IGraph graph)
    {



        //make an output stream
        FileOutputStream outStream = new FileOutputStream(outputFile);

        //use stream to make a streamWriter
        OutputStreamWriter writer = new OutputStreamWriter(outStream, "UTF-8");

        //make the renderer
        GrafRenderer grafRenderer = new GrafRenderer(writer);

        //render the graf
        grafRenderer.render(graph);

        //close the renderer
        grafRenderer.close();

    }


    IGraph getLocalGraph(File localFile)
    {
        File headerFile = new File('/Users/frankcascio/anc/corpora/OANC-1.2b1/resource-header.xml');
        //File headerFile = new File('/Users/frankcascio/anc/corpora/masc/MASC-3.0.0/resource-header.xml');
        ResourceHeader header = new ResourceHeader(headerFile);
        GrafParser grafParser = new GrafParser(header);
        GrafLoader grafLoader = new GrafLoader(header);
        //here is where you could use the set Types method so the loader only picks up the types that we want
        String[] listofTypesAsStringArray = ["f.s", "f.penn"];
       // grafLoader.setTypes(listofTypesAsStringArray[]);


        IGraph graphFromFile   = null;

        if (localFile != null)
        {

          //  println "Trying to get graph from file ${localFile}";

            try {
                graphFromFile = grafLoader.load(localFile);
            }

            catch(GrafException e)
            {
                println "GrafException with file ${localFile}";

            }

        }
        return graphFromFile ;
    }

    IGraph createGraph(Map map)
    {
        IDGenerator id = new IDGenerator()
        IGraph graph = Factory.newGraph()
        graph.addAnnotationSpace(space)


 //       map.each{k,v->

 //           println "${k}:${v.start}";


        map.each { senId,senDescription ->


            //makes some nodes, regions, an annotation just for the sentence label s , attribute the annotation to a space
//            INode wordNode = Factory.newNode(id.generate('s-n'));
//            IRegion wordRegion = Factory.newRegion(id.generate('s-r'), senDescription.start, senDescription.end);
//            IAnnotation wordAnnotation = Factory.newAnnotation(id.generate('a'), "w");
//            wordAnnotation.setAnnotationSpace(space);

//            //now connect all the things we just made, set the node to the region,
//            wordNode.addRegion(wordRegion);
//            //add the sentence Annotation to the sentence node
//            wordNode.addAnnotation(wordAnnotation);
//            //add the node to the graph
//            graph.addNode(wordNode);
//            //add the region to the graph also...
//            graph.addRegion(wordRegion);

            //make the words Node,
            INode wordNode = Factory.newNode(id.generate('w-n')) ;
            //add the node to the graph
            graph.addNode(wordNode);


            int startInt = Integer.parseInt(senDescription.start);
            int endInt   = Integer.parseInt(senDescription.end);

            //make a new region just for the word
            IRegion wordRegion = Factory.newRegion(id.generate('w-r'), startInt, endInt);
            //add this words region to the graph
            graph.addRegion(wordRegion);
            //connect the region to the word's node
            wordNode.addRegion(wordRegion);


            //make the annotation
            IAnnotation wordAnnotation = Factory.newAnnotation(id.generate('a'), "w");
            //connect it to the node
            wordNode.addAnnotation(wordAnnotation);
            //make the annotation part of the annotation space for wordnet
            wordAnnotation.setAnnotationSpace(space);

            wordAnnotation.addFeature(id.generate('f'),senDescription.wnkey);
         //   wordAnnotation.addFeature(id.generate('f'),senDescription.text);



        }


        return graph
    }





    static usage()
    {

        println();
        println "USAGE:";
        println "   groovy CreateWordNetStandOffFiles.groovy /path/to/input/files  /path/to/output";
        println();

    }




    static void main(args)
    {
        if (args.size() != 2)
        {
            usage() ;
            return;

        }

        def go = new CreateWordNetStandOffFiles();
        //input dir
        go.root = new File(args[0]);
        //output dir
        go.outDir = new File(args[1]);


        if(!go.root.exists())
        {
            println "Input directory not found: ${go.root.path}";
        }
        //if output dir doest no exist
        if(!go.outDir.exists())
        {
            //then make it
            new File(go.outDir.toString()).mkdir();
            //if output Dir still does not exist
            if(!go.outDir.exists())
            {

                println "Output Directory could not be: ${go.outDir.path}";
                //then get out
                return;

            }
        }

        //if we are here, then start the run
        go.run();

    }

}


class Masc {
    static final String rootString =  '/Users/frankcascio/anc/corpora/masc/MASC-3.0.0';
    static final File root = new File(rootString);
    static final File header = new File(root, 'resource-header.xml');
    static final File data = new File(root, 'data');
    }


class Oanc {
    static final String rootString = '/Users/frankcascio/anc/corpora/OANC-1.2b1';
    static final File root = new File(rootString);
    static final File header = new File (root, 'resource-header.xml');
    static final File data = new File(root, 'data');


}