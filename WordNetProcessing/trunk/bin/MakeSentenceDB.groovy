/*
 * Parses the sentence files for a GrAF corpus and generates text files containing
 * all the sentences with the start and end offsets.
 */
 
@Grab('org.tc37sc4.graf:graf-api:1.2.3-SNAPSHOT')
import org.xces.graf.api.*
@Grab('org.tc37sc4.graf:graf-io:1.2.3-SNAPSHOT')
import org.xces.graf.io.*
import org.xces.graf.io.dom.*
@Grab('org.tc37sc4.graf:graf-util:1.2.3-SNAPSHOT')
import org.xces.graf.util.*
import groovy.xml.MarkupBuilder

/*
 * Extracts the text from all sentences in the OANC (GrAF format) and writes them
 * out to new XML files. Includes the start and end offsets of each sentence.
 *
 */
class MakeSentenceDB
{
	void run()
	{
		File outputDir = new File('/var/corpora/OANC-Sentences')
		File corpus = new File('/var/corpora/OANC-1.2b1')
		if (!corpus.exists())
		{
			println "Can not find corpus at ${corpus.path}"
			return
		}
		
		File headerFile = new File(corpus, 'resource-header.xml')
		if (!headerFile.exists())
		{
			println "Could not locate the corpus resource-header.xml"
			return
		}
		
		ResourceHeader header = new ResourceHeader(headerFile)
		GrafParser parser = new GrafParser(header)
		File dataDir = new File(corpus, 'data')
		//println "Data directory is ${dataDir.path}"
		dataDir.eachDirRecurse { dir ->
			//println "Directory ${dir.path}"
			dir.eachFileMatch(~/.*-s\.xml/) { file ->
				//println "File ${file.path}"
				String textPath = file.path.replace('-s.xml', '.txt')
				File textFile = new File(textPath)
				if (!textFile.exists())
				{
					println "Can not find text file: ${textFile.path}"				
				}
				else
				{
					println "Processing ${textPath}"
					def sentences = []
					String content = textFile.getText('UTF-8')
					IGraph graph = parser.parse(file)
					graph.nodes().each { node ->
						IRegion region = GraphUtils.getSpan(node)
                        SentenceEntity s = new SentenceEntity()
						//Sentence s = new Sentence()
						s.start = region.start.offset.intValue()
						s.end = region.end.offset.intValue()
						s.id = node.annotation.getFeatureValue('id')
						if (s.end > content.length())
						{
							s.end = content.length()
						}
						if (s.end < s.start)
						{
							s.text("Invalid offsets ${region.start.offset.intValue()} ${region.end.offset.intValue()}")
						}
						else
						{
							s.text = content.substring(s.start, s.end)
						}
						sentences << s
					}
					sentences.sort()
					
					def newName = textFile.path.replace(dataDir.path, outputDir.path)
					newName = newName.replace('.txt', '.xml')
					File outputFile = new File(newName)
					File parent = outputFile.parentFile
					if (!parent.exists())
					{
						if (!parent.mkdirs())
						{
							throw new IOException("Unable to create ${parent.path}")
						}
					}
					println "Writing ${outputFile.path}"
					PrintWriter writer = new PrintWriter(outputFile)
					def xml = new MarkupBuilder(writer)
					xml.sentences() {
						sentences.each { s ->
							sentence(start:s.start, end:s.end, id:s.id, s.text)
						}
					}
					writer.close()
				}
			}
		}
		println "Done"
	}
	
	static void main(args)
	{
		new MakeSentenceDB().run()
	}
}

//change Sentence to SentenceEntity to get this to 'compile' in idea
class SentenceEntity implements Comparable<SentenceEntity >
{
	String id
	int start
	int end
	String text
	
	int compareTo(SentenceEntity  s)
	{
		return start - s.start
	}
}



/*     original Sentence Object
class Sentence implements Comparable<Sentence>
{
    String id
    int start
    int end
    String text

    int compareTo(Sentence s)
    {
        return start - s.start
    }
}
*/