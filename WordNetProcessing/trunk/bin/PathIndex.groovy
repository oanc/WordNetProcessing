
class PathIndex
{
	private static final File INDEX = new File('all-paths-index.txt')
	
	private def index = [:]
	
	public PathIndex()
	{
		if (INDEX.exists())
		{
			loadIndex()
		}
		else
		{
			println "File not found: ${INDEX.path}"
		}
	}
	
	String get(String key)
	{
		return index[key]
	}
	
	private void loadIndex()
	{
		INDEX.eachLine { line ->
			def parts = line.split()
			String key = parts[0]
			String value = parts[1]
			index[key] = value
		}
	}
	
}