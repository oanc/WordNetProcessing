/**
 * Created with IntelliJ IDEA.
 * User: frankcascio
 * Date: 10/9/13
 * Time: 10:15 PM
 * To change this template use File | Settings | File Templates.
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




class xmlReaderWriterExample {

    File inputDir;
    File outputDir;
    Logger logger = LoggerFactory.getLogger(OrganizeToFiles);
    SentenceListParser parser = new SentenceListParser();  //need import
    SentenceListParser outputParser = new SentenceListParser();


    void run() {
        println "inputDir is ${inputDir}";
        println "outputDir is ${outputDir}";

        writeDummyFile(inputDir);

        inputDir.eachFileMatch(~/.*\.xml/) { currentFile ->

            println "The current input file is ${currentFile.toString()}"

            try {

                def everything = new XmlParser().parse(currentFile);

                def i =1 ;
                everything.sentences.s.each { s ->
                    println   "${i}"
                    println "${s.'@path'}"
                    println "${s.'@start'}"
                    println   "${s.'@end'}"
                    println   "${s.'@offset'}"
                    println   "${s.'@sid'}"
                    println   "${s.'@wn'}"
                    println   "${s.'@wnkey'}"
                    println   "${s.'@text'}"
                    i++;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    File writeDummyFile(File inputDirectory) {

        ArrayList<SentenceDesc> sentenceDescList = new ArrayList<SentenceDesc>();

        for (int i = 1; i < 6; i++) {
            SentenceDesc desc = new SentenceDesc();
            desc.path = "the_path${i}";
            desc.start = "the_start${i}";
            desc.end = "the_end${i}";
            desc.sStart = "the_sStart${i}";
            desc.offset = "the_offset${i}";
            desc.sid = "the_sid${i}";
            desc.wn = "the_wn${i}";
            desc.wnkey = "the_wnkey${i}";
            desc.text = "the_text${i}";
            //add this single item to this list, there will be only this here...
            sentenceDescList.add(desc);
        }

        def outputFile = new File(outputDir, "dummyFileOut.xml");

        PrintWriter writer = new PrintWriter(outputFile);
        def xml = new MarkupBuilder(writer);

        xml.setDoubleQuotes(true);
        xml.mkp.xmlDeclaration(version: "1.0", encoding: "UTF-8");

        println " sentence Description list size is ${sentenceDescList.size()}";
        //    sentenceDescList.each{ sd ->

        def int j = 0
       // println "xml list size is " + xml.list().size();
        xml.everthing(){
        xml.sentences()
                {
                    sentenceDescList.each
                            { sd ->
                                s(path:"${sd.path}",
                                  start:"${sd.start}",
                                  end:"${sd.end}",
                                  sStart:"${sd.sStart}",
                                  offset:"${sd.offset}",
                                  sid:"${sd.sid}",
                                  wn:"${sd.wn}",
                                  wnkey:"${sd.wnkey}",
                                  text:"${sd.text}")
                            }
                }
        }

        writer.close()

        return outputFile;
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

        }
        else {
            value = "";
        }

        return value;
    }



    public static void main(args) {

        if (args.size() != 2) {
            println "Usage: xmlReaderWriterExample /path/to/data output/dir"
            return
        }

        def reader = new xmlReaderWriterExample();

        reader.inputDir = new File(args[0]);
        reader.outputDir = new File(args[1]);

        if (!reader.inputDir.exists()) {
            println "inputDir ${reader.inputDir.toString()} not found";
            return;
        }

        if (!reader.outputDir.exists()) {
            new File(reader.outputDir.toString()).mkdir();
            if (!reader.outputDir.exists()) {
                println "Could not create output directory ${reader.outputDir.toString()}";
                return
            }
        }


        reader.run();


    }
}
