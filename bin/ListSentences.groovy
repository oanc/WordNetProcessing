package org.anc.masc.wordnet

@Grab('org.anc.wn:SentenceList:1.2.2-SNAPSHOT')
import org.anc.wn.*

@Grab('org.anc.osgi:wordnet:2.0.0-SNAPSHOT')
import org.anc.wordnet.api.*

@Grab('org.anc:common:3.0.0')
import org.anc.util.SimpleBuffer

/*
 * Scans the Wordnet data directories and copies the XML files from all annotators
 * into a new directory.
 * >groovy ListSentences.groovy ../data ../words
 */
class ListSentences
{
	File root
	File outputDir
	
	public ListSentences(File root, File outputDir)
	{
		this.root = root
		this.outputDir = outputDir
	}
	
	void run()
	{
		List empty = []
		List invalid = []
		Map files = [:]
		SentenceListParser parser = new SentenceListParser()
		root.eachDirRecurse { dir ->
			dir.eachFileMatch(~/.*\.xml/) { file ->
				try 
				{
					SentenceList list = parser.parse(file)
					SenseKeyIndex index = list.getSenseKeyMap()
					if (index.size() == 0)
					{
						empty << file
					}
					else
					{
                  //split up the path, since there are two kinds of paths; eight directories deep or six directories deep
						def parts = file.path.split("/")
						//println "Parts is ${parts};
                  //create a new annotator object
						AnnotatorInfo info = new AnnotatorInfo()
                  
                  //set the annotatorinfo size to the size of the SentenceList parsed from the sentence List parser
						info.size = list.size()
                  //ok, what do if the path is eight directories deep or six directories deep
						switch (parts.size())
						{
							case 8:
								//../data/round-6/part3/branches/brubin/trunk/add-v.xml 6 8
                        //get round number from the path; second place
								info.round = parts[2]
                        //get the part number from the path; third place
								info.part = parts[3]
                        //what is the annotators name; fifth part
								info.annotator = parts[5]
								break
							//case 7:
							//	break
							case 6:
								//../data/round-6/cv/a4/window-n.xml 8 6
                        //get round number from the path; second place
								info.round = parts[2]
                        //get the part number from the path; third place
								info.part = parts[3]
                        //what is the annotators name; fourth part
								info.annotator = parts[4]
								break
							//case 5:
							//	break
							default:
                        //wait, don't know about this path
                     
								println "Unhandled path size: ${path.size()} ${path}"
								break

						}
						//println "${file.name} ${round} ${part} ${annotator}"
						
						/*
						def alist = files[file.name]
						if (alist == null)
						{
							alist = []
							files[file.name] = alist
						}
						alist << info
						*/
						//create the name of the output file
						String name = "${info.annotator}_${info.round}_${info.part}.xml"
                  //create a new dir based on the output Dir and the filename of the original xml file
						File newDir = new File(outputDir, file.name.replace('.xml', ''))
						if (!newDir.exists())
						{
							if (!newDir.mkdirs())
							{
								throw new IOException("Unable to create ${newDir.path}")
							}
						}
                  //make the newFile from the name above
						File newFile = new File(newDir, name)
                  //get the text from the old file
						String text = file.getText('UTF-8')
						println "Writing ${newFile.path}"
                  //put the text from the old file, into the new file
						newFile.setText(text, 'UTF-8')
					} 
				}
				catch (Exception e)
				{
					invalid << "${file.path} ${e.message}"
				}
			}
		}
		files.each { name, list ->
			println "${name}"
			list.each { info ->
				println "\t${info.annotator} ${info.round} ${info.part} ${info.size}"
			}
		}
		//println()
		//println "Empty files:"
		//empty.each { println it.path }
		//println()
		//println "Invalid files: ${invalid.size()}"
	}
	
	static void usage()
	{
		println()
		println "USAGE"
		println "    groovy ListSentences.groovy /path/to/root/data/ /path/to/output/"
		println()
	}
	
	static void main(args)
	{
		if (args.size() != 2)
		{
			usage()
			return
		}
		File dataDir = new File(args[0])
		if (!dataDir.exists())
		{
			println "Data directory not found: ${dataDir.path}"
			return
		}
		File outputDir = new File(args[1])
		if (!outputDir.exists())
		{
			println "Output directory not found: ${outputDir.path}"
			return
		}
		new ListSentences(dataDir, outputDir).run()
	}
}

class AnnotatorInfo
{
	String annotator
	String round
	String part
	int size	
}
