package org.anc.masc.wordnet

class CompileSentenceFiles
{

  File input
  File output
 
  static void main(args)
  {
     if(args.size() !=2)
     {
      usage()
      return
     }
   
     CompileSentenceFiles app = new CompileSentenceFiles()
     app.input  = new File(args[0])
     app.output = new File(args[1])
     app.run()

}
 
 static void usage()
 {
     println()
     println "Usage"
     println "groovy CompileSentenceFiles /inputDir /outputDir"     
 }
 
  void run()
  {
      println input.path
      
      input.eachFileMatch(~/.*\.txt/) { file ->
	  println file.path
		try
		{
			
		}
		catch (Exception e)
		{
			println e.message
		}
   }

 
 
  }
 
 
 
}