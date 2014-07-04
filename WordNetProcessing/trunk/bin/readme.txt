Scripts and usage

The original manually annotated sentences using the wordnet annotations can be found here
  http://www.anc.org/dev/masc/annotations/sense-tagging/
 Put these in a root directory called /sense-tagging/data

 All the scripts below should be in a directory called /sense-tagging/bin.
 The dir name 'sense-tagging' does not really matter, just make sure /bin and /data are in the same directory.


1. Run AllPaths.groovy
     Lists all files that have at least one sentence tagged.
      /sense-tagging/bin>groovy AllPaths.groovy ../data all-paths.txt

2. Delete ( or rename ) masc-index.txt if running on another machine or with old Masc dir structure. This needs to be re-created only as locations of Masc change

3. Update the Masc-index.groovy generateIndex() function to point to the local location of masc data

4. Run FindPaths.groovy
      /sense-tagging/bin>groovy FindPaths.groovy all-paths.txt all-paths-index.txt 

5.  Create a /words dir in the same dir as /data and run
    sense-tagging/bin>groovy ListSentences.groovy ../data ../words
    This fills /word with a directory for each word-sense ( like able-j ) and the /word/word-sense/ dir is filled with files like annotatior_round#_part.xml  
     ( like anfahmy_round-10_cv.xml)

6a. Run ProcessWordnetStandoff.groovy to create individual files in the /words dir that contain the sentences, the annotators, the word net sense key, etc
    ( it is a good idea to increase heap space here before running the script. Once started, go get lunch, this will take a while
      /sense-tagging/bin>export JAVA_OPTS="$JAVA_OPTS -Xmx1G" )
      /sense-tagging/bin>groovy ProcessWordnetStandoff.groovy ../words  ../WNSentenceCorpusDirectory
  Now you have the sentences listed in single files,  one file per word sense, to complete the 'wordnet sentence corpus' only the new headers are needed


7a. Run Create the new header files for the 'word net stand off files' .
      /sense-tagging/bin> groovy generateWordnetSentenceCorpusHeaders.groovy ../WNSenseTaggingCorpusDirectory ../WordNetSenseTaggingHeaders/
    This creates the wordnet sentence corpus headers, the outputDirectory can be the same as the input to put everything in the same place.


 This completes the sentence corpus generated from the sentences manually annotated with the wordnet sense annotations ( round 1, round2 etc )
   The final output directory will have file names that look something like this:
      able-j-wn.hdr    << wordnet corpus unique header
      able-j-wn.txt    << text file of all the sentences containing the word 'able' used as a 'j' ( adj ) that were manually annotated
      able-j-wn.xml    << graph file that contains the word regions where 'able' is in the text file and some other annotations identifying the annotator and wordnet sense etc
                        for example:

                              <node xml:id="w-n0">
                                  <link targets="w-r0"/>
                              </node>
                              <a xml:id="a1" label="wordnet" ref="w-n0">
                                  <fs>
                                      <f name="annotator" value="adelpriore"/>
                                      <f name="round" value="round-11"/>
                                      <f name="part" value="cv"/>
                                      <f name="sense" value="able%3:00:00::"/>
                                  </fs>
                              </a>


  Output on the anc svn server can be found here: https://www.anc.org/dev/WordNetSenseTagging/trunk/


  Now once the wordnet sentence corpus is done, continue to create the wordnet stand off files that will be incorporated in the oanc and masc.


6b. Run OrganizeToFiles.groovy to go through all those files and rearrange the information so there is one file per masc file. ie all annotations that are from file
    historygreek..hdr are all collected into one xml file. There might be only one or two word senses per original masc file, but that is ok… 
      /sense-tagging/bin>groovy OrganizeToFiles.groovy ../words ../byGrafFileName
    this produces a simple xml file with all the sentence object data intact, but one file per original masc graph. These are just temporary files.

    keep in mind there are certain annotators that are excluded, since they annotated to test the process only and the files that are of interest are the ones that are in the annotators' trunk directories…
    ...like /sense-tagging/data/round-1/cv/branches/adelpriore/trunk. Only those "round,annotators" files with the keys are used. i.e.
   <keys>      
     <key n="1" wn="development%1:04:01::"/>      
     <key n="2" wn="development%1:22:02::"/>
   </keys>
 
    This means the annotations were completed on this word. Now all the sentences are tied together in files based on the original graph file.

7b. Run CreateWordNetStandOffFiles to make a 'word net' stand off graph using the original masc graph ( like the -nc.xml or -vc.xml files) .
      /sense-tagging/bin>groovy CreateWordNetStandOffFiles.groovy ../byGrafFileName ../wordNetStandOffFiles

    This opens each 'byGraphName' temp file and searches locally to find the original .hdr file and loads the original graph,
    searches all the regions and find the one region with the start and end values that match the start and end value in the word net annotation sentence info.
    Once found, it saves the info to a map, once finished cycling through all the these sentences and the map is complete, it creates a new graph file.
    The new graph contains the nodes and regions that pertain to only those words annotated in the 'rounds' . The annotation is the word net sense key as the value is added to each node. 
    These stand-off files compliment the -nc.xml, -vc.xml, and -penn.xml files and can be re-incorporated back to the masc and oanc corpora

8b. Run adjustStandOffHeaders.groovy to get the masc or oanc headers and add the new annotation to refer to the new "-wn.txt" Stand-off files. You will need to merge the output back
   to the original masc or oanc directories
    /sense-tagging/bin>groovy adjustStandOffHeaders.groovy ../WordNetStandOffFiles ../finalOutputDir

    This file also recreates the local directory of the masc and oanc files in the final OutputDir for easy insertion into the masc and oanc

    Output on the anc server for this can be found here: https://www.anc.org/dev/WordNetProcessing/trunk/WordNetStandOffFilesWithNewHeaders




