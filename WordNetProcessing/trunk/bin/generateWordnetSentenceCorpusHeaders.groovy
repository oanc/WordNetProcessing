import groovy.xml.MarkupBuilder

/**
 * Created by frankcascio on 2/25/14.
 */
class generateWordnetCorpusHeaders {
    File inputDir;
    File outputDir;

    void run()
    {
        inputDir.eachFileMatch(~/.*\wn.xml/)
        {  currentFile ->

            String currentFileName = inputDir.toString() + File.separator + currentFile.getName().toString();

            println ("currentFileName is ${currentFileName}");
            //execute the linux wordcount command "wc -w filename"
            def command = """wc -w ${currentFileName}"""// Create the String
            def process = command.execute()
            process.waitFor();                    // Wait for the command to finish

            // Obtain status and output
           // println "return code: ${ process.exitValue()}" ;
           // println "stderr: ${process.err.text}" ;
            //println "stdout: ${process.in.text}" ;
            String output = "${process.in.text}" ;

           // println "output is ${output}";

            def (wordCount, value2) = output.tokenize( ' ' );

            //println "values are ${wordCount} ${value2}";
            createHeaderFile(wordCount,currentFile.getName().toString());
        }
    }

    void createHeaderFile(String wordCount, String xmlFileName)
    {

        String headerFileName = xmlFileName.replaceAll(".xml",".hdr");
        String textFileName = xmlFileName.replaceAll(".xml",".txt");
        String docId = "wn." + xmlFileName.replaceAll("-wn.xml","") ;
        docId = docId.replace("-",".") ;

        def outputFile = new File(outputDir, headerFileName);

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
                primaryData(loc:"${textFileName}", 'f.id':"f.wn");
                annotations()
                {
                    annotation(loc:"${xmlFileName}", 'f.id':"f.wn")
                }

            }
        }

        writer.close()

        //return outputFile;

    }

}



public static void main(args){

    if (args.size() < 1)
    {
        println "usage: groovy generateWordnetCorpusHeaders /path/to/WordnetCorpus /output/dir";
        return;
    }


    def headerMaker = new generateWordnetCorpusHeaders();

    headerMaker.inputDir = new File (args[0])    ;
    if (args.size() >1 )
    {
        headerMaker.outputDir = new File (args[1]);
    }


    if (!headerMaker.inputDir.exists())
    {
        println("input director ${headerMaker.inputDir} does not exist");
        return;
    }

    if (!headerMaker.outputDir.exists())
    {
        new File(headerMaker.outputDir.toString()).mkdir();
        if(!headerMaker.outputDir.exists())
        {
            println("Could not create ${headerMaker.outputDir.toString()}");
            return;
        }
    }


    headerMaker.run();



}