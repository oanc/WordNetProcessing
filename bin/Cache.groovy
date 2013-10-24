
import org.slf4j.*

/*
 * A simple LRU cache used to retrieve the text content of the files that
 * have been sense tagged.
 */
class Cache
{
	PathIndex index = new PathIndex()	
	Logger logger = LoggerFactory.getLogger(Cache)
	
	private class Item
	{
		String key
		String text
		int use
	}
	
	private def cache = [:]
	private int size = 0
	private int next = 0;
	
	public Cache()
	{
	}
	
	String get(String key)
	{
		logger.debug "get ${key}"
		Item item = cache.get(key)
		if (item == null)
		{
			item = new Item()
			item.key = key
			item.text = getText(key)
			add(item)
		}
		item.use = ++next
		return item.text
	}
	
	private void add(Item item)
	{
		if (size > 128)
		{
			// Remove the least recently used (lru) item from the cache.
			Item lru = new Item()
			lru.use = Integer.MAX_VALUE
			cache.each { key,value ->
				if (value.use < lru.use )
				{
					lru = value
				}	
			}
			cache.remove(lru.key)
		}
		else
		{
			++size
		}
		cache.put(item.key, item)
	}
	
	private String getText(String key)
	{
		logger.trace "getText ${key}"
		return getTextFile(key)?.getText('UTF-8')
	}
	
	private File getTextFile(String key)
	{
		logger.trace "getTextFile ${key}"
		String indexed = index.get(key)
		if (indexed == null)
		{
			logger.warn "No index entry for ${key}"
			return null
		}
		File file = new File(indexed)
		if (!file.exists())
		{
			//throw new FileNotFoundException("File for ${key} not found: ${file.path}")
			logger.error "File does not exist: ${file.path}"
			return null
		}
		return file
	}
	
}
