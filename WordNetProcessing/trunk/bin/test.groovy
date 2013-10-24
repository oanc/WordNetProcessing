import groovy.xml.MarkupBuilder

StringWriter writer = new StringWriter()
def xml = new MarkupBuilder(writer)
xml.root() {
	element(att:'value') {
		child1('value')
		child2('value')
	}
}

println writer.toString()