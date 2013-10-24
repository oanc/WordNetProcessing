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



class CreateWordNetStandOffFiles {
    File root;
    File outDir;

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
            println "Processing ${file.path}" ;

            //SentenceList sentenceList = parser.parse(file.path) ;

            try{

                def list = new XmlParser().parse(file);

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

                    //sentenceDescList.add(sen);

                    println "need to find ${sen.path}";
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

                    println "targetFile Name is ${targetFileName.toString()}";

                    if(targetFile != null)
                    {

                        IGraph graphFromFile = getLocalGraph(targetFile) ;
                        def i = 1;

                        graphFromFile.nodes().each { node ->

                            if (node.getId().toString().contains("penn")  )
                            {
                                println "${i}: ${node.getId()}";
                                i++;


                            }

                            //the nodes' spans and if they match the start and end of the sen object, then start adding
                            //annotations to that node...

                           // node.



                        }








                    }


                    println "path in ${file.path} is ${sen.path}";
                }

            }
            catch(SAXException e)
            {
                println "SAXException in file ${file}";

            }
        }
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

            println "Trying to get graph from file ${localFile}";

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