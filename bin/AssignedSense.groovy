@Grab('ch.qos.logback:logback-classic:1.0.0')
import org.slf4j.*

@Grab('org.tc37sc4.graf:graf-api:1.2.2')
import org.xces.graf.api.*
@Grab('org.tc37sc4.graf:graf-impl:1.2.2')
import org.xces.graf.impl.*


class AssignedSense
{
   static final Logger logger = LoggerFactory.getLogger(AssignedSense)
   String annotator
   String round
   String part
   String sense

   String toString()
   {
      return "${annotator} ${round} ${part} ${sense}"
   }
   
   IAnnotation createAnnotation(id)
   {
         IAnnotation a = Factory.newAnnotation(id, 'wordnet')
         addFeature(a, 'annotator', annotator)
         addFeature(a, 'round', round)
         addFeature(a, 'part', part)
         addFeature(a, 'sense', sense)
         return a
   }
   
   void addFeature(IAnnotation a, String name, String value)
   {
      if (value == null)
      {
         logger.error("Feature value for {} is null", name)
         value = "null"
      }
      
      IFeature f = Factory.newFeature(name, value)
      a.addFeature(f)
   }
}

