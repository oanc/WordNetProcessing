/**
 * Created by frankcascio on 2/27/14.
 */



/**
 * Created by frankcascio on 2/27/14.
 */
class cycleDirectories {

    File inputDir;
    //File outputDir;

    void run()
    {

        inputDir.eachDirRecurse { dir ->
            println "----------- Dir: ${dir.path} ------------" ;
            dir.eachFile { file ->
           // dir.eachFileMatch(~/.*\.hdr/) { file ->

                println "${file.toString()}";

                //String fileContents = file.text;

               // if(fileContents.contains(".xml-"))
                //{
               //     println "${file.toString()}";

               // }






            }

        }

    }











    public static void main(args)
    {

        if(args.size() < 1)
        {
            println "Usage: groovy cycleThruDirectories /path/to/input "  ;
            return;
        }

        def go = new cycleDirectories();

        go.inputDir = new File(args[0]);
        //go.outputDir = new File(args[1]);

        if(!go.inputDir.exists())
        {
            println "Input Directory not found: ${go.inputDir}"
        }

//        if (!go.outputDir.exists())
//        {
//            new File(go.outputDir.toString()).mkdir();
//            //if the output directory still does not exist
//            if(!go.outDir.exists())
//            {
//                println "Output Directory could not be made: ${go.outputDir.path}"
//            }
//        }


        go.run();



    }

}