

File directory = new File('../words')
def list = []
int missingStart = 0
int missingSStart = 0
int missingOffset = 0

XmlParser parser = new XmlParser()
directory.eachDir { dir ->
	dir.eachFileMatch(~/.*\.xml/) { file ->
		println "Parsing ${file.path}"
		println "Parsing ${file.path}"
		Record record = new Record();
		record.word = file.parentFile.name
		record.path = file.name
		def xml = parser.parse(file)
		record.keys = xml.keys.key.size()
		xml.s.each { s ->
			record.start += check(s, 'start')
			record.sStart += check(s, 'sStart')
			record.offset += check(s, 'offset')
		}
		if (record.start > 0)
		{
			++missingStart
		}
		if (record.sStart > 0)
		{
			++missingSStart
		}
		if (record.offset > 0)
		{
			++missingOffset
		}
		//index[file.parentFile.name + '/' + file.name] = record
		list << record
	}
}

println "Missing start: ${missingStart}"
println "Missing sStart: ${missingSStart}"
println "Missing offset: ${missingOffset}"

PrintWriter writer = new PrintWriter(new File('Offsets.csv'))
list.each { 
	writer.println it 
	//println it
}
writer.close()
println "Done."
return

int check(s, att)
{
	def value = s.attribute(att)
	if (value == null || value == "" || value == '-1')
	{
		return 1
	}
	return 0
}

class Record 
{
	String word
	String path
	int keys
	int start
	int sStart
	int offset
	
	String toString()
	{
		return "${word},${path},${keys},${start},${sStart},${offset}"
	}
}