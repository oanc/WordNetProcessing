
/**
 * Created with IntelliJ IDEA.
 * User: frankcascio
 * Date: 11/21/13
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */



 class renameWordNetFiles{


     File inputDir ;
     String suffix;


     void run() {

         inputDir.eachFile() { currentFile ->

//             print "The current file is ${currentFile.toString()}  ";

//             print "Path: ${currentFile.getParent().toString()}   ";

             def path = currentFile.getParent().toString();
             def name = currentFile.getName().toString();

//             print "Name: ${name}";
//             println "";

             name = name.replaceAll(".xml", "-" + suffix + ".xml");
             name = name.replaceAll(".txt", "-" + suffix + ".txt");

//             print "newName is $newName";

             def newFullName =  path+ "/" + name ;

             println "newFullName is $newFullName";

//             println "";

             currentFile.renameTo(new File(newFullName.toString()))  ;

//             println "currentFile is now ${currentFile.getName().toString()}";

//             return;

//             String oldFilename = "old.txt"
//             String newFilename = "new.txt"

//             new File(oldFilename).renameTo(new File(newFilename))



         }



     }











     public static void main(args){

         if (args.size() != 2){
             println "Usage: renameWordNetFiles /path/to/current/Files suffix"  ;
             return;
         }


         def rn = new renameWordNetFiles();

         rn.inputDir = new File(args[0]);
         rn.suffix = args[0];

         if(!rn.inputDir.exists()){
             println "inputDir ${rn.inputDir.toString{}} not found";
             return;
         }


         rn.run();


     }











 }












