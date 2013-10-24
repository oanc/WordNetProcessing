
/*
 * Returns the full path to a file given its filename.
 */
class MascIndex
{
	private static final File INDEX = new File("masc-index.txt")
	
	def index = [:]
	
	public MascIndex()
	{
		if (INDEX.exists())
		{
			loadIndex()
		}
		else
		{
			generateIndex()
		}
	}
	
	String get(String name)
	{
		return index[name]
	}
	
	private void loadIndex()
	{
		INDEX.eachLine { line ->
			def parts = line.split()
			index[parts[0]] = parts[1]
		}
	}
	
	private void generateIndex()
	{
		def paths = [
		     '/Users/frankcascio/anc/masc/MASC-3.0.0-RC2/data'
		]
		
		PrintWriter out = new PrintWriter(INDEX)
		paths.each {
			File corpus = new File(it)
			corpus.eachDirRecurse { dir ->
				dir.eachFileMatch(~/.*\.txt/) { file ->
					index[file.name] = file.path
					out.println "${file.name}\t${file.path}"
				}
			}
		}
		out.close()
	}
}


//		def paths = [
//		     '/Users/frankcascio/anc/masc/MASC-3.0.0-RC2/data'
//           //'/var/corpora/MASC-1.0.3/data',
//			//'/var/corpora/MASC-3.0.0/data'
//		]