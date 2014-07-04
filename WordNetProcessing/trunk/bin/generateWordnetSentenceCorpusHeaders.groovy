import groovy.xml.MarkupBuilder

/**
 * Created by frankcascio on 2/25/14.
 */
class generateWordnetSentenceCorpusHeaders {
    File inputDir;
   // File outputDir;

    void run()
    {

          inputDir.eachFileRecurse()
        //inputDir.eachFileMatch(~/.*\wn.xml/)
        {  currentFile ->

            if( (currentFile.name.endsWith('wn.xml'))  && (!currentFile.name.endsWith('s-wn.xml')) )
            {

                //String textFileName = inputDir.toString() + File.separator + currentFile.getName().toString();
                String textFileName =  currentFile.getParent() + File.separator + currentFile.getName().replaceAll('-wn.xml','.txt');

                println("textFileName is ${textFileName}");

                String wordCount = getWordCount(textFileName);


                //println "currentFile parent is ${currentFile.getParent()} and name is ${currentFile.getName().toString()}";
                createHeaderFile(wordCount, currentFile);
            }
        }
    }


    String getWordCount(String textFileName)
    {

        //execute the linux wordcount command "wc -w filename"
        def command = """wc -w ${textFileName}"""// Create the String ;
        def process = command.execute();
        process.waitFor();                    // Wait for the command to finish

        // Obtain status and output
        // println "return code: ${ process.exitValue()}" ;
        // println "stderr: ${process.err.text}" ;
        //println "stdout: ${process.in.text}" ;
        String output = "${process.in.text}";

        // println "output is ${output}";

        def (wordCount, value2) = output.tokenize(' ');

        //println "wordCount for ${textFileName} is ${wordCount} ... ${value2}";
        if (wordCount == null)
        {
            println "wordCount for ${textFileName} is ${wordCount}";

        }

        return wordCount;
    }

    void createHeaderFile(String wordCount, File xmlFile)
    {

        String xmlFileName = xmlFile.getName().toString();




        String headerFileName = xmlFileName.replaceAll("-wn.xml",".hdr");
        String sentenceXmlFileName= xmlFileName.replaceAll("-wn.xml","-s-wn.xml");
        String textFileName = xmlFileName.replaceAll("-wn.xml",".txt");
        String docId = "wn." + xmlFileName.replaceAll("-wn.xml","") ;
        docId = docId.replace("-",".") ;

        def outputFile = new File(xmlFile.getParent(), headerFileName);

        PrintWriter writer = new PrintWriter(outputFile);
        def xml = new MarkupBuilder(writer);

        xml.setDoubleQuotes(true);
        xml.mkp.xmlDeclaration(version: "1.0", encoding: "UTF-8");

        xml.documentHeader('xmlns:graf':"http//www.xces.org/ns/GRAF/1.0/",xmls:"http//www.xces.org/ns/GRAF/1.0/",docId:"${docId}",'date.created':"2014-03-01",creator:"fc",version:"1.0.0")
        {
            fileDesc()
            {
                titleStmt()
                {
                    title("${textFileName}");
                }
                extent(unit:"word",count:"${wordCount}") ;
                sourceDesc()
                {
                    title("${textFileName}");
                    publisher(type:"org","American National Corpus Project");
                    pubDate(value:"2014-03-01","March 01, 2014");
                    pubPlace("http://www.anc.org");
                }
            }
            profileDesc()
            {
                primaryData(loc:"${textFileName}", 'f.id':"f.text");
                annotations()
                {
                    annotation(loc:"${xmlFileName}", 'f.id':"f.wn");
                    annotation(loc:"${sentenceXmlFileName}",'f.id':"f.s");
                }

            }
        }

        writer.close()

        //return outputFile;

    }




public static void main(args)
{

    if (args.size() < 1)
    {
        println "usage: groovy generateWordnetCorpusHeaders /path/to/Wordnet_Sentence_Corpus /output/dir";
        return;
    }


    def headerMaker = new generateWordnetSentenceCorpusHeaders();

    headerMaker.inputDir = new File(args[0]);
    if (args.size() > 1)
    {
     //   headerMaker.outputDir = new File(args[1]);
    }


    if (!headerMaker.inputDir.exists())
    {
        println("input directory ${headerMaker.inputDir} does not exist");
        return;
    }

//    if (!headerMaker.outputDir.exists())
//    {
//        new File(headerMaker.outputDir.toString()).mkdir();
//        if (!headerMaker.outputDir.exists())
//        {
//            println("Could not create ${headerMaker.outputDir.toString()}");
//            return;
//        }
//    }


    headerMaker.run();
}


}