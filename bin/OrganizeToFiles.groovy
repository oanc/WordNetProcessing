/**
 * Takes MakeSentenceDB.groovy output of wordnet manually annotated files and consolidates all the annotations
 * to put all them in files based on the name of the original masc or oanc file.
 */


@GrabResolver(name = 'anc-releases', root = 'http://www.anc.org:8080/nexus/content/repositories/releases')


import org.slf4j.LoggerFactory
import groovy.xml.MarkupBuilder


@Grab('ch.qos.logback:logback-classic:1.0.0')
import org.slf4j.*

@Grab('org.anc.wn:SentenceList:1.2.2-SNAPSHOT')
import org.anc.wn.*

@Grab('org.anc.osgi:wordnet:2.0.0-SNAPSHOT')
import org.anc.wordnet.api.*

@Grab('org.anc:common:3.0.0')
import org.anc.util.*


@GrabResolver(name = 'anc-snapshots', root = 'http://www.anc.org:8080/nexus/content/repositories/snapshots')
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

import groovy.xml.XmlUtil
import groovy.xml.MarkupBuilder
import groovy.util.XmlParser;
import org.xml.sax.SAXException


class OrganizeToFiles {

    File inputDir
    File outputDir
    IAnnotationSpace space = Factory.newAnnotationSpace("wn", "http://wordnet.princeton.edu/");
    Logger logger = LoggerFactory.getLogger(OrganizeToFiles)
    SentenceListParser parser = new SentenceListParser()    //need import
    SentenceListParser outputParser = new SentenceListParser();

    void run() {

        //need to compile a map of the local masc files as before
        //need to compile a map of the local oanc files as before

        inputDir.eachDirRecurse() { subDir ->


            subDir.eachFileMatch(~/.*\.xml/) { currentFile ->


                println "The current file is ${currentFile.toString()}"
                def currentFileName = currentFile.getName().toString();

                def values = currentFileName.split('_');
                def annotatorFromFileName = values[0];
                println "annotator is ${annotatorFromFileName}";
                SentenceList slist = parser.parse(currentFile)
                SenseKeyIndex keyIndex = slist.getSenseKeyMap()



                slist.each { sentence ->

                    //println "Writing ${outputFile.path}"
                    //get the output file name; base it on the sentence.path; put it in the output directory
                    def pathParts = sentence.path.split(File.separator) ;
                    def newFileName = pathParts[pathParts.length - 1]  ;
                    newFileName = newFileName.replace(".anc", ".xml") ;
                    newFileName = newFileName.replace(".hdr", ".xml") ;

                    def outputFile = new File(outputDir, newFileName) ;

                    println " outputFile is ${outputFile.toString() } "    ;
                    ArrayList<SentenceDesc> sentenceDescList = new ArrayList<SentenceDesc>();

                    SentenceDesc desc = new SentenceDesc();
                    desc.path = sentence.path.toString();
                    desc.start = sentence.start.toString();
                    desc.end = sentence.end.toString();
                    desc.sStart = sentence.sentenceStart.toString();
                    desc.offset = sentence.offset.toString();
                    desc.sid = sentence.sid.toString();
                    desc.wn = sentence.WNSense.toString();
                    desc.wnkey = keyIndex.get(sentence.WNSense).toString();
                    desc.annotator = annotatorFromFileName;
                    desc.text = sentence.text.toString();


                    //add this single item to this list, there will be only this here...
                    sentenceDescList.add(desc);



                    //we already have this file, get the data out, prepare to recreate it with new data
                    if (outputFile.exists()) {
                        //load from the file first
                        //get all the sentences in a SentenceList and Sense Keys ( that area on the top of the file )

                        //println " outputFile ${outputFile.toString()} already exists";

                        // def i = 0;
                        try {
                            def list = new XmlParser().parse(outputFile);

                            def i = 1;
                            list.sentences.s.each { s ->

                                SentenceDesc sen = new SentenceDesc();

                                sen.path = "${s.'@path'}"     ;
                                sen.start = "${s.'@start'}"   ;
                                sen.end = "${s.'@end'}" ;
                                sen.offset = "${s.'@offset'}";
                                sen.sid = "${s.'@sid'}";
                                sen.wn = "${s.'@wn'}";
                                sen.wnkey = "${s.'@wnkey'}";
                                sen.annotator = "${s.'@annotator'}";
                                sen.text = "${s.'@text'}" ;




//                                println "${i}"          ;
//                                println "${s.'@path'}"  ;
//                                println "${s.'@start'}" ;
//                                println "${s.'@end'}"   ;
//                                println "${s.'@offset'}";
//                                println "${s.'@sid'}"   ;
//                                println "${s.'@wn'}"    ;
//                                println "${s.'@wnkey'}" ;
//                                println "${s.'@text'}"  ;
//                                i++;

                                sentenceDescList.add(sen);
                            }


                        }
                        catch (SAXException e) {
                            println " SAXException in file ${outputFile}";
                            //e.printStackTrace();
                        }
                    }

                    //file for this sentence not exists
                    else {

                       // println " first path on this file is ${desc.path} and sid is ${desc.sid}"
                    }

                    // println "outputFile new name will be ${outputFile.toString()}"

                    //Now we write the xml file

                    PrintWriter writer = new PrintWriter(outputFile)  ;
                    def xml = new MarkupBuilder(writer)        ;

                    xml.setDoubleQuotes(true);
                    xml.mkp.xmlDeclaration(version: "1.0", encoding: "UTF-8")   ;

                    //    sentenceDescList.each{ sd ->

                    def int j = 1   ;

                    xml.list() {
                        xml.sentences() {
                            sentenceDescList.each { sd ->
                                        s(path: "${sd.path}",
                                                start: "${sd.start}",
                                                end: "${sd.end}",
                                                sStart: "${sd.sStart}",
                                                offset: "${sd.offset}",
                                                sid: "${sd.sid}",
                                                wn: "${sd.wn}",
                                                wnkey: "${sd.wnkey}",
                                                annotator: "${sd.annotator}",
                                                text: "${sd.text}" )
                                       // println "j is ${j}";
                                      //  j++;
                                    }
                        }
                    }

                    writer.close()

                }

            }

        }


    }



    String getValue(String item) {
        def valueArray = item.split("value=");
        def size = valueArray.size();
        // println "length is " +val.length();

        def value = "";
        if (size > 0) {
            value = valueArray[size - 1];
            // println "value starts as "  + value;
            value = value.replaceAll("\\]", "");
            value = value.replaceAll("\\[", "");
            //println "value is " + value;

        } else {
            value = "";
        }

        return value;
    }






    public static void main(args) {

        if (args.size() != 2) {
            println "Usage: OrganizeToFiles /path/to/data /output/dir"
            return
        }

        def organize = new OrganizeToFiles();

        organize.inputDir = new File(args[0])
        organize.outputDir = new File(args[1])

        if (!organize.inputDir.exists()) {
            println "inputDir ${organize.inputDir.toString()} not found"
            return
        }


        if (!organize.outputDir.exists()) {
            new File(organize.outputDir.toString()).mkdir()
            if (!organize.outputDir.exists()) {
                println "Could not create output dir ${organize.outputDir.toString()}"
                return
            }
        }

        organize.run();

    }

}
