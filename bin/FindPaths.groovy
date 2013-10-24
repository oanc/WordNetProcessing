
class FindPaths
{
	//File allPaths = new File('all-paths.txt')
	//File outFile = new File('all-paths-index.txt')
	
	File input
	File output
	
	//File masc1 = new File('/var/corpora/MASC-1.0.3')
	//File masc3 = new File('/var/corpora/MASC-3.0.0')
	//File oanc = new File('/var/corpora/OANC')
	File oanc = new File('/Users/frankcascio/anc/corpora')
	
	
	enum Type { MASC, OANC, UNKNOWN }
	
	MascIndex index
	
	int failed = 0
	int good = 0
	
	void run()
	{
		index = new MascIndex()	
		
		PrintWriter out = new PrintWriter(output)
		input.eachLine { line ->
			File file = resolve(line)
			if (file != null)
			{
				++good
				out.println "${line} ${file.path}"
			}
			else
			{
				++failed
			}
		}
		out.close()
		println "Failed: ${failed}"
		println "Good  : ${good}"
	}
	
	private File resolve(String path)
	{
		int start = path.indexOf('/data')
		if (start < 0)
		{
			return null
		}
		File file
		String name = path.substring(start).replace('.anc', '.txt')
		switch (getType(path))
		{
			case Type.MASC:
				File f = new File(name)
				//println "name: ${name}"
				println "file: ${f}"
				String indexed = index.get(f.name)
				if (indexed != null)
				{
					file = new File(indexed)
					//println "file from index iss: ${file}"
				}
				break
			case Type.OANC:
				file = new File(oanc, name)
				break
			case Type.UNKNOWN:
				break
		}
		if (file == null)
		{
			println "Unable to resolve ${path} : ${new File(name).name}"
		}
		else if (!file.exists())
		{
			println "File does not exist: ${file.path}"
		}
		
		return file
	}
	
	private Type getType(String path)
	{
		if (path.contains('OANC') || path.contains('oanc')) return Type.OANC
		if (path.contains('MASC') || path.contains('masc')) return Type.MASC
		return Type.UKNOWN
	}
	
	static void usage()
	{
		println()
		println "USAGE"
		println "    groovy FindPaths /all/paths.txt /path/to/output.txt"
		println()
	}
	
	static void main(args)
	{
		if (args.size() != 2)
		{
			usage()
			return
		}
		FindPaths app = new FindPaths()
		app.input = new File(args[0])
		app.output = new File(args[1])
		app.run()
	}
}