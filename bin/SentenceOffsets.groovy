@Grab('org.tc37sc4.graf:graf-api:1.2.0')
import org.xces.graf.api.*

@Grab('org.tc37sc4.graf:graf-io:1.2.0')
import org.xces.graf.io.*
import org.xces.graf.io.dom.*

@Grab('org.tc37sc4.graf:graf-util:1.2.0')
import org.xces.graf.util.*

class Masc {
	static final File root = new File('/Users/frankcascio/anc/corpora/masc/MASC-3.0.0')
	static final File header = new File(root, 'resource-header.xml')
	static final File data = new File(root, 'data')
}

class Oanc {
	static final File root = new File('/Users/frankcascio/anc/corpora/OANC-1.2b1')
	static final File header = new File(root, 'OANC-corpus-header.xml')
	static final File data = new File(root, 'data')
}

File testFile = new File('/Users/frankcascio/anc/corpora/OANC-GrAF/data/written_2/travel_guides/berlitz1/WhatToHongKong-s.xml');
//File testFile = new File(Oanc.data, 'written_1/journal/slate/30/ArticleIP_1840-s.xml')

ResourceHeader header = new ResourceHeader(Oanc.header)
GrafParser parser = new GrafParser(header)
IGraph graph = parser.parse(testFile)

def sentences = []

graph.nodes().each { node ->
	if (node.annotation.label == 's')
	{
		IRegion span = GraphUtils.getSpan(node)
		Sentence s = new Sentence()
		s.id = node.id
		s.start = span.start.offset
		s.end = span.end.offset
		sentences << s		
	}
}

sentences.sort().each { println it }

class Sentence implements Comparable<Sentence> {
	String id
	int start
	int end
	
	String toString()
	{
		return "${id}\t${start}\t${end}"
	}
	int compareTo(Sentence sen)
	{
		return start - sen.start
	}
}
