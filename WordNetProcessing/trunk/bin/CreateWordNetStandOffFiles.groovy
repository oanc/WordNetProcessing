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
        mascList = getLocalMascFilePaths(Masc.rootString);

        //now find all the Oanc Files  make a second map for that
        def  OancList = [:];
        OancList =   getLocalMascFilePaths(Oanc.rootString);

        println "Masc size on disk is ${mascList.size()} \nOanc size on disk is ${OancList.size()}";

        File targetFile = null;

        //starting in root and cycle through all constructed files
        root.eachFileMatch(~/.*\.xml/) {file->         //go go go
            logger.info( " now processing..   ${file.path}" );
            logger.info (" ${file.path} exists ? ${file.exists()}");

            def nodeRegionList = [];



            //SentenceList sentenceList = parser.parse(file.path) ;

            try{
                //get the file name from the path ( change the extenstion to .hdr for later )
                String targetFileName = getFileNameFromPath (file.path.toString());

                //look for that in the masc map first
                targetFile = mascList[targetFileName];

                if(targetFile == null)     //if file not found in masc list
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

                    logger.info("Getting local graph ${targetFile}");
                    IGraph graphFromFile = getLocalGraph(targetFile) ;

                    //parse the transitional xml file
                    def list = new XmlParser().parse(file);
                    def sentenceDescMap = [:];

                    //list, sentences, and s come from the xml file, they are not objects
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
                        sen.annotator = "${s.'@annotator'}";
                        //new files should have the annotator !!

                       // println  " ${sen.path}   ${sen.start}   ${sen.end}   ${sen.wnkey}   ${sen.annotator}";

                        //sentenceDescList.add(sen);

                        def sentenceNumber = 1;

                        //lets go through all the regions in this local graf file, and find the region
                        //that matches the start and stop of the word that we are using now...

                        try{

                        graphFromFile.regions().each{region->

                            sentenceNumber++;
                            def regionStart = Integer.parseInt("${region.anchors[0]}");
                            def regionEnd   = Integer.parseInt("${region.anchors[1]}");
                            def sentenceStart = Integer.parseInt("${sen.start}");
                            def sentenceEnd = Integer.parseInt("${sen.end}");

                            //if we find something...cool, now throw into our map
                            if ((regionStart == sentenceStart) &&
                                    (regionEnd   == sentenceEnd  ))
                            {
                                region.nodes().each{ node ->

                                    // def id = node.getId();
                                    // println "node id is ${node.getId()}";

                                    if (node.getId().toString().contains("penn-"))
                                    {
                                        //println "penn node id is ${node.getId()}";
                                        def senId = "s" + sentenceNumber.toString();

                                        //now put the sentence Object thingy in the map with the useless key
                                        sentenceDescMap[senId] = sen;
                                        def pennNodeRegionPair = new nodeRegionPair(node,region,sen);
                                        //   def pair = new nodeRegionPair();
                                        //  pair.node = node;
                                        //  pair.region = region;
                                        nodeRegionList.add(pennNodeRegionPair);

                                    }
                                    //make a useless key, well not completely useless, just to keep things unique
                                }
                            }
                        }

                        }
                        catch (Exception e)
                        {

                            println "Exception on cycling through regions of graph ${sen.path}\n"
                            e.printStackTrace();
                        }
                    }   //end of this xml file's sentences

                }//end of this original graph file was found in masc or oanc locally
                IGraph graph = createGraph(nodeRegionList);//,file.getName());

                String outFileName = file.getName().replaceAll('.xml', '-wn.xml');
                File outFile = new File(outDir, outFileName);        //need to name things -wn.xml TODO
                print "outFile is ${outFile.toString()}";
                rendertheGraph(outFile,graph);

            }
            catch(SAXException e)
            {
                println "SAXException in file ${file}";

            }
            catch (FileNotFoundException e)
            {
                println ">>>>woops! during working with ${targetFile.name} got this FileNotFoundException message >>>  ${e.message}" ;
            }
        }    //end of this dir's files
    }

    /**
     * given Oanc or Masc root ( as a string ) return a map of the full paths of all the .hdr or .anc files
     * @param rootString
     * @return
     */
    Map getLocalMascFilePaths(String rootString)
    {
        //find the local masc files using the Masc.rootString below, make a map of the full paths
        def mascList = [:];
        def mascDir = new File(rootString);
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

        return mascList;
    }

    /**
     * given a path gets the file name replaces the extenstion with .hdr
     * @param pathString
     * @return
     */
    String getFileNameFromPath(String pathString)
    {
        def FilePathArray =  pathString.split(File.separator);
        //what is the original masc file name ( without the path ) !?
        String targetFileNameWithExtenstion = FilePathArray.last();
        File targetFile = null;

        def FileNameArray = targetFileNameWithExtenstion.tokenize('.');

        //ok here is the original masc file name
        String targetFileName = FileNameArray[0] + ".hdr";

        return targetFileName;


    }

    /**
     * renders the graph to the output file
     * @param outputFile
     * @param graph
     */
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
        GrafParser grafParser = new GrafParser(header);                  //for a single stand-off file use parser use the GrafParser
        GrafLoader grafLoader = new GrafLoader(header);                  //for loading a full graph with all dependencies use the GrafLoader
        //here is where you could use the set Types method so the loader only picks up the types that we want
        String[] listofTypesAsStringArray = ["f.s", "f.penn"];
       // grafLoader.setTypes(listofTypesAsStringArray[]);


        IGraph graphFromFile   = null;

        if (localFile != null)
        {

          //  println "Trying to get graph from file ${localFile}";

            try
            {
                graphFromFile = grafLoader.load(localFile);
            }

            catch(GrafException e)
            {
                println "GrafException with file ${localFile}";

            }

        }
        return graphFromFile ;
    }


    IGraph createGraph(List noderegionList)//,String dependency)
    {
        IDGenerator id = new IDGenerator();
        IGraph graph = Factory.newGraph();
        graph.addAnnotationSpace(space);
        IStandoffHeader header = graph.getHeader();
        println "Creating graph";
       // ArrayList headerFiles = new ArrayList();


        noderegionList.each { pair ->

           // println pair.getNode().getId().toString();
           // println pair.getRegion().getId().toString();
           // println pair.getSentenceDesc().text;

            //make the words Node,
            INode wordNode = Factory.newNode(id.generate('wn-n')) ;



            // need to use id generator
            IEdge edge = Factory.newEdge(id.generate('e'),wordNode,pair.getNode());
            //println "New edge is ${edge.getId()} from the fromNode ${edge.getFrom().getId()} and to the toNode  ${edge.getTo().getId()}";

            //add the edge to the graph, not the nodes..
            graph.addEdge(edge);

            //make the annotation to go with new wordNode
            IAnnotation wordAnnotation = Factory.newAnnotation(id.generate('a'), "sense");
            //make the annotation part of the annotation space for wordnet
            wordAnnotation.setAnnotationSpace(space);

            //now for the features
            wordAnnotation.addFeature('sense-key',pair.getSentenceDesc().wnkey);
            wordAnnotation.addFeature('annotator',pair.getSentenceDesc().annotator);
            //wordAnnotation.addFeature('path',pair.getSentenceDesc().path);

            String originalMascPath = pair.getSentenceDesc().path;
            String dependency = getFileNameFromPath(originalMascPath);

            //println "dependency is ${dependency}" ;

            dependency = dependency.replaceAll(".anc","-hepple.xml");
            dependency = dependency.replaceAll(".hdr","-penn.xml") ;

           // println "dependency is now ${dependency}" ;

            List<String> dependsOn = header.getDependsOn();

            if(dependsOn.contains(dependency))
            {

            }
            else
            {
                header.addDependency(dependency);
            }

          //  dependsOn.each{ depends->
          //      println "already depends on ${depends}";
         //   }





             //connect annotation to the node
             wordNode.addAnnotation(wordAnnotation);



            //add the node to the graph
            graph.addNode(wordNode);




        }



      //  logger.debug("Trying to add dependency file name ${dependency}");
       // header.addDependency(dependency);


        return graph


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
            wordAnnotation.addFeature(id.generate('f'),senDescription.annotator);

            //need to create an edge to the token node



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
            return;
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


class nodeRegionPair implements Comparable{

    INode node;
    IRegion region;
    SentenceDesc sent;

    nodeRegionPair(INode nodeIn, IRegion regionIn, SentenceDesc sentIn){

        node = nodeIn;
        region = regionIn;
        sent = sentIn;


    }

    public INode getNode()
    {
        return node;
    }

    public IRegion getRegion()
    {
        return region;
    }

    public SentenceDesc getSentenceDesc()
    {
        return sent;
    }

    //need to impliment a int compareTo(Object o)
    //that compares the region start and stop
    int compareTo(Object o)
    {
        int rsp =1 ;
        if ((region.getStart() == (IRegion)o.getStart()) &&
            (region.getEnd()   == (IRegion)o.getEnd())   )
        {
             rsp = 0;
        }
        else
        {
            if ((region.getStart() > (IRegion)o.getStart()) &&
                (region.getEnd()   > (IRegion)o.getEnd())   )
            {
                rsp = 1;
            }
            else
            {
                rsp = -1;
            }
        }

        return rsp;

    }



}