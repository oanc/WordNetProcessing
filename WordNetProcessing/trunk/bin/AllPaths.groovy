package org.anc.masc.wordnet

/*
 * Lists all the unique path identifiers used in the sense tagged data.
 */
 
@Grab('org.anc.wn:SentenceList:1.2.2-SNAPSHOT')
import org.anc.wn.*

@Grab('org.anc.osgi:wordnet:2.0.0-SNAPSHOT')
import org.anc.wordnet.api.*

@Grab('org.anc:common:3.0.0')
import org.anc.util.SimpleBuffer

if (this.args.size() != 2)
{
	println "USAGE"
	println "    groovy AllPaths.groovy /path/to/data/directory/ /path/to/output/file.txt"
	return
}

File data = new File(this.args[0])
if (!data.exists())
{
	println "Unable to find the data directory: ${data.path}"
	return
}

File output = new File(this.args[1])
Set<String> paths = new HashSet<String>()
SentenceListParser parser = new SentenceListParser()
data.eachDirRecurse { dir ->
	dir.eachFileMatch(~/.*\.xml/) { file ->
		println file.path
		try
		{
			SentenceList list = parser.parse(file)
			list.each { paths << it.path }
		}
		catch (Exception e)
		{
			println e.message
		}
	}
}

File getFileForPath(String path)
{
	
}

PrintWriter out = new PrintWriter(output)
paths.each { out.println it }
out.close()
println "Done"

