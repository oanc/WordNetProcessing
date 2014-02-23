/**
 *
 *
 *
 *
 *
 */

import groovy.io.FileType
@Grab('ch.qos.logback:logback-classic:1.0.0')
import org.slf4j.*

/**
 *
 */
class adjustHeaders {

    File inputDir;
    File outputDir;

    Logger logger = LoggerFactory.getLogger(adjustStandOffHeaders);

    void run() {

        //find the local masc files using the Masc.rootString below, make a map of the full paths
        def mascList = [:];
        mascList = getLocalMascFilePaths(Masc.rootString);

        //now find all the Oanc Files  make a second map for that
        def oancList = [:];
        oancList = getLocalMascFilePaths(Oanc.rootString);

        println "Masc size on disk is ${mascList.size()} \nOanc size on disk is ${oancList.size()}";

        inputDir.eachFileMatch(~/.*\wn.xml/) { currentFile ->


            String headerFileName = getHeaderFileNameFromPath(currentFile.toString());

            //print "The current input file is ${currentFile.toString()} and the corresponding header name would be ${headerFileName} ";

            String fullPath;
            String outXML = "";
            if (mascList.containsKey(headerFileName))
            {
                fullPath = mascList[headerFileName];
                outXML = addWNtoHeaderFile(fullPath,currentFile);
                //println "found in the masc ... ${fullPath}";
            }
            else
            {
               // headerFileName = headerFileName.replaceAll(".hdr", ".anc");
                if ((oancList.containsKey(headerFileName)))
                {
                    fullPath = oancList[headerFileName];
                    outXML = addWNtoHeaderFile(fullPath,currentFile);
                    //println "found in the oanc ...  ${fullPath}";

                }
                else
                {
                    println "${headerFileName} is not found in masc or oanc";
                    outXML = "";
                }

            }

            if (outXML != "")
            {
                File outFile = new File(outputDir, headerFileName);
                println "printing to ${outFile}";
                try
                {
                    outFile.write(outXML);
                }
                catch (Exception e)
                {
                    println "could not write outXML to file ${outFile.absoluteFile().toString()}";

                }

            }

        }

    }

    /**
     *  addWNtoHeaderFile
     * @param HDRFileName
     * @param currentFile
     * @return
     */
    String addWNtoHeaderFile(String HDRFileName,File currentFile)
    {
        println "The current input file is ${HDRFileName}" ;
        File headerFile = new File(HDRFileName);
        String wnFileName = currentFile.name.toString();

        String toAdd = '''<annotation> wn="wordnetFile.xml" </annotation>'''  ;

        //one of the false properties is to not use namespace graf: in every section
        def root = new XmlSlurper(false,false).parse( headerFile ) ;

        root.profileDesc.annotations.appendNode{
            annotation(loc:wnFileName, 'f.id':'f.wn');       //quotes around f.id just to escape the .

        }

        //def root = new XmlSlurper( false, false ).parse( headerFile ) ;
        //def fragmentToAdd = new XmlSlurper( false, false ).parseText( toAdd ) ;

        // Insert this new node at position 0 in the children of the first coreEntry node
        //root.find { it.name() == 'annotations' }.children().add( 0, fragmentToAdd )

        String outXml = groovy.xml.XmlUtil.serialize( root ) ;
        //println outXml;

        return outXml;

    }

    /**
     * given a path gets the file name replaces the extenstion with .hdr
     * @param pathString
     * @return
     */
    String getHeaderFileNameFromPath(String pathString) {
        def FilePathArray = pathString.split(File.separator);
        //what is the original masc file name ( without the path ) !?
        String targetFileNameWithExtension = FilePathArray.last();

        def FileNameArray = targetFileNameWithExtension.tokenize('.');

        //ok here is the original masc file name
        String targetFileName = FileNameArray[0] + ".hdr";

        targetFileName = targetFileName.replaceAll("-wn.hdr", ".hdr");

        return targetFileName;

    }



    /**
     * given Oanc or Masc root ( as a string ) return a map of the full paths of all the .hdr or .anc files
     * @param rootString
     * @return
     */
    Map getLocalMascFilePaths(String rootString) {
        //find the local masc files using the Masc.rootString below, make a map of the full paths
        def mascList = [:];
        def mascDir = new File(rootString);
        mascDir.eachFileRecurse(FileType.FILES)
                { file ->
                    if (file.toString().contains(".hdr") || file.toString().contains(".anc"))
                    {
                        if (mascList.containsKey(file.name.toString()))
                        {
                            //do nothing
                        } else {
                            mascList[file.name.toString()] = file;
                        }
                    }
                }
        return mascList;
    }

}


public static void main(args) {

    if (args.size() != 2) {
        println "Usage: groovy adjustStandOffHeaders.groovy /path/to/standOffFiles output/dir";
        return;
    }


    def tweaker = new adjustHeaders();


    tweaker.inputDir = new File(args[0]);
    tweaker.outputDir = new File(args[1]);

    if (!tweaker.inputDir.exists())
    {
        println "input Directory ${tweaker.inputDir.toString()} not found";
        return;
    }

    if (!tweaker.outputDir.exists())
    {
        new File(tweaker.outputDir.toString()).mkdir();
        if (!tweaker.outputDir.exists())
        {
            println "Could not create output directory ${tweaker.outputDir.toString()}";
            return;
        }

    }


    tweaker.run();


}


class Masc {
    static final String rootString = '/Users/frankcascio/anc/corpora/masc/MASC-3.0.0';
    static final File root = new File(rootString);
    static final File header = new File(root, 'resource-header.xml');
    static final File data = new File(root, 'data');
}


class Oanc {
    static final String rootString = '/Users/frankcascio/anc/corpora/OANC-1.2b1';
    static final File root = new File(rootString);
    static final File header = new File(root, 'resource-header.xml');
    static final File data = new File(root, 'data');


}